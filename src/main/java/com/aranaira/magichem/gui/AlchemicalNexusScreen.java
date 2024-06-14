package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
import com.aranaira.magichem.block.entity.CirclePowerBlockEntity;
import com.aranaira.magichem.block.entity.FuseryBlockEntity;
import com.aranaira.magichem.foundation.ButtonData;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class AlchemicalNexusScreen extends AbstractContainerScreen<AlchemicalNexusMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_alchemicalnexus.png");
    private static final ResourceLocation TEXTURE_SLURRY =
            new ResourceLocation(MagiChemMod.MODID, "textures/block/fluid/experience_still.png");
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
            SLURRY_X = 8, SLURRY_Y = 23, SLURRY_W = 8, SLURRY_H = 73,
            STAGE_INDICATOR_U = 108, STAGE_INDICATOR_V = 238, STAGE_INDICATOR_W = 12, STAGE_INDICATOR_W_END = 6, STAGE_INDICATOR_H = 9;

    private final ButtonData[] recipeSelectButtons = new ButtonData[15];
    private EditBox recipeFilterBox;

    public AlchemicalNexusScreen(AlchemicalNexusMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;
        Font font = Minecraft.getInstance().font;

        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        //slurry gauge
        int slurryH = AlchemicalNexusBlockEntity.getScaledSlurry(menu.getSlurryInTank());
        RenderSystem.setShaderTexture(1, TEXTURE_SLURRY);
        pGuiGraphics.blit(TEXTURE_SLURRY, x + SLURRY_X, y + SLURRY_Y + SLURRY_H - slurryH, 0, 0, SLURRY_W, slurryH, 16, 16);

        renderStageGauge(pGuiGraphics, x, y);

        menu.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            if(handler.getStackInSlot(AlchemicalNexusBlockEntity.SLOT_MARKS).isEmpty()) {
                pGuiGraphics.pose().scale(0.5f, 0.5f, 0.5f);
                pGuiGraphics.blit(TEXTURE, (x + 134) * 2, (y + 7) * 2, 66, 222, 32, 32);
                pGuiGraphics.pose().scale(2.0f, 2.0f, 2.0f);
            }

            if(handler.getStackInSlot(AlchemicalNexusBlockEntity.SLOT_RECIPE).isEmpty() || menu.getCurrentRecipe() == null) {
                pGuiGraphics.blit(TEXTURE, x + 79, y + 79, 28, 238, 18, 18);
            } else {
                pGuiGraphics.renderItem(handler.getStackInSlot(AlchemicalNexusBlockEntity.SLOT_RECIPE), x + 80, y + 80);

                pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 0.25f);
                //ingredients
                {
                    int i=0;
                    for (ItemStack is : menu.getStage(menu.blockEntity.getCraftingStage()).componentItems) {
                        pGuiGraphics.renderItem(is, x + 22, y + 8 + (i * 18));
                        i++;
                    }
                }
                pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

                //materia
                {
                    int i=0;
                    for (ItemStack is : menu.getStage(menu.blockEntity.getCraftingStage()).componentMateria) {
                        pGuiGraphics.renderItem(is, x + 44, y + 8 + (i * 18));
                        pGuiGraphics.renderItemDecorations(font, is, x + 44, y + 8 + (i * 18));
                        i++;
                    }
                }
            }
        });

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

    private void renderStageGauge(GuiGraphics pGuiGraphics, int pX, int pY) {
        int count = menu.getAllStages().size();

        if(count > 0) {
            int width = STAGE_INDICATOR_W_END + (count - 1) * STAGE_INDICATOR_W;
            int x = 115 + (27 - width / 2);
            int y = 29;

            for(int i=0; i<count; i++) {

                int activeShift = i <= menu.getCurrentStageID() ? STAGE_INDICATOR_H : 0;

                if(i == count - 1)
                    pGuiGraphics.blit(TEXTURE, pX + x + i * STAGE_INDICATOR_W, pY + y, STAGE_INDICATOR_U, STAGE_INDICATOR_V + activeShift, STAGE_INDICATOR_W_END, STAGE_INDICATOR_H, 256, 256);
                else
                    pGuiGraphics.blit(TEXTURE, pX + x + i * STAGE_INDICATOR_W, pY + y, STAGE_INDICATOR_U, STAGE_INDICATOR_V + activeShift, STAGE_INDICATOR_W, STAGE_INDICATOR_H, 256, 256);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {

    }
}
