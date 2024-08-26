package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.ColorationRecipe;
import com.aranaira.magichem.recipe.ConjurationRecipe;
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.List;

public class ConjurationRecipeCategory implements IRecipeCategory<ConjurationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "conjuration");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_02.png");

    public static final HashMap<DyeColor, Triplet<ItemStack, Vector2i, Vector2i>> colorAndPosData = new HashMap();

    private final IDrawable background;
    private final IDrawable icon;

    public ConjurationRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 48, 16);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_PROCESS_CONJURATION.get()));
    }

    @Override
    public RecipeType<ConjurationRecipe> getRecipeType() {
        return JEIPlugin.CONJURATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.conjuration");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ConjurationRecipe recipe, IFocusGroup group) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 0, 0).addItemStack(recipe.getPassiveData(false).getFirst());
        builder.addSlot(RecipeIngredientRole.INPUT, 16, 0).addItemStack(new ItemStack(recipe.getMateria()));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 32, 0).addItemStack(recipe.getSuppliedData(false).getFirst());
    }

    @Override
    public List<Component> getTooltipStrings(ConjurationRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {

        return IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
    }

    public void draw(ConjurationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gui, double mouseX, double mouseY) {

        IRecipeCategory.super.draw(recipe, recipeSlotsView, gui, mouseX, mouseY);
    }
}
