package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.CircleFabricationBlockEntity;
import com.aranaira.magichem.foundation.ButtonData;
import com.aranaira.magichem.gui.element.ImageButtonFabricationRecipeSelector;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CircleFabricationScreen extends AbstractContainerScreen<CircleFabricationMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fabrication.png");
    private ImageButton
            b_powerLevelUp, b_powerLevelDown;
    private ButtonData[] recipeSelectButtons = new ButtonData[15];
    private EditBox recipeFilterBox;
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 192,
            PANEL_RECIPE_X = 176, PANEL_RECIPE_Y = 0, PANEL_RECIPE_W = 80, PANEL_RECIPE_H = 126,
            PANEL_POWER_X = 176, PANEL_POWER_Y = 126, PANEL_POWER_W = 80, PANEL_POWER_H = 66;

    public CircleFabricationScreen(CircleFabricationMenu menu, Inventory inventory, Component component) {
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

    private void initializeRecipeFilterBox() {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        recipeFilterBox = new EditBox(Minecraft.getInstance().font, x, y, 67, 18, Component.empty());
        this.addWidget(recipeFilterBox);
    }

    private void initializePowerLevelButtons(){
        b_powerLevelUp = this.addRenderableWidget(new ImageButton(this.leftPos + 180, this.topPos + 126, 12, 7, 232, 242, TEXTURE, button -> {
            menu.blockEntity.incrementPowerLevel();
        }));
        b_powerLevelDown = this.addRenderableWidget(new ImageButton(180, 126, 12, 7, 244, 242, TEXTURE, button -> {
            menu.blockEntity.decrementPowerLevel();
        }));
    }

    private void initializeRecipeSelectorButtons(){
        int c = 0;
        for(int y=0; y<5; y++) {
            for(int x=0; x<3; x++) {
                recipeSelectButtons[c] = new ButtonData(this.addRenderableWidget(new ImageButtonFabricationRecipeSelector(
                        this, c, this.leftPos, this.topPos, 18, 18, 92, 220, TEXTURE, button -> {
                    CircleFabricationScreen query = (CircleFabricationScreen) ((ImageButtonFabricationRecipeSelector) button).getScreen();
                    query.setActiveRecipe(((ImageButtonFabricationRecipeSelector) button).getArrayIndex());
                })), x*18 - 71, y*18 + 42);
                c++;
            }
        }
    }

    public void setActiveRecipe(int index) {
        int trueIndex = recipeFilterRow*3 + index;
        if(trueIndex < filteredRecipeItems.size()) {
            menu.blockEntity.setCurrentRecipeTarget(filteredRecipeItems.get(trueIndex));
        }
    }

    private static List<Item> filteredRecipeItems = new ArrayList<>();
    private int recipeFilterRow, recipeFilterRowTotal;
    private void updateDisplayedRecipes(String filter) {
        List<Item> fabrictionRecipeOutputs = AlchemicalCompositionRecipe.getFabrictionRecipeOutputs(menu.blockEntity.getLevel());
        filteredRecipeItems.clear();

        if(filter == "" || filter == null) {
            filteredRecipeItems.addAll(fabrictionRecipeOutputs);
        }
        else {
            for(Item i : fabrictionRecipeOutputs) {
                if(i.toString().contains(filter)) {
                    filteredRecipeItems.add(i);
                }
            }
        }

        recipeFilterRowTotal = filteredRecipeItems.size() / 3;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Panels
        this.blit(poseStack, x - 78, y + 13, PANEL_RECIPE_X, PANEL_RECIPE_Y, PANEL_RECIPE_W, PANEL_RECIPE_H);
        this.blit(poseStack, x + 176, y + 19, PANEL_POWER_X, PANEL_POWER_Y, PANEL_POWER_W, PANEL_POWER_H);
        this.blit(poseStack, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        renderProgressBar(poseStack, x + 55, y + 12);

        RenderSystem.setShader(GameRenderer::getBlockShader);
        renderSelectedRecipe(poseStack, x + 79, y + 79);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        renderPowerLevelBar(poseStack, x + 182, y + 37);
    }

    private void renderButtons(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        b_powerLevelUp.setPosition(x+180, y+26);
        b_powerLevelUp.renderButton(poseStack, mouseX, mouseY, partialTick);
        b_powerLevelUp.active = true;
        b_powerLevelUp.visible = true;

        b_powerLevelDown.setPosition(x+180, y+71);
        b_powerLevelDown.renderButton(poseStack, mouseX, mouseY, partialTick);
        b_powerLevelDown.active = true;
        b_powerLevelDown.visible = true;

        for(ButtonData bd : recipeSelectButtons) {
            bd.getButton().setPosition(x+bd.getXOffset(), y+bd.getYOffset());
            bd.getButton().renderButton(poseStack, mouseX, mouseY, partialTick);
            bd.getButton().active = true;
            bd.getButton().visible = true;
        }
    }

    private void renderPowerLevelBar(PoseStack poseStack, int x, int y) {
        int powerLevel = menu.blockEntity.getPowerLevel();

        this.blit(poseStack, x, y + (30 - powerLevel), 84, 256 - powerLevel, 8, powerLevel);
    }

    private void renderSelectedRecipe(PoseStack poseStack, int x, int y) {
        if(menu.blockEntity.getCurrentRecipeTarget() == null) {
            this.blit(poseStack, x, y, 66, 238, 18, 18);
        }
        else
            Minecraft.getInstance().getItemRenderer().renderGuiItem(new ItemStack(menu.blockEntity.getCurrentRecipeTarget()), x+1, y+1);
    }

    private void renderProgressBar(PoseStack poseStack, int x, int y) {
        int sp = CircleFabricationBlockEntity.getScaledProgress(menu.blockEntity);
        if(sp > 0)
            this.blit(poseStack, x, y , 0, 199, sp, CircleFabricationBlockEntity.PROGRESS_BAR_HEIGHT);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
        renderButtons(poseStack, delta, mouseX, mouseY);
        renderFilterBox();
        updateDisplayedRecipes(recipeFilterBox.getValue());
        renderRecipeOptions();
    }

    private void renderFilterBox() {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        recipeFilterBox.x = xOrigin - 70;
        recipeFilterBox.y = yOrigin + 21;

        if(recipeFilterBox.getValue().isEmpty())
            recipeFilterBox.setSuggestion(Component.translatable("gui.magichem.typetofilter").getString());
        else
            recipeFilterBox.setSuggestion("");

        addRenderableWidget(recipeFilterBox);
    }

    private void renderRecipeOptions() {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        List<Item> snipped = new ArrayList<>();
        for(int i=recipeFilterRow*3; i<Math.min(filteredRecipeItems.size(), recipeFilterRow*3 + 15); i++) {
            snipped.add(filteredRecipeItems.get(i));
        }

        int c = 0;
        int cLimit = Math.min(15, snipped.size());
        while(c < cLimit) {

            for(int y=0; y<5; y++) {
                for (int x = 0; x < 3; x++) {

                    Minecraft.getInstance().getItemRenderer().renderGuiItem(
                            new ItemStack(snipped.get(c)),
                            xOrigin - 70 + x*18, yOrigin + 43 + y*18
                            );
                    c++;
                    if(c >= cLimit) break;
                }
                if(c >= cLimit) break;
            }
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int x, int y) {
        int powerDraw = menu.blockEntity.getPowerDraw();
        int secWhole = menu.blockEntity.getOperationTicks() / 20;
        int secPartial = (menu.blockEntity.getOperationTicks() % 20) * 5;

        Minecraft.getInstance().font.draw(poseStack, powerDraw+"/t", 208, 26, 0xff000000);
        Minecraft.getInstance().font.draw(poseStack, secWhole+"."+(secPartial < 10 ? "0"+secPartial : secPartial)+" s", 208, 45, 0xff000000);
    }
}
