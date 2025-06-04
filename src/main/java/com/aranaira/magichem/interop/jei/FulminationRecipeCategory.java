package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.enums.DistillationSourceCategory;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.DistillationFabricationRecipe;
import com.aranaira.magichem.recipe.FulminationRecipe;
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

public class FulminationRecipeCategory implements IRecipeCategory<FulminationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "fulmination");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_04.png");

    private final IDrawable background;
    private final IDrawable icon;

    public FulminationRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 96, 110);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_PROCESS_FULMINATION.get()));
    }

    @Override
    public RecipeType<FulminationRecipe> getRecipeType() {
        return JEIPlugin.FULMINATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.fulmination");
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
    public void setRecipe(IRecipeLayoutBuilder builder, FulminationRecipe recipe, IFocusGroup group) {
        builder.addSlot(RecipeIngredientRole.INPUT, 40, 4).addItemStack(recipe.getInput());
        builder.addSlot(RecipeIngredientRole.OUTPUT,40,88).addItemStack(recipe.getResult());
    }

    public void draw(FulminationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gui, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, gui, mouseX, mouseY);
    }
}
