package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
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

public class DistillationRecipeCategory implements IRecipeCategory<AlchemicalCompositionRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "distillation");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_01.png");

    private final IDrawable background;
    private final IDrawable icon;

    public DistillationRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 96, 110);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_PROCESS_DISTILLATION.get()));
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

    @Override
    public List<Component> getTooltipStrings(AlchemicalCompositionRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {

        return IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
    }

    public void draw(AlchemicalCompositionRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gui, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.font != null) {
            if(recipe.getOutputRate() < 1.0f) {
                String oRateFormatted = String.format("%.1f", recipe.getOutputRate()*100.0f);
                Component oRateComponent = Component.literal(oRateFormatted+"%");

                gui.drawString(mc.font, oRateComponent, 64, 34, 0x000000, false);
            }
        }
        IRecipeCategory.super.draw(recipe, recipeSlotsView, gui, mouseX, mouseY);
    }
}
