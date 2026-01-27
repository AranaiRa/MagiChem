package com.aranaira.magichem.gui;

import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.DistilleryBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractDistillationBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DistilleryScreen extends AbstractContainerScreen<DistilleryMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_distillery.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 204,
            PANEL_GRIME_X = 176, PANEL_GRIME_Y = 14, PANEL_GRIME_W = 64, PANEL_GRIME_H = 59, PANEL_GRIME_U = 176, PANEL_GRIME_V = 0,
            TOOLTIP_EFFICIENCY_X = 178, TOOLTIP_EFFICIENCY_Y = 18, TOOLTIP_EFFICIENCY_W = 57, TOOLTIP_EFFICIENCY_H = 15,
            TOOLTIP_OPERATIONTIME_X = 178, TOOLTIP_OPERATIONTIME_Y = 37, TOOLTIP_OPERATIONTIME_W = 57, TOOLTIP_OPERATIONTIME_H = 15,
            TOOLTIP_GRIME_X = 179, TOOLTIP_GRIME_Y = 53, TOOLTIP_GRIME_W = 56, TOOLTIP_GRIME_H = 14;

    public DistilleryScreen(DistilleryMenu menu, Inventory inventory, Component component) {
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

        int sProg = DistilleryBlockEntity.getScaledProgress(menu.getProgress(), menu.getGrime(), menu.getBatchSize(), menu.getOperationTimeMod(), DistilleryBlockEntity::getVar, menu.blockEntity::getPoweredOperationTime);
        if(sProg > 0)
            gui.blit(TEXTURE, x+76, y+47, 0, 228, sProg, 28);

        int sGrime = DistilleryBlockEntity.getScaledGrime(menu.getGrime());
        if(sGrime > 0)
            gui.blit(TEXTURE, x+182, y+57, 24, 248, sGrime, 8);

        if(menu.getHeatDuration() > 0) {
            int sHeat = DistilleryBlockEntity.getScaledHeat(menu.getHeat(), menu.getHeatDuration(), DistilleryBlockEntity::getVar);
            int hHeat = DistilleryBlockEntity.getVar(AbstractDistillationBlockEntity.IDs.GUI_HEAT_GAUGE_HEIGHT) - sHeat;
            gui.blit(TEXTURE, x + 79, y + 78 + hHeat, 24, 232 + hHeat, 18, sHeat);
        }

        if(!menu.blockEntity.getFluidInTank(0).isEmpty()) {
            IClientFluidTypeExtensions extension = IClientFluidTypeExtensions.of(menu.blockEntity.getFluidInTank(0).getFluid());
            int packedTint = extension.getTintColor();
            float a = ((packedTint >> 24) & 0xff) / 255.0f;
            float r = ((packedTint >> 16) & 0xff) / 255.0f;
            float g = ((packedTint >> 8) & 0xff) / 255.0f;
            float b = ((packedTint) & 0xff) / 255.0f;
            gui.setColor(r,g,b,a);

            int height = menu.blockEntity.getFluidInTank(0).getAmount() * 106 / menu.blockEntity.getTankCapacity(0);

            ResourceLocation rl = new ResourceLocation(extension.getStillTexture().getNamespace(), "textures/"+extension.getStillTexture().getPath()+".png");
            gui.blit(rl, x + 8, y + 114 - height, 0, 0, 16, height, 16, 16);

            gui.setColor(1.0f,1.0f,1.0f,1.0f);
        }
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

        int g = 40;

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
                    .append(Component.translatable("tooltip.magichem.gui.operation_time").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.operation_time.line1")));
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
                    .append(Component.literal(ServerConfig.grimePenaltyPoint+"%").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.grime.line2.2")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.grime.line3").withStyle(ChatFormatting.DARK_GRAY))
                    .append(" ")
                    .append(Component.literal(String.format("%.1f", DistilleryBlockEntity.getGrimePercent(menu.getGrime(), DistilleryBlockEntity::getVar)*100.0f)+"%").withStyle(ChatFormatting.DARK_AQUA)));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Fluid Bar
        if(!menu.blockEntity.getFluidInTank(0).isEmpty()) {
            if (mouseX >= x + 8 && mouseX <= x + 24 &&
                    mouseY >= y + 8 && mouseY <= y + 114) {

                tooltipContents.add(Component.empty()
                        .append(Component.translatable(menu.blockEntity.getFluidInTank(0).getTranslationKey()).withStyle(ChatFormatting.GOLD)));
                tooltipContents.add(Component.empty()
                        .append(Component.literal(menu.blockEntity.getFluidInTank(0).getAmount() + " / " + menu.blockEntity.getTankCapacity(0)).withStyle(ChatFormatting.DARK_AQUA)));
                gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int pMouseX, int pMouseY) {
        Font font = Minecraft.getInstance().font;

        gui.drawString(font, Component.literal(DistilleryBlockEntity.getActualEfficiency(menu.getEfficiencyMod(), menu.getGrime(), DistilleryBlockEntity::getVar)+"%"), PANEL_GRIME_X + 20, PANEL_GRIME_Y - 10, 0xff000000, false);

        int secWhole = DistilleryBlockEntity.getOperationTicks(menu.getGrime(), menu.getBatchSize(), menu.getOperationTimeMod(), DistilleryBlockEntity::getVar, menu.blockEntity::getPoweredOperationTime) / 20;
        int secPartial = (DistilleryBlockEntity.getOperationTicks(menu.getGrime(), menu.getBatchSize(), menu.getOperationTimeMod(), DistilleryBlockEntity::getVar, menu.blockEntity::getPoweredOperationTime) % 20) * 5;
        gui.drawString(font ,secWhole+"."+(secPartial < 10 ? "0"+secPartial : secPartial)+" s", PANEL_GRIME_X + 20, PANEL_GRIME_Y + 9, 0xff000000, false);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        boolean isNumber = (pKeyCode >= 48) && (pKeyCode <= 57);
        boolean isNumpadNumber = (pKeyCode >= 97) && (pKeyCode <= 105);

        if(isNumber || isNumpadNumber) return false;

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    public static List<Rect2i> getGuiExtraAreas(DistilleryScreen screen) {
        int xOrigin = (screen.width - PANEL_MAIN_W) / 2;
        int yOrigin = (screen.height - PANEL_MAIN_H) / 2;
        return List.of(
                new Rect2i(xOrigin + PANEL_GRIME_X, yOrigin + PANEL_GRIME_Y, PANEL_GRIME_W, PANEL_GRIME_H)
        );
    }
}
