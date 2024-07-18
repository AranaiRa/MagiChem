package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChargingTalismanScreen extends AbstractContainerScreen<ChargingTalismanMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_charging_talisman.png");

    public static final int
        PANEL_MAIN_W = 176, PANEL_MAIN_H = 168,
        SPIKE_U = 201, SPIKE_V = 0, SPIKE_S = 55,
        GHOST_U = 238, GHOST_V = 55, GHOST_S = 18;

    public ChargingTalismanScreen(ChargingTalismanMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Draw the power spike graphic if there's one inserted
        if(menu.itemHandler.getStackInSlot(ChargingTalismanMenu.SLOT_SPIKE) != ItemStack.EMPTY)
            pGuiGraphics.blit(TEXTURE, x + 86, y - 8, SPIKE_U, SPIKE_V, SPIKE_S, SPIKE_S);

        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        //Draw the ghost slot if there isn't a Power Spike
        if(menu.itemHandler.getStackInSlot(ChargingTalismanMenu.SLOT_SPIKE) == ItemStack.EMPTY)
            pGuiGraphics.blit(TEXTURE, x + 93, y + 22, GHOST_U, GHOST_V, GHOST_S, GHOST_S);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        super.render(gui, mouseX, mouseY, delta);
        renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        super.renderTooltip(pGuiGraphics, pX, pY);

        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        if(pX >= x + 93 && pX < x + 93 + 18 && pY >= y + 22 && pY < y + 22 + 18) {
            if(menu.itemHandler.getStackInSlot(ChargingTalismanMenu.SLOT_SPIKE) == ItemStack.EMPTY) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.chargingtalisman.spike.line1"))
                );
                tooltipContents.add(Component.empty());
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.chargingtalisman.spike.line2"))
                );
                pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
            }
        }

        if(pX >= x + 65 && pX < x + 65 + 18 && pY >= y + 50 && pY < y + 50 + 18) {
            if(menu.itemHandler.getStackInSlot(ChargingTalismanMenu.SLOT_CHARGEABLE_ITEM) == ItemStack.EMPTY) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.chargingtalisman.chargee.line1"))
                );
                tooltipContents.add(Component.empty());
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.chargingtalisman.chargee.line2"))
                );
                pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
            }
        }

    }
}
