package com.aranaira.magichem.gui;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.FuseryBlockEntity;
import com.aranaira.magichem.foundation.ButtonData;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.gui.element.FuseryButtonRecipeSelector;
import com.aranaira.magichem.networking.FuserySyncDataC2SPacket;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.PacketRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FuseryScreen extends AbstractContainerScreen<FuseryMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fusery.png");
    private static final ResourceLocation TEXTURE_EXT =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fabrication_ext.png");
    private static final ResourceLocation TEXTURE_SLURRY =
            new ResourceLocation(MagiChemMod.MODID, "textures/block/fluid/experience_still.png");
    private final ButtonData[] recipeSelectButtons = new ButtonData[15];
    private EditBox recipeFilterBox;
    private static final int
        PANEL_MAIN_W = 176, PANEL_MAIN_H = 207,
        PANEL_RECIPE_X = -84, PANEL_RECIPE_Y = -7, PANEL_RECIPE_U = 176, PANEL_RECIPE_W = 80, PANEL_RECIPE_H = 126,
        PANEL_INGREDIENTS_X = 176, PANEL_INGREDIENTS_Y = 98, PANEL_INGREDIENTS_W = 80,
        PANEL_INGREDIENTS_U1 = 160, PANEL_INGREDIENTS_U2 = 80, PANEL_INGREDIENTS_U3 = 160, PANEL_INGREDIENTS_U4 = 80, PANEL_INGREDIENTS_U5 = 0,
        PANEL_INGREDIENTS_V1 =  66, PANEL_INGREDIENTS_V2 = 84, PANEL_INGREDIENTS_V3 =   0, PANEL_INGREDIENTS_V4 =  0, PANEL_INGREDIENTS_V5 = 0,
        PANEL_INGREDIENTS_H1 =  30, PANEL_INGREDIENTS_H2 = 48, PANEL_INGREDIENTS_H3 =  66, PANEL_INGREDIENTS_H4 = 84, PANEL_INGREDIENTS_H5 = 102,
        PANEL_GRIME_X = 176, PANEL_GRIME_Y = 38, PANEL_GRIME_W = 64, PANEL_GRIME_H = 59, PANEL_GRIME_U = 176, PANEL_GRIME_V = 126,
        SLURRY_X = 8, SLURRY_Y = 23, SLURRY_W = 8, SLURRY_H = 88,
        TOOLTIP_EFFICIENCY_X = 180, TOOLTIP_EFFICIENCY_Y = 43, TOOLTIP_EFFICIENCY_W = 57, TOOLTIP_EFFICIENCY_H = 15,
        TOOLTIP_OPERATIONTIME_X = 180, TOOLTIP_OPERATIONTIME_Y = 62, TOOLTIP_OPERATIONTIME_W = 57, TOOLTIP_OPERATIONTIME_H = 15,
        TOOLTIP_GRIME_X = 181, TOOLTIP_GRIME_Y = 78, TOOLTIP_GRIME_W = 56, TOOLTIP_GRIME_H = 14,
        TOOLTIP_SELECTED_RECIPE_X = 79, TOOLTIP_SELECTED_RECIPE_Y = 94, TOOLTIP_SELECTED_RECIPE_S = 18,
        TOOLTIP_SLURRY_X = 7, TOOLTIP_SLURRY_Y = 22, TOOLTIP_SLURRY_W = 10, TOOLTIP_SLURRY_H = 88,
        TOOLTIP_RECIPE_ZONE_X = -77, TOOLTIP_RECIPE_ZONE_Y = 22, TOOLTIP_RECIPE_ZONE_W = 54, TOOLTIP_RECIPE_ZONE_H = 90;
    private FixationSeparationRecipe lastRecipe = null;
    private NonNullList<ItemStack> lastRecipeComponentMateria = NonNullList.create();
    private ItemStack lastRecipeResultAdmixture = ItemStack.EMPTY;

    public FuseryScreen(FuseryMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        updateDisplayedRecipes("");
    }

    private Triplet<FixationSeparationRecipe, NonNullList<ItemStack>, ItemStack> getOrUpdateRecipe(){
        if(lastRecipeResultAdmixture.getItem() != menu.blockEntity.getRecipeItem(FuseryBlockEntity::getVar).getItem()) {
            lastRecipe = menu.getCurrentRecipe();
            lastRecipeResultAdmixture = menu.getCurrentRecipe().getResultAdmixture().copy();
            lastRecipeComponentMateria = NonNullList.create();
            for(ItemStack is : menu.getCurrentRecipe().getComponentMateria()) {
                lastRecipeComponentMateria.add(is.copy());
            }
        }
        return new Triplet<>(lastRecipe, lastRecipeComponentMateria, lastRecipeResultAdmixture);
    }

    @Override
    protected void init() {
        initializeRecipeSelectorButtons();
        initializeRecipeFilterBox();
        super.init();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if(pKeyCode == InputConstants.KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return this.getFocused() != null && this.getFocused().keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    private void initializeRecipeSelectorButtons(){
        int c = 0;
        for(int y=0; y<5; y++) {
            for(int x=0; x<3; x++) {
                recipeSelectButtons[c] = new ButtonData(this.addRenderableWidget(new FuseryButtonRecipeSelector(
                        this, c, this.leftPos, this.topPos, 18, 18, 46, 220, TEXTURE, button -> {

                    FuseryScreen query = (FuseryScreen) ((FuseryButtonRecipeSelector) button).getScreen();
                    query.setActiveRecipe(((FuseryButtonRecipeSelector) button).getArrayIndex());
                })), x*18 - 77, y*18 + 22);
                c++;
            }
        }
    }

    private void initializeRecipeFilterBox() {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        this.recipeFilterBox = new EditBox(Minecraft.getInstance().font, x, y, 65, 16, Component.empty()) {
            @Override
            public boolean charTyped(char pCodePoint, int pModifiers) {
                updateDisplayedRecipes(recipeFilterBox.getValue());
                return super.charTyped(pCodePoint, pModifiers);
            }

            @Override
            public void deleteChars(int pNum) {
                super.deleteChars(pNum);
                updateDisplayedRecipes(recipeFilterBox.getValue());
            }

            @Override
            public void deleteWords(int pNum) {
                super.deleteWords(pNum);
                updateDisplayedRecipes(recipeFilterBox.getValue());
            }
        };
        this.recipeFilterBox.setMaxLength(60);
        this.recipeFilterBox.setFocused(false);
        this.recipeFilterBox.setCanLoseFocus(false);
        this.setFocused(this.recipeFilterBox);
    }

    public void setActiveRecipe(int index) {
        int trueIndex = recipeFilterRow*3 + index;
        if(trueIndex < filteredRecipeOutputs.size()) {
            PacketRegistry.sendToServer(new FuserySyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    filteredRecipeOutputs.get(trueIndex).getItem()
            ));
            menu.setInputSlotFilters(menu.getRecipeItem());
        }
    }

    private static List<ItemStack> filteredRecipeOutputs = new ArrayList<>();
    private int recipeFilterRow, recipeFilterRowTotal;
    private void updateDisplayedRecipes(String filter) {
        List<FixationSeparationRecipe> fabricationRecipeOutputs = menu.blockEntity.getLevel().getRecipeManager().getAllRecipesFor(FixationSeparationRecipe.Type.INSTANCE);
        filteredRecipeOutputs.clear();

        recipeFilterRowTotal = (int)Math.ceil(fabricationRecipeOutputs.size() / 3.0f);
        recipeFilterRow = 0;

        for(FixationSeparationRecipe fsr : fabricationRecipeOutputs) {
            String display = fsr.getResultAdmixture().getDisplayName().getString();
            if((Objects.equals(filter, "") || display.toLowerCase().contains(filter.toLowerCase()))) {
                filteredRecipeOutputs.add(fsr.getResultAdmixture());
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Main BG image
        gui.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        //Grime panel
        gui.blit(TEXTURE, x + PANEL_GRIME_X, y + PANEL_GRIME_Y, PANEL_GRIME_U, PANEL_GRIME_V, PANEL_GRIME_W, PANEL_GRIME_H);

        //Recipe panel
        gui.blit(TEXTURE, x + PANEL_RECIPE_X, y + PANEL_RECIPE_Y, PANEL_RECIPE_U, 0, PANEL_RECIPE_W, PANEL_RECIPE_H);

        //Progress bar
        int sp = FuseryBlockEntity.getScaledProgress(menu.getProgress(), menu.getGrime(), menu.getOperationTimeMod(), FuseryBlockEntity::getVar);
        if(sp > 0)
            gui.blit(TEXTURE, x+74, y+53, 0, 228, sp, FuseryBlockEntity.PROGRESS_BAR_WIDTH);

        renderSelectedRecipe(gui, x + 79, y + 94);

        //Grime bar
        int sGrime = FuseryBlockEntity.getScaledGrime(menu.getGrime(), FuseryBlockEntity::getVar);
        if(sGrime > 0)
            gui.blit(TEXTURE, x+182, y+81, 64, 248, sGrime, 8);

        //water gauge
        int slurryH = FuseryBlockEntity.getScaledSlurry(menu.getSlurryInTank(), FuseryBlockEntity::getVar);
        RenderSystem.setShaderTexture(1, TEXTURE_SLURRY);
        gui.blit(TEXTURE_SLURRY, x + SLURRY_X, y + SLURRY_Y + SLURRY_H - slurryH, 0, 0, SLURRY_W, slurryH, 16, 16);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        renderIngredientPanel(gui, x + PANEL_INGREDIENTS_X, y + PANEL_INGREDIENTS_Y);
        renderSlotGhosts(gui);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        renderBackground(gui);
        super.render(gui, mouseX, mouseY, delta);
        renderTooltip(gui, mouseX, mouseY);
        renderButtons(gui, delta, mouseX, mouseY);
        renderFilterBox();
        renderRecipeOptions(gui);
    }

    private void renderSlotGhosts(GuiGraphics gui) {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        Triplet<FixationSeparationRecipe, NonNullList<ItemStack>, ItemStack> recipeCompound = getOrUpdateRecipe();

        if(recipeCompound.getFirst() == null)
            return;

        gui.setColor(1f, 1f, 1f, 0.25f);
        int slotGroup = 0;
        for(ItemStack stack : recipeCompound.getSecond()) {
            gui.renderItem(stack, xOrigin+26, yOrigin+23 + (18*slotGroup));
            gui.renderItem(stack, xOrigin+44, yOrigin+23 + (18*slotGroup));
            slotGroup++;
        }
        gui.setColor(1f, 1f, 1f, 1f);
    }

    private void renderSelectedRecipe(GuiGraphics gui, int x, int y) {
        Triplet<FixationSeparationRecipe, NonNullList<ItemStack>, ItemStack> recipeCompound = getOrUpdateRecipe();

        if(recipeCompound.getThird() == ItemStack.EMPTY) {
            gui.blit(TEXTURE, x, y, 28, 238, 18, 18);
        }
        else {
            gui.renderFakeItem(recipeCompound.getThird(), x+1, y+1);
        }
    }

    private void renderIngredientPanel(GuiGraphics gui, int x, int y) {
        RenderSystem.setShaderTexture(0, TEXTURE_EXT);

        if(menu.getCurrentRecipe() != null) {
            Triplet<FixationSeparationRecipe, NonNullList<ItemStack>, ItemStack> recipeCompound = getOrUpdateRecipe();
            NonNullList<ItemStack> componentMateria = recipeCompound.getSecond();

            //A switch statement doesn't work here and I have no idea why.
            if(componentMateria.size() == 1) {
                gui.blit(TEXTURE_EXT, x, y, PANEL_INGREDIENTS_U1, PANEL_INGREDIENTS_V1, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H1);
            }
            else if(componentMateria.size() == 2) {
                gui.blit(TEXTURE_EXT, x, y, PANEL_INGREDIENTS_U2, PANEL_INGREDIENTS_V2, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H2);
            }
            else if(componentMateria.size() == 3) {
                gui.blit(TEXTURE_EXT, x, y, PANEL_INGREDIENTS_U3, PANEL_INGREDIENTS_V3, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H3);
            }
            else if(componentMateria.size() == 4) {
                gui.blit(TEXTURE_EXT, x, y, PANEL_INGREDIENTS_U4, PANEL_INGREDIENTS_V4, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H4);
            }
            else if(componentMateria.size() == 5) {
                gui.blit(TEXTURE_EXT, x, y, PANEL_INGREDIENTS_U5, PANEL_INGREDIENTS_V5, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H5);
            }

            for(int i=0; i<componentMateria.size(); i++) {
                gui.renderFakeItem(componentMateria.get(i), x+4, y+7 + i*18);
            }
        }

        RenderSystem.setShaderTexture(0, TEXTURE);
    }

    private void renderButtons(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        for(ButtonData bd : recipeSelectButtons) {
            bd.getButton().setPosition(x+bd.getXOffset(), y+bd.getYOffset());
            bd.getButton().renderWidget(gui, mouseX, mouseY, partialTick);
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

    private void renderRecipeOptions(GuiGraphics gui) {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        List<ItemStack> snipped = new ArrayList<>();
        for(int i=recipeFilterRow*3; i<Math.min(filteredRecipeOutputs.size(), recipeFilterRow*3 + 15); i++) {
            snipped.add(filteredRecipeOutputs.get(i));
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
    protected void renderLabels(GuiGraphics gui, int x, int y) {
        //Recipe selector + current
        Triplet<FixationSeparationRecipe, NonNullList<ItemStack>, ItemStack> recipeCompound = getOrUpdateRecipe();
        if(recipeCompound.getFirst() != null) {
            for (int i = 0; i < recipeCompound.getSecond().size(); i++) {
                Component text = Component.literal(recipeCompound.getSecond().get(i).getCount() + " x ")
                        .append(Component.translatable("item."+MagiChemMod.MODID+"."+recipeCompound.getSecond().get(i).getItem()+".short"));
                gui.pose().scale(0.5f, 0.5f, 0.5f);

                gui.drawString(Minecraft.getInstance().font, text, 400, 184 + i*36, 0xff000000, false);
                gui.pose().scale(2.0f, 2.0f, 2.0f);
            }
        }

        //Grime panel
        gui.drawString(font, Component.literal(FuseryBlockEntity.getActualEfficiency(menu.getEfficiencyMod(), menu.getGrime(), FuseryBlockEntity::getVar)+"%"), PANEL_GRIME_X + 20, PANEL_GRIME_Y - 11, 0xff000000, false);

        int secWhole = FuseryBlockEntity.getOperationTicks(menu.getGrime(), menu.getOperationTimeMod(), FuseryBlockEntity::getVar) / 20;
        int secPartial = (FuseryBlockEntity.getOperationTicks(menu.getGrime(), menu.getOperationTimeMod(), FuseryBlockEntity::getVar) % 20) * 5;
        gui.drawString(font ,secWhole+"."+(secPartial < 10 ? "0"+secPartial : secPartial)+" s", PANEL_GRIME_X + 20, PANEL_GRIME_Y + 8, 0xff000000, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderTooltip(gui, mouseX, mouseY);

        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Selected recipe
        if(mouseX >= x+TOOLTIP_SELECTED_RECIPE_X && mouseX <= x+TOOLTIP_SELECTED_RECIPE_X+TOOLTIP_SELECTED_RECIPE_S &&
                mouseY >= y+TOOLTIP_SELECTED_RECIPE_Y && mouseY <= y+TOOLTIP_SELECTED_RECIPE_Y+TOOLTIP_SELECTED_RECIPE_S) {
            ItemStack recipeItem = menu.getRecipeItem();
            if(recipeItem == ItemStack.EMPTY) {
                tooltipContents.add(Component.translatable("tooltip.magichem.gui.noselectedrecipe").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            } else {
                tooltipContents.addAll(recipeItem.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
                tooltipContents.add(Component.empty());
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.fixationcost.part1").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(menu.getCurrentRecipe().getSlurryCost()+"mB").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable("tooltip.magichem.gui.fixationcost.part2").withStyle(ChatFormatting.DARK_GRAY))
                );
            }
        }

        //Items in recipe picker
        if(mouseX >= x+TOOLTIP_RECIPE_ZONE_X && mouseX <= x+TOOLTIP_RECIPE_ZONE_X+TOOLTIP_RECIPE_ZONE_W &&
                mouseY >= y+TOOLTIP_RECIPE_ZONE_Y && mouseY <= y+TOOLTIP_RECIPE_ZONE_Y+TOOLTIP_RECIPE_ZONE_H) {
            int mx = mouseX - (x+TOOLTIP_RECIPE_ZONE_X);
            int my = mouseY - (y+TOOLTIP_RECIPE_ZONE_Y);
            int id = ((my / 18) * 3) + ((mx / 18) % 3);

            if(id < filteredRecipeOutputs.size()) {
                if (id >= 0 && id < 16) {
                    ItemStack stackUnderMouse = filteredRecipeOutputs.get(id);
                    tooltipContents.addAll(stackUnderMouse.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
                }
            }
        }

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
                    .append(Component.literal(Config.grimePenaltyPoint+"%").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.grime.line2.2")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.grime.line3").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(String.format("%.1f", FuseryBlockEntity.getGrimePercent(menu.getGrime(), FuseryBlockEntity::getVar)*100.0f)+"%").withStyle(ChatFormatting.DARK_AQUA)));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Slurry Bar
        if(mouseX >= x+TOOLTIP_SLURRY_X && mouseX <= x+TOOLTIP_SLURRY_X+TOOLTIP_SLURRY_W &&
                mouseY >= y+TOOLTIP_SLURRY_Y && mouseY <= y+TOOLTIP_SLURRY_Y+TOOLTIP_SLURRY_H) {

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.slurry.tank").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.slurry.tank.line1"))
                    .append(menu.blockEntity.getDisplayName())
                    .append("."));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.translatable("tooltip.magichem.gui.slurry.tank.line2a")
                    .append(Component.literal(Config.fixationFailureRefund+"%").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.slurry.tank.line2b")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.slurry.tank.line3").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(menu.getSlurryInTank()+"mB").withStyle(ChatFormatting.DARK_AQUA)));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
    }
}
