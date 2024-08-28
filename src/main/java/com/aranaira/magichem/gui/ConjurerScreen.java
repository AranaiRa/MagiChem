package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.aranaira.magichem.block.entity.ConjurerBlockEntity.*;

public class ConjurerScreen extends AbstractContainerScreen<ConjurerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_conjurer.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 155;
    private ItemStack materiaStack = ItemStack.EMPTY;

    public ConjurerScreen(ConjurerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Main panel
        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        //Progress gauge
        int sp = menu.blockEntity.getScaledProgress();
        pGuiGraphics.blit(TEXTURE, x + 74, y + 11, 0, 230, sp, 26);

        //Materia indicator
        if(menu.blockEntity.getRecipe() != null) {
            if (menu.blockEntity.getRecipe().getMateria() != materiaStack.getItem()) {
                materiaStack = new ItemStack(menu.blockEntity.getRecipe().getMateria());
            }
            pGuiGraphics.renderItem(materiaStack, x + 80, y + 50);

            //Materia fill gauge
            if(menu.blockEntity.getMateriaType() != null) {
                int sm = menu.blockEntity.getScaledMateria();
                int packedColor = menu.blockEntity.getMateriaType().getMateriaColor();
                int rI = (packedColor & 0x00ff0000) >> 16;
                int gI = (packedColor & 0x0000ff00) >> 8;
                int bI = (packedColor & 0x000000ff);

                pGuiGraphics.setColor(rI / 255f, gI / 255f, bI / 255f, 1f);
                pGuiGraphics.blit(TEXTURE, x + 102, y + 66 - sm, 253, 0, 3, sm);
                pGuiGraphics.setColor(1f, 1f, 1f, 1f);
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

        if (pX >= x + 80 && pX <= x + 96 &&
                pY >= y + 50 && pY <= y + 66) {

            tooltipContents.addAll(materiaStack.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
        }


        if(doOriginalTooltip)
            super.renderTooltip(pGuiGraphics, pX, pY);

        pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {

    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}
