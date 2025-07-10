package com.aranaira.magichem.interop.jei;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.enums.DistillationSourceCategory;
import com.aranaira.magichem.interop.JEIPlugin;
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
import net.minecraft.world.item.ItemStack;

public class FabricationRecipeCategory implements IRecipeCategory<DistillationFabricationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(MagiChemMod.MODID, "fabrication");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/jei/jei_recipecategory_01.png");

    private final IDrawable background;
    private final IDrawable icon;

    public FabricationRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 96, 0, 96, 110);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ItemRegistry.DUMMY_PROCESS_FABRICATION.get()));
    }

    @Override
    public RecipeType<DistillationFabricationRecipe> getRecipeType() {
        return JEIPlugin.FABRICATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.magichem.fabrication");
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
    public void setRecipe(IRecipeLayoutBuilder builder, DistillationFabricationRecipe recipe, IFocusGroup group) {

        builder.addSlot(RecipeIngredientRole.OUTPUT,40,88).addItemStack(recipe.getAlchemyObject());

        int i=0;
        for(ItemStack stack : recipe.getComponentMateria()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 4 + i*18, 4).addItemStack(stack);
            i++;
        }

        if(recipe.getWisdom() < 6 && recipe.getWisdom() > 0) {
            builder.addSlot(RecipeIngredientRole.CATALYST, 4, 22).addItemStack(getStackForWisdom(recipe.getWisdom()));
        }
    }

    public void draw(DistillationFabricationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gui, double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.font != null) {
            if(recipe.getOutputRate() < 1.0f) {
                int amt = (int)Math.round(1f / recipe.getOutputRate());

                Component oRateComponent = Component.literal("x"+amt);

                gui.drawString(mc.font, oRateComponent, 62, 91, 0x000000, false);
            }

            int offset = 0;
            for(DistillationSourceCategory dsc : recipe.getSourceCategories()) {
                gui.drawString(mc.font, dsc.name(), -120, offset, 0xffffff, true);
                offset += 12;
            }

            offset += 12;
            if(recipe.isAdvancementRequired()) {
                gui.drawString(mc.font, "Needs Advancement:", -120, offset, 0xffffff, true);
                offset += 12;
                gui.drawString(mc.font, recipe.getRequiredAdvancement().getNamespace()+":"+recipe.getRequiredAdvancement().getPath(), -120, offset, 0xffffff, true);
            }

            offset += recipe.isAdvancementRequired() ? 12 : 0;
            if(recipe.isForbiddenByAdvancement()) {
                gui.drawString(mc.font, "Removed by Advancement:", -120, offset, 0xffffff, true);
                offset += 12;
                gui.drawString(mc.font, recipe.getForbiddenAdvancement().getNamespace()+":"+recipe.getForbiddenAdvancement().getPath(), -120, offset, 0xffffff, true);
            }
        }

        IRecipeCategory.super.draw(recipe, recipeSlotsView, gui, mouseX, mouseY);
    }

    private static ItemStack
            ASHEN = ItemStack.EMPTY,
            BLEACHED = ItemStack.EMPTY,
            YELLOWED = ItemStack.EMPTY,
            FLUSHED = ItemStack.EMPTY,
            PHILOSOPHERS = ItemStack.EMPTY;
    public static ItemStack getStackForWisdom(int pWisdom) {
        if(pWisdom == 1) {
            if(ASHEN.isEmpty()) {
                ASHEN = new ItemStack(ItemRegistry.ASHEN_WISDOM_STONE.get());
            }
            return ASHEN;
        }
        else if(pWisdom == 2) {
            if(BLEACHED.isEmpty()) {
                BLEACHED = new ItemStack(ItemRegistry.BLEACHED_WISDOM_STONE.get());
            }
            return BLEACHED;
        }
        else if(pWisdom == 3) {
            if(YELLOWED.isEmpty()) {
                YELLOWED = new ItemStack(ItemRegistry.YELLOWED_WISDOM_STONE.get());
            }
            return YELLOWED;
        }
        else if(pWisdom == 4) {
            if(FLUSHED.isEmpty()) {
                FLUSHED = new ItemStack(ItemRegistry.FLUSHED_WISDOM_STONE.get());
            }
            return FLUSHED;
        }
        else if(pWisdom == 5) {
            if(PHILOSOPHERS.isEmpty()) {
                PHILOSOPHERS = new ItemStack(ItemRegistry.PHILOSOPHERS_STONE.get());
            }
            return PHILOSOPHERS;
        }

        return ItemStack.EMPTY;
    }
}
