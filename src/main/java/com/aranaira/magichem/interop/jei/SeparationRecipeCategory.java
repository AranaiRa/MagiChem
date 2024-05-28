package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class SeparationRecipeCategory implements IRecipeCategory<FixationSeparationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "separation");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_01.png");

    private final IDrawable background;
    private final IDrawable icon;

    public SeparationRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 110, 96, 110);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_PROCESS_SEPARATION.get()));
    }

    @Override
    public RecipeType<FixationSeparationRecipe> getRecipeType() {
        return JEIPlugin.SEPARATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.separation");
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
    public void setRecipe(IRecipeLayoutBuilder builder, FixationSeparationRecipe recipe, IFocusGroup group) {

        builder.addSlot(RecipeIngredientRole.INPUT,40,6).addItemStack(recipe.getResultAdmixture());

        int i=0;
        for(ItemStack stack : recipe.getComponentMateria()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 4 + i*18, 90).addItemStack(stack);
            i++;
        }
    }
}
