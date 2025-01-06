package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.networking.StandingRetortSyncDataC2SPacket;
import com.aranaira.magichem.registry.PacketRegistry;
import com.mna.gui.GuiTextures;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class StandingRetortScreen extends AbstractContainerScreen<StandingRetortMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_standing_retort.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 159;


    public StandingRetortScreen(StandingRetortMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        initializeAffinityButtons();
    }

    private void initializeAffinityButtons() {
        //0: Ender
        this.addRenderableWidget(new ImageButton(this.leftPos + 46, this.topPos + 31, 11, 13, 0, 230, TEXTURE, button -> {
            PacketRegistry.sendToServer(new StandingRetortSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    0
            ));
        }));

        //1: Earth
        this.addRenderableWidget(new ImageButton(this.leftPos + 56, this.topPos + 48, 11, 13, 55, 230, TEXTURE, button -> {
            PacketRegistry.sendToServer(new StandingRetortSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    1
            ));
        }));

        //2: Water
        this.addRenderableWidget(new ImageButton(this.leftPos + 73, this.topPos + 48, 11, 13, 44, 230, TEXTURE, button -> {
            PacketRegistry.sendToServer(new StandingRetortSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    2
            ));
        }));

        //3: Air
        this.addRenderableWidget(new ImageButton(this.leftPos + 56, this.topPos + 13, 11, 13, 11, 230, TEXTURE, button -> {
            PacketRegistry.sendToServer(new StandingRetortSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    3
            ));
        }));

        //4: Fire
        this.addRenderableWidget(new ImageButton(this.leftPos + 73, this.topPos + 13, 11, 13, 22, 230, TEXTURE, button -> {
            PacketRegistry.sendToServer(new StandingRetortSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    4
            ));
        }));

        //5: Arcane
        this.addRenderableWidget(new ImageButton(this.leftPos + 83, this.topPos + 31, 11, 13, 33, 230, TEXTURE, button -> {
            PacketRegistry.sendToServer(new StandingRetortSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    5
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

        ItemStack affStack = (ItemStack) GuiTextures.affinityIcons.get(menu.blockEntity.getAffinityFromElement());
        if (!affStack.isEmpty()) {
            gui.renderItem(affStack, this.leftPos + 62, this.topPos + 29);
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

        gui.blit(TEXTURE, x+10, y-30, 0, 230, 156, 26);
        if(cycle < 10) {
            gui.blit(TEXTURE, x + 17, y - 23, 156, 244, 12, 12);
            gui.blit(TEXTURE, x + 147, y - 23, 156, 244, 12, 12);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {

    }

    @Override
    protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderTooltip(gui, mouseX, mouseY);

        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Power Level
//        if(mouseX >= x+TOOLTIP_POWER_X && mouseX <= x+TOOLTIP_POWER_X+TOOLTIP_POWER_W &&
//                mouseY >= y+TOOLTIP_POWER_Y && mouseY <= y+TOOLTIP_POWER_Y+TOOLTIP_POWER_H) {
//
//            tooltipContents.add(Component.empty()
//                    .append(Component.translatable("tooltip.magichem.gui.actuator.powerlevel").withStyle(ChatFormatting.GOLD))
//                    .append(": ")
//                    .append(Component.translatable("tooltip.magichem.gui.actuator.arcane.powerlevel.line1")));
//            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
//        }
//
//        //Slurry tank
//        if(mouseX >= x+TOOLTIP_SLURRY_X && mouseX <= x+TOOLTIP_SLURRY_X+TOOLTIP_SLURRY_W &&
//                mouseY >= y+TOOLTIP_SLURRY_Y && mouseY <= y+TOOLTIP_SLURRY_Y+TOOLTIP_SLURRY_H) {
//
//            tooltipContents.add(Component.empty()
//                    .append(Component.translatable("tooltip.magichem.gui.actuator.arcane.tank").withStyle(ChatFormatting.GOLD))
//                    .append(": ")
//                    .append(Component.translatable("tooltip.magichem.gui.actuator.arcane.tank.line1")));
//            tooltipContents.add(Component.empty());
//            tooltipContents.add(Component.empty()
//                    .append(Component.translatable("tooltip.magichem.gui.actuator.arcane.tank.line2").withStyle(ChatFormatting.DARK_GRAY))
//                    .append(Component.literal(menu.blockEntity.getSlurryInTank() + " / " + Config.occultMatrixTankCapacity).withStyle(ChatFormatting.DARK_AQUA))
//                    .append(Component.literal("  ")
//                            .append(Component.literal("( ").withStyle(ChatFormatting.DARK_GRAY))
//                            .append(Component.literal(String.format("%.1f", ActuatorArcaneBlockEntity.getSlurryPercent(menu.blockEntity.getSlurryInTank()))+"%")).withStyle(ChatFormatting.DARK_AQUA))
//                    .append(Component.literal(" )").withStyle(ChatFormatting.DARK_GRAY)));
//            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
//        }
//
//        if(mouseX >= x+TOOLTIP_EXPERIENCE_X && mouseX <= x+TOOLTIP_EXPERIENCE_X+TOOLTIP_EXPERIENCE_W &&
//                mouseY >= y+TOOLTIP_EXPERIENCE_Y && mouseY <= y+TOOLTIP_EXPERIENCE_Y+TOOLTIP_EXPERIENCE_H) {
//
//            //Reduction Mode
//            if(menu.blockEntity.getIsReductionMode()) {
//                tooltipContents.add(Component.empty()
//                        .append(Component.translatable("tooltip.magichem.gui.actuator.slurryreduction").withStyle(ChatFormatting.GOLD))
//                        .append(": ")
//                        .append(Component.translatable("tooltip.magichem.gui.actuator.slurryreduction.line1")));
//            } else {
//                tooltipContents.add(Component.empty()
//                        .append(Component.translatable("tooltip.magichem.gui.actuator.slurrygeneration").withStyle(ChatFormatting.GOLD))
//                        .append(": ")
//                        .append(Component.translatable("tooltip.magichem.gui.actuator.slurrygeneration.line1")));
//            }
//            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
//        }
//
//        //Eldrin
//        if(mouseX >= x+TOOLTIP_ELDRIN_X && mouseX <= x+TOOLTIP_ELDRIN_X+TOOLTIP_ELDRIN_W &&
//                mouseY >= y+TOOLTIP_ELDRIN_Y && mouseY <= y+TOOLTIP_ELDRIN_Y+TOOLTIP_ELDRIN_H) {
//
//            float singleDrawTime = Config.actuatorSingleSuppliedPeriod / 20.0f;
//            float doubleDrawTime = Config.actuatorDoubleSuppliedPeriod / 20.0f;
//
//            tooltipContents.clear();
//            tooltipContents.add(Component.empty()
//                    .append(Component.translatable("tooltip.magichem.gui.actuator.cycleconsumption.arcane").withStyle(ChatFormatting.GOLD))
//                    .append(": ")
//                    .append(Component.translatable("tooltip.magichem.gui.actuator.cycleconsumption.line1.arcane")));
//            tooltipContents.add((Component.empty()));
//            tooltipContents.add((Component.empty())
//                    .append(Component.translatable("tooltip.magichem.gui.actuator.cycleconsumption.line2a"))
//                    .append(Component.literal(String.format("%.1f", singleDrawTime)).withStyle(ChatFormatting.DARK_AQUA))
//                    .append(Component.translatable("tooltip.magichem.gui.actuator.cycleconsumption.line2b"))
//                    .append(Component.literal(String.format("%.1f", doubleDrawTime)).withStyle(ChatFormatting.DARK_AQUA))
//                    .append(Component.translatable("tooltip.magichem.gui.actuator.cycleconsumption.line2c")));
//            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
//        }
//
//        //Essentia
//        if(mouseX >= x+165 && mouseX <= x+168 &&
//                mouseY >= y+10 && mouseY <= y+54) {
//
//            int current = menu.blockEntity.getStoredMateria();
//            int max = Config.actuatorMateriaBufferMaximum;
//            float percent = (float)current / (float)max;
//
//            tooltipContents.clear();
//            tooltipContents.add(Component.empty()
//                    .append(Component.translatable("tooltip.magichem.gui.actuator.essentia.arcane").withStyle(ChatFormatting.GOLD))
//                    .append(": ")
//                    .append(Component.translatable("tooltip.magichem.gui.actuator.essentia.line1.arcane")));
//            tooltipContents.add((Component.empty()));
//            tooltipContents.add((Component.empty())
//                    .append(Component.translatable("tooltip.magichem.gui.actuator.essentia.line2a"))
//                    .append(Component.literal(Config.actuatorMateriaUnitsPerDram+"").withStyle(ChatFormatting.DARK_AQUA))
//                    .append(Component.translatable("tooltip.magichem.gui.actuator.essentia.line2b")));
//            tooltipContents.add((Component.empty()));
//            tooltipContents.add(Component.empty()
//                    .append(Component.translatable("tooltip.magichem.gui.actuator.essentia.line3").withStyle(ChatFormatting.DARK_GRAY))
//                    .append(Component.literal(Math.min(max, current) + " / " + max).withStyle(ChatFormatting.DARK_AQUA))
//                    .append(Component.literal("  ")
//                            .append(Component.literal("( ").withStyle(ChatFormatting.DARK_GRAY))
//                            .append(Component.literal(String.format("%.1f", Math.min(1, percent) * 100)+"%")).withStyle(ChatFormatting.DARK_AQUA))
//                    .append(Component.literal(" )").withStyle(ChatFormatting.DARK_GRAY)));
//            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
//        }
    }
}
