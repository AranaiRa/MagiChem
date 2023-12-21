package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CirclePowerScreen extends AbstractContainerScreen<CirclePowerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_circle_power.png");

    public CirclePowerScreen(CirclePowerMenu menu, Inventory inventory, Component component) {
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

        int w = 176;
        int h = 139;

        int x = (width - w) / 2;
        int y = (height - h) / 2;

        this.blit(poseStack, x, y, 0, 0, w, h);
        renderProgressBar(1, poseStack, x + 23, y + 42);
        renderProgressBar(2, poseStack, x + 59, y + 42);
    }

    private void renderProgressBar(int tier, PoseStack poseStack, int x, int y) {
        int sp = menu.getScaledProgress(tier);

        if(menu.isCrafting(tier))
            blit(poseStack, x, y, 0, 253, sp, 3);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {

    }
}
