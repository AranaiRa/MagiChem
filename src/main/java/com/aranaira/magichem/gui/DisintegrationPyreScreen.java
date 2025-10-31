package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.networking.DisintegrationPyreSyncDataC2SPacket;
import com.aranaira.magichem.registry.PacketRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DisintegrationPyreScreen extends AbstractContainerScreen<DisintegrationPyreMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_disintegration_pyre.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 148;
    private EditBox percentSelectorBox;

    public DisintegrationPyreScreen(DisintegrationPyreMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Main panel
        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        //Materia panel
        pGuiGraphics.blit(TEXTURE, x + 157, y - 6, 216, 0, 40, 58);
        int sM = Math.min(42, Math.round(menu.blockEntity.getDropletsPercent() * 42f));
        pGuiGraphics.blit(TEXTURE, x + 165, y + 2 + (42 - sM), 214, 0, 2, sM);

        //Scroll Nubbin for Batch Size
        float percentAsFloat = (float)(menu.blockEntity.getPercent() - 1) / 98f;
        int nubbinShift = (int)Math.floor(percentAsFloat * 100);
        pGuiGraphics.blit(TEXTURE, x + 34 + nubbinShift, y + 36, 0, 248, 8, 8);
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {

        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;
        boolean doOriginalTooltip = true;

        //Admixture
        if(pX >= x+164 && pX <= x+167 &&
                pY >= y+1 && pY <= y+45) {

            int current = menu.blockEntity.getDroplets();
            int max = ServerConfig.disintegrationPyreMateriaUnitsPerDram * 3;
            float percent = menu.blockEntity.getDropletsPercent();

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.disintegration_pyre.admixture").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.disintegration_pyre.admixture.line1")));
            tooltipContents.add((Component.empty()));
            tooltipContents.add((Component.empty())
                    .append(Component.translatable("tooltip.magichem.gui.disintegration_pyre.admixture.line2a"))
                    .append(Component.literal(ServerConfig.disintegrationPyreMateriaUnitsPerDram+"").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.disintegration_pyre.admixture.line2b")));
            tooltipContents.add((Component.empty()));
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.disintegration_pyre.admixture.line3").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(Math.min(max, current) + " / " + max).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal("  ")
                            .append(Component.literal("( ").withStyle(ChatFormatting.DARK_GRAY))
                            .append(Component.literal(String.format("%.1f", Math.min(1, percent) * 100)+"%")).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal(" )").withStyle(ChatFormatting.DARK_GRAY)));
            pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
        }


        if(doOriginalTooltip)
            super.renderTooltip(pGuiGraphics, pX, pY);

        pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        Font font = Minecraft.getInstance().font;
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void init() {
        super.init();
        initializePercentSelectorBox();
    }

    private static final char[] VALID_CHARACTERS = new char[]{'0','1','2','3','4','5','6','7','8','9'};
    private void initializePercentSelectorBox() {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        this.percentSelectorBox = new EditBox(Minecraft.getInstance().font, x, y, 38, 16, Component.empty()) {
            @Override
            public boolean charTyped(char pCodePoint, int pModifiers) {
                for(char c : VALID_CHARACTERS) {
                    if(c == pCodePoint) {
                        super.charTyped(pCodePoint, pModifiers);
                        int out = 0;
                        if(getValue().length() > 0) out = Math.max(1, Math.min(99, Integer.parseInt(getValue())));
                        setValue(""+out);
                        PacketRegistry.sendToServer(new DisintegrationPyreSyncDataC2SPacket(
                                menu.blockEntity.getBlockPos(), out));
                        return true;
                    }
                }

                return false;
            }

            @Override
            public void deleteChars(int pNum) {
                super.deleteChars(pNum);
                int out = 1;
                if(getValue().length() > 0) out = Math.max(1, Math.min(99, Integer.parseInt(getValue())));
                setValue(""+out);
                PacketRegistry.sendToServer(new DisintegrationPyreSyncDataC2SPacket(
                        menu.blockEntity.getBlockPos(), out));
            }

            @Override
            public void deleteWords(int pNum) {
                super.deleteWords(pNum);
                int out = 1;
                if(getValue().length() > 0) out = Math.max(1, Math.min(99, Integer.parseInt(getValue())));
                setValue(""+out);
                PacketRegistry.sendToServer(new DisintegrationPyreSyncDataC2SPacket(
                        menu.blockEntity.getBlockPos(), out));
            }
        };
        this.percentSelectorBox.setMaxLength(60);
        this.percentSelectorBox.setFocused(false);
        this.percentSelectorBox.setCanLoseFocus(false);
        this.setFocused(this.percentSelectorBox);

        renderSelectorBox();
    }

    private void renderSelectorBox() {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        percentSelectorBox.setX(xOrigin + 34);
        percentSelectorBox.setY(yOrigin + 15);
        percentSelectorBox.setValue(""+menu.blockEntity.getPercent());

        addRenderableWidget(percentSelectorBox);
    }


    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if(pButton == 0) {
            int x = (width - PANEL_MAIN_W) / 2;
            int y = (height - PANEL_MAIN_H) / 2;

            if (pMouseX >= x+33 && pMouseX <= x+143 &&
                    pMouseY >= y+35 && pMouseY <= y+45) {
                double point = pMouseX - (x + 33);
                double percent = point / 108d;

                int newPercent = Math.max(1, Math.min(99, (int)Math.round(percent*100d)));
                menu.blockEntity.setPercent(newPercent);
                percentSelectorBox.setValue(""+newPercent);
                PacketRegistry.sendToServer(new DisintegrationPyreSyncDataC2SPacket(
                        menu.blockEntity.getBlockPos(), newPercent));
            }
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if(pButton == 0) {
            int x = (width - PANEL_MAIN_W) / 2;
            int y = (height - PANEL_MAIN_H) / 2;

            if (pMouseX >= x+33 && pMouseX <= x+143 &&
                    pMouseY >= y+35 && pMouseY <= y+45) {
                double point = pMouseX - (x + 33);
                double percent = point / 108d;

                int newPercent = Math.max(1, Math.min(99, (int)Math.round(percent*100d)));
                menu.blockEntity.setPercent(newPercent);
                percentSelectorBox.setValue(""+newPercent);
            }
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }
}
