package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.enums.LuminType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CovetousCofferScreen extends AbstractContainerScreen<CovetousCofferMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_covetous_coffer.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 198;

    public CovetousCofferScreen(CovetousCofferMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
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

        for(int i=1; i<=4; i++)
        if(menu.blockEntity.getTypeFromSlotID(i) != null) {
            pGuiGraphics.renderItem(menu.blockEntity.getOutputStackFromSlotID(i), x + 26 + (i-1)*36, y + 49);
            String storedAmount = ""+menu.blockEntity.getCountFromSlotID(i);
            pGuiGraphics.drawString(font, storedAmount, x+34 - font.width(storedAmount)/2 + (i-1)*36, y+70, 0xff000000, false);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {

        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;
        boolean doOriginalTooltip = true;

//        if (pX >= x + 79 && pX <= x + 106 &&
//                pY >= y + 48 && pY <= y + 68) {
//
//            tooltipContents.addAll(materiaStack.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
//            tooltipContents.add(Component.empty());
//            tooltipContents.add(Component.empty()
//                    .append(Component.literal("" + Math.min(ServerConfig.conjurerMateriaCapacity, menu.blockEntity.getMateriaAmount())).withStyle(ChatFormatting.DARK_AQUA))
//                    .append(Component.literal(" / ").withStyle(ChatFormatting.DARK_GRAY))
//                    .append(Component.literal("" + ServerConfig.conjurerMateriaCapacity).withStyle(ChatFormatting.DARK_AQUA))
//            );
//        }


        if(doOriginalTooltip)
            super.renderTooltip(pGuiGraphics, pX, pY);

        pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        Font font = Minecraft.getInstance().font;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}
