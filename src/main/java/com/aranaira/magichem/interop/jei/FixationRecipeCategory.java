package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class FixationRecipeCategory implements IRecipeCategory<FixationSeparationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "fixation");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_01.png");

    private final IDrawable background;
    private final IDrawable icon;

    public FixationRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 96, 110, 96, 110);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_PROCESS_FIXATION.get()));
    }

    @Override
    public RecipeType<FixationSeparationRecipe> getRecipeType() {
        return JEIPlugin.FIXATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.fixation");
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

        builder.addSlot(RecipeIngredientRole.OUTPUT,40,88).addItemStack(recipe.getResultAdmixture());
        builder.addSlot(RecipeIngredientRole.INPUT, 4096, 4096).addFluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), recipe.getSlurryCost());

        int i=0;
        for(ItemStack stack : recipe.getComponentMateria()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 4 + i*18, 4).addItemStack(stack);
            i++;
        }
    }

    @Override
    public void draw(FixationSeparationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gui, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, gui, mouseX, mouseY);
        final Font font = Minecraft.getInstance().font;

        String slurryText = recipe.getSlurryCost() + "";
        gui.drawString(font, slurryText, 94 - font.width(slurryText), 33, 0xff000000, false);
        gui.drawString(font, "mB", 94 - font.width("mB"), 43, 0xff000000, false);
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, FixationSeparationRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        boolean xCoord = mouseX >= 86 && mouseX <= 94;
        boolean yCoord = mouseY >= 24 && mouseY <= 32;
        if(xCoord && yCoord) {
            tooltip.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.jei.exaltation.slurry.part1"))
                    .append(Component.translatable("tooltip.magichem.jei.exaltation.slurry.part2").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("tooltip.magichem.jei.exaltation.slurry.part3"))
            );
        }
    }
}
