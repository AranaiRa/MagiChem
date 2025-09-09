package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.capabilities.wisdom.WisdomProvider;
import com.aranaira.magichem.networking.WisdomSyncC2SPacket;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.PacketRegistry;
import com.mna.api.spells.attributes.Attribute;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CodexMateriaScreen extends AbstractContainerScreen<CodexMateriaMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_codex_materia.png");

    public static final int
        PANEL_MAIN_W = 256, PANEL_MAIN_H = 178;

    public CodexMateriaScreen(CodexMateriaMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        initializeRecipeButtons();
    }

    private void initializeRecipeButtons() {
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        pGuiGraphics.blit(TEXTURE, x, y-20, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);
        pGuiGraphics.blit(TEXTURE, x+89, y+206, 224, 220, 32, 36);

        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(2.0f, 2.0f, 2.0f);
        pGuiGraphics.pose().translate(0.0f, 0.5f, 0.0f);
//        pGuiGraphics.renderFakeItem(blah, (x/2)+45, (y/2)+35);
        pGuiGraphics.pose().popPose();
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

//        if(pX >= x + 93 && pX < x + 93 + 18 && pY >= y + 22 && pY < y + 22 + 18) {
//            if(menu.itemHandler.getStackInSlot(ChargingTalismanMenu.SLOT_SPIKE) == ItemStack.EMPTY) {
//                tooltipContents.add(Component.empty()
//                        .append(Component.translatable("tooltip.magichem.gui.charging_talisman.spike.line1"))
//                );
//                tooltipContents.add(Component.empty());
//                tooltipContents.add(Component.empty()
//                        .append(Component.translatable("tooltip.magichem.gui.charging_talisman.spike.line2"))
//                );
//                pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
//            }
//        }
//
//        if(pX >= x + 65 && pX < x + 65 + 18 && pY >= y + 50 && pY < y + 50 + 18) {
//            if(menu.itemHandler.getStackInSlot(ChargingTalismanMenu.SLOT_CHARGEABLE_ITEM) == ItemStack.EMPTY) {
//                tooltipContents.add(Component.empty()
//                        .append(Component.translatable("tooltip.magichem.gui.charging_talisman.chargee.line1"))
//                );
//                tooltipContents.add(Component.empty());
//                tooltipContents.add(Component.empty()
//                        .append(Component.translatable("tooltip.magichem.gui.charging_talisman.chargee.line2"))
//                );
//                pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
//            }
//        }

    }
}
