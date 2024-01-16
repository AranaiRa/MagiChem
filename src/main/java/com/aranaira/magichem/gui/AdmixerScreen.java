package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AdmixerScreen extends AbstractContainerScreen<AdmixerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_admixer.png");

    public AdmixerScreen(AdmixerMenu menu, Inventory inventory, Component component) {
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

        //main panel
        int w = 176;
        int h = 207;

        //filter panel
        int fu = 176;
        int fw = 80;
        int fh = 126;

        int x = (width - w) / 2;
        int y = (height - h) / 2;

        this.blit(poseStack, x, y, 0, 0, w, h);
        this.blit(poseStack, x-66, y-13, fu, 0, fw, fh);

        int sp = menu.blockEntity.getScaledProgress(menu.blockEntity);
        if(sp > 0)
            this.blit(poseStack, x+59, y+83, 0, 253, sp, 3);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {

    }
}
