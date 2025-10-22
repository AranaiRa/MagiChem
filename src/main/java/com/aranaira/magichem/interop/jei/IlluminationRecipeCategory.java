package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.config.ServerConfig;
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
import org.apache.logging.log4j.core.jmx.Server;

import java.util.ArrayList;
import java.util.List;

public class IlluminationRecipeCategory implements IRecipeCategory<IlluminationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "illumination");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_04.png");

    private final IDrawable background;
    private final IDrawable icon;

    private static ItemStack
        LENS_GLASS = null,
        LENS_CLOISTER_SOLAR = null, LENS_CLOISTER_LUNAR = null, LENS_CLOISTER_SIDEREAL = null,
        LENS_FARSIGHT_SOLAR = null, LENS_FARSIGHT_LUNAR = null, LENS_FARSIGHT_SIDEREAL = null;

    public IlluminationRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 96, 110, 96, 110);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_ILLUMINATION.get()));

        if(LENS_GLASS == null) LENS_GLASS = new ItemStack(ItemRegistry.GLASS_LENS.get());
        if(LENS_CLOISTER_SOLAR == null) LENS_CLOISTER_SOLAR = new ItemStack(ItemRegistry.SOLAR_CLOISTER_LENS.get());
        if(LENS_CLOISTER_LUNAR == null) LENS_CLOISTER_LUNAR = new ItemStack(ItemRegistry.LUNAR_CLOISTER_LENS.get());
        if(LENS_CLOISTER_SIDEREAL == null) LENS_CLOISTER_SIDEREAL = new ItemStack(ItemRegistry.SIDEREAL_CLOISTER_LENS.get());
        if(LENS_FARSIGHT_SOLAR == null) LENS_FARSIGHT_SOLAR = new ItemStack(ItemRegistry.SOLAR_FARSIGHT_LENS.get());
        if(LENS_FARSIGHT_LUNAR == null) LENS_FARSIGHT_LUNAR = new ItemStack(ItemRegistry.LUNAR_FARSIGHT_LENS.get());
        if(LENS_FARSIGHT_SIDEREAL == null) LENS_FARSIGHT_SIDEREAL = new ItemStack(ItemRegistry.SIDEREAL_FARSIGHT_LENS.get());
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

        gui.blit(TEXTURE, 13, 5, 192, 110 + (recipe.getLuminType().ordinal() - 1) * 14,14, 14);
        final Font font = Minecraft.getInstance().font;

        long loop = Minecraft.getInstance().level.getGameTime() % 180;
        int opTime = recipe.getCraftTime();

        if(loop >= 120) {
            gui.renderItem(recipe.getLuminType() == LuminType.SOLAR ? LENS_FARSIGHT_SOLAR : recipe.getLuminType() == LuminType.LUNAR ? LENS_FARSIGHT_LUNAR : LENS_FARSIGHT_SIDEREAL, 70, 4);
            opTime = Math.round((float)opTime / ((float)ServerConfig.astralObserverLuminGainFarsight / (float)ServerConfig.astralObserverLuminGainStandard));
        } else if(loop >= 60) {
            gui.renderItem(recipe.getLuminType() == LuminType.SOLAR ? LENS_CLOISTER_SOLAR : recipe.getLuminType() == LuminType.LUNAR ? LENS_CLOISTER_LUNAR : LENS_CLOISTER_SIDEREAL, 70, 4);
            opTime = Math.round((float)opTime / ((float)ServerConfig.astralObserverLuminGainCloister / (float)ServerConfig.astralObserverLuminGainStandard));
        } else {
            gui.renderItem(LENS_GLASS, 70, 4);
        }
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
        gui.drawString(font, time, 79 - font.width(time) / 2, 24, 0xff000000, false);
    }

    @Override
    public List<Component> getTooltipStrings(IlluminationRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        List<Component> in = IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
        ArrayList<Component> out = new ArrayList<>();

        boolean xCoord = mouseX >= 13 && mouseX <= 27;
        boolean yCoord = mouseY >= 5  && mouseY <= 19;
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
