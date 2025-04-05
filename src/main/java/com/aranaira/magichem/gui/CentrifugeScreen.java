package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.GrandCentrifugeBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.foundation.ButtonData;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.gui.element.CentrifugeButtonRecipeSelector;
import com.aranaira.magichem.gui.element.GrandCentrifugeButtonRecipeSelector;
import com.aranaira.magichem.networking.DeviceRecipeClearC2SPacket;
import com.aranaira.magichem.networking.DeviceRecipeSyncDataC2SPacket;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.PacketRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CentrifugeScreen extends AbstractContainerScreen<CentrifugeMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_centrifuge.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 207,
            PANEL_GRIME_X = 158, PANEL_GRIME_Y = 38, PANEL_GRIME_W = 64, PANEL_GRIME_H = 59, PANEL_GRIME_U = 176, PANEL_GRIME_V = 126,
            PANEL_RECIPE_X = -84, PANEL_RECIPE_Y = -7, PANEL_RECIPE_U = 176, PANEL_RECIPE_W = 80, PANEL_RECIPE_H = 126,
            TOOLTIP_EFFICIENCY_X = 160, TOOLTIP_EFFICIENCY_Y = 43, TOOLTIP_EFFICIENCY_W = 57, TOOLTIP_EFFICIENCY_H = 15,
            TOOLTIP_OPERATIONTIME_X = 160, TOOLTIP_OPERATIONTIME_Y = 62, TOOLTIP_OPERATIONTIME_W = 57, TOOLTIP_OPERATIONTIME_H = 15,
            TOOLTIP_GRIME_X = 161, TOOLTIP_GRIME_Y = 78, TOOLTIP_GRIME_W = 56, TOOLTIP_GRIME_H = 14;
    private final ButtonData[] recipeSelectButtons = new ButtonData[15];
    private EditBox recipeFilterBox;
    private FixationSeparationRecipe lastRecipe = null;
    private NonNullList<ItemStack> lastRecipeComponentMateria = NonNullList.create();
    private ItemStack lastRecipeResultAdmixture = ItemStack.EMPTY;
    private boolean recipesChanged = false;

    public CentrifugeScreen(CentrifugeMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        updateDisplayedRecipes("");
    }

    private Triplet<FixationSeparationRecipe, NonNullList<ItemStack>, ItemStack> getOrUpdateRecipe(){
        ItemStack recipeItemQuery = menu.blockEntity.getRecipeItem(CentrifugeBlockEntity::getVar);
        if(lastRecipeResultAdmixture.getItem() != recipeItemQuery.getItem()) {
            if(recipeItemQuery.isEmpty()) {
                lastRecipe = null;
                lastRecipeResultAdmixture = ItemStack.EMPTY;
                lastRecipeComponentMateria = NonNullList.create();
            } else {
                lastRecipe = menu.getCurrentRecipe();
                lastRecipeResultAdmixture = menu.getCurrentRecipe().getResultAdmixture().copy();
                lastRecipeComponentMateria = NonNullList.create();
                for (ItemStack is : menu.getCurrentRecipe().getComponentMateria()) {
                    lastRecipeComponentMateria.add(is.copy());
                }
            }
        }
        return new Triplet<>(lastRecipe, lastRecipeComponentMateria, lastRecipeResultAdmixture);
    }

    @Override
    protected void init() {
        super.init();
        initializeRecipeSelectorButtons();
        initializeRecipeFilterBox();
    }

    private void initializeRecipeSelectorButtons(){
        int c = 0;
        for(int y=0; y<5; y++) {
            for(int x=0; x<3; x++) {
                recipeSelectButtons[c] = new ButtonData(this.addRenderableWidget(new CentrifugeButtonRecipeSelector(
                        this, c, this.leftPos, this.topPos, 18, 18, 42, 220, TEXTURE, button -> {

                    CentrifugeScreen query = (CentrifugeScreen) ((CentrifugeButtonRecipeSelector) button).getScreen();
                    query.setActiveRecipe(((CentrifugeButtonRecipeSelector) button).getArrayIndex());
                })), x*18 - 77, y*18 + 22);
                c++;
            }
        }

        int x = this.leftPos + 50;
        int y = this.topPos + 74;
        new ButtonData(this.addRenderableWidget(new CentrifugeButtonRecipeSelector(
                this, c, x, y, 9, 9, 33, 220, TEXTURE, button -> {

            CentrifugeScreen query = (CentrifugeScreen) ((CentrifugeButtonRecipeSelector) button).getScreen();
            query.clearActiveRecipe();
        })), x, y);

        renderButtons();
    }

    private void initializeRecipeFilterBox() {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        this.recipeFilterBox = new EditBox(Minecraft.getInstance().font, x, y, 65, 16, Component.empty()) {
            @Override
            public boolean charTyped(char pCodePoint, int pModifiers) {
                recipesChanged = true;
                recipeFilterRow = 0;
                return super.charTyped(pCodePoint, pModifiers);
            }

            @Override
            public void deleteChars(int pNum) {
                recipesChanged = true;
                recipeFilterRow = 0;
                super.deleteChars(pNum);
            }

            @Override
            public void deleteWords(int pNum) {
                recipesChanged = true;
                recipeFilterRow = 0;
                super.deleteWords(pNum);
            }
        };
        this.recipeFilterBox.setMaxLength(60);
        this.recipeFilterBox.setFocused(false);
        this.recipeFilterBox.setCanLoseFocus(false);
        this.setFocused(this.recipeFilterBox);

        renderFilterBox();
    }

    public void setActiveRecipe(int index) {
        int trueIndex = recipeFilterRow*3 + index;
        if(trueIndex < filteredRecipes.size()) {
            PacketRegistry.sendToServer(new DeviceRecipeSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    filteredRecipes.get(trueIndex).getItem()
            ));
        }
    }

    public void clearActiveRecipe() {
        menu.blockEntity.clearRecipeAfterNextProcess = true;
        PacketRegistry.sendToServer(new DeviceRecipeClearC2SPacket(
                menu.blockEntity.getBlockPos()
        ));
    }

    private List<ItemStack> filteredRecipes = new ArrayList<>();
    private int recipeFilterRow, recipeFilterRowTotal;
    private void updateDisplayedRecipes(String filter) {
        List<FixationSeparationRecipe> fixationRecipeOutputs = getAllRecipes();
        filteredRecipes.clear();

        for(FixationSeparationRecipe fsr : fixationRecipeOutputs) {
            String display = fsr.getResultAdmixture().getDisplayName().getString();
            if((Objects.equals(filter, "") || display.toLowerCase().contains(filter.toLowerCase()))) {
                filteredRecipes.add(fsr.getResultAdmixture());
            }
        }

        recipeFilterRowTotal = (int)Math.ceil(filteredRecipes.size() / 3d);

        recipesChanged = false;
    }

    private List<FixationSeparationRecipe> allRecipes = new ArrayList<>();
    @NotNull
    private List<FixationSeparationRecipe> getAllRecipes() {
        if(allRecipes.size() == 0) {
            List<FixationSeparationRecipe> raw = menu.blockEntity.getLevel().getRecipeManager().getAllRecipesFor(FixationSeparationRecipe.Type.INSTANCE);
            Object[] sortable = raw.toArray();
            Arrays.sort(sortable, Comparator.comparing(o -> ((FixationSeparationRecipe)o).getResultAdmixture().getDisplayName().getString()));
            for (Object o : sortable) {
                allRecipes.add((FixationSeparationRecipe) o);
            }
        }

        return allRecipes;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        gui.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        gui.blit(TEXTURE, x + PANEL_GRIME_X, y + PANEL_GRIME_Y, PANEL_GRIME_U, PANEL_GRIME_V, PANEL_GRIME_W, PANEL_GRIME_H);

        gui.blit(TEXTURE, x + PANEL_RECIPE_X, y + PANEL_RECIPE_Y, PANEL_RECIPE_U, 0, PANEL_RECIPE_W, PANEL_RECIPE_H);

        renderSelectedRecipe(gui, x + 61, y + 94);

        int sProg = CentrifugeBlockEntity.getScaledProgress(menu.getProgress(), menu.getGrime(), menu.getBatchSize(), menu.getOperationTimeMod(), CentrifugeBlockEntity::getVar, menu.blockEntity::getPoweredOperationTime);
        if(sProg > 0)
            gui.blit(TEXTURE, x+58, y+53, 0, 228, sProg, 28);

        int sGrime = CentrifugeBlockEntity.getScaledGrime(menu.getGrime());
        if(sGrime > 0)
            gui.blit(TEXTURE, x+164, y+81, 60, 248, sGrime, 8);

        //Scroll Nubbin
        if(recipeFilterRowTotal > 5) {
            float percent = (float)recipeFilterRow / (float)(recipeFilterRowTotal - 5);
            int nubbinShift = (int)Math.floor(percent * 80);
            gui.blit(TEXTURE, x - 19, y + 23 + nubbinShift, 60, 240, 8, 8);
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        renderBackground(gui);
        super.render(gui, mouseX, mouseY, delta);
        renderTooltip(gui, mouseX, mouseY);
        if(recipesChanged)
            updateDisplayedRecipes(recipeFilterBox == null ? "" : recipeFilterBox.getValue());
        renderRecipeOptions(gui);
        updateFilterBoxContents();
    }

    private void renderSelectedRecipe(GuiGraphics gui, int x, int y) {
        Triplet<FixationSeparationRecipe, NonNullList<ItemStack>, ItemStack> recipeCompound = getOrUpdateRecipe();

        if(recipeCompound.getThird() == ItemStack.EMPTY) {
            gui.blit(TEXTURE, x, y, 24, 238, 18, 18);
        }
        else {
            float alpha = menu.blockEntity.clearRecipeAfterNextProcess ? 0.5f : 1.0f;
            gui.setColor(1,1,1, alpha);
            gui.renderFakeItem(recipeCompound.getThird(), x+1, y+1);
            gui.setColor(1,1,1, 1);
        }
    }

    private void renderButtons() {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        for (ButtonData bd : recipeSelectButtons) {
            bd.getButton().setPosition(x + bd.getXOffset(), y + bd.getYOffset());
            bd.getButton().active = true;
            bd.getButton().visible = true;
        }
    }

    private void renderFilterBox() {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        recipeFilterBox.setX(xOrigin - 76);
        recipeFilterBox.setY(yOrigin + 1);

        if(recipeFilterBox.getValue().isEmpty())
            recipeFilterBox.setSuggestion(Component.translatable("gui.magichem.typetofilter").getString());
        else
            recipeFilterBox.setSuggestion("");

        addRenderableWidget(recipeFilterBox);
    }

    private void updateFilterBoxContents() {
        if(recipeFilterBox.getValue().isEmpty())
            recipeFilterBox.setSuggestion(Component.translatable("gui.magichem.typetofilter").getString());
        else
            recipeFilterBox.setSuggestion("");
    }

    private void renderRecipeOptions(GuiGraphics gui) {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        List<ItemStack> snipped = new ArrayList<>();
        for(int i = recipeFilterRow*3; i<Math.min(filteredRecipes.size(), recipeFilterRow*3 + 15); i++) {
            snipped.add(filteredRecipes.get(i));
        }

        int c = 0;
        int cLimit = Math.min(15, snipped.size());
        while(c < cLimit) {

            for(int y=0; y<5; y++) {
                for (int x = 0; x < 3; x++) {

                    gui.renderItem(snipped.get(c), xOrigin - 76 + x*18, yOrigin + 23 + y*18);
                    c++;
                    if(c >= cLimit) break;
                }
                if(c >= cLimit) break;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        if(pMouseX >= x - 77 && pMouseX <= x - 11 &&
                pMouseY >= y + 21 && pMouseY <= y + 114) {
            if (recipeFilterRowTotal > 5) {
                if (pDelta < 0)
                    recipeFilterRow = Math.min(recipeFilterRowTotal - 5, recipeFilterRow + 1);
                else
                    recipeFilterRow = Math.max(0, recipeFilterRow - 1);
            }
        }

        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if(recipeFilterRowTotal > 5 && pButton == 0) {
            int x = (width - PANEL_MAIN_W) / 2;
            int y = (height - PANEL_MAIN_H) / 2;

            if (pMouseX >= x - 20 && pMouseX <= x - 11 &&
                    pMouseY >= y + 25 && pMouseY <= y + 115) {
                double point = pMouseY - (y + 42);
                double percent = point / 80d;

                recipeFilterRow = Math.max(0, Math.min(recipeFilterRowTotal - 5, (int) Math.round(percent * recipeFilterRowTotal)));
            }
        }

        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if(recipeFilterRowTotal > 5 && pButton == 0) {
            int x = (width - PANEL_MAIN_W) / 2;
            int y = (height - PANEL_MAIN_H) / 2;

            if (pMouseX >= x - 20 && pMouseX <= x - 11 &&
                    pMouseY >= y + 25 && pMouseY <= y + 115) {
                double point = pMouseY - (y + 42);
                double percent = point / 80d;

                recipeFilterRow = Math.max(0, Math.min(recipeFilterRowTotal - 5, (int) Math.round(percent * recipeFilterRowTotal)));
            }
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderTooltip(gui, mouseX, mouseY);

        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Efficiency
        if(mouseX >= x+TOOLTIP_EFFICIENCY_X && mouseX <= x+TOOLTIP_EFFICIENCY_X+TOOLTIP_EFFICIENCY_W &&
                mouseY >= y+TOOLTIP_EFFICIENCY_Y && mouseY <= y+TOOLTIP_EFFICIENCY_Y+TOOLTIP_EFFICIENCY_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.efficiency").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.efficiency.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.translatable("tooltip.magichem.gui.efficiency.line2"));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Operation Time
        if(mouseX >= x+TOOLTIP_OPERATIONTIME_X && mouseX <= x+TOOLTIP_OPERATIONTIME_X+TOOLTIP_OPERATIONTIME_W &&
                mouseY >= y+TOOLTIP_OPERATIONTIME_Y && mouseY <= y+TOOLTIP_OPERATIONTIME_Y+TOOLTIP_OPERATIONTIME_H) {

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.operationtime").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.operationtime.line1")));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Grime Bar
        if(mouseX >= x+TOOLTIP_GRIME_X && mouseX <= x+TOOLTIP_GRIME_X+TOOLTIP_GRIME_W &&
                mouseY >= y+TOOLTIP_GRIME_Y && mouseY <= y+TOOLTIP_GRIME_Y+TOOLTIP_GRIME_H) {

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.grime").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.grime.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.translatable("tooltip.magichem.gui.grime.line2.1")
                    .append(Component.literal(ServerConfig.grimePenaltyPoint+"%").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.grime.line2.2")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.grime.line3").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(String.format("%.1f", CentrifugeBlockEntity.getGrimePercent(menu.getGrime(), CentrifugeBlockEntity::getVar)*100.0f)+"%").withStyle(ChatFormatting.DARK_AQUA)));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;

        gui.drawString(font, Component.literal(CentrifugeBlockEntity.getActualEfficiency(menu.getEfficiencyMod(), menu.getGrime(), CentrifugeBlockEntity::getVar)+"%"), PANEL_GRIME_X + 20, PANEL_GRIME_Y - 11, 0xff000000, false);

        int secWhole = CentrifugeBlockEntity.getOperationTicks(menu.getGrime(), menu.getBatchSize(), menu.getOperationTimeMod(), CentrifugeBlockEntity::getVar, menu.blockEntity::getPoweredOperationTime) / 20;
        int secPartial = (CentrifugeBlockEntity.getOperationTicks(menu.getGrime(), menu.getBatchSize(), menu.getOperationTimeMod(), CentrifugeBlockEntity::getVar, menu.blockEntity::getPoweredOperationTime) % 20) * 5;
        gui.drawString(font ,secWhole+"."+(secPartial < 10 ? "0"+secPartial : secPartial)+" s", PANEL_GRIME_X + 20, PANEL_GRIME_Y + 8, 0xff000000, false);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        boolean isNumber = (pKeyCode >= 48) && (pKeyCode <= 57);
        boolean isNumpadNumber = (pKeyCode >= 97) && (pKeyCode <= 105);

        if(isNumber || isNumpadNumber) return false;

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
}
