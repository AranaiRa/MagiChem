package com.aranaira.magichem.interop.mna.guide;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.InfusionStage;
import com.aranaira.magichem.recipe.AnointingRecipe;
import com.aranaira.magichem.recipe.SublimationRecipe;
import com.mna.api.guidebook.RecipeRendererBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class RecipeAnointing extends RecipeRendererBase {
    private AnointingRecipe recipe;
    private static final ResourceLocation TEXTURE = new ResourceLocation(MagiChemMod.MODID, "textures/gui/guide/anointing.png");

    public RecipeAnointing(int xIn, int yIn) {
        super(xIn, yIn);
    }

    protected void drawForeground(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY, float pPartialTicks) {
        if(this.recipe != null) {
            this.renderItemStack(pGuiGraphics, new ItemStack(this.recipe.getTarget()),
                    (int)((float)this.getX() / this.scale + 43f),
                    (int)((float)this.getY() / this.scale + 111.0f)
            );
            this.renderItemStack(pGuiGraphics, new ItemStack(this.recipe.getMateria()),
                    (int)((float)this.getX() / this.scale + 99f),
                    (int)((float)this.getY() / this.scale + 111.0f)
            );
            this.renderItemStack(pGuiGraphics, new ItemStack(this.recipe.getResult()),
                    (int)((float)this.getX() / this.scale + 160f),
                    (int)((float)this.getY() / this.scale + 111.0f)
            );


            String cRateFormatted = String.format("%.1f", recipe.getChance());
            Component cRateComponent = Component.literal(cRateFormatted+"%");

            pGuiGraphics.drawString(Minecraft.getInstance().font, cRateComponent,
                    (int)((float)this.getX() / this.scale + 108f) - Minecraft.getInstance().font.width(cRateComponent) / 2,
                    (int)((float)this.getY() / this.scale + 140.0f),
                    0x555555, false);
        }
    }

    protected ResourceLocation backgroundTexture() {
        return TEXTURE;
    }

    public void init_internal(ResourceLocation pRecipeLocation) {
        Optional<? extends net.minecraft.world.item.crafting.Recipe<?>> pattern = this.minecraft.level.getRecipeManager().byKey(pRecipeLocation);
        if(pattern.isPresent() && pattern.get() instanceof AnointingRecipe ar) {
            this.recipe = ar;
        }
    }

    public int getTier() {
        return 1;
    }
}
