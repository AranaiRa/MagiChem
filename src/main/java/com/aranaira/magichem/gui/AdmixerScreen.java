package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.AdmixerBlockEntity;
import com.aranaira.magichem.foundation.ButtonData;
import com.aranaira.magichem.gui.element.AdmixerButtonRecipeSelector;
import com.aranaira.magichem.networking.AdmixerSyncDataC2SPacket;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.PacketRegistry;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdmixerScreen extends AbstractContainerScreen<AdmixerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_admixer.png");
    private static final ResourceLocation TEXTURE_EXT =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fabrication_ext.png");
    private final ButtonData[] recipeSelectButtons = new ButtonData[15];
    private EditBox recipeFilterBox;
    private static final int
        PANEL_MAIN_W = 176, PANEL_MAIN_H = 207,
        PANEL_RECIPE_X = -66, PANEL_RECIPE_Y = -13, PANEL_RECIPE_U = 176, PANEL_RECIPE_W = 80, PANEL_RECIPE_H = 126,
        PANEL_INGREDIENTS_X = 176, PANEL_INGREDIENTS_Y = 85, PANEL_INGREDIENTS_W = 80,
        PANEL_INGREDIENTS_U1 = 160, PANEL_INGREDIENTS_U2 = 80, PANEL_INGREDIENTS_U3 = 160, PANEL_INGREDIENTS_U4 = 80, PANEL_INGREDIENTS_U5 = 0,
        PANEL_INGREDIENTS_V1 =  66, PANEL_INGREDIENTS_V2 = 84, PANEL_INGREDIENTS_V3 =   0, PANEL_INGREDIENTS_V4 =  0, PANEL_INGREDIENTS_V5 = 0,
        PANEL_INGREDIENTS_H1 =  30, PANEL_INGREDIENTS_H2 = 48, PANEL_INGREDIENTS_H3 =  66, PANEL_INGREDIENTS_H4 = 84, PANEL_INGREDIENTS_H5 = 102;
    private FixationSeparationRecipe lastClickedRecipe = null;

    public AdmixerScreen(AdmixerMenu menu, Inventory inventory, Component component) {
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
                recipeSelectButtons[c] = new ButtonData(this.addRenderableWidget(new AdmixerButtonRecipeSelector(
                        this, c, this.leftPos, this.topPos, 18, 18, 46, 220, TEXTURE, button -> {

                    AdmixerScreen query = (AdmixerScreen) ((AdmixerButtonRecipeSelector) button).getScreen();
                    query.setActiveRecipe(((AdmixerButtonRecipeSelector) button).getArrayIndex());
                })), x*18 - 59, y*18 + 16);
                c++;
            }
        }
    }

    private void initializeRecipeFilterBox() {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        this.recipeFilterBox = new EditBox(Minecraft.getInstance().font, x, y, 65, 18, Component.empty());
        this.recipeFilterBox.setMaxLength(60);
        this.recipeFilterBox.setFocused(false);
        this.recipeFilterBox.setCanLoseFocus(false);
        this.setFocused(this.recipeFilterBox);
    }

    public void setActiveRecipe(int index) {
        int trueIndex = recipeFilterRow*3 + index;
        if(trueIndex < filteredRecipes.size()) {
            PacketRegistry.sendToServer(new AdmixerSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    filteredRecipes.get(trueIndex).getResultAdmixture().getItem()
            ));
            lastClickedRecipe = filteredRecipes.get(trueIndex);
            menu.setInputSlotFilters(lastClickedRecipe);
            menu.blockEntity.setCurrentRecipeByOutput(lastClickedRecipe.getResultAdmixture().getItem());
        }
    }

    private static List<FixationSeparationRecipe> filteredRecipes = new ArrayList<>();
    private int recipeFilterRow, recipeFilterRowTotal;
    private void updateDisplayedRecipes(String filter) {
        List<FixationSeparationRecipe> fabricationRecipeOutputs = menu.blockEntity.getLevel().getRecipeManager().getAllRecipesFor(FixationSeparationRecipe.Type.INSTANCE);
        filteredRecipes.clear();

        for(FixationSeparationRecipe fsr : fabricationRecipeOutputs) {
            String display = fsr.getResultAdmixture().getDisplayName().getString();
            if((Objects.equals(filter, "") || display.toLowerCase().contains(filter.toLowerCase()))) {
                filteredRecipes.add(fsr);
            }
        }

        recipeFilterRowTotal = filteredRecipes.size() / 3;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        gui.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);
        gui.blit(TEXTURE, x+PANEL_RECIPE_X, y+PANEL_RECIPE_Y, PANEL_RECIPE_U, 0, PANEL_RECIPE_W, PANEL_RECIPE_H);

        int sp = AdmixerBlockEntity.getScaledProgress(menu.blockEntity);
        if(sp > 0)
            gui.blit(TEXTURE, x+74, y+53, 0, 228, sp, AdmixerBlockEntity.PROGRESS_BAR_SIZE);

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

        if(menu.blockEntity.getCurrentRecipe() == null)
            return;

        FixationSeparationRecipe fsr = menu.blockEntity.getCurrentRecipe();

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
        if(menu.blockEntity.getCurrentRecipe() == null) {
            gui.blit(TEXTURE, x, y, 28, 238, 18, 18);
        }
        else {
            gui.renderFakeItem(menu.blockEntity.getCurrentRecipe().getResultAdmixture(), x+1, y+1);
        }
    }

    private void renderIngredientPanel(GuiGraphics gui, int x, int y) {
        RenderSystem.setShaderTexture(0, TEXTURE_EXT);

        if(menu.blockEntity.getCurrentRecipe() != null) {
            FixationSeparationRecipe recipe = menu.blockEntity.getCurrentRecipe();

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

        recipeFilterBox.setX(xOrigin - 58);
        recipeFilterBox.setY(yOrigin - 7);

        if(recipeFilterBox.getValue().isEmpty())
            recipeFilterBox.setSuggestion(Component.translatable("gui.magichem.typetofilter").getString());
        else
            recipeFilterBox.setSuggestion("");

        addRenderableWidget(recipeFilterBox);
    }

    private void renderRecipeOptions(GuiGraphics gui) {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        List<FixationSeparationRecipe> snipped = new ArrayList<>();
        for(int i=recipeFilterRow*3; i<Math.min(filteredRecipes.size(), recipeFilterRow*3 + 15); i++) {
            snipped.add(filteredRecipes.get(i));
        }

        int c = 0;
        int cLimit = Math.min(15, snipped.size());
        while(c < cLimit) {

            for(int y=0; y<5; y++) {
                for (int x = 0; x < 3; x++) {

                    gui.renderItem(snipped.get(c).getResultAdmixture(), xOrigin - 58 + x*18, yOrigin + 17 + y*18);
                    c++;
                    if(c >= cLimit) break;
                }
                if(c >= cLimit) break;
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int x, int y) {
        FixationSeparationRecipe recipe = menu.blockEntity.getCurrentRecipe();
        if(recipe != null) {
            for (int i = 0; i < recipe.getComponentMateria().size(); i++) {
                Component text = Component.literal(recipe.getComponentMateria().get(i).getCount() + " x ")
                        .append(Component.translatable("item."+MagiChemMod.MODID+"."+recipe.getComponentMateria().get(i).getItem()+".short"));
                gui.pose().scale(0.5f, 0.5f, 0.5f);

                gui.drawString(Minecraft.getInstance().font, text, 400, 153 + i*36, 0xff000000);
                gui.pose().scale(2.0f, 2.0f, 2.0f);
            }
        }
    }
}
