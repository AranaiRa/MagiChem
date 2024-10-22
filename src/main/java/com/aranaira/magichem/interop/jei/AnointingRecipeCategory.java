package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.AnointingRecipe;
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

public class AnointingRecipeCategory implements IRecipeCategory<AnointingRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "anointing");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_02.png");

    private final IDrawable background;
    private final IDrawable icon;

    public AnointingRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 96, 189, 96, 60);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_ANOINTING.get()));
    }

    @Override
    public RecipeType<AnointingRecipe> getRecipeType() {
        return JEIPlugin.ANOINTING_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.anointing");
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
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, AnointingRecipe anointingRecipe, IFocusGroup iFocusGroup) {
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 7, 27).addItemStack(new ItemStack(anointingRecipe.getTarget()));
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 40, 11).addItemStack(new ItemStack(anointingRecipe.getMateria()));
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 73, 27).addItemStack(new ItemStack(anointingRecipe.getResult()));
    }

    @Override
    public List<Component> getTooltipStrings(AnointingRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        return IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
    }

    @Override
    public void draw(AnointingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.font != null) {
            if(recipe.getChance() < 100.0f) {
                String cRateFormatted = String.format("%.1f", recipe.getChance());
                Component cRateComponent = Component.literal(cRateFormatted+"%");

                guiGraphics.drawString(mc.font, cRateComponent, 48 - mc.font.width(cRateComponent) / 2, 46, 0x000000, false);
            }
        }
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
    }
}
