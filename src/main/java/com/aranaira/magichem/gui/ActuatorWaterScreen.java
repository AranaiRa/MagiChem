package com.aranaira.magichem.gui;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.ActuatorWaterBlock;
import com.aranaira.magichem.block.entity.ActuatorWaterBlockEntity;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
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

public class ActuatorWaterScreen extends AbstractContainerScreen<ActuatorWaterMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_actuator_water.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 159,
            SYMBOL_X = 57, SYMBOL_Y = 21, SYMBOL_U = 184, SYMBOL_V = 0, SYMBOL_W = 15, SYMBOL_H = 21,
            POWER_X = 43, POWER_Y = 19, POWER_U = 176, POWER_V = 0, POWER_W = 8, POWER_H = 26,
            TOOLTIP_POWER_X = 41, TOOLTIP_POWER_Y = 17, TOOLTIP_POWER_W = 12, TOOLTIP_POWER_H = 30,
            TOOLTIP_WATER_X = 76, TOOLTIP_WATER_Y = 14, TOOLTIP_WATER_W = 10, TOOLTIP_WATER_H = 35,
            TOOLTIP_STEAM_X = 88, TOOLTIP_STEAM_Y = 14, TOOLTIP_STEAM_W = 6, TOOLTIP_STEAM_H = 35,
            TOOLTIP_EFFICIENCY_X = 98, TOOLTIP_EFFICIENCY_Y = 17, TOOLTIP_EFFICIENCY_W = 38, TOOLTIP_EFFICIENCY_H = 11,
            TOOLTIP_ELDRIN_X = 98, TOOLTIP_ELDRIN_Y = 36, TOOLTIP_ELDRIN_W = 38, TOOLTIP_ELDRIN_H = 11;
    private ImageButton
        b_powerLevelUp, b_powerLevelDown;


    public ActuatorWaterScreen(ActuatorWaterMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        initializePowerLevelButtons();
    }

    private void initializePowerLevelButtons() {
        b_powerLevelUp = this.addRenderableWidget(new ImageButton(this.leftPos + 41, this.topPos + 12, 12, 7, 176, 26, TEXTURE, button -> {
            menu.incrementPowerLevel();

        }));
        b_powerLevelDown = this.addRenderableWidget(new ImageButton(this.leftPos + 41, this.topPos + 53, 12, 7, 188, 26, TEXTURE, button -> {
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
                    .append(Component.translatable("tooltip.magichem.gui.actuator.water.powerlevel.line1")));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Water Tank
        if(mouseX >= x+TOOLTIP_WATER_X && mouseX <= x+TOOLTIP_WATER_X+TOOLTIP_WATER_W &&
                mouseY >= y+TOOLTIP_WATER_Y && mouseY <= y+TOOLTIP_WATER_Y+TOOLTIP_WATER_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.water.tank1").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.water.tank1.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.literal(ActuatorWaterBlockEntity.getWaterPerOperation(menu.getPowerLevel()) + " mB ").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.actuator.water.tank1.line2")));
            tooltipContents.add(Component.empty());
            int fakeFluidLevel = 0;
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.water.tank1.line3").withStyle(ChatFormatting.DARK_GRAY))
                    //TODO: replace with actual tank contents
                    .append(Component.literal(fakeFluidLevel + " / " + Config.delugePurifierTankCapacity).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal("  ")
                    .append(Component.literal("( ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(String.format("%.1f", (float)fakeFluidLevel / Config.delugePurifierTankCapacity)+"%")).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal(" )").withStyle(ChatFormatting.DARK_GRAY)));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Steam Tank
        if(mouseX >= x+TOOLTIP_STEAM_X && mouseX <= x+TOOLTIP_STEAM_X+TOOLTIP_STEAM_W &&
                mouseY >= y+TOOLTIP_STEAM_Y && mouseY <= y+TOOLTIP_STEAM_Y+TOOLTIP_STEAM_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.water.tank2").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.water.tank2.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.literal(ActuatorWaterBlockEntity.getSteamPerProcess(menu.getPowerLevel()) + " mB ").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.actuator.water.tank2.line2")));
            tooltipContents.add(Component.empty());
            int fakeFluidLevel = 0;
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.actuator.water.tank2.line3").withStyle(ChatFormatting.DARK_GRAY))
                    //TODO: replace with actual tank contents
                    .append(Component.literal(fakeFluidLevel + " / " + Config.delugePurifierTankCapacity).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal("  ")
                            .append(Component.literal("( ").withStyle(ChatFormatting.DARK_GRAY))
                            .append(Component.literal(String.format("%.1f", 0f / Config.delugePurifierTankCapacity)+"%")).withStyle(ChatFormatting.DARK_AQUA))
                            .append(Component.literal(" )").withStyle(ChatFormatting.DARK_GRAY)));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Efficiency
        if(mouseX >= x+TOOLTIP_EFFICIENCY_X && mouseX <= x+TOOLTIP_EFFICIENCY_X+TOOLTIP_EFFICIENCY_W &&
                mouseY >= y+TOOLTIP_EFFICIENCY_Y && mouseY <= y+TOOLTIP_EFFICIENCY_Y+TOOLTIP_EFFICIENCY_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.efficiency").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.efficiency")));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Eldrin
        if(mouseX >= x+TOOLTIP_ELDRIN_X && mouseX <= x+TOOLTIP_ELDRIN_X+TOOLTIP_ELDRIN_W &&
                mouseY >= y+TOOLTIP_ELDRIN_Y && mouseY <= y+TOOLTIP_ELDRIN_Y+TOOLTIP_ELDRIN_H) {

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.eldrin.water").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.actuator.eldrin")));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;

        //Efficiency increase
        if((menu.getFlags() & ActuatorWaterBlockEntity.FLAG_IS_SATISFIED) == ActuatorWaterBlockEntity.FLAG_IS_SATISFIED)
            gui.drawString(font, Component.literal("+"+ActuatorWaterBlockEntity.getEfficiencyIncrease(menu.getPowerLevel())+"%"), 112, 23, 0xff000000, false);
        else
            gui.drawString(font, Component.literal("   0%"), 112, 23, 0xffaa0000, false);

        //Eldrin power usage
        gui.drawString(font, Component.literal(""+ActuatorWaterBlockEntity.getEldrinPowerUsage(menu.getPowerLevel())), 112, 42, 0xff000000, false);
    }

    private int getScaledEldrinTime() {
        return menu.getRemainingEldrinTime() * SYMBOL_H / Config.delugePurifierOperationTime;
    }
}
