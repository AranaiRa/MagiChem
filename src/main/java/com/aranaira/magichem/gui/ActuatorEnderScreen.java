package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ActuatorEnderBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ActuatorEnderScreen extends AbstractContainerScreen<ActuatorEnderMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_actuator_ender.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 159,
            SYMBOL_X = 36, SYMBOL_Y = 21, SYMBOL_U = 184, SYMBOL_V = 0, SYMBOL_W = 15, SYMBOL_H = 21,
            POWER_X = 22, POWER_Y = 19, POWER_U = 176, POWER_V = 0, POWER_W = 8, POWER_H = 26,
            GRIME_X = 56, GRIME_Y = 15, GRIME_U = 176, GRIME_V = 40, GRIME_W = 4,
            RAREFIED_GRIME_X = 56, RAREFIED_GRIME_Y = 15, RAREFIED_GRIME_U = 180, RAREFIED_GRIME_V = 40, RAREFIED_GRIME_W = 1,
            SAND_X = 106, SAND_Y = 15, SAND_W = 4,
            TOOLTIP_POWER_X = 26, TOOLTIP_POWER_Y = 17, TOOLTIP_POWER_W = 12, TOOLTIP_POWER_H = 30,
            TOOLTIP_GRIME_X = 55, TOOLTIP_GRIME_Y = 14, TOOLTIP_GRIME_W = 6, TOOLTIP_GRIME_H = 35,
            TOOLTIP_SAND_X = 105, TOOLTIP_SAND_Y = 14, TOOLTIP_SAND_W = 6, TOOLTIP_SAND_H = 35,
            TOOLTIP_IMPORTRATE_X = 108, TOOLTIP_IMPORTRATE_Y = 10, TOOLTIP_IMPORTRATE_W = 46, TOOLTIP_IMPORTRATE_H = 11,
            TOOLTIP_EXPORTRATE_X = 108, TOOLTIP_EXPORTRATE_Y = 25, TOOLTIP_EXPORTRATE_W = 46, TOOLTIP_EXPORTRATE_H = 11,
            TOOLTIP_ELDRIN_X = 108, TOOLTIP_ELDRIN_Y = 40, TOOLTIP_ELDRIN_W = 46, TOOLTIP_ELDRIN_H = 11;
    public static final int
            FLUID_GAUGE_H = 33;
    private ImageButton
        b_powerLevelUp, b_powerLevelDown;


    public ActuatorEnderScreen(ActuatorEnderMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        initializePowerLevelButtons();
    }

    private void initializePowerLevelButtons() {
        b_powerLevelUp = this.addRenderableWidget(new ImageButton(this.leftPos + 26, this.topPos + 12, 12, 7, 176, 26, TEXTURE, button -> {
            menu.incrementPowerLevel();
        }));
        b_powerLevelDown = this.addRenderableWidget(new ImageButton(this.leftPos + 26, this.topPos + 53, 12, 7, 188, 26, TEXTURE, button -> {
            menu.decrementPowerLevel();
        }));
        this.addRenderableWidget(new ImageButton(this.leftPos + 218, this.topPos + 12, 11, 11, 238, 0, TEXTURE, button -> {
            menu.toggleEldrinMode();
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

        //mark slot bg
        gui.pose().scale(0.5f, 0.5f, 0.5f);
        gui.blit(TEXTURE, x*2 + 174, y*2 + 44, 202, 0, 36, 36);
        gui.pose().scale(2.0f, 2.0f, 2.0f);

        //power level
        int plH = (menu.blockEntity.getPowerLevel() - 1) * 12 + 2;
        int plY = POWER_H - plH;
        gui.blit(TEXTURE, x + 28, y + 19 + plY, POWER_U, plY, POWER_W, plH);

        //progress symbol
        int sH = menu.blockEntity.getScaledCycleTime();
        int sY = SYMBOL_H - sH;
        gui.blit(TEXTURE, x + 42, y + SYMBOL_Y + sY, SYMBOL_U, sY, SYMBOL_W, sH);

        //Essentia insertion
        gui.blit(TEXTURE, x + 167, y + 3, 0, 172, 40, 58);
        int sM = Math.min(42, menu.blockEntity.getStoredMateria() * 42 / ServerConfig.actuatorMateriaBufferMaximum);
        gui.blit(TEXTURE, x + 175, y + 11 + (42 - sM), 200, 0, 2, sM);

        //Power draw
        gui.blit(TEXTURE, x + 211, y, 40, 174 + (menu.blockEntity.doEldrinPowerConsumption ? 28 : 0), 57, 28);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        renderBackground(gui);
        super.render(gui, mouseX, mouseY, delta);
        renderTooltip(gui, mouseX, mouseY);
    }

    protected void renderPowerWarning(GuiGraphics gui, int x, int y) {
        long cycle = Minecraft.getInstance().level.getGameTime() % 20;

        gui.blit(TEXTURE, x+2, y-30, 0, 230, 172, 26);
        if(cycle < 10) {
            gui.blit(TEXTURE, x + 9, y - 23, 172, 244, 12, 12);
            gui.blit(TEXTURE, x + 155, y - 23, 172, 244, 12, 12);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderTooltip(gui, mouseX, mouseY);

        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Power Level
        if(mouseX >= x+TOOLTIP_POWER_X && mouseX <= x+TOOLTIP_POWER_X+TOOLTIP_POWER_W &&
                mouseY >= y+TOOLTIP_POWER_Y && mouseY <= y+TOOLTIP_POWER_Y+TOOLTIP_POWER_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.powerlevel").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.ender.powerlevel.line1")));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Materia Import Rate
        if(mouseX >= x+ TOOLTIP_IMPORTRATE_X && mouseX <= x+ TOOLTIP_IMPORTRATE_X + TOOLTIP_IMPORTRATE_W &&
                mouseY >= y+ TOOLTIP_IMPORTRATE_Y && mouseY <= y+ TOOLTIP_IMPORTRATE_Y + TOOLTIP_IMPORTRATE_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.importrate").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.importrate.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.translatable("tooltip.magichem.gui.actuator.importexporttime"));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Materia Export Rate
        if(mouseX >= x+ TOOLTIP_EXPORTRATE_X && mouseX <= x+ TOOLTIP_EXPORTRATE_X + TOOLTIP_EXPORTRATE_W &&
                mouseY >= y+ TOOLTIP_EXPORTRATE_Y && mouseY <= y+ TOOLTIP_EXPORTRATE_Y + TOOLTIP_EXPORTRATE_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.exportrate").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.exportrate.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.translatable("tooltip.magichem.gui.actuator.importexporttime"));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Eldrin
        if(mouseX >= x+TOOLTIP_ELDRIN_X && mouseX <= x+TOOLTIP_ELDRIN_X+TOOLTIP_ELDRIN_W &&
                mouseY >= y+TOOLTIP_ELDRIN_Y && mouseY <= y+TOOLTIP_ELDRIN_Y+TOOLTIP_ELDRIN_H) {

            float singleDrawTime = ServerConfig.actuatorSingleSuppliedPeriod / 20.0f;
            float doubleDrawTime = ServerConfig.actuatorDoubleSuppliedPeriod / 20.0f;

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.cycleconsumption.ender").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.cycleconsumption.line1.ender")));
            tooltipContents.add((Component.empty()));
            tooltipContents.add((Component.empty())
                    .append(Component.translatable("tooltip.magichem.gui.actuator.cycleconsumption.line2a"))
                    .append(Component.literal(String.format("%.1f", singleDrawTime)).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.actuator.cycleconsumption.line2b"))
                    .append(Component.literal(String.format("%.1f", doubleDrawTime)).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.actuator.cycleconsumption.line2c")));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Essentia
        if(mouseX >= x+174 && mouseX <= x+177 &&
                mouseY >= y+10 && mouseY <= y+54) {

            int current = menu.blockEntity.getStoredMateria();
            int max = ServerConfig.actuatorMateriaBufferMaximum;
            float percent = (float)current / (float)max;

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.essentia.ender").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.essentia.line1.ender")));
            tooltipContents.add((Component.empty()));
            tooltipContents.add((Component.empty())
                    .append(Component.translatable("tooltip.magichem.gui.actuator.essentia.line2a"))
                    .append(Component.literal(ServerConfig.actuatorMateriaUnitsPerDram+"").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.actuator.essentia.line2b")));
            tooltipContents.add((Component.empty()));
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.essentia.line3").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(Math.min(max, current) + " / " + max).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal("  ")
                            .append(Component.literal("( ").withStyle(ChatFormatting.DARK_GRAY))
                            .append(Component.literal(String.format("%.1f", Math.min(1, percent) * 100)+"%")).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal(" )").withStyle(ChatFormatting.DARK_GRAY)));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Consumption rules
        if(mouseX >= x+211 && mouseX <= x+211+57 &&
                mouseY >= y && mouseY <= y+28) {

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.eldrin_mode").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable(menu.blockEntity.doEldrinPowerConsumption ? "tooltip.magichem.gui.eldrin_mode.both" : "tooltip.magichem.gui.eldrin_mode.single")));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;

        //Materia import rate
        if(!menu.blockEntity.getIsSatisfied() || menu.blockEntity.getPaused())
            gui.drawString(font, Component.literal("-"), 124, 17, 0xffaa0000, false);
        else if(menu.blockEntity.getPowerLevel() == 1)
            gui.drawString(font, Component.translatable("tooltip.magichem.gui.actuator.ender.speed.fast"), 124, 17, 0xff000000, false);
        else
            gui.drawString(font, Component.translatable("tooltip.magichem.gui.actuator.ender.speed.instant.import"), 124, 17, 0xff000000, false);

        //Materia export rate
        if(!menu.blockEntity.getIsSatisfied() || menu.blockEntity.getPaused())
            gui.drawString(font, Component.literal("-"), 124, 32, 0xffaa0000, false);
        else if(menu.blockEntity.getPowerLevel() <= 2)
            gui.drawString(font, Component.translatable("tooltip.magichem.gui.actuator.ender.speed.fast"), 124, 32, 0xff000000, false);
        else
            gui.drawString(font, Component.translatable("tooltip.magichem.gui.actuator.ender.speed.instant.export"), 124, 32, 0xff000000, false);

        //Eldrin power usage
        gui.drawString(font, Component.literal(""+ActuatorEnderBlockEntity.getEldrinPowerUsage(menu.blockEntity.getPowerLevel())), 124, 47, 0xff000000, false);
    }
}
