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
            this.renderItemStack(pGuiGraphics, new ItemStack(this.recipe.getInputItem().getItem()),
                    (int)((float)this.getX() / this.scale + 87.5f),
                    (int)((float)this.getY() / this.scale + 50.0f)
            );
            if(this.recipe.getInputItem().getCount() > 1) {
                pGuiGraphics.drawString(Minecraft.getInstance().font,
                        "x"+this.recipe.getInputItem().getCount(),
                        (int) ((float) this.getX() / this.scale + 82f),
                        (int) ((float) this.getY() / this.scale + 76.0f),
                        0xff000000, false
                );
            }

            if(this.recipe.hasInputFluidOverride()) {
                this.renderItemStack(pGuiGraphics, new ItemStack(this.recipe.getInputFluidOverride().getBucket()),
                        (int) ((float) this.getX() / this.scale + 115.5f),
                        (int) ((float) this.getY() / this.scale + 50.0f)
                );
                pGuiGraphics.drawString(Minecraft.getInstance().font,
                        this.recipe.getBaseFluidConsumed()+" mB",
                        (int) ((float) this.getX() / this.scale + 122f),
                        (int) ((float) this.getY() / this.scale + 76.0f),
                        0xff000000, false
                );
            }
            else if(this.recipe.getMinimumAcidStrength() > 0) {
                this.renderItemStack(pGuiGraphics, new ItemStack(VitriolationRecipe.getAllFluidsOfAcidStrength(this.recipe.getMinimumAcidStrength()).get(0).getBucket()),
                        (int) ((float) this.getX() / this.scale + 115f),
                        (int) ((float) this.getY() / this.scale + 50.0f)
                );
            }

            if(this.recipe.hasResultFluid()) {
                this.renderItemStack(pGuiGraphics, new ItemStack(this.recipe.getResultFluid().getFluid().getBucket()),
                        (int) ((float) this.getX() / this.scale + 160f),
                        (int) ((float) this.getY() / this.scale + 175.0f)
                );
                String out = this.recipe.getResultFluid().getAmount()+" mB";
                int width = Minecraft.getInstance().font.width(out);
                pGuiGraphics.drawString(Minecraft.getInstance().font,
                        out,
                        (int) ((float) this.getX() / this.scale + 168f - (width * 0.5f)),
                        (int) ((float) this.getY() / this.scale + 210.0f),
                        0xff000000, false
                );
            }
            if(this.recipe.hasResultItem()) {
                this.renderItemStack(pGuiGraphics, this.recipe.getResultItem(),
                        (int) ((float) this.getX() / this.scale + 43f),
                        (int) ((float) this.getY() / this.scale + 175.0f)
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
