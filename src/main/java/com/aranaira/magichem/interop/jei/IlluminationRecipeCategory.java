package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.enums.LuminType;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.IlluminationRecipe;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class IlluminationRecipeCategory implements IRecipeCategory<IlluminationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "illumination");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_04.png");

    private final IDrawable background;
    private final IDrawable icon;

    public IlluminationRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 96, 110, 96, 110);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_ILLUMINATION.get()));
    }

    @Override
    public RecipeType<IlluminationRecipe> getRecipeType() {
        return JEIPlugin.ILLUMINATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.illumination");
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
    public void setRecipe(IRecipeLayoutBuilder builder, IlluminationRecipe recipe, IFocusGroup group) {
        builder.addSlot(RecipeIngredientRole.INPUT, 40, 4).addItemStack(recipe.getInputItem());
        builder.addSlot(RecipeIngredientRole.OUTPUT,40,88).addItemStack(recipe.getResultItem());
    }

    public void draw(IlluminationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gui, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, gui, mouseX, mouseY);

        gui.blit(TEXTURE, 13, 3, 192, 110 + (recipe.getLuminType().ordinal() - 1) * 14,14, 14);
        final Font font = Minecraft.getInstance().font;

        int opTime = recipe.getCraftTime();
        int hourWhole = opTime / 60;
        int minWhole = opTime % 60;

        String time = "";
        if(hourWhole > 0) {
            time = hourWhole + "h";
        }
        if(hourWhole > 0 && minWhole > 0) {
            time += " ";
        }
        if(minWhole > 0) {
            time += minWhole + "m";
        }
        gui.drawString(font, time, 21 - font.width(time) / 2, 21, 0xff000000, false);
    }

    @Override
    public List<Component> getTooltipStrings(IlluminationRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> in = IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
        ArrayList<Component> out = new ArrayList<>();

        boolean xCoord = mouseX >= 13 && mouseX <= 27;
        boolean yCoord = mouseY >= 3  && mouseY <= 17;
        if(in.size() > 0) {
            for (Object o : in.stream().toArray()) {
                if (o instanceof Component c) {
                    out.add(c);
                }
            }
        }

        if(xCoord && yCoord) {
            if(recipe.getLuminType() == LuminType.SOLAR) {
                out.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.jei.illumination.lumins"))
                        .append(Component.translatable("tooltip.magichem.jei.illumination.lumins.solar").withStyle(ChatFormatting.GOLD))
                );
            }
            else if(recipe.getLuminType() == LuminType.LUNAR) {
                out.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.jei.illumination.lumins"))
                        .append(Component.translatable("tooltip.magichem.jei.illumination.lumins.lunar").withStyle(ChatFormatting.GOLD))
                );
            }
            else if(recipe.getLuminType() == LuminType.SIDEREAL) {
                out.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.jei.illumination.lumins"))
                        .append(Component.translatable("tooltip.magichem.jei.illumination.lumins.sidereal").withStyle(ChatFormatting.GOLD))
                );
            }
        }

        return out.stream().toList();
    }
}
