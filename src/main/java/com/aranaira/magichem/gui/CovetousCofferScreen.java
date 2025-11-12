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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

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
        menu.blockEntity.isLidOpening = true;
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

        for(int i=1; i<=4; i++) {
            final ItemStack displayStack = menu.blockEntity.getDisplayStackFromSlotID(i);
            if (displayStack != null) {
                pGuiGraphics.renderItem(displayStack, x + 26 + (i - 1) * 36, y + 49);
                int count = menu.blockEntity.getCountFromSlotID(i);
                String formattedCount = "" + menu.blockEntity.getCountFromSlotID(i);
                if(count > 999999999) formattedCount = (count / 1000000000)+"B";
                else if(count > 999999) formattedCount = (count / 1000000)+"M";
                else if(count > 99999) formattedCount = (count / 1000)+"K";
                pGuiGraphics.drawString(font, formattedCount, x + 34 - font.width(formattedCount) / 2 + (i - 1) * 36, y + 70, displayStack.isEmpty() ? 0xff444444 : 0xff000000, false);
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

        if (pX >= x + 25 && pX <= x + 43 &&
                pY >= y + 48 && pY <= y + 66) {
            if(!menu.blockEntity.getDisplayStackFromSlotID(1).isEmpty()) tooltipContents.addAll(menu.blockEntity.getDisplayStackFromSlotID(1).getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
        }

        if (pX >= x + 61 && pX <= x + 104 &&
                pY >= y + 48 && pY <= y + 66) {
            if(!menu.blockEntity.getDisplayStackFromSlotID(2).isEmpty()) tooltipContents.addAll(menu.blockEntity.getDisplayStackFromSlotID(2).getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
        }

        if (pX >= x + 97 && pX <= x + 115 &&
                pY >= y + 48 && pY <= y + 66) {
            if(!menu.blockEntity.getDisplayStackFromSlotID(3).isEmpty()) tooltipContents.addAll(menu.blockEntity.getDisplayStackFromSlotID(3).getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
        }

        if (pX >= x + 133 && pX <= x + 151 &&
                pY >= y + 48 && pY <= y + 66) {
            if(!menu.blockEntity.getDisplayStackFromSlotID(4).isEmpty()) tooltipContents.addAll(menu.blockEntity.getDisplayStackFromSlotID(4).getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
        }


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

    @Override
    public void onClose() {
        super.onClose();
        menu.blockEntity.isLidOpening = false;
    }
}
