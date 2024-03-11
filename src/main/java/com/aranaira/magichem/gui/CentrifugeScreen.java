package com.aranaira.magichem.gui;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CentrifugeScreen extends AbstractContainerScreen<CentrifugeMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_centrifuge.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 189,
            PANEL_GRIME_X = 158, PANEL_GRIME_Y = 38, PANEL_GRIME_W = 64, PANEL_GRIME_H = 59, PANEL_GRIME_U = 176, PANEL_GRIME_V = 0,
            TOOLTIP_EFFICIENCY_X = 160, TOOLTIP_EFFICIENCY_Y = 43, TOOLTIP_EFFICIENCY_W = 57, TOOLTIP_EFFICIENCY_H = 15,
            TOOLTIP_OPERATIONTIME_X = 160, TOOLTIP_OPERATIONTIME_Y = 62, TOOLTIP_OPERATIONTIME_W = 57, TOOLTIP_OPERATIONTIME_H = 15,
            TOOLTIP_GRIME_X = 161, TOOLTIP_GRIME_Y = 78, TOOLTIP_GRIME_W = 56, TOOLTIP_GRIME_H = 14;

    public CentrifugeScreen(CentrifugeMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        gui.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        renderGrimePanel(gui, x + PANEL_GRIME_X, y + PANEL_GRIME_Y);

        float a = menu.getOperationTimeMod();
        int b = menu.getGrime();
        int c = menu.getProgress();

        int sProg = CentrifugeBlockEntity.getScaledProgress(menu.getProgress(), menu.getGrime(), menu.getOperationTimeMod());
        if(sProg > 0)
            gui.blit(TEXTURE, x+58, y+53, 0, 228, sProg, 28);

        int sGrime = CentrifugeBlockEntity.getScaledGrime(menu.getGrime());
        if(sGrime > 0)
            gui.blit(TEXTURE, x+164, y+81, 24, 248, sGrime, 8);
    }

    private void renderGrimePanel(GuiGraphics gui, int x, int y) {
        gui.blit(TEXTURE, x, y, PANEL_GRIME_U, PANEL_GRIME_V, PANEL_GRIME_W, PANEL_GRIME_H);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        renderBackground(gui);
        super.render(gui, mouseX, mouseY, delta);
        renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderTooltip(gui, mouseX, mouseY);

        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Efficiency
        if(mouseX >= x+TOOLTIP_EFFICIENCY_X && mouseX <= x+TOOLTIP_EFFICIENCY_X+TOOLTIP_EFFICIENCY_W &&
                mouseY >= y+TOOLTIP_EFFICIENCY_Y && mouseY <= y+TOOLTIP_EFFICIENCY_Y+TOOLTIP_EFFICIENCY_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.efficiency").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.efficiency.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.translatable("tooltip.magichem.gui.efficiency.line2"));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Operation Time
        if(mouseX >= x+TOOLTIP_OPERATIONTIME_X && mouseX <= x+TOOLTIP_OPERATIONTIME_X+TOOLTIP_OPERATIONTIME_W &&
                mouseY >= y+TOOLTIP_OPERATIONTIME_Y && mouseY <= y+TOOLTIP_OPERATIONTIME_Y+TOOLTIP_OPERATIONTIME_H) {

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.operationtime").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.operationtime.line1")));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Grime Bar
        if(mouseX >= x+TOOLTIP_GRIME_X && mouseX <= x+TOOLTIP_GRIME_X+TOOLTIP_GRIME_W &&
                mouseY >= y+TOOLTIP_GRIME_Y && mouseY <= y+TOOLTIP_GRIME_Y+TOOLTIP_GRIME_H) {

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.grime").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.grime.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.translatable("tooltip.magichem.gui.grime.line2.1")
                    .append(Component.literal(Config.grimePenaltyPoint+"%").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.grime.line2.2")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.grime.line3").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(String.format("%.1f", CentrifugeBlockEntity.getGrimePercent(menu.getGrime())*100.0f)+"%").withStyle(ChatFormatting.DARK_AQUA)));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;

        gui.drawString(font, Component.literal(CentrifugeBlockEntity.getActualEfficiency(menu.getEfficiencyMod(), menu.getGrime())+"%"), PANEL_GRIME_X + 20, PANEL_GRIME_Y - 2, 0xff000000, false);

        int secWhole = CentrifugeBlockEntity.getOperationTicks(menu.getGrime(), menu.getOperationTimeMod()) / 20;
        int secPartial = (CentrifugeBlockEntity.getOperationTicks(menu.getGrime(), menu.getOperationTimeMod()) % 20) * 5;
        gui.drawString(font ,secWhole+"."+(secPartial < 10 ? "0"+secPartial : secPartial)+" s", PANEL_GRIME_X + 20, PANEL_GRIME_Y + 17, 0xff000000, false);
    }
}
