package com.aranaira.magichem.gui;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.CircleFabricationBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CircleFabricationScreen extends AbstractContainerScreen<CircleFabricationMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fabrication.png");

    public CircleFabricationScreen(CircleFabricationMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 192,
            PANEL_RECIPE_X = 176, PANEL_RECIPE_Y = 0, PANEL_RECIPE_W = 80, PANEL_RECIPE_H = 126,
            PANEL_POWER_X = 176, PANEL_POWER_Y = 126, PANEL_POWER_W = 80, PANEL_POWER_H = 65;

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Panels
        this.blit(poseStack, x - 78, y + 13, PANEL_RECIPE_X, PANEL_RECIPE_Y, PANEL_RECIPE_W, PANEL_RECIPE_H);
        this.blit(poseStack, x + 176, y + 19, PANEL_POWER_X, PANEL_POWER_Y, PANEL_POWER_W, PANEL_POWER_H);
        this.blit(poseStack, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        renderProgressBar(poseStack, x + 55, y + 12);
        renderSelectedRecipe(poseStack, x + 79, y + 79);
        renderPowerPips(poseStack, x + 182, y + 37);


    }

    private void renderPowerPips(PoseStack poseStack, int x, int y) {
        int powerLevel = menu.blockEntity.getPowerLevel();
        int powerHeight = 31 - (2 * ((powerLevel + 2) / 3) + Math.max((powerLevel / 3 - 1),0));
        int powerWidth = 2 * (powerLevel % 3) + ((powerLevel - 1) % 3);

        //this.blit(poseStack, x, y + powerHeight, 84, 227 + powerHeight, 8, 29 - powerHeight);
        this.blit(poseStack, x, y + powerHeight, 84, 227 + powerHeight, powerWidth, 29 - powerHeight);
    }

    private void renderSelectedRecipe(PoseStack poseStack, int x, int y) {
        if(menu.blockEntity.getCurrentRecipeID() == "") {
            this.blit(poseStack, x, y, 66, 238, 18, 18);
        }
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

        menu.blockEntity.setPowerLevel(mouseY * 30 / height);
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {

    }
}
