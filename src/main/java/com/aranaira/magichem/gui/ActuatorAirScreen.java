package com.aranaira.magichem.gui;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ActuatorAirBlockEntity;
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

public class ActuatorAirScreen extends AbstractContainerScreen<ActuatorAirMenu> {
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


    public ActuatorAirScreen(ActuatorAirMenu menu, Inventory inventory, Component component) {
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
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;

        //Efficiency increase
        if((menu.getFlags() & ActuatorAirBlockEntity.FLAG_IS_SATISFIED) == ActuatorAirBlockEntity.FLAG_IS_SATISFIED)
            gui.drawString(font, Component.literal("-"+ ActuatorAirBlockEntity.getReductionRate(menu.getPowerLevel(), menu.getFlags())+"%"), 114, 17, 0xff000000, false);
        else
            gui.drawString(font, Component.literal("-"), 114, 17, 0xffaa0000, false);

        //Smoke generation
        gui.drawString(font, Component.literal(ActuatorAirBlockEntity.getSmokePerProcess(menu.getPowerLevel()) + "mB"), 114, 32, 0xff000000, false);

        //Eldrin power usage
        gui.drawString(font, Component.literal(""+ActuatorAirBlockEntity.getEldrinPowerUsage(menu.getPowerLevel())), 114, 47, 0xff000000, false);
    }

    private int getScaledEldrinTime() {
        return menu.getRemainingEldrinTime() * SYMBOL_H / Config.infernoEngineOperationTime;
    }
}
