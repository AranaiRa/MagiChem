package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;

public class VariegatorScreen extends AbstractContainerScreen<VariegatorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_variegator.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 161,
            PANEL_INSERTION_U = 176, PANEL_INSERTION_V = 0, PANEL_INSERTION_W = 32, PANEL_INSERTION_H = 58,
            PANEL_ADMIXTURE_U = 176, PANEL_ADMIXTURE_V = 58, PANEL_ADMIXTURE_W = 18, PANEL_ADMIXTURE_H = 63,
            PANEL_COLORS_U = 0, PANEL_COLORS_V = 161, PANEL_COLORS_W = 191, PANEL_COLORS_H = 32,
            PANEL_COLORLESS_U = 0, PANEL_COLORLESS_V = 193, PANEL_COLORS_S = 11;
    private static final ItemStack
        STACK_ADMIXTURE_COLOR = new ItemStack(ItemRegistry.getAdmixturesMap(false, false).get("color"));
    private static final HashMap<DyeColor, ItemStack> STACK_DYES = new HashMap<>();

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
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        pGuiGraphics.blit(TEXTURE, x - 8, y - 37, PANEL_COLORS_U, PANEL_COLORS_V, PANEL_COLORS_W, PANEL_COLORS_H);

        pGuiGraphics.blit(TEXTURE, x + 15, y + 6, PANEL_ADMIXTURE_U, PANEL_ADMIXTURE_V, PANEL_ADMIXTURE_W, PANEL_ADMIXTURE_H);

        pGuiGraphics.blit(TEXTURE, x + 4, y + 6, PANEL_COLORLESS_U, PANEL_COLORLESS_V, PANEL_COLORS_S, PANEL_COLORS_S);

        pGuiGraphics.blit(TEXTURE, x + 149, y + 4, PANEL_INSERTION_U, PANEL_INSERTION_V, PANEL_INSERTION_W, PANEL_INSERTION_H);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {

    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        super.renderTooltip(pGuiGraphics, pX, pY);
    }
}
