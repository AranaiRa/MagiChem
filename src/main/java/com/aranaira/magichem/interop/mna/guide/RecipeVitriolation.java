package com.aranaira.magichem.interop.mna.guide;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.recipe.AnointingRecipe;
import com.aranaira.magichem.recipe.VitriolationRecipe;
import com.mna.api.guidebook.RecipeRendererBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class RecipeVitriolation extends RecipeRendererBase {
    private VitriolationRecipe recipe;
    private static final ResourceLocation TEXTURE = new ResourceLocation(MagiChemMod.MODID, "textures/gui/guide/vitriolation.png");

    public RecipeVitriolation(int xIn, int yIn) {
        super(xIn, yIn);
    }

    protected void drawForeground(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY, float pPartialTicks) {
        if(this.recipe != null) {
            this.renderItemStack(pGuiGraphics, this.recipe.getInputItem(),
                    (int)((float)this.getX() / this.scale + 43f),
                    (int)((float)this.getY() / this.scale + 111.0f)
            );
            this.renderItemStack(pGuiGraphics, new ItemStack(VitriolationRecipe.getAllFluidsOfAcidStrength(this.recipe.getMinimumAcidStrength()).get(0).getBucket(), this.recipe.getBaseFluidConsumed()),
                    (int)((float)this.getX() / this.scale + 43f),
                    (int)((float)this.getY() / this.scale + 111.0f)
            );
            if(this.recipe.hasResultFluid()) {
                this.renderItemStack(pGuiGraphics, new ItemStack(this.recipe.getResultFluid().getFluid().getBucket(), this.recipe.getResultFluid().getAmount()),
                        (int) ((float) this.getX() / this.scale + 99f),
                        (int) ((float) this.getY() / this.scale + 111.0f)
                );
            }
            if(this.recipe.hasResultItem()) {
                this.renderItemStack(pGuiGraphics, this.recipe.getResultItem(),
                        (int) ((float) this.getX() / this.scale + 160f),
                        (int) ((float) this.getY() / this.scale + 111.0f)
                );
            }
        }
    }

    protected ResourceLocation backgroundTexture() {
        return TEXTURE;
    }

    public void init_internal(ResourceLocation pRecipeLocation) {
        Optional<? extends net.minecraft.world.item.crafting.Recipe<?>> pattern = this.minecraft.level.getRecipeManager().byKey(pRecipeLocation);
        if(pattern.isPresent() && pattern.get() instanceof VitriolationRecipe vr) {
            this.recipe = vr;
        }
    }

    public int getTier() {
        return 1;
    }
}
