package com.aranaira.magichem.gui;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ActuatorArcaneBlockEntity;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ActuatorArcaneScreen extends AbstractContainerScreen<ActuatorArcaneMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_actuator_arcane.png");
    private static final ResourceLocation TEXTURE_SLURRY =
            new ResourceLocation(MagiChemMod.MODID, "textures/block/fluid/experience_still.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 159,
            SYMBOL_X = 47, SYMBOL_Y = 21, SYMBOL_U = 184, SYMBOL_V = 0, SYMBOL_W = 15, SYMBOL_H = 21,
            POWER_X = 33, POWER_Y = 19, POWER_U = 176, POWER_V = 0, POWER_W = 8, POWER_H = 26,
            GRIME_X = 56, GRIME_Y = 15, GRIME_U = 176, GRIME_V = 40, GRIME_W = 4,
            RAREFIED_GRIME_X = 56, RAREFIED_GRIME_Y = 15, RAREFIED_GRIME_U = 180, RAREFIED_GRIME_V = 40, RAREFIED_GRIME_W = 1,
            SAND_X = 106, SAND_Y = 15, SAND_W = 4,
            TOOLTIP_POWER_X = 31, TOOLTIP_POWER_Y = 17, TOOLTIP_POWER_W = 12, TOOLTIP_POWER_H = 30,
            TOOLTIP_SLURRY_X = 65, TOOLTIP_SLURRY_Y = 13, TOOLTIP_SLURRY_W = 12, TOOLTIP_SLURRY_H = 37,
            TOOLTIP_INPUT_X = 82, TOOLTIP_INPUT_Y = 10, TOOLTIP_INPUT_S = 20,
            TOOLTIP_OUTPUT_X = 82, TOOLTIP_OUTPUT_Y = 33, TOOLTIP_OUTPUT_S = 20,
            TOOLTIP_EXPERIENCE_X = 104, TOOLTIP_EXPERIENCE_Y = 14, TOOLTIP_EXPERIENCE_W = 46, TOOLTIP_EXPERIENCE_H = 13,
            TOOLTIP_ELDRIN_X = 104, TOOLTIP_ELDRIN_Y = 36, TOOLTIP_ELDRIN_W = 46, TOOLTIP_ELDRIN_H = 13;
    public static final int
            FLUID_GAUGE_H = 33;
    private ImageButton
        b_powerLevelUp, b_powerLevelDown;


    public ActuatorArcaneScreen(ActuatorArcaneMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        initializePowerLevelButtons();
    }

    private void initializePowerLevelButtons() {
        b_powerLevelUp = this.addRenderableWidget(new ImageButton(this.leftPos + 31, this.topPos + 12, 12, 7, 176, 26, TEXTURE, button -> {
            menu.incrementPowerLevel();

        }));
        b_powerLevelDown = this.addRenderableWidget(new ImageButton(this.leftPos + 31, this.topPos + 53, 12, 7, 188, 26, TEXTURE, button -> {
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
        int plH = menu.getPowerLevel() * 2;
        int plY = POWER_H - plH;
        gui.blit(TEXTURE, x + POWER_X, y + POWER_Y + plY, POWER_U, plY, POWER_W, plH);

        //progress symbol
        int sH = getScaledEldrinTime();
        int sY = SYMBOL_H - sH;
        gui.blit(TEXTURE, x + SYMBOL_X, y + SYMBOL_Y + sY, SYMBOL_U, sY, SYMBOL_W, sH);

        //change icon for generator mode
        if(menu.getIsReductionMode()) {
            gui.blit(TEXTURE, x + 105, y + 15, 176, 40, 11, 11);
        }

        //water gauge
        int slurryH = ActuatorArcaneBlockEntity.getScaledSlurry(menu.getSlurryInTank());
        gui.blit(TEXTURE_SLURRY, x + 67, y + 15 + FLUID_GAUGE_H - slurryH, 0, 0, 8, slurryH, 16, 16);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        renderBackground(gui);
        super.render(gui, mouseX, mouseY, delta);
        renderTooltip(gui, mouseX, mouseY);
    }

    protected void renderPowerWarning(GuiGraphics gui, int x, int y) {
        long cycle = Minecraft.getInstance().level.getGameTime() % 20;

        gui.blit(TEXTURE, x+10, y-30, 0, 230, 156, 26);
        if(cycle < 10) {
            gui.blit(TEXTURE, x + 17, y - 23, 156, 244, 12, 12);
            gui.blit(TEXTURE, x + 147, y - 23, 156, 244, 12, 12);
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
                    .append(Component.translatable("tooltip.magichem.gui.actuator.arcane.powerlevel.line1")));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Slurry tank
        if(mouseX >= x+TOOLTIP_SLURRY_X && mouseX <= x+TOOLTIP_SLURRY_X+TOOLTIP_SLURRY_W &&
                mouseY >= y+TOOLTIP_SLURRY_Y && mouseY <= y+TOOLTIP_SLURRY_Y+TOOLTIP_SLURRY_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.arcane.tank").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.arcane.tank.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.arcane.tank.line2").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(menu.getSlurryInTank() + " / " + Config.occultMatrixTankCapacity).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal("  ")
                            .append(Component.literal("( ").withStyle(ChatFormatting.DARK_GRAY))
                            .append(Component.literal(String.format("%.1f", ActuatorArcaneBlockEntity.getSlurryPercent(menu.getSlurryInTank()))+"%")).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal(" )").withStyle(ChatFormatting.DARK_GRAY)));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        if(mouseX >= x+TOOLTIP_EXPERIENCE_X && mouseX <= x+TOOLTIP_EXPERIENCE_X+TOOLTIP_EXPERIENCE_W &&
                mouseY >= y+TOOLTIP_EXPERIENCE_Y && mouseY <= y+TOOLTIP_EXPERIENCE_Y+TOOLTIP_EXPERIENCE_H) {

            //Reduction Mode
            if(menu.getIsReductionMode()) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.actuator.slurryreduction").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.actuator.slurryreduction.line1")));
            } else {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.actuator.slurrygeneration").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.actuator.slurrygeneration.line1")));
            }
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Eldrin
        if(mouseX >= x+TOOLTIP_ELDRIN_X && mouseX <= x+TOOLTIP_ELDRIN_X+TOOLTIP_ELDRIN_W &&
                mouseY >= y+TOOLTIP_ELDRIN_Y && mouseY <= y+TOOLTIP_ELDRIN_Y+TOOLTIP_ELDRIN_H) {

            float drawTime = Config.actuatorSingleSuppliedPeriod / 20.0f;

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.eldrin.arcane").withStyle(ChatFormatting.GOLD))
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

        if(menu.getIsReductionMode()) {
            //Reduction rate
            gui.drawString(font, Component.literal("-" + ActuatorArcaneBlockEntity.getSlurryReductionRate(menu.getPowerLevel())+"%"), 120, 21, 0xff000000, false);
        } else {
            //Generation rate
            gui.drawString(font, Component.literal("" + ActuatorArcaneBlockEntity.getSlurryGeneratedPerOperation(menu.getPowerLevel())+" mB"), 120, 21, 0xff000000, false);
        }

        //Eldrin power usage
        gui.drawString(font, Component.literal(""+ActuatorArcaneBlockEntity.getEldrinPowerUsage(menu.getPowerLevel())), 120, 43, 0xff000000, false);
    }

    private int getScaledEldrinTime() {
        return menu.getRemainingEldrinTime() * SYMBOL_H / Config.actuatorSingleSuppliedPeriod;
    }
}
