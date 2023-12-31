package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import com.aranaira.magichem.registry.BlockRegistry;
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

public class DistillationRecipeCategory implements IRecipeCategory<AlchemicalCompositionRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "distillation");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/alchemical_composition.png");

    private final IDrawable background;
    private final IDrawable icon;

    public DistillationRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 96, 110);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockRegistry.ALEMBIC.get()));
    }

    @Override
    public RecipeType<AlchemicalCompositionRecipe> getRecipeType() {
        return JEIPlugin.DISTILLATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.distillation");
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
    public void setRecipe(IRecipeLayoutBuilder builder, AlchemicalCompositionRecipe recipe, IFocusGroup group) {

        builder.addSlot(RecipeIngredientRole.INPUT,40,6).addItemStack(recipe.getAlchemyObject());

        int i=0;
        for(ItemStack stack : recipe.getComponentMateria()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 4 + i*18, 90).addItemStack(stack);
            i++;
        }
    }
}
