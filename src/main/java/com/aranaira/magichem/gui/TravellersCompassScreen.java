package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TravellersCompassScreen extends AbstractContainerScreen<TravellersCompassMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_travellers_compass.png");

    public static final int
        PANEL_MAIN_W = 196, PANEL_MAIN_H = 252;

    public TravellersCompassScreen(TravellersCompassMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);
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
