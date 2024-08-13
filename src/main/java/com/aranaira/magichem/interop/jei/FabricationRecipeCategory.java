package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.DistillationFabricationRecipe;
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

public class FabricationRecipeCategory implements IRecipeCategory<DistillationFabricationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "fabrication");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_01.png");

    private final IDrawable background;
    private final IDrawable icon;

    public FabricationRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 96, 0, 96, 110);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_PROCESS_FABRICATION.get()));
    }

    @Override
    public RecipeType<DistillationFabricationRecipe> getRecipeType() {
        return JEIPlugin.FABRICATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.fabrication");
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
    public void setRecipe(IRecipeLayoutBuilder builder, DistillationFabricationRecipe recipe, IFocusGroup group) {

        builder.addSlot(RecipeIngredientRole.OUTPUT,40,88).addItemStack(recipe.getAlchemyObject());

        int i=0;
        for(ItemStack stack : recipe.getComponentMateria()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 4 + i*18, 4).addItemStack(stack);
            i++;
        }
    }
}
