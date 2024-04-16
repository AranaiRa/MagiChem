package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.FuseryBlock;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.FuseryBlockEntity;
import com.aranaira.magichem.foundation.ButtonData;
import com.aranaira.magichem.gui.element.FuseryButtonRecipeSelector;
import com.aranaira.magichem.networking.FabricationSyncDataC2SPacket;
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
        TOOLTIP_EFFICIENCY_X = 160, TOOLTIP_EFFICIENCY_Y = 43, TOOLTIP_EFFICIENCY_W = 57, TOOLTIP_EFFICIENCY_H = 15,
        TOOLTIP_OPERATIONTIME_X = 160, TOOLTIP_OPERATIONTIME_Y = 62, TOOLTIP_OPERATIONTIME_W = 57, TOOLTIP_OPERATIONTIME_H = 15,
        TOOLTIP_GRIME_X = 161, TOOLTIP_GRIME_Y = 78, TOOLTIP_GRIME_W = 56, TOOLTIP_GRIME_H = 14,
        TOOLTIP_SELECTED_RECIPE_X = 79, TOOLTIP_SELECTED_RECIPE_Y = 94, TOOLTIP_SELECTED_RECIPE_S = 18;
    private FixationSeparationRecipe lastClickedRecipe = null;

    public FuseryScreen(FuseryMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
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

        this.recipeFilterBox = new EditBox(Minecraft.getInstance().font, x, y, 65, 16, Component.empty());
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
            lastClickedRecipe = FixationSeparationRecipe.getSeparatingRecipe(menu.blockEntity.getLevel(), filteredRecipeOutputs.get(trueIndex));
            menu.setInputSlotFilters(menu.getRecipeItem());
        }
    }

    private static List<ItemStack> filteredRecipeOutputs = new ArrayList<>();
    private int recipeFilterRow, recipeFilterRowTotal;
    private void updateDisplayedRecipes(String filter) {
        List<FixationSeparationRecipe> fabricationRecipeOutputs = menu.blockEntity.getLevel().getRecipeManager().getAllRecipesFor(FixationSeparationRecipe.Type.INSTANCE);
        filteredRecipeOutputs.clear();

        for(FixationSeparationRecipe fsr : fabricationRecipeOutputs) {
            String display = fsr.getResultAdmixture().getDisplayName().getString();
            if((Objects.equals(filter, "") || display.toLowerCase().contains(filter.toLowerCase()))) {
                filteredRecipeOutputs.add(fsr.getResultAdmixture());
            }
        }

        recipeFilterRowTotal = filteredRecipeOutputs.size() / 3;
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

        int sp = FuseryBlockEntity.getScaledProgress(menu.getProgress(), menu.getGrime(), menu.getOperationTimeMod(), FuseryBlockEntity::getVar);
        if(sp > 0)
            gui.blit(TEXTURE, x+74, y+53, 0, 228, sp, FuseryBlockEntity.PROGRESS_BAR_WIDTH);

        renderSelectedRecipe(gui, x + 79, y + 94);

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
        updateDisplayedRecipes(recipeFilterBox.getValue());
        renderRecipeOptions(gui);
    }

    private void renderSlotGhosts(GuiGraphics gui) {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        if(menu.getCurrentRecipe() == null)
            return;

        FixationSeparationRecipe fsr = menu.getCurrentRecipe();

        gui.setColor(1f, 1f, 1f, 0.25f);
        int slotGroup = 0;
        for(ItemStack stack : fsr.getComponentMateria()) {
            gui.renderItem(stack, xOrigin+26, yOrigin+23 + (18*slotGroup));
            gui.renderItem(stack, xOrigin+44, yOrigin+23 + (18*slotGroup));
            slotGroup++;
        }
        gui.setColor(1f, 1f, 1f, 1f);
    }

    private void renderSelectedRecipe(GuiGraphics gui, int x, int y) {
        ItemStack recipeItem = menu.blockEntity.getRecipeItem(FuseryBlockEntity::getVar);
        if(recipeItem == ItemStack.EMPTY) {
            gui.blit(TEXTURE, x, y, 28, 238, 18, 18);
        }
        else {
            gui.renderFakeItem(recipeItem, x+1, y+1);
        }
    }

    private void renderIngredientPanel(GuiGraphics gui, int x, int y) {
        RenderSystem.setShaderTexture(0, TEXTURE_EXT);

        if(menu.getCurrentRecipe() != null) {
            FixationSeparationRecipe recipe = menu.getCurrentRecipe();

            //A switch statement doesn't work here and I have no idea why.
            if(recipe.getComponentMateria().size() == 1) {
                gui.blit(TEXTURE_EXT, x, y, PANEL_INGREDIENTS_U1, PANEL_INGREDIENTS_V1, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H1);
            }
            else if(recipe.getComponentMateria().size() == 2) {
                gui.blit(TEXTURE_EXT, x, y, PANEL_INGREDIENTS_U2, PANEL_INGREDIENTS_V2, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H2);
            }
            else if(recipe.getComponentMateria().size() == 3) {
                gui.blit(TEXTURE_EXT, x, y, PANEL_INGREDIENTS_U3, PANEL_INGREDIENTS_V3, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H3);
            }
            else if(recipe.getComponentMateria().size() == 4) {
                gui.blit(TEXTURE_EXT, x, y, PANEL_INGREDIENTS_U4, PANEL_INGREDIENTS_V4, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H4);
            }
            else if(recipe.getComponentMateria().size() == 5) {
                gui.blit(TEXTURE_EXT, x, y, PANEL_INGREDIENTS_U5, PANEL_INGREDIENTS_V5, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H5);
            }

            for(int i=0; i<recipe.getComponentMateria().size(); i++) {
                gui.renderFakeItem(recipe.getComponentMateria().get(i), x+4, y+7 + i*18);
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
        FixationSeparationRecipe recipe = menu.getCurrentRecipe();
        if(recipe != null) {
            for (int i = 0; i < recipe.getComponentMateria().size(); i++) {
                Component text = Component.literal(recipe.getComponentMateria().get(i).getCount() + " x ")
                        .append(Component.translatable("item."+MagiChemMod.MODID+"."+recipe.getComponentMateria().get(i).getItem()+".short"));
                gui.pose().scale(0.5f, 0.5f, 0.5f);

                gui.drawString(Minecraft.getInstance().font, text, 400, 184 + i*36, 0xff000000, false);
                gui.pose().scale(2.0f, 2.0f, 2.0f);
            }
        }

        //Grime panel
        gui.drawString(font, Component.literal(CentrifugeBlockEntity.getActualEfficiency(menu.getEfficiencyMod(), menu.getGrime(), CentrifugeBlockEntity::getVar)+"%"), PANEL_GRIME_X + 20, PANEL_GRIME_Y - 11, 0xff000000, false);

        int secWhole = CentrifugeBlockEntity.getOperationTicks(menu.getGrime(), menu.getOperationTimeMod(), CentrifugeBlockEntity::getVar) / 20;
        int secPartial = (CentrifugeBlockEntity.getOperationTicks(menu.getGrime(), menu.getOperationTimeMod(), CentrifugeBlockEntity::getVar) % 20) * 5;
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
                tooltipContents.add(Component.translatable("tooltip.magichem.noselectedrecipe").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            } else {
                for (Component c : recipeItem.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL)) {
                    gui.drawString(Minecraft.getInstance().font, "YES", 20, 20, 0xffffffff, false);
                    tooltipContents.add(c);
                }
            }
        }
        gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
    }
}
