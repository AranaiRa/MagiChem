package com.aranaira.magichem.gui;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ActuatorAirBlockEntity;
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

public class ActuatorAirScreen extends AbstractContainerScreen<ActuatorAirMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_actuator_air.png");
    private static final ResourceLocation TEXTURE_EXT =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fabrication_ext.png");
    private static final ResourceLocation TEXTURE_GAS =
            new ResourceLocation("minecraft", "textures/block/water_still.png");
    public static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 159,
            SYMBOL_X = 53, SYMBOL_Y = 21, SYMBOL_U = 184, SYMBOL_V = 0, SYMBOL_W = 15, SYMBOL_H = 21,
            POWER_X = 39, POWER_Y = 19, POWER_U = 176, POWER_V = 0, POWER_W = 8, POWER_H = 26,
            SMOKE_X = 73, STEAM_X = 85, GAS_Y = 15, GAS_W = 8,
            TOOLTIP_POWER_X = 37, TOOLTIP_POWER_Y = 17, TOOLTIP_POWER_W = 12, TOOLTIP_POWER_H = 30,
            TOOLTIP_SMOKE_X = 72, TOOLTIP_STEAM_X = 84, TOOLTIP_GAS_Y = 14, TOOLTIP_GAS_W = 10, TOOLTIP_GAS_H = 35,
            TOOLTIP_BATCH_X = 97, TOOLTIP_BATCH_Y = 5, TOOLTIP_BATCH_W = 46, TOOLTIP_BATCH_H = 11,
            TOOLTIP_OPTIME_X = 97, TOOLTIP_OPTIME_Y = 19, TOOLTIP_OPTIME_W = 46, TOOLTIP_OPTIME_H = 11,
            TOOLTIP_GASCONSUME_X = 97, TOOLTIP_GASCONSUME_Y = 33, TOOLTIP_GASCONSUME_W = 46, TOOLTIP_GASCONSUME_H = 11,
            TOOLTIP_ELDRIN_X = 97, TOOLTIP_ELDRIN_Y = 47, TOOLTIP_ELDRIN_W = 46, TOOLTIP_ELDRIN_H = 11;
    public static final int
            FLUID_GAUGE_H = 33;
    private ImageButton
        b_powerLevelUp, b_powerLevelDown;


    public ActuatorAirScreen(ActuatorAirMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        initializePowerLevelButtons();
    }

    private void initializePowerLevelButtons() {
        b_powerLevelUp = this.addRenderableWidget(new ImageButton(this.leftPos + 37, this.topPos + 12, 12, 7, 176, 26, TEXTURE, button -> {
            menu.incrementPowerLevel();

        }));
        b_powerLevelDown = this.addRenderableWidget(new ImageButton(this.leftPos + 37, this.topPos + 53, 12, 7, 188, 26, TEXTURE, button -> {
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
        int plH = 2 + (menu.getPowerLevel() - 1) * 12;
        int plY = POWER_H - plH;
        gui.blit(TEXTURE, x + POWER_X, y + POWER_Y + plY, POWER_U, plY, POWER_W, plH);

        //progress symbol
        int sH = getScaledEldrinTime();
        int sY = SYMBOL_H - sH;
        gui.blit(TEXTURE, x + SYMBOL_X, y + SYMBOL_Y + sY, SYMBOL_U, sY, SYMBOL_W, sH);

        //smoke gauge
        int smokeH = ActuatorAirBlockEntity.getScaledSmoke(menu.getSmokeInTank());
        gui.setColor(0.2f, 0.1875f, 0.1875f, 1.0f);
        gui.blit(TEXTURE_GAS, x + SMOKE_X, y + GAS_Y, 11, 0, GAS_W, smokeH, 16, 16);
        gui.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        //steam gauge
        int steamH = ActuatorAirBlockEntity.getScaledSteam(menu.getSteamInTank());
        gui.blit(TEXTURE_GAS, x + STEAM_X, y + GAS_Y, 11, 0, GAS_W, steamH, 16, 16);

        boolean sufficientSmoke = menu.getSmokeInTank() >= ActuatorAirBlockEntity.getGasPerProcess(menu.getPowerLevel());
        boolean sufficientSteam = menu.getSteamInTank() >= ActuatorAirBlockEntity.getGasPerProcess(menu.getPowerLevel());
        //Insufficient input warnings
        if(menu.getPowerLevel() == 2 && !(sufficientSmoke || sufficientSteam)) {
            renderPowerWarning(gui, x, y);
        } else if(menu.getPowerLevel() == 3 && !(sufficientSmoke && sufficientSteam)) {
            renderPowerWarning(gui, x, y);
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        renderBackground(gui);
        super.render(gui, mouseX, mouseY, delta);
        renderTooltip(gui, mouseX, mouseY);
    }

    protected void renderPowerWarning(GuiGraphics gui, int x, int y) {
        long cycle = Minecraft.getInstance().level.getGameTime() % 20;

        gui.blit(TEXTURE, x+3, y-30, 0, 230, 172, 26);
        if(cycle < 10) {
            gui.blit(TEXTURE, x + 10, y - 23, 172, 244, 12, 12);
            gui.blit(TEXTURE, x + 156, y - 23, 172, 244, 12, 12);
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
                    .append(Component.translatable("tooltip.magichem.gui.actuator.air.powerlevel.line1")));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Smoke Tank
        if(mouseX >= x+ TOOLTIP_SMOKE_X && mouseX <= x+ TOOLTIP_SMOKE_X + TOOLTIP_GAS_W &&
                mouseY >= y+ TOOLTIP_GAS_Y && mouseY <= y+ TOOLTIP_GAS_Y + TOOLTIP_GAS_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.air.tank1").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.air.tank1.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.air.tank1.line3").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(menu.getSmokeInTank() + " / " + Config.galePressurizerTankCapacity).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal("  ")
                            .append(Component.literal("( ").withStyle(ChatFormatting.DARK_GRAY))
                            .append(Component.literal(String.format("%.1f", ActuatorAirBlockEntity.getSmokePercent(menu.getSmokeInTank()))+"%")).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal(" )").withStyle(ChatFormatting.DARK_GRAY)));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Steam Tank
        if(mouseX >= x+ TOOLTIP_STEAM_X && mouseX <= x+ TOOLTIP_STEAM_X + TOOLTIP_GAS_W &&
                mouseY >= y+ TOOLTIP_GAS_Y && mouseY <= y+ TOOLTIP_GAS_Y + TOOLTIP_GAS_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.air.tank2").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.air.tank2.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.air.tank2.line3").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(menu.getSteamInTank() + " / " + Config.galePressurizerTankCapacity).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal("  ")
                            .append(Component.literal("( ").withStyle(ChatFormatting.DARK_GRAY))
                            .append(Component.literal(String.format("%.1f", ActuatorAirBlockEntity.getSteamPercent(menu.getSmokeInTank()))+"%")).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal(" )").withStyle(ChatFormatting.DARK_GRAY)));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Batch Size
        if(mouseX >= x+TOOLTIP_BATCH_X && mouseX <= x+TOOLTIP_BATCH_X+TOOLTIP_BATCH_W &&
                mouseY >= y+TOOLTIP_BATCH_Y && mouseY <= y+TOOLTIP_BATCH_Y+TOOLTIP_BATCH_H) {

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.batchsize").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.batchsize.line1")));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Operation Time
        if(mouseX >= x+TOOLTIP_OPTIME_X && mouseX <= x+TOOLTIP_OPTIME_X+TOOLTIP_OPTIME_W &&
                mouseY >= y+TOOLTIP_OPTIME_Y && mouseY <= y+TOOLTIP_OPTIME_Y+TOOLTIP_OPTIME_H) {

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.operationtime").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.operationtime")));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Gas consumption
        if(mouseX >= x+TOOLTIP_GASCONSUME_X && mouseX <= x+TOOLTIP_GASCONSUME_X+TOOLTIP_GASCONSUME_W &&
                mouseY >= y+TOOLTIP_GASCONSUME_Y && mouseY <= y+TOOLTIP_GASCONSUME_Y+TOOLTIP_GASCONSUME_H) {

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.gasconsume").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.gasconsume.line1")));
            tooltipContents.add((Component.empty()));
            tooltipContents.add((Component.empty())
                    .append(Component.translatable("tooltip.magichem.gui.actuator.gasconsume.line2.p"+menu.getPowerLevel()))
            );
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Eldrin
        if(mouseX >= x+TOOLTIP_ELDRIN_X && mouseX <= x+TOOLTIP_ELDRIN_X+TOOLTIP_ELDRIN_W &&
                mouseY >= y+TOOLTIP_ELDRIN_Y && mouseY <= y+TOOLTIP_ELDRIN_Y+TOOLTIP_ELDRIN_H) {

            float drawTime = Config.galePressurizerOperationTime / 20.0f;

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.eldrin.air").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.eldrin.line1")));
            tooltipContents.add((Component.empty()));
            tooltipContents.add((Component.empty())
                    .append(Component.translatable("tooltip.magichem.gui.actuator.eldrin.line2a"))
                    .append(Component.literal(String.format("%.1f", drawTime)).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.actuator.eldrin.line2b"))
            );
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;

        //Batch size
        if(((menu.getFlags() & ActuatorAirBlockEntity.FLAG_IS_SATISFIED) == ActuatorAirBlockEntity.FLAG_IS_SATISFIED) && ((menu.getFlags() & ActuatorAirBlockEntity.FLAG_GAS_SATISFACTION) == ActuatorAirBlockEntity.FLAG_GAS_SATISFACTION))
            gui.drawString(font, Component.literal("x" + (ActuatorAirBlockEntity.getRawBatchSize(menu.getPowerLevel()))), 112, 12, 0xff000000, false);
        else
            gui.drawString(font, Component.literal("1"), 112, 12, 0xffaa0000, false);

        //Operation time
        float penaltyRate = ActuatorAirBlockEntity.getPenaltyRate(menu.getPowerLevel());
        gui.drawString(font, Component.literal("x" + String.format("%.1f", penaltyRate)), 112, 26, 0xff000000, false);

        //Gas usage
        gui.drawString(font, Component.literal(ActuatorAirBlockEntity.getGasPerProcess(menu.getPowerLevel()) + "mB"), 112, 40, 0xff000000, false);

        //Eldrin power usage
        gui.drawString(font, Component.literal(""+ActuatorAirBlockEntity.getEldrinPowerUsage(menu.getPowerLevel())), 112, 54, 0xff000000, false);

        boolean sufficientSmoke = menu.getSmokeInTank() >= ActuatorAirBlockEntity.getGasPerProcess(menu.getPowerLevel());
        boolean sufficientSteam = menu.getSteamInTank() >= ActuatorAirBlockEntity.getGasPerProcess(menu.getPowerLevel());
        //power warning
        if((menu.getPowerLevel() == 2 && !(sufficientSmoke || sufficientSteam)) || (menu.getPowerLevel() == 3 && !(sufficientSmoke && sufficientSteam))) {
            MutableComponent warningText;
            if (menu.getPowerLevel() == 2) {
                warningText = Component.translatable("gui.magichem.insufficientsteamsmoke");
            } else if (menu.getPowerLevel() == 3) {
                if (sufficientSmoke && !sufficientSteam)
                    warningText = Component.translatable("gui.magichem.insufficientsteam");
                else if (!sufficientSmoke && sufficientSteam)
                    warningText = Component.translatable("gui.magichem.insufficientsmoke");
                else

                    warningText = Component.translatable("gui.magichem.insufficientsteamsmoke");
            } else {
                //error state that should never occur
                warningText = Component.literal("oh no");
            }
            int width = Minecraft.getInstance().font.width(warningText.getString());
            gui.drawString(font, warningText, 89 - width / 2, -17, 0xff000000, false);
        }
    }

    private int getScaledEldrinTime() {
        return menu.getRemainingEldrinTime() * SYMBOL_H / Config.galePressurizerOperationTime;
    }
}
