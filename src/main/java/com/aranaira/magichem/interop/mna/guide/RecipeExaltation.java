package com.aranaira.magichem.interop.mna.guide;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.enums.LuminType;
import com.aranaira.magichem.recipe.ExaltationRecipe;
import com.aranaira.magichem.recipe.IlluminationRecipe;
import com.mna.api.affinity.Affinity;
import com.mna.api.guidebook.RecipeRendererBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class RecipeExaltation extends RecipeRendererBase {
    private ExaltationRecipe recipe;
    private static final ResourceLocation TEXTURE = new ResourceLocation(MagiChemMod.MODID, "textures/gui/guide/exaltation.png");
    private static final ResourceLocation TEXTURE_ELDRIN = new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_04.png");
    private static final ResourceLocation TEXTURE_SLURRY = new ResourceLocation(MagiChemMod.MODID, "textures/block/fluid/experience_still.png");

    public RecipeExaltation(int xIn, int yIn) {
        super(xIn, yIn);
    }

    protected void drawForeground(GuiGraphics pGuiGraphics, int pX, int pY, int pMouseX, int pMouseY, float pPartialTicks) {
        if(this.recipe != null) {
            final Font font = Minecraft.getInstance().font;

            this.renderItemStack(pGuiGraphics, this.recipe.getResultItem(),
                    (int)((float)this.getX() / this.scale + 100.0f),
                    (int)((float)this.getY() / this.scale + 120.0f)
            );

            this.renderItemStack(pGuiGraphics, this.recipe.getInputItemAsStack(),
                    (int)((float)this.getX() / this.scale + 19.0f),
                    (int)((float)this.getY() / this.scale + 78.0f)
            );
            pGuiGraphics.drawString(font, "x "+recipe.getItemsRequired(),
                    (int)((float)this.getX() / this.scale + 15.0f),
                    (int)((float)this.getY() / this.scale + 62.0f),
                    0x00000000, false
            );

            this.renderItemStack(pGuiGraphics, this.recipe.getMateriaTypeAsStack(),
                    (int)((float)this.getX() / this.scale + 183.0f),
                    (int)((float)this.getY() / this.scale + 78.0f)
            );
            String materiaCount = recipe.getMateriaRequired()+" x";
            pGuiGraphics.drawString(font, materiaCount,
                    (int)((float)this.getX() / this.scale + 205.0f) - font.width(materiaCount),
                    (int)((float)this.getY() / this.scale + 62.0f),
                    0x00000000, false
            );

            pGuiGraphics.blit(TEXTURE_SLURRY,
                    (int)((float)this.getX() / this.scale + 183.0f),
                    (int)((float)this.getY() / this.scale + 162.0f),
                    0, 0, 16, 16, 16, 16
            );
            String slurryCount = recipe.getSlurryRequired()+"mB";
            pGuiGraphics.drawString(font, slurryCount,
                    (int)((float)this.getX() / this.scale + 205.0f) - font.width(slurryCount),
                    (int)((float)this.getY() / this.scale + 188.0f),
                    0x00000000, false
            );

            if(recipe.usesEldrinType(Affinity.EARTH)) {
                pGuiGraphics.blit(TEXTURE_ELDRIN,
                        (int) ((float) this.getX() / this.scale + 19.0f),
                        (int) ((float) this.getY() / this.scale + 179.0f),
                        199, 56, 7, 7, 256, 256
                );
            }
            if(recipe.usesEldrinType(Affinity.ENDER)) {
                pGuiGraphics.blit(TEXTURE_ELDRIN,
                        (int) ((float) this.getX() / this.scale + 36.0f),
                        (int) ((float) this.getY() / this.scale + 179.0f),
                        192, 56, 7, 7, 256, 256
                );
            }
            if(recipe.usesEldrinType(Affinity.WATER)) {
                pGuiGraphics.blit(TEXTURE_ELDRIN,
                        (int) ((float) this.getX() / this.scale + 53.0f),
                        (int) ((float) this.getY() / this.scale + 179.0f),
                        206, 56, 7, 7, 256, 256
                );
            }
            if(recipe.usesEldrinType(Affinity.WIND)) {
                pGuiGraphics.blit(TEXTURE_ELDRIN,
                        (int) ((float) this.getX() / this.scale + 19.0f),
                        (int) ((float) this.getY() / this.scale + 162.0f),
                        213, 56, 7, 7, 256, 256
                );
            }
            if(recipe.usesEldrinType(Affinity.ARCANE)) {
                pGuiGraphics.blit(TEXTURE_ELDRIN,
                        (int) ((float) this.getX() / this.scale + 36.0f),
                        (int) ((float) this.getY() / this.scale + 162.0f),
                        227, 56, 7, 7, 256, 256
                );
            }
            if(recipe.usesEldrinType(Affinity.FIRE)) {
                pGuiGraphics.blit(TEXTURE_ELDRIN,
                        (int) ((float) this.getX() / this.scale + 53.0f),
                        (int) ((float) this.getY() / this.scale + 162.0f),
                        220, 56, 7, 7, 256, 256
                );
            }
            pGuiGraphics.drawString(font, "x "+recipe.getEldrinRequired(),
                    (int)((float)this.getX() / this.scale + 15.0f),
                    (int)((float)this.getY() / this.scale + 195.0f),
                    0x00000000, false
            );
        }
    }

    protected ResourceLocation backgroundTexture() {
        return TEXTURE;
    }

    public void init_internal(ResourceLocation pRecipeLocation) {
        Optional<? extends net.minecraft.world.item.crafting.Recipe<?>> pattern = this.minecraft.level.getRecipeManager().byKey(pRecipeLocation);
        if(pattern.isPresent() && pattern.get() instanceof ExaltationRecipe er) {
            this.recipe = er;
        }
    }

    public int getTier() {
        return recipe.getTier();
    }
}
