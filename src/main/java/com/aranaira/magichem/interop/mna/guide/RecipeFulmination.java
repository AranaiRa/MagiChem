package com.aranaira.magichem.interop.mna.guide;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.recipe.AnointingRecipe;
import com.aranaira.magichem.recipe.FulminationRecipe;
import com.mna.api.guidebook.RecipeRendererBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class RecipeFulmination extends RecipeRendererBase {
    private FulminationRecipe recipe;
    private static final ResourceLocation TEXTURE = new ResourceLocation(MagiChemMod.MODID, "textures/gui/guide/fulmination.png");

    public RecipeFulmination(int xIn, int yIn) {
        super(xIn, yIn);
    }

    protected void drawForeground(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY, float pPartialTicks) {
        if(this.recipe != null) {
            this.renderItemStack(pGuiGraphics, this.recipe.getInput(),
                    (int)((float)this.getX() / this.scale + 102f),
                    (int)((float)this.getY() / this.scale + 40.0f)
            );
            this.renderItemStack(pGuiGraphics, this.recipe.getResult(),
                    (int)((float)this.getX() / this.scale + 102f),
                    (int)((float)this.getY() / this.scale + 194.0f)
            );
        }
    }

    protected ResourceLocation backgroundTexture() {
        return TEXTURE;
    }

    public void init_internal(ResourceLocation pRecipeLocation) {
        Optional<? extends net.minecraft.world.item.crafting.Recipe<?>> pattern = this.minecraft.level.getRecipeManager().byKey(pRecipeLocation);
        if(pattern.isPresent() && pattern.get() instanceof FulminationRecipe fr) {
            this.recipe = fr;
        }
    }

    public int getTier() {
        return 1;
    }
}
