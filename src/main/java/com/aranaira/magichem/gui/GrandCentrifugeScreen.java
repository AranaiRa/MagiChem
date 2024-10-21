package com.aranaira.magichem.gui;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.GrandCentrifugeBlockEntity;
import com.aranaira.magichem.networking.GrandDeviceSyncDataC2SPacket;
import com.aranaira.magichem.registry.PacketRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GrandCentrifugeScreen extends AbstractContainerScreen<GrandCentrifugeMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_grand_centrifuge.png");
    private static final ResourceLocation TEXTURE_GDIST =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_grand_distillery.png");
    private static final ResourceLocation TEXTURE_EXT =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fabrication_ext.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 204,
            PANEL_GRIME_X = 176, PANEL_GRIME_Y = 14, PANEL_GRIME_W = 80, PANEL_GRIME_H = 80, PANEL_GRIME_U = 176, PANEL_GRIME_V = 0,
            TOOLTIP_EFFICIENCY_X = 193, TOOLTIP_EFFICIENCY_Y = 22, TOOLTIP_EFFICIENCY_W = 59, TOOLTIP_EFFICIENCY_H = 15,
            TOOLTIP_POWERUSAGE_X = 193, TOOLTIP_POWERUSAGE_Y = 39, TOOLTIP_POWERUSAGE_W = 59, TOOLTIP_POWERUSAGE_H = 15,
            TOOLTIP_OPERATIONTIME_X = 193, TOOLTIP_OPERATIONTIME_Y = 56, TOOLTIP_OPERATIONTIME_W = 59, TOOLTIP_OPERATIONTIME_H = 15,
            TOOLTIP_GRIME_X = 180, TOOLTIP_GRIME_Y = 77, TOOLTIP_GRIME_W = 69, TOOLTIP_GRIME_H = 10;
    private ImageButton
            b_powerLevelUp, b_powerLevelDown;

    public GrandCentrifugeScreen(GrandCentrifugeMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        initializePowerLevelButtons();
    }

    private void initializePowerLevelButtons(){
        b_powerLevelUp = this.addRenderableWidget(new ImageButton(this.leftPos + 180, this.topPos + 2, 12, 7, 232, 242, TEXTURE_GDIST, button -> {
            menu.blockEntity.incrementPowerUsageSetting();
            PacketRegistry.sendToServer(new GrandDeviceSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    menu.blockEntity.getPowerUsageSetting()
            ));
        }));
        b_powerLevelDown = this.addRenderableWidget(new ImageButton(this.leftPos + 180, this.topPos + 47, 12, 7, 244, 242, TEXTURE_GDIST, button -> {
            menu.blockEntity.decrementPowerUsageSetting();
            PacketRegistry.sendToServer(new GrandDeviceSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    menu.blockEntity.getPowerUsageSetting()
            ));
        }));
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

        int sProg = GrandCentrifugeBlockEntity.getScaledProgress(menu.getProgress(), menu.getGrime(), menu.getBatchSize(), menu.getOperationTimeMod(), GrandCentrifugeBlockEntity::getVar, menu.blockEntity::getPoweredOperationTime);
        if(sProg > 0)
            gui.blit(TEXTURE, x+76, y+47, 0, 228, sProg, 28);

        int powerLevel = menu.blockEntity.getPowerUsageSetting();
        gui.blit(TEXTURE_GDIST, x+182, y + (62 - powerLevel), 24, 248 - powerLevel, 8, powerLevel);

        int sGrime = GrandCentrifugeBlockEntity.getScaledGrime(menu.getGrime());
        if(sGrime > 0)
            gui.blit(TEXTURE_GDIST, x+181, y+78, 24, 248, sGrime, 8);

        if(!menu.blockEntity.getPowerSufficiency()) {
            renderPowerWarning(gui, x, y);
        }
    }

    private void renderGrimePanel(GuiGraphics gui, int x, int y) {
        gui.blit(TEXTURE_GDIST, x, y, PANEL_GRIME_U, PANEL_GRIME_V, PANEL_GRIME_W, PANEL_GRIME_H);
    }

    protected void renderPowerWarning(GuiGraphics gui, int x, int y) {
        long cycle = Minecraft.getInstance().level.getGameTime() % 20;

        gui.blit(TEXTURE_EXT, x+10, y-30, 0, 230, 156, 26);
        if(cycle < 10) {
            gui.blit(TEXTURE_EXT, x + 17, y - 23, 156, 244, 12, 12);
            gui.blit(TEXTURE_EXT, x + 147, y - 23, 156, 244, 12, 12);
        }
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

        //Efficiency
        if(mouseX >= x+TOOLTIP_POWERUSAGE_X && mouseX <= x+TOOLTIP_POWERUSAGE_X+TOOLTIP_POWERUSAGE_W &&
                mouseY >= y+TOOLTIP_POWERUSAGE_Y && mouseY <= y+TOOLTIP_POWERUSAGE_Y+TOOLTIP_POWERUSAGE_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.powerusage").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.powerusage.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.translatable("tooltip.magichem.gui.powerusage.line2"));
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
                    .append(" ")
                    .append(Component.literal(String.format("%.1f", GrandCentrifugeBlockEntity.getGrimePercent(menu.getGrime(), GrandCentrifugeBlockEntity::getVar)*100.0f)+"%").withStyle(ChatFormatting.DARK_AQUA)));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int pMouseX, int pMouseY) {
        Font font = Minecraft.getInstance().font;

        gui.drawString(font, Component.literal(GrandCentrifugeBlockEntity.getActualEfficiency(menu.getEfficiencyMod(), menu.getGrime(), GrandCentrifugeBlockEntity::getVar)+"%"), PANEL_GRIME_X + 32, PANEL_GRIME_Y - 7, 0xff000000, false);

        float fireActuatorReduction = 1 - (menu.getOperationTimeMod() / 10000f);
        int powerDraw = Math.round((float)menu.blockEntity.getPowerDraw() * fireActuatorReduction);
        gui.drawString(font, Component.literal(powerDraw + "/t"), PANEL_GRIME_X + 32, PANEL_GRIME_Y + 10, 0xff000000, false);

        int opTicks = GrandCentrifugeBlockEntity.getOperationTicks(menu.getGrime(), menu.getBatchSize(), menu.getOperationTimeMod(), GrandCentrifugeBlockEntity::getVar, menu.blockEntity::getPoweredOperationTime);
        int secWhole = opTicks / 20;
        int secPartial = (opTicks % 20) * 5;
        gui.drawString(font ,secWhole+"."+(secPartial < 10 ? "0"+secPartial : secPartial)+" s", PANEL_GRIME_X + 32, PANEL_GRIME_Y + 27, 0xff000000, false);

        if(!menu.blockEntity.getPowerSufficiency()) {
            MutableComponent warningText = Component.translatable("gui.magichem.insufficientpower");
            int width = Minecraft.getInstance().font.width(warningText.getString());
            gui.drawString(font, warningText, 89 - width/2, -40, 0xff000000, false);
        }
    }
}
