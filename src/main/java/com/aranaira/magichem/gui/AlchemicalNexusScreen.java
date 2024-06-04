package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.ButtonData;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class AlchemicalNexusScreen extends AbstractContainerScreen<AlchemicalNexusMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_alchemicalnexus.png");
    private static final ResourceLocation TEXTURE_INGREDIENTS =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fabrication_ext.png");

    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 192,
            PANEL_STATS_X = 176, PANEL_STATS_Y = 53, PANEL_STATS_W = 64, PANEL_STATS_H = 59, PANEL_STATS_U = 176, PANEL_STATS_V = 0,
            PANEL_RECIPE_X = -84, PANEL_RECIPE_Y = -7, PANEL_RECIPE_U = 176, PANEL_RECIPE_W = 80, PANEL_RECIPE_H = 126,
            PANEL_INGREDIENTS_X = 176, PANEL_INGREDIENTS_Y = 98, PANEL_INGREDIENTS_W = 80,
            PANEL_INGREDIENTS_U1 = 160, PANEL_INGREDIENTS_U2 = 80, PANEL_INGREDIENTS_U3 = 160, PANEL_INGREDIENTS_U4 = 80, PANEL_INGREDIENTS_U5 = 0,
            PANEL_INGREDIENTS_V1 =  66, PANEL_INGREDIENTS_V2 = 84, PANEL_INGREDIENTS_V3 =   0, PANEL_INGREDIENTS_V4 =  0, PANEL_INGREDIENTS_V5 = 0,
            PANEL_INGREDIENTS_H1 =  30, PANEL_INGREDIENTS_H2 = 48, PANEL_INGREDIENTS_H3 =  66, PANEL_INGREDIENTS_H4 = 84, PANEL_INGREDIENTS_H5 = 102,
            SLURRY_X = 8, SLURRY_Y = 23, SLURRY_W = 8, SLURRY_H = 88;

    private final ButtonData[] recipeSelectButtons = new ButtonData[15];
    private EditBox recipeFilterBox;

    public AlchemicalNexusScreen(AlchemicalNexusMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

    }

//    private void initializeRecipeFilterBox() {
//        int x = (width - PANEL_MAIN_W) / 2;
//        int y = (height - PANEL_MAIN_H) / 2;
//
//        this.recipeFilterBox = new EditBox(Minecraft.getInstance().font, x, y, 65, 16, Component.empty()) {
//            @Override
//            public boolean charTyped(char pCodePoint, int pModifiers) {
//                updateDisplayedRecipes(recipeFilterBox.getValue());
//                return super.charTyped(pCodePoint, pModifiers);
//            }
//
//            @Override
//            public void deleteChars(int pNum) {
//                super.deleteChars(pNum);
//                updateDisplayedRecipes(recipeFilterBox.getValue());
//            }
//
//            @Override
//            public void deleteWords(int pNum) {
//                super.deleteWords(pNum);
//                updateDisplayedRecipes(recipeFilterBox.getValue());
//            }
//        };
//        this.recipeFilterBox.setMaxLength(60);
//        this.recipeFilterBox.setFocused(false);
//        this.recipeFilterBox.setCanLoseFocus(false);
//        this.setFocused(this.recipeFilterBox);
//    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {

    }
}
