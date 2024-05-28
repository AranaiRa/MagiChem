package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import com.aranaira.magichem.recipe.AlchemicalInfusionRitualRecipe;
import com.aranaira.magichem.registry.ItemRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SublimationRitualRecipeCategory implements IRecipeCategory<AlchemicalInfusionRitualRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "sublimation_ritual");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_02.png");

    private final IDrawable background;
    private final IDrawable icon;

    public SublimationRitualRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 96, 110);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_PROCESS_SUBLIMATION.get()));
    }

    @Override
    public RecipeType<AlchemicalInfusionRitualRecipe> getRecipeType() {
        return JEIPlugin.SUBLIMATION_RITUAL_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.sublimation_ritual");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AlchemicalInfusionRitualRecipe recipe, IFocusGroup group) {

        builder.addSlot(RecipeIngredientRole.OUTPUT,40,88).addItemStack(recipe.getAlchemyObject());

        int i=0;
        for(ItemStack stack : recipe.getIngredientItemStacks()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 4 + i*18, 4).addItemStack(stack);
            i++;
        }

        builder.addSlot(RecipeIngredientRole.INPUT, 76, 4).addItemStack(recipe.getComponentMateria().getFirst());
        builder.addSlot(RecipeIngredientRole.INPUT, 76, 22).addItemStack(recipe.getComponentMateria().getSecond());

        builder.addSlot(RecipeIngredientRole.INPUT,4,22).addItemStack(new ItemStack(ItemRegistry.SUBLIMATION_PRIMER.get(), 1));
    }

    @Override
    public List<Component> getTooltipStrings(AlchemicalInfusionRitualRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {

        return IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
    }
}
