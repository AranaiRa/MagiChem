package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.InfoPanelRecipe;
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

public class InfoPanelRecipeCategory implements IRecipeCategory<InfoPanelRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "info_panel");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_03.png");
    private static final int CHARACTER_PER_LINE_LIMIT = 33;

    private final IDrawable background;
    private final IDrawable icon;

    public InfoPanelRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 180, 211);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_INFO_PANEL.get()));
    }

    @Override
    public RecipeType<InfoPanelRecipe> getRecipeType() {
        return JEIPlugin.INFO_PANEL_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.info_panel");
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
    public void setRecipe(IRecipeLayoutBuilder iRecipeLayoutBuilder, InfoPanelRecipe recipe, IFocusGroup iFocusGroup) {
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.OUTPUT, 78, 7).addItemStack(recipe.getItem());
        iRecipeLayoutBuilder.addSlot(RecipeIngredientRole.INPUT, 78, -10357).addItemStack(recipe.getItem());
    }

    @Override
    public List<Component> getTooltipStrings(InfoPanelRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        return IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
    }

    @Override
    public void draw(InfoPanelRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        //Item slot
        guiGraphics.blit(TEXTURE, 75, 4, 0, 234, 22, 22);

        Minecraft mc = Minecraft.getInstance();
        if (mc.font != null) {
            Component infoText = Component.translatable(recipe.getInfo());
            final String[] s = infoText.getString().split(" ");

            int lineOffset = 0, index = 0;
            while(index < s.length) {
                String thisLine = "";
                boolean lineFormed = false;
                boolean spaceNextLine = false;
                while(!lineFormed) {
                    if(s[index].equals("\n")) {
                        index++;
                        lineFormed = true;
                        spaceNextLine = true;
                    } else {
                        if (thisLine.length() > 0) thisLine += " ";
                        thisLine += s[index];
                        index++;
                        if (index >= s.length || thisLine.length() + 1 + s[index].length() > CHARACTER_PER_LINE_LIMIT)
                            lineFormed = true;
                    }
                }
                guiGraphics.drawString(mc.font, thisLine, 90 - mc.font.width(thisLine) / 2, 32 + lineOffset, 0x000000, false);
                lineOffset += spaceNextLine ? 17 : 11;
            }

        }
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
    }
}
