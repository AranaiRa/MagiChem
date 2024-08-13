package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.InfusionStage;
import com.aranaira.magichem.interop.JEIPlugin;
import com.aranaira.magichem.recipe.SublimationRecipe;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
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
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SublimationRecipeCategory implements IRecipeCategory<SublimationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "sublimation");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_03.png");

    private IDrawable background;
    private final IDrawable icon;

    public SublimationRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 180, 211);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_PROCESS_SUBLIMATION.get()));
    }

    @Override
    public RecipeType<SublimationRecipe> getRecipeType() {
        return JEIPlugin.SUBLIMATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.sublimation");
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
    public void draw(SublimationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);

        int stages = recipe.getStages(false).size();
        int verticalShift = (int)(21f * (5 - stages));
        int padding = new int[]{43, 22, 0, 0, 0}[stages - 1];

        //Slot backgrounds
        {
            guiGraphics.blit(TEXTURE, 11, 13 + verticalShift + padding, 112, 243, 8, 13);
            guiGraphics.blit(TEXTURE, 21, 1 + verticalShift + padding, 22, 219, 90, 37);

            if (stages >= 2) {
                guiGraphics.blit(TEXTURE, 7, 56 + verticalShift + padding, 120, 243, 12, 13);
                guiGraphics.blit(TEXTURE, 21, 44 + verticalShift + padding, 22, 219, 90, 37);
            }

            if (stages >= 3) {
                guiGraphics.blit(TEXTURE, 5, 99 + verticalShift + padding, 132, 243, 14, 13);
                guiGraphics.blit(TEXTURE, 21, 87 + verticalShift + padding, 22, 219, 90, 37);
            }

            if (stages >= 4) {
                guiGraphics.blit(TEXTURE, 1, 142 + verticalShift + padding, 146, 243, 18, 13);
                guiGraphics.blit(TEXTURE, 21, 130 + verticalShift + padding, 22, 219, 90, 37);
            }

            if (stages >= 5) {
                guiGraphics.blit(TEXTURE, 4, 185 + verticalShift + padding, 164, 243, 15 , 13);
                guiGraphics.blit(TEXTURE, 21, 173 + verticalShift + padding, 22, 219, 90, 37);
            }
        }

        //Output area
        {
            guiGraphics.blit(TEXTURE, 135, 87 - verticalShift + padding, 0, 234, 22, 22);
            guiGraphics.blit(TEXTURE, 112, 112 - verticalShift + padding, 189, 0, 67, 83);
        }

        //Tier label; stolen from MnA code
        {
            Minecraft mc = Minecraft.getInstance();
            int tier = recipe.getTier();
            int playerTier = ((IPlayerProgression)mc.player.getCapability(PlayerProgressionProvider.PROGRESSION).resolve().get()).getTier();
            int color = tier <= playerTier ? FastColor.ARGB32.color(255, 0, 128, 0) : FastColor.ARGB32.color(255, 255, 0, 0);

            Component name = Component.translatable(recipe.getAlchemyObject().getDescriptionId().toString());
            Component tierPrompt = Component.translatable("gui.mna.item-tier", new Object[]{tier});

            int stringWidth = mc.font.width(name);
            int textX = this.getWidth() / 2 - stringWidth / 2;
            int textY = 2;

            if(recipe.getStages(false).size() == 5)
                guiGraphics.drawString(mc.font, tierPrompt, this.getWidth() / 2 - mc.font.width(tierPrompt) / 2, -12, color, false);
            else {
                guiGraphics.drawString(mc.font, name, textX, textY, FastColor.ARGB32.color(255, 255, 255, 255), false);
                guiGraphics.drawString(mc.font, tierPrompt, this.getWidth() / 2 - mc.font.width(tierPrompt) / 2, 12, color, false);
            }
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SublimationRecipe recipe, IFocusGroup group) {

        int stages = recipe.getStages(false).size();
        int verticalShift = (int)(21f * (5 - stages));
        int padding = new int[]{43, 22, 0, 0, 0}[stages - 1];

        builder.addSlot(RecipeIngredientRole.OUTPUT,138,90 - verticalShift + padding).addItemStack(recipe.getAlchemyObject());

        for(int si=0; si<stages; si++) {
            InfusionStage stage = recipe.getStages(false).get(si);

            for(int ci=0; ci<stage.componentItems.size(); ci++) {
                builder.addSlot(RecipeIngredientRole.INPUT, 22 + (ci * 18), 2 + (si * 43) + verticalShift + padding).addItemStack(stage.componentItems.get(ci));
            }

            for(int ci=0; ci<stage.componentMateria.size(); ci++) {
                builder.addSlot(RecipeIngredientRole.INPUT, 22 + (ci * 18), 21 + (si * 43) + verticalShift + padding).addItemStack(stage.componentMateria.get(ci));
            }
        }
//
//        int i=0;
//        for(ItemStack stack : recipe.getIngredientItemStacks()) {
//            builder.addSlot(RecipeIngredientRole.INPUT, 4 + i*18, 4).addItemStack(stack);
//            i++;
//        }
//
//        builder.addSlot(RecipeIngredientRole.INPUT, 76, 4).addItemStack(recipe.getComponentMateria().getFirst());
//        builder.addSlot(RecipeIngredientRole.INPUT, 76, 22).addItemStack(recipe.getComponentMateria().getSecond());
//
//        builder.addSlot(RecipeIngredientRole.INPUT,4,22).addItemStack(new ItemStack(ItemRegistry.SUBLIMATION_PRIMER.get(), 1));
    }

    @Override
    public List<Component> getTooltipStrings(SublimationRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {

        return IRecipeCategory.super.getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
    }
}
