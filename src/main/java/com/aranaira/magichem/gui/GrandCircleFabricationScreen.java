package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.GrandCircleFabricationBlockEntity;
import com.aranaira.magichem.foundation.ButtonData;
import com.aranaira.magichem.gui.element.FabricationButtonRecipeSelector;
import com.aranaira.magichem.networking.FabricationSyncDataC2SPacket;
import com.aranaira.magichem.recipe.DistillationFabricationRecipe;
import com.aranaira.magichem.registry.PacketRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GrandCircleFabricationScreen extends AbstractContainerScreen<GrandCircleFabricationMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fabrication.png");
    private static final ResourceLocation TEXTURE_EXT =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fabrication_ext.png");
    private ImageButton
            b_powerLevelUp, b_powerLevelDown;
    private ButtonData[] recipeSelectButtons = new ButtonData[15];
    private EditBox recipeFilterBox;
    private static final int
            PANEL_MAIN_W = 181, PANEL_MAIN_H = 192,
            PANEL_RECIPE_U = 160, PANEL_RECIPE_V = 96, PANEL_RECIPE_W = 81, PANEL_RECIPE_H = 126,
            PANEL_POWER_U = 0, PANEL_POWER_V = 102, PANEL_POWER_W = 80, PANEL_POWER_H = 66;
    private DistillationFabricationRecipe lastClickedRecipe = null;
    private boolean recipesChanged = true;

    public GrandCircleFabricationScreen(GrandCircleFabricationMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        initializePowerLevelButtons();
        initializeRecipeSelectorButtons();
        updateDisplayedRecipes("");
        initializeRecipeFilterBox();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == InputConstants.KEY_ESCAPE) {
            this.onClose();
            return true;
        } else if (this.recipeFilterBox.keyPressed(pKeyCode, pScanCode, pModifiers)) {
            return true;
        } else {
            return this.recipeFilterBox.isFocused() && this.recipeFilterBox.isVisible() || super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    private void initializeRecipeFilterBox() {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        this.recipeFilterBox = new EditBox(Minecraft.getInstance().font, x, y, 67, 18, Component.empty()) {
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
        };
        this.recipeFilterBox.setMaxLength(60);
        this.recipeFilterBox.setFocused(false);
        this.recipeFilterBox.setCanLoseFocus(false);
        this.setFocused(this.recipeFilterBox);

        renderFilterBox();
    }

    private void initializePowerLevelButtons(){
        b_powerLevelUp = this.addRenderableWidget(new ImageButton(this.leftPos + 167, this.topPos + 126, 12, 7, 232, 242, TEXTURE, button -> {
            menu.blockEntity.incrementPowerUsageSetting();
            Item recipeTarget = null;
            if(menu.blockEntity.getCurrentRecipe() != null) {
                recipeTarget = menu.blockEntity.getCurrentRecipe().getAlchemyObject().getItem();
            }
            PacketRegistry.sendToServer(new FabricationSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    recipeTarget,
                    menu.blockEntity.getPowerUsageSetting()
            ));
        }));
        b_powerLevelDown = this.addRenderableWidget(new ImageButton(this.leftPos + 167, this.topPos + 126, 12, 7, 244, 242, TEXTURE, button -> {
            menu.blockEntity.decrementPowerUsageSetting();
            Item recipeTarget = null;
            if(menu.blockEntity.getCurrentRecipe() != null) {
                recipeTarget = menu.blockEntity.getCurrentRecipe().getAlchemyObject().getItem();
            }
            PacketRegistry.sendToServer(new FabricationSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    recipeTarget,
                    menu.blockEntity.getPowerUsageSetting()
            ));
        }));
    }

    private void initializeRecipeSelectorButtons(){
        int c = 0;
        for(int y=0; y<5; y++) {
            for(int x=0; x<3; x++) {
                recipeSelectButtons[c] = new ButtonData(this.addRenderableWidget(new FabricationButtonRecipeSelector(
                        this, c, this.leftPos, this.topPos, 18, 18, 54, 220, TEXTURE, button -> {

                            GrandCircleFabricationScreen query = (GrandCircleFabricationScreen) ((FabricationButtonRecipeSelector) button).getScreen();
                            query.setActiveRecipe(((FabricationButtonRecipeSelector) button).getArrayIndex());
                })), x*18 - 78, y*18 + 39);
                c++;
            }
        }

        renderButtons();
    }

    public void setActiveRecipe(int index) {
        int trueIndex = recipeFilterRow*3 + index;
        if(trueIndex < filteredRecipes.size()) {
            PacketRegistry.sendToServer(new FabricationSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    filteredRecipes.get(trueIndex).getAlchemyObject().getItem(),
                    menu.blockEntity.getPowerUsageSetting()
            ));
            lastClickedRecipe = filteredRecipes.get(trueIndex);
        }
    }

    private List<DistillationFabricationRecipe> filteredRecipes = new ArrayList<>();
    private int recipeFilterRow, recipeFilterRowTotal;
    private void updateDisplayedRecipes(String filter) {
        List<DistillationFabricationRecipe> fabricationRecipeOutputs = getAllRecipes();
        filteredRecipes.clear();

        for(DistillationFabricationRecipe acr : fabricationRecipeOutputs) {
            String display = acr.getAlchemyObject().getDisplayName().getString();
            if((Objects.equals(filter, "") || display.toLowerCase().contains(filter.toLowerCase())) && !acr.getIsDistillOnly()) {
                filteredRecipes.add(acr);
            }
        }

        recipeFilterRowTotal = (int)Math.ceil(filteredRecipes.size() / 3d);

        recipesChanged = false;
    }

    private List<DistillationFabricationRecipe> allRecipes = new ArrayList<>();
    @NotNull
    private List<DistillationFabricationRecipe> getAllRecipes() {
        if(allRecipes.size() == 0) {
            List<DistillationFabricationRecipe> raw = menu.blockEntity.getLevel().getRecipeManager().getAllRecipesFor(DistillationFabricationRecipe.Type.INSTANCE);
            Object[] sortable = raw.toArray();
            Arrays.sort(sortable, Comparator.comparing(o -> ((DistillationFabricationRecipe)o).getAlchemyObject().getDisplayName().getString()));
            for (Object o : sortable) {
                allRecipes.add((DistillationFabricationRecipe) o);
            }
        }

        return allRecipes;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Main Panel
        gui.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        //Recipe Selector Panel
        gui.blit(TEXTURE_EXT, x - 85, y + 10, PANEL_RECIPE_U, PANEL_RECIPE_V, PANEL_RECIPE_W, PANEL_RECIPE_H);

        //Power Settings Panel
        gui.blit(TEXTURE_EXT, x + 163, y + 19, PANEL_POWER_U, PANEL_POWER_V, PANEL_POWER_W, PANEL_POWER_H);

        renderProgressBar(gui, x + 79, y + 39);

        renderSelectedRecipe(gui, x + 84, y + 79);

        renderPowerLevelBar(gui, x + 169, y + 37);

        renderSlotGhosts(gui);

        if(!menu.blockEntity.hasSufficientPower()) {
            RenderSystem.setShaderTexture(0, TEXTURE_EXT);
            renderPowerWarning(gui, x, y);
        }

        //Scroll Nubbin
        if(recipeFilterRowTotal > 5) {
            float percent = (float)recipeFilterRow / (float)(recipeFilterRowTotal - 5);
            int nubbinShift = (int)Math.floor(percent * 80);
            gui.blit(TEXTURE, x - 20, y + 40 + nubbinShift, 28, 230, 8, 8);
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

    private void renderButtons() {

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        b_powerLevelUp.setPosition(x+167, y+26);
        b_powerLevelUp.active = true;
        b_powerLevelUp.visible = true;

        b_powerLevelDown.setPosition(x+167, y+71);
        b_powerLevelDown.active = true;
        b_powerLevelDown.visible = true;

        for(ButtonData bd : recipeSelectButtons) {
            bd.getButton().setPosition(x+bd.getXOffset(), y+bd.getYOffset());
            bd.getButton().active = true;
            bd.getButton().visible = true;
        }
    }

    private void renderPowerLevelBar(GuiGraphics gui, int x, int y) {
        int powerLevel = menu.blockEntity.getPowerUsageSetting();

        gui.blit(TEXTURE, x, y + (30 - powerLevel), 46, 256 - powerLevel, 8, powerLevel);
    }

    private void renderSelectedRecipe(GuiGraphics gui, int x, int y) {
        if(menu.blockEntity.getCurrentRecipe() == null) {
            gui.blit(TEXTURE, x, y, 28, 238, 18, 18);
        }
        else {
            gui.renderItem(menu.blockEntity.getCurrentRecipe().getAlchemyObject(), x+1, y+1);
        }
    }

    private void renderProgressBar(GuiGraphics gui, int x, int y) {
        int sp = GrandCircleFabricationBlockEntity.getScaledProgress(menu.blockEntity);
        if(sp > 0)
            gui.blit(TEXTURE, x, y , 0, 230, sp, 26);
    }

    private void renderFilterBox() {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        recipeFilterBox.setX(xOrigin - 78);
        recipeFilterBox.setY(yOrigin + 17);

        if(recipeFilterBox.getValue().isEmpty())
            recipeFilterBox.setSuggestion(Component.translatable("gui.magichem.typetofilter").getString());
        else
            recipeFilterBox.setSuggestion("");

        addRenderableWidget(recipeFilterBox);
    }

    private void renderSlotGhosts(GuiGraphics gui) {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        if(menu.blockEntity.getCurrentRecipe() == null)
            return;

        DistillationFabricationRecipe acr = menu.blockEntity.getCurrentRecipe();

        gui.setColor(1f, 1f, 1f, 0.25f);
        int slotGroup = 0;
        for(ItemStack stack : acr.getComponentMateria()) {
            gui.renderItem(stack, xOrigin + 31, yOrigin+8 + (18*slotGroup));
            gui.renderItem(stack, xOrigin + 49, yOrigin+8 + (18*slotGroup));
            slotGroup++;
        }
        gui.setColor(1f, 1f, 1f, 1f);
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

        List<DistillationFabricationRecipe> snipped = new ArrayList<>();
        for(int i=recipeFilterRow*3; i<Math.min(filteredRecipes.size(), recipeFilterRow*3 + 15); i++) {
            snipped.add(filteredRecipes.get(i));
        }

        int c = 0;
        int cLimit = Math.min(15, snipped.size());
        while(c < cLimit) {

            for(int y=0; y<5; y++) {
                for (int x = 0; x < 3; x++) {
                    gui.renderItem(snipped.get(c).getAlchemyObject(), xOrigin-77 + x*18, yOrigin+40 + y*18);
                    c++;
                    if(c >= cLimit) break;
                }
                if(c >= cLimit) break;
            }
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {

        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;
        boolean doOriginalTooltip = true;

        //Selected Recipe
        if(pX >= x+79 && pX <= x+97 &&
                pY >= y+79 && pY <= y+97) {
            if(menu.blockEntity.getCurrentRecipe() == null) {
                tooltipContents.add(Component.translatable("tooltip.magichem.gui.noselectedrecipe").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            } else {
                ItemStack recipeItem = menu.blockEntity.getCurrentRecipe().getAlchemyObject();
                if (recipeItem == ItemStack.EMPTY) {
                    tooltipContents.add(Component.translatable("tooltip.magichem.gui.noselectedrecipe").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                } else {
                    tooltipContents.addAll(recipeItem.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
                }
            }
        }

        //Items in recipe picker
        if(pX >= x-78 && pX <= x-25 &&
                pY >= y+42 && pY <= y+132) {
            int mx = pX - (x-78);
            int my = pY - (y+42);
            int id = ((my / 18) * 3) + ((mx / 18) % 3);

            if (id >= 0 && id < 16) {
                if(id + recipeFilterRow * 3 < filteredRecipes.size()) {
                    ItemStack stackUnderMouse = filteredRecipes.get(id + recipeFilterRow * 3).getAlchemyObject();
                    tooltipContents.addAll(stackUnderMouse.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
                }
            }
        }

        //Item ghosts
        if(menu.blockEntity.getCurrentRecipe() != null) {
            if (pX >= x + 30 && pX <= x + 66 &&
                    pY >= y + 7 && pY <= y + 96) {
                int recipeIndex = (pY - (y + 7)) / 18;
                int left = pX <= x + 48 ? 0 : 1;
                int slotIndex = recipeIndex * 2 + left;

                ItemStack stackInSlot = menu.inputSlots[slotIndex].getItem();

                if(stackInSlot.isEmpty() && recipeIndex < menu.blockEntity.getCurrentRecipe().getComponentMateria().size()) {
                    String name = menu.blockEntity.getCurrentRecipe().getComponentMateria().get(recipeIndex).getDisplayName().getString();
                    tooltipContents.add(Component.literal(name.substring(1, name.length() - 1)).withStyle(ChatFormatting.DARK_GRAY));
                }

            }
        }

        if(doOriginalTooltip)
            super.renderTooltip(pGuiGraphics, pX, pY);

        pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
    }

    protected void renderPowerWarning(GuiGraphics gui, int x, int y) {
        long cycle = Minecraft.getInstance().level.getGameTime() % 20;

        gui.blit(TEXTURE_EXT, x+10, y-30, 0, 230, 156, 26);
        if(cycle < 10) {
            gui.blit(TEXTURE_EXT, x + 17, y - 23, 156, 244, 12, 12);
            gui.blit(TEXTURE_EXT, x + 147, y - 23, 156, 244, 12, 12);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        if(pMouseX >= x - 78 && pMouseX <= x - 12 &&
                pMouseY >= y + 42 && pMouseY <= y + 132) {
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

            if (pMouseX >= x - 21 && pMouseX <= x - 12 &&
                    pMouseY >= y + 39 && pMouseY <= y + 129) {
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

            if (pMouseX >= x - 21 && pMouseX <= x - 12 &&
                    pMouseY >= y + 39 && pMouseY <= y + 129) {
                double point = pMouseY - (y + 42);
                double percent = point / 80d;

                recipeFilterRow = Math.max(0, Math.min(recipeFilterRowTotal - 5, (int) Math.round(percent * recipeFilterRowTotal)));
            }
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int x, int y) {
        int powerDraw = menu.blockEntity.getPowerDraw();
        int secWhole = menu.blockEntity.getOperationTicks() / 20;
        int secPartial = (menu.blockEntity.getOperationTicks() % 20) * 5;

        Font font = Minecraft.getInstance().font;
        gui.drawString(font ,powerDraw+"/t", 193, 26, 0xff000000, false);
        gui.drawString(font ,secWhole+"."+(secPartial < 10 ? "0"+secPartial : secPartial)+" s", 193, 45, 0xff000000, false);

        DistillationFabricationRecipe recipe = menu.blockEntity.getCurrentRecipe();
        if(recipe != null) {
            for (int i = 0; i < recipe.getComponentMateria().size(); i++) {
                Component text = Component.literal(recipe.getComponentMateria().get(i).getCount() + "");
                int rightAlignShift = 17 - font.width(text.getString());

                gui.drawString(font, text, 6 + rightAlignShift, -1 + i * 18, 0xff000000, false);
            }
        }

        if(!menu.blockEntity.hasSufficientPower()) {
            MutableComponent warningText = Component.translatable("gui.magichem.insufficientpower");
            int width = Minecraft.getInstance().font.width(warningText.getString());
            gui.drawString(font, warningText, 89 - width/2, -33, 0xff000000, false);
        }
    }
}
