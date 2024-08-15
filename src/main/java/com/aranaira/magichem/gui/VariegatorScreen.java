package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class VariegatorScreen extends AbstractContainerScreen<VariegatorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_variegator.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 161,
            PANEL_INSERTION_U = 176, PANEL_INSERTION_V = 0, PANEL_INSERTION_W = 32, PANEL_INSERTION_H = 58,
            PANEL_ADMIXTURE_U = 176, PANEL_ADMIXTURE_V = 58, PANEL_ADMIXTURE_W = 18, PANEL_ADMIXTURE_H = 63,
            PANEL_COLORS_U = 0, PANEL_COLORS_V = 161, PANEL_COLORS_W = 191, PANEL_COLORS_H = 32,
            PANEL_COLORLESS_U = 0, PANEL_COLORLESS_V = 193, PANEL_COLORS_S = 11;

    public VariegatorScreen(VariegatorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        pGuiGraphics.blit(TEXTURE, x - 8, y - 37, PANEL_COLORS_U, PANEL_COLORS_V, PANEL_COLORS_W, PANEL_COLORS_H);

        pGuiGraphics.blit(TEXTURE, x + 15, y + 6, PANEL_ADMIXTURE_U, PANEL_ADMIXTURE_V, PANEL_ADMIXTURE_W, PANEL_ADMIXTURE_H);

        pGuiGraphics.blit(TEXTURE, x + 4, y + 6, PANEL_COLORLESS_U, PANEL_COLORLESS_V, PANEL_COLORS_S, PANEL_COLORS_S);

        pGuiGraphics.blit(TEXTURE, x + 149, y + 4, PANEL_INSERTION_U, PANEL_INSERTION_V, PANEL_INSERTION_W, PANEL_INSERTION_H);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {

    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        super.renderTooltip(pGuiGraphics, pX, pY);
    }
}
