package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.ColorationRecipe;
import com.aranaira.magichem.recipe.DistillationFabricationRecipe;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.List;

public class ColorationRecipeCategory implements IRecipeCategory<ColorationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "coloration");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_02.png");

    public static final HashMap<DyeColor, Triplet<ItemStack, Vector2i, Vector2i>> colorAndPosData = new HashMap();

    private final IDrawable background;
    private final IDrawable icon;

    public ColorationRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 96, 0, 146, 189);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_PROCESS_COLORATION.get()));
        generateDataIfNotPresent();
    }

    private void generateDataIfNotPresent() {
        if(colorAndPosData.size() == 0) {
            colorAndPosData.put(DyeColor.RED, new Triplet<>(new ItemStack(Items.RED_DYE), new Vector2i(42, 1), new Vector2i(42, 19)));
            colorAndPosData.put(DyeColor.ORANGE, new Triplet<>(new ItemStack(Items.ORANGE_DYE), new Vector2i(65, 1), new Vector2i(65, 19)));
            colorAndPosData.put(DyeColor.YELLOW, new Triplet<>(new ItemStack(Items.YELLOW_DYE), new Vector2i(88, 1), new Vector2i(88, 19)));

            colorAndPosData.put(DyeColor.LIME, new Triplet<>(new ItemStack(Items.LIME_DYE), new Vector2i(129, 42), new Vector2i(111, 42)));
            colorAndPosData.put(DyeColor.GREEN, new Triplet<>(new ItemStack(Items.GREEN_DYE), new Vector2i(129, 65), new Vector2i(111, 65)));
            colorAndPosData.put(DyeColor.CYAN, new Triplet<>(new ItemStack(Items.CYAN_DYE), new Vector2i(129, 88), new Vector2i(111, 88)));

            colorAndPosData.put(DyeColor.LIGHT_BLUE, new Triplet<>(new ItemStack(Items.LIGHT_BLUE_DYE), new Vector2i(88, 129), new Vector2i(88, 111)));
            colorAndPosData.put(DyeColor.BLUE, new Triplet<>(new ItemStack(Items.BLUE_DYE), new Vector2i(65, 129), new Vector2i(65, 111)));
            colorAndPosData.put(DyeColor.PURPLE, new Triplet<>(new ItemStack(Items.PURPLE_DYE), new Vector2i(42, 129), new Vector2i(42, 111)));

            colorAndPosData.put(DyeColor.BROWN, new Triplet<>(new ItemStack(Items.BROWN_DYE), new Vector2i(1, 42), new Vector2i(19, 42)));
            colorAndPosData.put(DyeColor.PINK, new Triplet<>(new ItemStack(Items.PINK_DYE), new Vector2i(1, 65), new Vector2i(19, 65)));
            colorAndPosData.put(DyeColor.MAGENTA, new Triplet<>(new ItemStack(Items.MAGENTA_DYE), new Vector2i(1, 88), new Vector2i(19, 88)));

            colorAndPosData.put(DyeColor.WHITE, new Triplet<>(new ItemStack(Items.WHITE_DYE), new Vector2i(1, 19), new Vector2i(19, 19)));
            colorAndPosData.put(DyeColor.LIGHT_GRAY, new Triplet<>(new ItemStack(Items.LIGHT_GRAY_DYE), new Vector2i(129, 19), new Vector2i(111, 19)));
            colorAndPosData.put(DyeColor.GRAY, new Triplet<>(new ItemStack(Items.GRAY_DYE), new Vector2i(1, 111), new Vector2i(19, 111)));
            colorAndPosData.put(DyeColor.BLACK, new Triplet<>(new ItemStack(Items.BLACK_DYE), new Vector2i(129, 111), new Vector2i(111, 111)));
        }
    }

    @Override
    public RecipeType<ColorationRecipe> getRecipeType() {
        return JEIPlugin.COLORATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.coloration");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ColorationRecipe recipe, IFocusGroup group) {
        HashMap<DyeColor, ItemStack> resultsMap = recipe.getResultsAsMap(false);
        boolean defaultMatchesOtherOutput = false;

        for(DyeColor color : DyeColor.values()) {
            Vector2i slotPos = colorAndPosData.get(color).getThird();
            ItemStack stack = resultsMap.get(color);

            if(stack != null){
                if (!stack.isEmpty()) {
                    builder.addSlot(RecipeIngredientRole.INPUT, slotPos.x, slotPos.y).addItemStack(stack);
                    builder.addSlot(RecipeIngredientRole.OUTPUT, slotPos.x, slotPos.y - 360000).addItemStack(stack);
                }

                if (stack.getItem() == recipe.getColorlessDefault().getItem()) {
                    defaultMatchesOtherOutput = true;
                }
            }
        }

        if(!defaultMatchesOtherOutput) {
            builder.addSlot(RecipeIngredientRole.INPUT, 65, 154).addItemStack(recipe.getColorlessDefault());
            builder.addSlot(RecipeIngredientRole.OUTPUT, 65, 154 - 360000).addItemStack(recipe.getColorlessDefault());
        }
    }

    @Override
    public List<Component> getTooltipStrings(ColorationRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {

        return IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
    }

    public void draw(ColorationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gui, double mouseX, double mouseY) {

        for(DyeColor color : DyeColor.values()) {
            ItemStack dyeStack = colorAndPosData.get(color).getFirst();
            Vector2i dyePos = colorAndPosData.get(color).getSecond();

            gui.renderItem(dyeStack, dyePos.x, dyePos.y);
        }

        IRecipeCategory.super.draw(recipe, recipeSlotsView, gui, mouseX, mouseY);
    }
}
