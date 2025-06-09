package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.VitriolationRecipe;
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

public class VitriolationRecipeCategory implements IRecipeCategory<VitriolationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "vitriolation");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_04.png");

    private final IDrawable background;
    private final IDrawable icon;

    public VitriolationRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 96, 0, 96, 110);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_VITRIOLATION.get()));
    }

    @Override
    public RecipeType<VitriolationRecipe> getRecipeType() {
        return JEIPlugin.VITRIOLATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.vitriolation");
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
    public void setRecipe(IRecipeLayoutBuilder builder, VitriolationRecipe recipe, IFocusGroup group) {
        builder.addSlot(RecipeIngredientRole.INPUT, 40, 4).addItemStack(recipe.getInputItem());
        builder.addSlot(RecipeIngredientRole.OUTPUT,74,88).addItemStack(recipe.getResultItem());
        builder.addSlot(RecipeIngredientRole.OUTPUT,6,88).addFluidStack(recipe.getResultFluid().getFluid(), recipe.getResultFluid().getAmount());
    }

    public void draw(VitriolationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gui, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, gui, mouseX, mouseY);
        Minecraft mc = Minecraft.getInstance();

        if(recipe.hasResultFluid())
            gui.blit(TEXTURE, 0, 82, 192, 0, 48, 28);
        if(recipe.hasResultItem())
            gui.blit(TEXTURE, 48, 82, 192, 28, 48, 28);

        if (mc.font != null) {
            gui.drawString(mc.font, ""+recipe.getMinimumAcidStrength(), 20, 8, 0xff000000, false);
        }
    }
}
