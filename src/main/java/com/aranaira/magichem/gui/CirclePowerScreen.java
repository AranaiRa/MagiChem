package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.CirclePowerBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CirclePowerScreen extends AbstractContainerScreen<CirclePowerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_circle_power.png");

    public CirclePowerScreen(CirclePowerMenu menu, Inventory inventory, Component component) {
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

        int w = 176;
        int h = 167;

        int x = (width - w) / 2;
        int y = (height - h) / 2;

        gui.blit(TEXTURE, x, y, 0, 0, w, h);
        renderProgressBar(1, gui, x + 23, y + 42);
        renderProgressBar(2, gui, x + 59, y + 42);
        renderProgressBar(3, gui, x + 95, y + 42);
        renderProgressBar(4, gui, x + 131, y + 42);

        gui.blit(TEXTURE, x + w + 4, y + 10, 176, 0, 32, 32);

        //generation panel
        gui.blit(TEXTURE, x + 2, y - 30, 0, 167, 172, 25);
    }

    private void renderProgressBar(int tier, GuiGraphics gui, int x, int y) {
        int sp = menu.getScaledProgress(tier);

        if(menu.isCrafting(tier))
            gui.blit(TEXTURE, x, y, 0, 253, sp, 3);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        renderBackground(gui);
        super.render(gui, mouseX, mouseY, delta);
        renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        int reagentCount =
                (menu.blockEntity.hasReagent(1) ? 1 : 0) +
                (menu.blockEntity.hasReagent(2) ? 1 : 0) +
                (menu.blockEntity.hasReagent(3) ? 1 : 0) +
                (menu.blockEntity.hasReagent(4) ? 1 : 0);

        final MutableComponent generation = Component.empty()
                .append(Component.translatable("gui.magichem.generating")
                .append(Component.literal(CirclePowerBlockEntity.getGenRate(reagentCount) + "").withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.literal(" FE / tick"))
                );
        pGuiGraphics.drawString(font, generation, 25, -21, 0x00000000, false);
    }
}
