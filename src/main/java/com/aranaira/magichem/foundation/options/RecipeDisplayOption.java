package com.aranaira.magichem.foundation.options;

import com.aranaira.magichem.foundation.enums.DistillationSourceCategory;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.DistillationFabricationRecipe;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.recipe.FluidDistillationFabricationRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import java.util.ArrayList;
import java.util.List;

public class RecipeDisplayOption {
    private DistillationFabricationRecipe itemRecipe;
    private FluidDistillationFabricationRecipe fluidRecipe;
    private FixationSeparationRecipe separationRecipe;

    public RecipeDisplayOption(DistillationFabricationRecipe pRecipe) {
        itemRecipe = pRecipe;
        fluidRecipe = null;
        separationRecipe = null;
    }

    public RecipeDisplayOption(FluidDistillationFabricationRecipe pRecipe) {
        itemRecipe = null;
        fluidRecipe = pRecipe;
        separationRecipe = null;
    }

    public RecipeDisplayOption(FixationSeparationRecipe pRecipe) {
        itemRecipe = null;
        fluidRecipe = null;
        separationRecipe = pRecipe;
    }

    public void draw(GuiGraphics pGui, int pX, int pY) {
        if(itemRecipe != null) {
            pGui.renderItem(itemRecipe.getAlchemyObject(), pX, pY);
//                pGui.renderItemDecorations(Minecraft.getInstance().font, itemRecipe.getAlchemyObject(), pX, pY);
        } else if(fluidRecipe != null) {
            IClientFluidTypeExtensions extension = IClientFluidTypeExtensions.of(fluidRecipe.getAlchemyFluid().getFluid());
            int packedTint = extension.getTintColor();
            float a = ((packedTint >> 24) & 0xff) / 255.0f;
            float r = ((packedTint >> 16) & 0xff) / 255.0f;
            float g = ((packedTint >> 8) & 0xff) / 255.0f;
            float b = ((packedTint) & 0xff) / 255.0f;
            pGui.setColor(r,g,b,a);
            ResourceLocation rl = new ResourceLocation(extension.getStillTexture().getNamespace(), "textures/"+extension.getStillTexture().getPath()+".png");
            pGui.blit(rl, pX, pY, 0, 0, 16, 16, 16, 16);
            pGui.setColor(1f,1f,1f,1f);
        } else if(separationRecipe != null) {
            pGui.renderItem(separationRecipe.getResultAdmixture(), pX, pY);
        }
    }

    public float getSortingFloat(MateriaItem pFilter) {
        if(itemRecipe != null) {
            for(ItemStack stackQuery : itemRecipe.getComponentMateria()) {
                if(stackQuery.getItem() == pFilter) return (float)stackQuery.getCount() * itemRecipe.getOutputRate();
            }
        } else if(fluidRecipe != null) {
            for(ItemStack stackQuery : fluidRecipe.getComponentMateria()) {
                if(stackQuery.getItem() == pFilter) return (float)stackQuery.getCount() * fluidRecipe.getOutputRate();
            }
        } else {
            for(ItemStack stackQuery : separationRecipe.getComponentMateria()) {
                if(stackQuery.getItem() == pFilter) return (float)stackQuery.getCount();
            }
        }
        return 0f;
    }

    public boolean isFluidRecipe() {
        return fluidRecipe != null && itemRecipe == null && separationRecipe == null;
    }

    public boolean isSeparationRecipe() {
        return separationRecipe != null && itemRecipe == null && fluidRecipe == null;
    }

    public float getOutputRate() {
        return itemRecipe != null ? itemRecipe.getOutputRate() :
                (fluidRecipe != null ? fluidRecipe.getOutputRate() : 1.0f);
    }

    public NonNullList<ItemStack> getComponentMateria() {
        return itemRecipe != null ? itemRecipe.getComponentMateria() :
                (fluidRecipe != null ? fluidRecipe.getComponentMateria() : separationRecipe.getComponentMateria());
    }

    public boolean hasSourceCategory(DistillationSourceCategory pCategory) {
        return itemRecipe != null ? itemRecipe.hasSourceCategory(pCategory) :
                (fluidRecipe != null && fluidRecipe.hasSourceCategory(pCategory));
    }

    public List<Component> getTooltipLines() {
        if(itemRecipe != null)
            return itemRecipe.getAlchemyObject().getTooltipLines(Minecraft.getInstance().player, TooltipFlag.NORMAL);
        else if(fluidRecipe != null) {
            List<Component> out = new ArrayList<>();
            out.add(Component.empty()
                    .append(Component.translatable(fluidRecipe.getAlchemyFluid().getTranslationKey()))
            );
            return out;
        } else {
            return separationRecipe.getResultAdmixture().getTooltipLines(Minecraft.getInstance().player, TooltipFlag.NORMAL);
        }
    }

    public DistillationFabricationRecipe getItemRecipe() {
        return itemRecipe;
    }

    public FluidDistillationFabricationRecipe getFluidRecipe() {
        return fluidRecipe;
    }

    public FixationSeparationRecipe getSeparationRecipe() {
        return separationRecipe;
    }

    public Recipe<SimpleContainer> getRecipe() {
        return itemRecipe != null ? itemRecipe : (fluidRecipe != null ? fluidRecipe : separationRecipe);
    }

    public String getSortingString() {
        return itemRecipe != null ? itemRecipe.getAlchemyObject().getDisplayName().getString() :
                (fluidRecipe != null ? fluidRecipe.getAlchemyFluid().getDisplayName().getString() :
                        separationRecipe.getResultAdmixture().getDisplayName().getString());
    }
}