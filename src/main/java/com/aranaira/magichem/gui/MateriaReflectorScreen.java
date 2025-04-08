package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.config.ServerConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
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

public class MateriaReflectorScreen extends AbstractContainerScreen<MateriaReflectorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_materia_reflector.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 177;
    private ItemStack materiaStack = ItemStack.EMPTY;

    public MateriaReflectorScreen(MateriaReflectorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
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

        //Rune of marking slot
        pGuiGraphics.pose().scale(0.5f, 0.5f, 0.5f);
        pGuiGraphics.blit(TEXTURE, x*2 + 133*2, y*2 + 61*2, 0, 220, 36, 36);
        pGuiGraphics.pose().scale(2.0f, 2.0f, 2.0f);
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

        final MutableComponent n = Component.translatable("gui.magichem.direction.north.short");
        pGuiGraphics.drawString(font, n, 88 - font.width(n) / 2, 1, 0xff000000, false);

        final MutableComponent s = Component.translatable("gui.magichem.direction.south.short");
        pGuiGraphics.drawString(font, s, 88 - font.width(s) / 2, 76, 0xff000000, false);

        final MutableComponent w = Component.translatable("gui.magichem.direction.west.short");
        pGuiGraphics.drawString(font, w, 54 - font.width(w), 38, 0xff000000, false);

        final MutableComponent e = Component.translatable("gui.magichem.direction.east.short");
        pGuiGraphics.drawString(font, e, 129 - font.width(e), 38, 0xff000000, false);

        final MutableComponent u = Component.translatable("gui.magichem.direction.up.short");
        pGuiGraphics.drawString(font, u, 23 - font.width(u), 15, 0xff000000, false);

        final MutableComponent d = Component.translatable("gui.magichem.direction.down.short");
        pGuiGraphics.drawString(font, d, 23 - font.width(d), 61, 0xff000000, false);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}
