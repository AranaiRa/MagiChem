package com.aranaira.magichem.interop.mna.guide;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.InfusionStage;
import com.aranaira.magichem.recipe.SublimationRecipe;
import com.mna.api.guidebook.RecipeRendererBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class RecipeSublimation extends RecipeRendererBase {
    private SublimationRecipe recipe;
    private static final ResourceLocation TEXTURE = new ResourceLocation(MagiChemMod.MODID, "textures/gui/guide/sublimation.png");

    public RecipeSublimation(int xIn, int yIn) {
        super(xIn, yIn);
    }

    private static final int[] HORIZONTAL_CORRECTION = {0, 0, 1, 2, 3};
    protected void drawForeground(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY, float pPartialTicks) {
        if(this.recipe != null) {
            NonNullList<InfusionStage> stages = recipe.getStages(false);
            int stageCount = stages.size();

            pGuiGraphics.renderItem(recipe.getAlchemyObject(), pX + 178, pY + 72);
            pGuiGraphics.renderItemDecorations(Minecraft.getInstance().font, recipe.getAlchemyObject(), pX + 178, pY + 72);
            if(pMouseX >= pX + 178 && pMouseX <= pX + 198) {
                if(pMouseY >= pY + 72 && pMouseY <= pY + 92) {
                    pGuiGraphics.renderTooltip(Minecraft.getInstance().font, recipe.getAlchemyObject(), pMouseX, pMouseY);
                }
            }

            if(stageCount >= 1) {
                InfusionStage stage = stages.get(0);
                for(int i=0; i<stage.componentItems.size(); i++) {
                    this.renderItemStack(pGuiGraphics, stage.componentItems.get(i), pX + 39 + (22 * i) + HORIZONTAL_CORRECTION[i], pY + 11);
                }
                for(int i=0; i<stage.componentMateria.size(); i++) {
                    int x = pX + 39 + (22 * i) + HORIZONTAL_CORRECTION[i];
                    int y = pY + 34;
                    pGuiGraphics.renderItem(stage.componentMateria.get(i), x, y);
                    pGuiGraphics.renderItemDecorations(Minecraft.getInstance().font, stage.componentMateria.get(i), x, y);
                    if(pMouseX >= x && pMouseX <= x + 20) {
                        if(pMouseY >= y && pMouseY <= y + 20) {
                            pGuiGraphics.renderTooltip(Minecraft.getInstance().font, stage.componentMateria.get(i), pMouseX, pMouseY);
                        }
                    }
                }
            }

            if(stageCount >= 2) {
                InfusionStage stage = stages.get(1);
                for(int i=0; i<stage.componentItems.size(); i++) {
                    this.renderItemStack(pGuiGraphics, stage.componentItems.get(i), pX + 29 + (22 * i) + HORIZONTAL_CORRECTION[i], pY + 60);
                }
                for(int i=0; i<stage.componentMateria.size(); i++) {
                    int x = pX + 29 + (22 * i) + HORIZONTAL_CORRECTION[i];
                    int y = pY + 83;
                    pGuiGraphics.renderItem(stage.componentMateria.get(i), x, y);
                    pGuiGraphics.renderItemDecorations(Minecraft.getInstance().font, stage.componentMateria.get(i), x, y);
                    if(pMouseX >= x && pMouseX <= x + 20) {
                        if(pMouseY >= y && pMouseY <= y + 20) {
                            pGuiGraphics.renderTooltip(Minecraft.getInstance().font, stage.componentMateria.get(i), pMouseX, pMouseY);
                        }
                    }
                }
            }

            if(stageCount >= 3) {
                InfusionStage stage = stages.get(2);
                for(int i=0; i<stage.componentItems.size(); i++) {
                    this.renderItemStack(pGuiGraphics, stage.componentItems.get(i), pX + 21 + (22 * i) + HORIZONTAL_CORRECTION[i], pY + 109);
                }
                for(int i=0; i<stage.componentMateria.size(); i++) {
                    int x = pX + 21 + (22 * i) + HORIZONTAL_CORRECTION[i];
                    int y = pY + 131;
                    pGuiGraphics.renderItem(stage.componentMateria.get(i), x, y);
                    pGuiGraphics.renderItemDecorations(Minecraft.getInstance().font, stage.componentMateria.get(i), x, y);
                    if(pMouseX >= x && pMouseX <= x + 20) {
                        if(pMouseY >= y && pMouseY <= y + 20) {
                            pGuiGraphics.renderTooltip(Minecraft.getInstance().font, stage.componentMateria.get(i), pMouseX, pMouseY);
                        }
                    }
                }
            }

            if(stageCount >= 4) {
                InfusionStage stage = stages.get(3);
                for(int i=0; i<stage.componentItems.size(); i++) {
                    this.renderItemStack(pGuiGraphics, stage.componentItems.get(i), pX + 29 + (22 * i) + HORIZONTAL_CORRECTION[i], pY + 157);
                }
                for(int i=0; i<stage.componentMateria.size(); i++) {
                    int x = pX + 29 + (22 * i) + HORIZONTAL_CORRECTION[i];
                    int y = pY + 180;
                    pGuiGraphics.renderItem(stage.componentMateria.get(i), x, y);
                    pGuiGraphics.renderItemDecorations(Minecraft.getInstance().font, stage.componentMateria.get(i), x, y);
                    if(pMouseX >= x && pMouseX <= x + 20) {
                        if(pMouseY >= y && pMouseY <= y + 20) {
                            pGuiGraphics.renderTooltip(Minecraft.getInstance().font, stage.componentMateria.get(i), pMouseX, pMouseY);
                        }
                    }
                }
            }

            if(stageCount >= 5) {
                InfusionStage stage = stages.get(4);
                for(int i=0; i<stage.componentItems.size(); i++) {
                    this.renderItemStack(pGuiGraphics, stage.componentItems.get(i), pX + 39 + (22 * i) + HORIZONTAL_CORRECTION[i], pY + 206);
                }
                for(int i=0; i<stage.componentMateria.size(); i++) {
                    int x = pX + 39 + (22 * i) + HORIZONTAL_CORRECTION[i];
                    int y = pY + 228;
                    pGuiGraphics.renderItem(stage.componentMateria.get(i), x, y);
                    pGuiGraphics.renderItemDecorations(Minecraft.getInstance().font, stage.componentMateria.get(i), x, y);
                    if(pMouseX >= x && pMouseX <= x + 20) {
                        if(pMouseY >= y && pMouseY <= y + 20) {
                            pGuiGraphics.renderTooltip(Minecraft.getInstance().font, stage.componentMateria.get(i), pMouseX, pMouseY);
                        }
                    }
                }
            }
        }
    }

    protected ResourceLocation backgroundTexture() {
        return TEXTURE;
    }

    public void init_internal(ResourceLocation pRecipeLocation) {
        Optional<? extends net.minecraft.world.item.crafting.Recipe<?>> pattern = this.minecraft.level.getRecipeManager().byKey(pRecipeLocation);
        if(pattern.isPresent() && pattern.get() instanceof SublimationRecipe sr) {
            this.recipe = sr;
        }
    }

    public int getTier() {
        return 1;
    }
}
