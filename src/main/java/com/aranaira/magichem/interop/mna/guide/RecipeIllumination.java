package com.aranaira.magichem.interop.mna.guide;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.enums.LuminType;
import com.aranaira.magichem.recipe.FulminationRecipe;
import com.aranaira.magichem.recipe.IlluminationRecipe;
import com.mna.api.guidebook.RecipeRendererBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class RecipeIllumination extends RecipeRendererBase {
    private IlluminationRecipe recipe;
    private static final ResourceLocation TEXTURE = new ResourceLocation(MagiChemMod.MODID, "textures/gui/guide/illumination.png");

    public RecipeIllumination(int xIn, int yIn) {
        super(xIn, yIn);
    }

    protected void drawForeground(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY, float pPartialTicks) {
        if(this.recipe != null) {
            this.renderItemStack(pGuiGraphics, this.recipe.getInputItem(),
                    (int)((float)this.getX() / this.scale + 42f),
                    (int)((float)this.getY() / this.scale + 120.0f)
            );
            if(this.recipe.getLuminType() == LuminType.SOLAR) {
                this.renderItemStack(pGuiGraphics, this.recipe.getResultItem(),
                        (int) ((float) this.getX() / this.scale + 159f),
                        (int) ((float) this.getY() / this.scale + 59.0f)
                );
            }
            else if(this.recipe.getLuminType() == LuminType.LUNAR) {
                this.renderItemStack(pGuiGraphics, this.recipe.getResultItem(),
                        (int) ((float) this.getX() / this.scale + 159f),
                        (int) ((float) this.getY() / this.scale + 120.0f)
                );
            }
            else if(this.recipe.getLuminType() == LuminType.SIDEREAL) {
                this.renderItemStack(pGuiGraphics, this.recipe.getResultItem(),
                        (int) ((float) this.getX() / this.scale + 159f),
                        (int) ((float) this.getY() / this.scale + 181.0f)
                );
            }
        }
    }

    protected ResourceLocation backgroundTexture() {
        return TEXTURE;
    }

    public void init_internal(ResourceLocation pRecipeLocation) {
        Optional<? extends net.minecraft.world.item.crafting.Recipe<?>> pattern = this.minecraft.level.getRecipeManager().byKey(pRecipeLocation);
        if(pattern.isPresent() && pattern.get() instanceof IlluminationRecipe ir) {
            this.recipe = ir;
        }
    }

    public int getTier() {
        return 3;
    }
}
