package com.aranaira.magichem.interop.mna.guide;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.recipe.SublimationRitualRecipe;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.guidebook.RecipeRendererBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class RecipeInfusionRitual extends RecipeRendererBase {
    private SublimationRitualRecipe recipe;
    private static final ResourceLocation TEXTURE = new ResourceLocation(MagiChemMod.MODID, "textures/gui/guide/alchemical_infusion_ritual.png");
    private static final ItemStack SUBLIMATION_PRIMER = new ItemStack(ItemRegistry.SUBLIMATION_PRIMER.get());

    public RecipeInfusionRitual(int xIn, int yIn) {
        super(xIn, yIn);
    }

    protected void drawForeground(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY, float pPartialTicks) {
        if(this.recipe != null) {
            for(int i=0;i<recipe.getIngredientItemStacks().size();i++) {
                this.renderItemStack(pGuiGraphics, recipe.getIngredientItemStacks().get(i),
                        (int)((float)this.getX() / this.scale + 69.0f + i*32.0f),
                        (int)((float)this.getY() / this.scale + 37.0f)
                );
            }

            this.renderItemStack(pGuiGraphics, SUBLIMATION_PRIMER,
                    (int)((float)this.getX() / this.scale + 101.0f),
                    (int)((float)this.getY() / this.scale + 69.0f)
            );

            this.renderItemStack(pGuiGraphics, recipe.getComponentMateria().getFirst(),
                    (int)((float)this.getX() / this.scale + 118.0f),
                    (int)((float)this.getY() / this.scale + 117.0f)
            );

            this.renderItemStack(pGuiGraphics, recipe.getComponentMateria().getSecond(),
                    (int)((float)this.getX() / this.scale + 118.0f),
                    (int)((float)this.getY() / this.scale + 135.0f)
            );

            this.renderItemStack(pGuiGraphics, recipe.getAlchemyObject(),
                    (int)((float)this.getX() / this.scale + 101.0f),
                    (int)((float)this.getY() / this.scale + 195.0f)
            );
        }
    }

    protected ResourceLocation backgroundTexture() {
        return TEXTURE;
    }

    public void init_internal(ResourceLocation pRecipeLocation) {
        Optional<? extends net.minecraft.world.item.crafting.Recipe<?>> pattern = this.minecraft.level.getRecipeManager().byKey(pRecipeLocation);
        if(pattern.isPresent() && pattern.get() instanceof SublimationRitualRecipe airr) {
            this.recipe = airr;
        }
    }

    public int getTier() {
        return 1;
    }
}
