package com.aranaira.magichem.gui;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ActuatorFireBlockEntity;
import com.aranaira.magichem.block.entity.ActuatorWaterBlockEntity;
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

public class ActuatorFireScreen extends AbstractContainerScreen<ActuatorFireMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_actuator_fire.png");
    private static final ResourceLocation TEXTURE_SMOKE =
            new ResourceLocation("minecraft", "textures/block/water_still.png");
    public static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 159,
            SYMBOL_X = 50, SYMBOL_Y = 21, SYMBOL_U = 184, SYMBOL_V = 0, SYMBOL_W = 15, SYMBOL_H = 21,
            POWER_X = 36, POWER_Y = 19, POWER_U = 176, POWER_V = 0, POWER_W = 8, POWER_H = 26,
            FUEL_GAUGE_X = 69, FUEL_GAUGE_Y = 12, FUEL_GAUGE_U = 176, FUEL_GAUGE_V = 40, FUEL_GAUGE_W = 18, FUEL_GAUGE_H = 16,
            SMOKE_X = 92, SMOKE_Y = 15, SMOKE_W = 4,
            TOOLTIP_POWER_X = 32, TOOLTIP_POWER_Y = 17, TOOLTIP_POWER_W = 12, TOOLTIP_POWER_H = 30,
            TOOLTIP_FUEL_X = 66, TOOLTIP_FUEL_Y = 11, TOOLTIP_FUEL_W = 20, TOOLTIP_FUEL_H = 18,
            TOOLTIP_SMOKE_X = 91, TOOLTIP_SMOKE_Y = 14, TOOLTIP_SMOKE_W = 6, TOOLTIP_SMOKE_H = 35,
            TOOLTIP_REDUCTION_X = 100, TOOLTIP_REDUCTION_Y = 10, TOOLTIP_REDUCTION_W = 46, TOOLTIP_REDUCTION_H = 11,
            TOOLTIP_SMOKEGEN_X = 100, TOOLTIP_SMOKEGEN_Y = 25, TOOLTIP_SMOKEGEN_W = 46, TOOLTIP_SMOKEGEN_H = 11,
            TOOLTIP_ELDRIN_X = 100, TOOLTIP_ELDRIN_Y = 40, TOOLTIP_ELDRIN_W = 46, TOOLTIP_ELDRIN_H = 11;
    public static final int
            FLUID_GAUGE_H = 33;
    private ImageButton
        b_powerLevelUp, b_powerLevelDown;


    public ActuatorFireScreen(ActuatorFireMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        initializePowerLevelButtons();
    }

    private void initializePowerLevelButtons() {
        b_powerLevelUp = this.addRenderableWidget(new ImageButton(this.leftPos + 34, this.topPos + 12, 12, 7, 176, 26, TEXTURE, button -> {
            menu.incrementPowerLevel();

        }));
        b_powerLevelDown = this.addRenderableWidget(new ImageButton(this.leftPos + 34, this.topPos + 53, 12, 7, 188, 26, TEXTURE, button -> {
            menu.decrementPowerLevel();

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

        //power level
        int plH = menu.blockEntity.getPowerLevel() * 2;
        int plY = POWER_H - plH;
        gui.blit(TEXTURE, x + POWER_X, y + POWER_Y + plY, POWER_U, plY, POWER_W, plH);

        //progress symbol
        int sH = menu.blockEntity.getScaledCycleTime();
        int sY = SYMBOL_H - sH;
        gui.blit(TEXTURE, x + SYMBOL_X, y + SYMBOL_Y + sY, SYMBOL_U, sY, SYMBOL_W, sH);

        //fuel burn
        if(menu.blockEntity.getRemainingFuelTime() > 0) {
            int fH = ActuatorFireBlockEntity.getScaledFuel(menu.blockEntity.getRemainingFuelTime(), menu.blockEntity.getFuelDuration());
            int fY = FUEL_GAUGE_H - fH;
            gui.blit(TEXTURE, x + FUEL_GAUGE_X, y + FUEL_GAUGE_Y + fY, FUEL_GAUGE_U, FUEL_GAUGE_V + fY, FUEL_GAUGE_W, fH);
        }

        //smoke gauge
        int steamH = ActuatorFireBlockEntity.getScaledSmoke(menu.blockEntity.getSmokeInTank());
        gui.setColor(0.2f, 0.1875f, 0.1875f, 1.0f);
        gui.blit(TEXTURE_SMOKE, x + SMOKE_X, y + SMOKE_Y, 11, 0, SMOKE_W, steamH, 16, 16);
        gui.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        //Time icon if mode calls for it
        if(!ActuatorFireBlockEntity.getIsPowerReductionMode(menu.blockEntity.getFlags()))
            gui.blit(TEXTURE, x + 101, y + 11, 176, 56, 11, 11);

        //Essentia insertion
        gui.blit(TEXTURE, x + 153, y + 3, 0, 172, 40, 58);
        int sM = Math.min(42, menu.blockEntity.getStoredMateria() * 42 / Config.actuatorMateriaBufferMaximum);
        gui.blit(TEXTURE, x + 161, y + 11 + (42 - sM), 200, 0, 2, sM);
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

        //Power Level
        if(mouseX >= x+TOOLTIP_POWER_X && mouseX <= x+TOOLTIP_POWER_X+TOOLTIP_POWER_W &&
                mouseY >= y+TOOLTIP_POWER_Y && mouseY <= y+TOOLTIP_POWER_Y+TOOLTIP_POWER_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.powerlevel").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.fire.powerlevel.line1")));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Fuel Burn
        if(mouseX >= x+ TOOLTIP_FUEL_X && mouseX <= x+ TOOLTIP_FUEL_X + TOOLTIP_FUEL_W &&
                mouseY >= y+ TOOLTIP_FUEL_Y && mouseY <= y+ TOOLTIP_FUEL_Y + TOOLTIP_FUEL_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.fuelburn").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.fuelburn.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.translatable("tooltip.magichem.gui.actuator.fuelburn.line2"));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Smoke Tank
        if(mouseX >= x+ TOOLTIP_SMOKE_X && mouseX <= x+ TOOLTIP_SMOKE_X + TOOLTIP_SMOKE_W &&
                mouseY >= y+ TOOLTIP_SMOKE_Y && mouseY <= y+ TOOLTIP_SMOKE_Y + TOOLTIP_SMOKE_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.fire.tank").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.fire.tank.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.literal(ActuatorFireBlockEntity.getSmokePerProcess(menu.blockEntity.getPowerLevel()) + " mB ").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.actuator.fire.tank.line2")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.fire.tank.line3").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(menu.blockEntity.getSmokeInTank() + " / " + Config.infernoEngineTankCapacity).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal("  ")
                            .append(Component.literal("( ").withStyle(ChatFormatting.DARK_GRAY))
                            .append(Component.literal(String.format("%.1f", ActuatorFireBlockEntity.getSmokePercent(menu.blockEntity.getSmokeInTank()))+"%")).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal(" )").withStyle(ChatFormatting.DARK_GRAY)));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Power/Time Reduction
        if(mouseX >= x+ TOOLTIP_REDUCTION_X && mouseX <= x+ TOOLTIP_REDUCTION_X + TOOLTIP_REDUCTION_W &&
                mouseY >= y+ TOOLTIP_REDUCTION_Y && mouseY <= y+ TOOLTIP_REDUCTION_Y + TOOLTIP_REDUCTION_H) {

            boolean powerReductionMode = ActuatorFireBlockEntity.getIsPowerReductionMode(menu.blockEntity.getFlags());

            if(powerReductionMode) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.energyusage").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.actuator.energyusage")));
            }
            else {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.operationtime").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.actuator.operationtime")));
            }

            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Smoke Generation
        if(mouseX >= x+TOOLTIP_SMOKEGEN_X && mouseX <= x+TOOLTIP_SMOKEGEN_X+TOOLTIP_SMOKEGEN_W &&
                mouseY >= y+TOOLTIP_SMOKEGEN_Y && mouseY <= y+TOOLTIP_SMOKEGEN_Y+TOOLTIP_SMOKEGEN_H) {

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.smokegen").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.smokegen.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.translatable("tooltip.magichem.gui.actuator.smokegen.line2"));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Eldrin
        if(mouseX >= x+TOOLTIP_ELDRIN_X && mouseX <= x+TOOLTIP_ELDRIN_X+TOOLTIP_ELDRIN_W &&
                mouseY >= y+TOOLTIP_ELDRIN_Y && mouseY <= y+TOOLTIP_ELDRIN_Y+TOOLTIP_ELDRIN_H) {

            float singleDrawTime = Config.actuatorSingleSuppliedPeriod / 20.0f;
            float doubleDrawTime = Config.actuatorDoubleSuppliedPeriod / 20.0f;

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.cycleconsumption.fire").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.cycleconsumption.line1.fire")));
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
        if(mouseX >= x+160 && mouseX <= x+163 &&
                mouseY >= y+10 && mouseY <= y+54) {

            int current = menu.blockEntity.getStoredMateria();
            int max = Config.actuatorMateriaBufferMaximum;
            float percent = (float)current / (float)max;

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.essentia.fire").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.essentia.line1.fire")));
            tooltipContents.add((Component.empty()));
            tooltipContents.add((Component.empty())
                    .append(Component.translatable("tooltip.magichem.gui.actuator.essentia.line2a"))
                    .append(Component.literal(Config.actuatorMateriaUnitsPerDram+"").withStyle(ChatFormatting.DARK_AQUA))
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
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;

        //Efficiency increase
        if(menu.blockEntity.getIsSatisfied())
            gui.drawString(font, Component.literal("-"+ ActuatorFireBlockEntity.getReductionRate(menu.blockEntity.getPowerLevel(), menu.blockEntity.getFlags())+"%"), 114, 17, 0xff000000, false);
        else
            gui.drawString(font, Component.literal("-"), 114, 17, 0xffaa0000, false);

        //Smoke generation
        gui.drawString(font, Component.literal(ActuatorFireBlockEntity.getSmokePerProcess(menu.blockEntity.getPowerLevel()) + "mB"), 114, 32, 0xff000000, false);

        //Eldrin power usage
        gui.drawString(font, Component.literal(""+ActuatorFireBlockEntity.getEldrinPowerUsage(menu.blockEntity.getPowerLevel())), 114, 47, 0xff000000, false);
    }
}
