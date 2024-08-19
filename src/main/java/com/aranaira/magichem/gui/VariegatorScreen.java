package com.aranaira.magichem.gui;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ext.AbstractDistillationBlockEntity;
import com.aranaira.magichem.networking.GrandDeviceSyncDataC2SPacket;
import com.aranaira.magichem.networking.VariegatorSyncDataC2SPacket;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.PacketRegistry;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.aranaira.magichem.block.entity.VariegatorBlockEntity.*;

public class VariegatorScreen extends AbstractContainerScreen<VariegatorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_variegator.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 161,
            PANEL_INSERTION_U = 176, PANEL_INSERTION_V = 0, PANEL_INSERTION_W = 32, PANEL_INSERTION_H = 58,
            PANEL_ADMIXTURE_U = 176, PANEL_ADMIXTURE_V = 58, PANEL_ADMIXTURE_W = 18, PANEL_ADMIXTURE_H = 63,
            PANEL_COLORS_U = 0, PANEL_COLORS_V = 161, PANEL_COLORS_W = 191, PANEL_COLORS_H = 32,
            PANEL_COLORLESS_U = 0, PANEL_COLORLESS_V = 193, PANEL_COLORLESS_S = 11,
            PROGRESS_BAR_SIZE = 28;
    private static final ItemStack
        STACK_ADMIXTURE_COLOR = new ItemStack(ItemRegistry.getAdmixturesMap(false, false).get("color"));
    private static final HashMap<DyeColor, ItemStack> STACK_DYES = new HashMap<>();
    private List<ImageButton> buttons = new ArrayList<>();

    public VariegatorScreen(VariegatorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);

        if(STACK_DYES.size() == 0) {
            STACK_DYES.put(DyeColor.RED, new ItemStack(Items.RED_DYE));
            STACK_DYES.put(DyeColor.ORANGE, new ItemStack(Items.ORANGE_DYE));
            STACK_DYES.put(DyeColor.YELLOW, new ItemStack(Items.YELLOW_DYE));
            STACK_DYES.put(DyeColor.LIME, new ItemStack(Items.LIME_DYE));
            STACK_DYES.put(DyeColor.GREEN, new ItemStack(Items.GREEN_DYE));
            STACK_DYES.put(DyeColor.CYAN, new ItemStack(Items.CYAN_DYE));
            STACK_DYES.put(DyeColor.LIGHT_BLUE, new ItemStack(Items.LIGHT_BLUE_DYE));
            STACK_DYES.put(DyeColor.BLUE, new ItemStack(Items.BLUE_DYE));
            STACK_DYES.put(DyeColor.PURPLE, new ItemStack(Items.PURPLE_DYE));
            STACK_DYES.put(DyeColor.MAGENTA, new ItemStack(Items.MAGENTA_DYE));
            STACK_DYES.put(DyeColor.PINK, new ItemStack(Items.PINK_DYE));
            STACK_DYES.put(DyeColor.BROWN, new ItemStack(Items.BROWN_DYE));
            STACK_DYES.put(DyeColor.BLACK, new ItemStack(Items.BLACK_DYE));
            STACK_DYES.put(DyeColor.GRAY, new ItemStack(Items.GRAY_DYE));
            STACK_DYES.put(DyeColor.LIGHT_GRAY, new ItemStack(Items.LIGHT_GRAY_DYE));
            STACK_DYES.put(DyeColor.WHITE, new ItemStack(Items.WHITE_DYE));
        }
    }

    @Override
    protected void init() {
        super.init();
        initializeColorSelectionButtons();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        renderBackground(gui);
        super.render(gui, mouseX, mouseY, delta);
        renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        pGuiGraphics.blit(TEXTURE, x - 8, y - 37, PANEL_COLORS_U, PANEL_COLORS_V, PANEL_COLORS_W, PANEL_COLORS_H);
        for(int i=0; i<COLOR_GUI_ORDER.length; i++) {
            int scaled = Math.min(Config.variegatorMaxDye, (menu.blockEntity.getDyeFillByColor(COLOR_GUI_ORDER[i]) * 20) / Config.variegatorMaxDye);
            pGuiGraphics.blit(TEXTURE, x - 4 + (i * 12), y - 28 + (20 - scaled), 28 + (i*3), 236, 3, scaled);
        }

        int scaledAdmixture = Math.min(Config.variegatorMaxAdmixture, (menu.blockEntity.dyeAdmixture * 40) / Config.variegatorMaxAdmixture);
        pGuiGraphics.blit(TEXTURE, x + 15, y + 6, PANEL_ADMIXTURE_U, PANEL_ADMIXTURE_V, PANEL_ADMIXTURE_W, PANEL_ADMIXTURE_H);
        pGuiGraphics.blit(TEXTURE, x + 22, y + 50 - scaledAdmixture, 252, 216, 4, scaledAdmixture);

        pGuiGraphics.blit(TEXTURE, x + 4, y + 6, PANEL_COLORLESS_U, PANEL_COLORLESS_V, PANEL_COLORLESS_S, PANEL_COLORLESS_S);

        pGuiGraphics.blit(TEXTURE, x + 149, y + 4, PANEL_INSERTION_U, PANEL_INSERTION_V, PANEL_INSERTION_W, PANEL_INSERTION_H);

        pGuiGraphics.renderItem(STACK_ADMIXTURE_COLOR, x + 16, y + 51);

        //If we don't have a selected color, default to un-dyeing
        if(menu.blockEntity.selectedColor == -1) {
            pGuiGraphics.blit(TEXTURE, x + 83, y + 59, 76, 246, 10, 10);
        }
        //Otherwise, draw the dye for the currently selected color
        else {
            pGuiGraphics.renderItem(STACK_DYES.get(COLOR_GUI_ORDER[menu.blockEntity.selectedColor]), x + 80, y + 56);
        }

        //progress bar
        pGuiGraphics.blit(TEXTURE, x + 74, y + 20, 0, 228, getScaledProgress(), PROGRESS_BAR_SIZE);
    }

    private void initializeColorSelectionButtons() {
        for(int i=0; i<COLOR_GUI_ORDER.length; i++) {
            int finalI = i;
            ImageButton colorButton = this.addRenderableWidget(new ImageButton(this.leftPos - 9 + (12 * i), this.topPos - 37, 12, 31, 0, 0, TEXTURE, button -> {
                PacketRegistry.sendToServer(new VariegatorSyncDataC2SPacket(
                        menu.blockEntity.getBlockPos(),
                        finalI
                ));
            }));
            buttons.add(colorButton);
        }

        ImageButton colorlessButton = this.addRenderableWidget(new ImageButton(this.leftPos + 4, this.topPos + 6, 11, 11, 0, 0, TEXTURE, button -> {
            PacketRegistry.sendToServer(new VariegatorSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    -1
            ));
        }));
        buttons.add(colorlessButton);
    }

    private int getScaledProgress() {
        return Math.min(PROGRESS_BAR_SIZE, PROGRESS_BAR_SIZE * menu.blockEntity.progress / getOperationTicks(menu.blockEntity));
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        Font font = Minecraft.getInstance().font;

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;
        pGuiGraphics.drawString(font, "sP:"+getScaledProgress(), -150, 0, 0xffffffcc, true);
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        super.renderTooltip(pGuiGraphics, pMouseX, pMouseY);

        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Color Gauges
        for(int i=0; i<COLOR_GUI_ORDER.length; i++) {
            DyeColor color = COLOR_GUI_ORDER[i];

            if (pMouseX > x - 9 + (12 * i) && pMouseX <= x - 9 + (12 * (i + 1)) &&
                    pMouseY >= y - 37 && pMouseY <= y - 6) {
                int fill = Math.min(menu.blockEntity.getDyeFillByColor(color), Config.variegatorMaxDye);
                float fillPercent = ((float)fill / Config.variegatorMaxDye) * 100f;

                tooltipContents.add(Component.empty()
                        .append(Component.translatable("item.minecraft."+color.getName()+"_dye").withStyle(ChatFormatting.GOLD))
                        .append(":"));
                tooltipContents.add(Component.empty()
                        .append(Component.literal(fill+"").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.literal(" / ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(Config.variegatorMaxDye+"").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(" (").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(String.format("%.1f", fillPercent)+"%").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.literal(")").withStyle(ChatFormatting.DARK_GRAY))
                );
                tooltipContents.add(Component.empty());
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.variegator.color.select.part1"))
                        .append(Component.translatable("item.minecraft.firework_star."+color.getName()).withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable("tooltip.magichem.gui.variegator.color.select.part2")));

                pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pMouseX, pMouseY);
            }
        }

        if (pMouseX >= x + 4 && pMouseX <= x + 15 &&
                pMouseY >= y + 6 && pMouseY <= y + 17) {
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.variegator.color.select.part1"))
                    .append(Component.translatable("tooltip.magichem.gui.variegator.color.colorless").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.variegator.color.select.part2")));

            pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pMouseX, pMouseY);
        }

        if (pMouseX >= x + 79 && pMouseX <= x + 96 &&
                pMouseY >= y + 55 && pMouseY <= y + 72) {
            int colorID = menu.blockEntity.selectedColor;
            String color = "";
            if(colorID == -1)
                color = "tooltip.magichem.gui.variegator.color.colorless";
            else
                color = "item.minecraft.firework_star."+COLOR_GUI_ORDER[colorID].getName();

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.variegator.color.current.part1"))
                    .append(Component.translatable(color).withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.variegator.color.current.part2")));

            pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pMouseX, pMouseY);
        }

        if (pMouseX >= x + 15 && pMouseX <= x + 15 + PANEL_ADMIXTURE_W &&
                pMouseY >= y + 6 && pMouseY <= y + 6 + PANEL_ADMIXTURE_H) {
            int fill = Math.min(menu.blockEntity.dyeAdmixture, Config.variegatorMaxAdmixture);
            float fillPercent = ((float)fill / Config.variegatorMaxAdmixture) * 100f;

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("item.magichem.admixture_color").withStyle(ChatFormatting.GOLD))
                    .append(":"));
            tooltipContents.add(Component.empty()
                    .append(Component.literal(fill+"").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal(" / ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(Config.variegatorMaxAdmixture+"").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(" (").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(String.format("%.1f", fillPercent)+"%").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.literal(")").withStyle(ChatFormatting.DARK_GRAY))
            );
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.variegator.admixture.part1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.variegator.admixture.part2")));

            pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pMouseX, pMouseY);
        }
    }

}
