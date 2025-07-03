package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.EldrinOrreryBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.mna.ManaAndArtifice;
import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IWellspringNodeRegistry;
import com.mna.capabilities.worlddata.WorldMagicProvider;
import com.mna.items.ItemInit;
import com.mna.network.ClientMessageDispatcher;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.aranaira.magichem.block.entity.EldrinOrreryBlockEntity.*;

public class EldrinOrreryScreen extends AbstractContainerScreen<EldrinOrreryMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_orrery.png");
    private static final int
            PANEL_MAIN_W = 212, PANEL_MAIN_H = 207;
    private static final ItemStack
            MOTE_ENDER = new ItemStack(ItemInit.GREATER_MOTE_ENDER.get()),
            MOTE_EARTH = new ItemStack(ItemInit.GREATER_MOTE_EARTH.get()),
            MOTE_WATER = new ItemStack(ItemInit.GREATER_MOTE_WATER.get()),
            MOTE_AIR = new ItemStack(ItemInit.GREATER_MOTE_AIR.get()),
            MOTE_FIRE = new ItemStack(ItemInit.GREATER_MOTE_FIRE.get()),
            MOTE_ARCANE = new ItemStack(ItemInit.GREATER_MOTE_ARCANE.get());

    public EldrinOrreryScreen(EldrinOrreryMenu menu, Inventory inventory, Component component) {
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

        gui.renderItem(MOTE_AIR, x+95, y+10);
        gui.renderItem(MOTE_ENDER, x+95, y+45);
        gui.renderItem(MOTE_EARTH, x+95, y+80);
        gui.renderItem(MOTE_FIRE, x+119, y+10);
        gui.renderItem(MOTE_ARCANE, x+119, y+45);
        gui.renderItem(MOTE_WATER, x+119, y+80);

        menu.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent((cap) -> {
            if(cap.getStackInSlot(SLOT_SOLAR_INPUT).isEmpty())
                gui.blit(TEXTURE, x+7, y+62, 238, 0, 18, 18);
            if(cap.getStackInSlot(SLOT_LUNAR_INPUT).isEmpty())
                gui.blit(TEXTURE, x+25, y+62, 238, 18, 18, 18);
            if(cap.getStackInSlot(SLOT_SIDEREAL_INPUT).isEmpty())
                gui.blit(TEXTURE, x+43, y+62, 238, 36, 18, 18);

            if(cap.getStackInSlot(SLOT_FIRMAMENT_INPUT).isEmpty())
                gui.blit(TEXTURE, x+169, y+62, 238, 54, 18, 18);
            if(cap.getStackInSlot(SLOT_REALM_INPUT).isEmpty())
                gui.blit(TEXTURE, x+187, y+62, 238, 54, 18, 18);
        });

        this.minecraft.level.getCapability(WorldMagicProvider.MAGIC).ifPresent((m) -> {
            if (ManaAndArtifice.instance.proxy.getGameTicks() % 20L == 0L) {
                ClientMessageDispatcher.sendRequestWellspringNetworkSyncMessage(false);
            }

            IWellspringNodeRegistry wsRegistry = m.getWellspringRegistry();
            HashMap<Affinity, Float> nodeAmounts = wsRegistry.getNodeNetworkAmountFor(this.minecraft.player);

//            gui.setColor(Affinity.WIND.getColor()[0]/255f, Affinity.WIND.getColor()[1]/255f, Affinity.WIND.getColor()[2]/255f, 1f);
            int air = Math.round(nodeAmounts.get(Affinity.WIND) / 1000f * 44);
            gui.blit(TEXTURE, x+111-air, y+32, 212, 254, air, 2);

            gui.setColor(Affinity.ENDER.getColor()[0]/255f, Affinity.ENDER.getColor()[1]/255f, Affinity.ENDER.getColor()[2]/255f, 1f);
            int ender = Math.round(nodeAmounts.get(Affinity.ENDER) /1000f * 44);
            gui.blit(TEXTURE, x+111-ender, y+67, 212, 254, ender, 2);

            gui.setColor(Affinity.EARTH.getColor()[0]/255f, Affinity.EARTH.getColor()[1]/255f, Affinity.EARTH.getColor()[2]/255f, 1f);
            int earth = Math.round(nodeAmounts.get(Affinity.EARTH) / 1000f * 44);
            gui.blit(TEXTURE, x+111-earth, y+102, 212, 254, earth, 2);

            gui.setColor(Affinity.FIRE.getColor()[0]/255f, Affinity.FIRE.getColor()[1]/255f, Affinity.FIRE.getColor()[2]/255f, 1f);
            int fire = Math.round(nodeAmounts.get(Affinity.FIRE) / 1000f * 44);
            gui.blit(TEXTURE, x+119, y+32, 212, 252, fire, 2);

            gui.setColor(168/255f, 94/255f, 214/255f, 1f);
            int arcane = Math.round(nodeAmounts.get(Affinity.ARCANE) / 1000f * 44);
            gui.blit(TEXTURE, x+119, y+67, 212, 252, arcane, 2);

            gui.setColor(Affinity.WATER.getColor()[0]/255f, Affinity.WATER.getColor()[1]/255f, Affinity.WATER.getColor()[2]/255f, 1f);
            int water = Math.round(nodeAmounts.get(Affinity.WATER) / 1000f * 44);
            gui.blit(TEXTURE, x+119, y+102, 212, 252, water, 2);

            gui.setColor(1f,1,1,1);
        });

        int barFill;
        gui.setColor(0.933f,0.714f,0.052f,1);
        barFill = Math.round(menu.blockEntity.getSolarFillPercent() * 45);
        gui.blit(TEXTURE, x+15, y+9 + 45-barFill, 254, 207, 2, barFill);

        gui.setColor(0.851f,0.918f,0.933f,1);
        barFill = Math.round(menu.blockEntity.getLunarFillPercent() * 45);
        gui.blit(TEXTURE, x+33, y+9 + 45-barFill, 254, 207, 2, barFill);

        gui.setColor(0.282f,0.435f,0.933f,1);
        barFill = Math.round(menu.blockEntity.getSiderealFillPercent() * 45);
        gui.blit(TEXTURE, x+51, y+9 + 45-barFill, 254, 207, 2, barFill);

        gui.setColor(0.294f,0.356f,0.949f,1);
        barFill = Math.round(menu.blockEntity.getFirmamentFillPercent() * 45);
        gui.blit(TEXTURE, x+177, y+9 + 45-barFill, 254, 207, 2, barFill);

        gui.setColor(0.220f,0.447f,0.576f,1);
        barFill = Math.round(menu.blockEntity.getRealmFillPercent() * 45);
        gui.blit(TEXTURE, x+195, y+9 + 45-barFill, 254, 207, 2, barFill);

        gui.setColor(1,1,1,1);
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
    protected void renderLabels(GuiGraphics gui, int pMouseX, int pMouseY) {
        Font font = Minecraft.getInstance().font;
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        this.minecraft.level.getCapability(WorldMagicProvider.MAGIC).ifPresent((m) -> {
            IWellspringNodeRegistry wsRegistry = m.getWellspringRegistry();
            HashMap<Affinity, Float> nodeAmounts = wsRegistry.getNodeNetworkAmountFor(this.minecraft.player);

            float air = wsRegistry.getEldrinGenerationMultiplierFor(this.minecraft.player, Affinity.WIND) * menu.blockEntity.getXMultRate();
            gui.drawString(font, "x"+(air >= 10 ? Math.round(air) : String.format("%.1f", air)), 49, -11, 0x000000, false);
            gui.drawString(font, nodeAmounts.get(Affinity.WIND).intValue()+"", 49, 0, 0x000000, false);

            float ender = wsRegistry.getEldrinGenerationMultiplierFor(this.minecraft.player, Affinity.ENDER) * menu.blockEntity.getXMultRate();
            gui.drawString(font, "x"+(ender >= 10 ? Math.round(ender) : String.format("%.1f", ender)), 49, 24, 0x000000, false);
            gui.drawString(font, nodeAmounts.get(Affinity.ENDER).intValue()+"", 49, 35, 0x000000, false);

            float earth = wsRegistry.getEldrinGenerationMultiplierFor(this.minecraft.player, Affinity.EARTH) * menu.blockEntity.getXMultRate();
            gui.drawString(font, "x"+(earth >= 10 ? Math.round(earth) : String.format("%.1f", earth)), 49, 59, 0x000000, false);
            gui.drawString(font, nodeAmounts.get(Affinity.EARTH).intValue()+"", 49, 70, 0x000000, false);

            float fire = wsRegistry.getEldrinGenerationMultiplierFor(this.minecraft.player, Affinity.FIRE) * menu.blockEntity.getXMultRate();
            gui.drawString(font, "x"+(fire >= 10 ? Math.round(fire) : String.format("%.1f", fire)), 122, -11, 0x000000, false);
            gui.drawString(font, nodeAmounts.get(Affinity.FIRE).intValue()+"", 122, 0, 0x000000, false);

            float arcane = wsRegistry.getEldrinGenerationMultiplierFor(this.minecraft.player, Affinity.ARCANE) * menu.blockEntity.getXMultRate();
            gui.drawString(font, "x"+(arcane >= 10 ? Math.round(arcane) : String.format("%.1f", arcane)), 122, 24, 0x000000, false);
            gui.drawString(font, nodeAmounts.get(Affinity.ARCANE).intValue()+"", 122, 35, 0x000000, false);

            float water = wsRegistry.getEldrinGenerationMultiplierFor(this.minecraft.player, Affinity.WATER) * menu.blockEntity.getXMultRate();
            gui.drawString(font, "x"+(water >= 10 ? Math.round(water) : String.format("%.1f", water)), 122, 59, 0x000000, false);
            gui.drawString(font, nodeAmounts.get(Affinity.WATER).intValue()+"", 122, 70, 0x000000, false);
        });
    }
}
