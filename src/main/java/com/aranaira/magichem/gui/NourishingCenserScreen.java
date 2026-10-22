package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.aranaira.magichem.block.entity.NourishingCenserBlockEntity.SLOT_MARK_PAIR;

public class NourishingCenserScreen extends AbstractContainerScreen<NourishingCenserMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_nourishing_censer.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 147;
    private EditBox percentSelectorBox;
    private String lastUsedFilter = null;

    public NourishingCenserScreen(NourishingCenserMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
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
        pGuiGraphics.blit(TEXTURE, x + 136, y - 39, 216, 0, 40, 58);
        int sM = Math.min(42, Math.round(menu.blockEntity.getDropletsPercent() * 42f));
        pGuiGraphics.blit(TEXTURE, x + 144, y - 31 + (42 - sM), 214, 0, 2, sM);

        menu.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            if(handler.getStackInSlot(SLOT_MARK_PAIR).isEmpty()) {
                pGuiGraphics.pose().scale(0.5f, 0.5f, 0.5f);
                pGuiGraphics.blit(TEXTURE, (x + 80) * 2, (y + 8) * 2, 224, 224, 32, 32);
                pGuiGraphics.pose().scale(2.0f, 2.0f, 2.0f);
            }
        });
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
            int max = ServerConfig.nourishingCenserMateriaUnitsPerDram * 3;
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

        MutableComponent mc = Component.literal(menu.blockEntity.getMateriaCost()+" droplets / "+menu.blockEntity.getTickModulus()+"t ("+menu.blockEntity.getOperatingAreaSize()+" blocks)");
//        MutableComponent mc = Component.translatable("gui.magichem.nourishingcenser.overlapping");
//        MutableComponent mc = Component.translatable("gui.magichem.nourishingcenser.toosmall");
        int halfWidth = font.width(mc.toString()) / 2;
        pGuiGraphics.drawString(font, mc, 181 - halfWidth, 44, 0x000000, false);
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

        this.percentSelectorBox = new EditBox(Minecraft.getInstance().font, x, y, 38, 16, Component.empty());
        this.percentSelectorBox.setMaxLength(60);
        this.percentSelectorBox.setResponder(value -> {
            if (Objects.equals(value, lastUsedFilter)) return;
            int out = 1;
            try {
                if (!value.isEmpty()) out = Math.max(1, Math.min(99, Integer.parseInt(value)));
            } catch (NumberFormatException e) {
                if (lastUsedFilter != null) {
                    value = lastUsedFilter;
                    out = Integer.parseInt(value);
                }
            }
            value = "" + out;
            lastUsedFilter = value;
            this.percentSelectorBox.setValue(value);
            PacketRegistry.sendToServer(new DisintegrationPyreSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(), out));
        });
        this.percentSelectorBox.setFocused(false);
        this.percentSelectorBox.setCanLoseFocus(false);
        this.setFocused(this.percentSelectorBox);
    }
}
