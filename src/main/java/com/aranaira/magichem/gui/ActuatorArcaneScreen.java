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
            TOOLTIP_POWER_X = 20, TOOLTIP_POWER_Y = 17, TOOLTIP_POWER_W = 12, TOOLTIP_POWER_H = 30,
            TOOLTIP_GRIME_X = 55, TOOLTIP_GRIME_Y = 14, TOOLTIP_GRIME_W = 6, TOOLTIP_GRIME_H = 35,
            TOOLTIP_SAND_X = 105, TOOLTIP_SAND_Y = 14, TOOLTIP_SAND_W = 6, TOOLTIP_SAND_H = 35,
            TOOLTIP_GRIMEREDUCTION_X = 114, TOOLTIP_GRIMEREDUCTION_Y = 10, TOOLTIP_GRIMEREDUCTION_W = 46, TOOLTIP_GRIMEREDUCTION_H = 11,
            TOOLTIP_SANDCONSUMPTION_X = 114, TOOLTIP_SANDCONSUMPTION_Y = 25, TOOLTIP_SANDCONSUMPTION_W = 46, TOOLTIP_SANDCONSUMPTION_H = 11,
            TOOLTIP_ELDRIN_X = 114, TOOLTIP_ELDRIN_Y = 40, TOOLTIP_ELDRIN_W = 46, TOOLTIP_ELDRIN_H = 11;
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

        //progress symbol
        int sH = getScaledEldrinTime();
        int sY = SYMBOL_H - sH;
        gui.blit(TEXTURE, x + SYMBOL_X, y + SYMBOL_Y + sY, SYMBOL_U, sY, SYMBOL_W, sH);

        //Grime Reduction Rate
        if(mouseX >= x+TOOLTIP_GRIMEREDUCTION_X && mouseX <= x+TOOLTIP_GRIMEREDUCTION_X+TOOLTIP_GRIMEREDUCTION_W &&
                mouseY >= y+TOOLTIP_GRIMEREDUCTION_Y && mouseY <= y+TOOLTIP_GRIMEREDUCTION_Y+TOOLTIP_GRIMEREDUCTION_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.grimereduction").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.grimereduction.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.literal(Config.quakeRefineryRarefiedRate+"% ").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.actuator.grimereduction.line2")));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Sand Consumption Rate
        if(mouseX >= x+TOOLTIP_SANDCONSUMPTION_X && mouseX <= x+TOOLTIP_SANDCONSUMPTION_X+TOOLTIP_SANDCONSUMPTION_W &&
                mouseY >= y+TOOLTIP_SANDCONSUMPTION_Y && mouseY <= y+TOOLTIP_SANDCONSUMPTION_Y+TOOLTIP_SANDCONSUMPTION_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.sandconsume").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.sandconsume.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.translatable("tooltip.magichem.gui.actuator.sandconsume.line2"));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Eldrin
        if(mouseX >= x+TOOLTIP_ELDRIN_X && mouseX <= x+TOOLTIP_ELDRIN_X+TOOLTIP_ELDRIN_W &&
                mouseY >= y+TOOLTIP_ELDRIN_Y && mouseY <= y+TOOLTIP_ELDRIN_Y+TOOLTIP_ELDRIN_H) {

            float drawTime = Config.quakeRefineryOperationTime / 20.0f;

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

        //Grime Reduction
//        if((menu.getFlags() & ActuatorArcaneBlockEntity.FLAG_IS_SATISFIED) == ActuatorArcaneBlockEntity.FLAG_IS_SATISFIED)
//            gui.drawString(font, Component.literal("-"+ActuatorArcaneBlockEntity.getGrimeReductionRate(menu.getPowerLevel())+"%"), 128, 17, 0xff000000, false);
//        else
//            gui.drawString(font, Component.literal("-"), 128, 17, 0xffaa0000, false);

        //Sand per Operation
//        gui.drawString(font, Component.literal(ActuatorArcaneBlockEntity.getSandPerOperation(menu.getPowerLevel())+"mB"), 128, 32, 0xff000000, false);

        //Eldrin power usage
        gui.drawString(font, Component.literal(""+ActuatorArcaneBlockEntity.getEldrinPowerUsage(menu.getPowerLevel())), 128, 47, 0xff000000, false);

        //Warning label
//        if(menu.getSlurryInTank() < ActuatorArcaneBlockEntity.getSandPerOperation(menu.getPowerLevel())) {
//            MutableComponent warningText = Component.translatable("gui.magichem.insufficientslurry");
//            int width = Minecraft.getInstance().font.width(warningText.getString());
//            gui.drawString(font, warningText, 89 - width / 2, -17, 0xff000000, false);
//        }
    }

    private int getScaledEldrinTime() {
        return menu.getRemainingEldrinTime() * SYMBOL_H / Config.quakeRefineryOperationTime;
    }
}
