package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.ConstructStudyMaterialRecipe;
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

public class ConstructStudyMaterialRecipeCategory implements IRecipeCategory<ConstructStudyMaterialRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "construct_study_material");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_04.png");

    private final IDrawable background;
    private final IDrawable icon;

    public ConstructStudyMaterialRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 192, 164, 64, 92);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_CONSTRUCT_STUDY_MATERIAL.get()));
    }

    @Override
    public RecipeType<ConstructStudyMaterialRecipe> getRecipeType() {
        return JEIPlugin.CONSTRUCT_STUDY_MATERIAL_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.construct_study_material");
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
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, ConstructStudyMaterialRecipe ConstructStudyMaterialRecipe, IFocusGroup iFocusGroup) {
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 24, 5).addItemStack(new ItemStack(ConstructStudyMaterialRecipe.getItem()));
    }

    @Override
    public List<Component> getTooltipStrings(ConstructStudyMaterialRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        return IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
    }

    @Override
    public void draw(ConstructStudyMaterialRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.font != null) {
            Component xpComponent = Component.literal(recipe.getExperience() + " xp");

            guiGraphics.drawString(mc.font, xpComponent, 32 - mc.font.width(xpComponent) / 2, 77, 0x27753b, false);
        }
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
    }
}
