package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.SkywrathAltarBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class SkywrathCondenserScreen extends AbstractContainerScreen<SkywrathCondenserMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_skywrath_condenser.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 154;
    private static final Pair<Integer,Integer>[] ALTAR_GUI_COORDS = new Pair[] {
            new Pair(60, 23), new Pair(52, 27), new Pair(44, 31),
            new Pair(69, 27), new Pair(60, 31), new Pair(52, 35),
            new Pair(77, 31), new Pair(69, 35), new Pair(60, 39)
    };
    private int altarFlags = 0;

    public SkywrathCondenserScreen(SkywrathCondenserMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);

        final HashMap<Integer, SkywrathAltarBlockEntity> altarsInOperatingArea = menu.blockEntity.getAltarsInOperatingArea();
        for(int i=0; i<9; i++) {
            if(altarsInOperatingArea.containsKey(i)) {
                altarFlags |= (1 << i);
            }
        }
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

        //little altars
        if(altarFlags == 0) {
            renderWarningBar(pGuiGraphics, x, y);
        } else {
            for (int i = 0; i < 9; i++) {
                int check = (1 << i);
                if ((altarFlags & check) == check) {
                    pGuiGraphics.blit(TEXTURE, x + ALTAR_GUI_COORDS[i].getFirst(), y + ALTAR_GUI_COORDS[i].getSecond(), 197, 0, 10, 12);
                }
            }
        }

        //Storm droplets gauge
        int sM = Math.min(42, menu.blockEntity.getDroplets() * 42 / (ServerConfig.skywrathCondenserMateriaUnitsPerDram * 5));
        pGuiGraphics.blit(TEXTURE, x + 107, y + 8 + (42 - sM), 254, 0, 2, sM);
    }

    protected void renderWarningBar(GuiGraphics gui, int x, int y) {
        long cycle = Minecraft.getInstance().level.getGameTime() % 20;

        gui.blit(TEXTURE, x+2, y-30, 0, 230, 172, 26);
        if(cycle < 10) {
            gui.blit(TEXTURE, x + 9, y - 23, 172, 244, 12, 12);
            gui.blit(TEXTURE, x + 155, y - 23, 172, 244, 12, 12);
        }

        cycle = Minecraft.getInstance().level.getGameTime() % 60;
        if(cycle < 30) {
            gui.blit(TEXTURE, x + 42, y + 5, 207, 0, 47, 47);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {

        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;
        boolean doOriginalTooltip = true;

        //Essentia
        if(pX >= x+106 && pX <= x+109 &&
                pY >= y+7 && pY <= y+51) {

            int current = menu.blockEntity.getDroplets();
            int max = ServerConfig.skywrathCondenserMateriaUnitsPerDram * 5;
            float percent = menu.blockEntity.getDropletsPercent();

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.skywrath_condenser.admixture").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.skywrath_condenser.admixture.line1")));
            tooltipContents.add((Component.empty()));
            tooltipContents.add((Component.empty())
                    .append(Component.translatable("tooltip.magichem.gui.skywrath_condenser.admixture.line2a"))
                    .append(Component.literal(ServerConfig.skywrathCondenserMateriaUnitsPerDram+"").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.skywrath_condenser.admixture.line2b")));
            tooltipContents.add((Component.empty()));
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.skywrath_condenser.admixture.line3").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(Math.min(max, current) + " / " + max).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal("  ")
                            .append(Component.literal("( ").withStyle(ChatFormatting.DARK_GRAY))
                            .append(Component.literal(String.format("%.1f", Math.min(1, percent) * 100)+"%")).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal(" )").withStyle(ChatFormatting.DARK_GRAY)));
            pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
        }


        if(doOriginalTooltip)
            super.renderTooltip(pGuiGraphics, pX, pY);

        pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        Font font = Minecraft.getInstance().font;

        //Warning label
        if(altarFlags == 0) {
            MutableComponent warningText = Component.translatable("gui.magichem.noaltarsinreach");
            int width = Minecraft.getInstance().font.width(warningText.getString());
            pGuiGraphics.drawString(font, warningText, 89 - width / 2, -15, 0xff000000, false);
        }

        pGuiGraphics.drawString(font, Component.translatable("gui.magichem.direction.north.short"), 43, 25, 0xff555555, false);

//        final MutableComponent n = Component.translatable("gui.magichem.direction.north.short");
//        pGuiGraphics.drawString(font, n, 88 - font.width(n) / 2, 1, 0xff000000, false);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}
