package com.aranaira.magichem.interop;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.interop.jei.*;
import com.aranaira.magichem.recipe.*;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mna.items.ItemInit;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static RecipeType<DistillationFabricationRecipe> DISTILLATION_TYPE =
            new RecipeType<>(DistillationRecipeCategory.UID, DistillationFabricationRecipe.class);
    public static RecipeType<DistillationFabricationRecipe> FABRICATION_TYPE =
            new RecipeType<>(FabricationRecipeCategory.UID, DistillationFabricationRecipe.class);
    public static RecipeType<FixationSeparationRecipe> FIXATION_TYPE =
            new RecipeType<>(FixationRecipeCategory.UID, FixationSeparationRecipe.class);
    public static RecipeType<FixationSeparationRecipe> SEPARATION_TYPE =
            new RecipeType<>(SeparationRecipeCategory.UID, FixationSeparationRecipe.class);
    public static RecipeType<SublimationRitualRecipe> SUBLIMATION_RITUAL_TYPE =
            new RecipeType<>(SublimationRitualRecipeCategory.UID, SublimationRitualRecipe.class);
    public static RecipeType<SublimationRecipe> SUBLIMATION_TYPE =
            new RecipeType<>(SublimationRecipeCategory.UID, SublimationRecipe.class);
    public static RecipeType<ColorationRecipe> COLORATION_TYPE =
            new RecipeType<>(ColorationRecipeCategory.UID, ColorationRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(MagiChemMod.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new
                DistillationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                FabricationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                FixationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                SeparationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                SublimationRitualRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                SublimationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new
                ColorationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager rm = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();

        List<DistillationFabricationRecipe> recipesDistillation = rm.getAllRecipesFor(DistillationFabricationRecipe.Type.INSTANCE);
        registration.addRecipes(DISTILLATION_TYPE, recipesDistillation);

        List<DistillationFabricationRecipe> recipesFabrication = new ArrayList<>();
        for(DistillationFabricationRecipe acr : recipesDistillation) {
            if(!acr.getIsDistillOnly())  recipesFabrication.add(acr);
        }
        registration.addRecipes(FABRICATION_TYPE, recipesFabrication);

        List<FixationSeparationRecipe> recipesFixationSeparation = rm.getAllRecipesFor(FixationSeparationRecipe.Type.INSTANCE);
        registration.addRecipes(FIXATION_TYPE, recipesFixationSeparation);
        registration.addRecipes(SEPARATION_TYPE, recipesFixationSeparation);

        List<SublimationRitualRecipe> recipesSublimationRitual = rm.getAllRecipesFor(SublimationRitualRecipe.Type.INSTANCE);
        registration.addRecipes(SUBLIMATION_RITUAL_TYPE, recipesSublimationRitual);

        List<SublimationRecipe> recipesSublimation = rm.getAllRecipesFor(SublimationRecipe.Type.INSTANCE);
        registration.addRecipes(SUBLIMATION_TYPE, recipesSublimation);

        List<ColorationRecipe> recipesColoration = rm.getAllRecipesFor(ColorationRecipe.Type.INSTANCE);
        registration.addRecipes(COLORATION_TYPE, recipesColoration);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.ALEMBIC.get(), 1), DISTILLATION_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.DISTILLERY.get(), 1), DISTILLATION_TYPE);
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.GRAND_DISTILLERY.get(), 1), DISTILLATION_TYPE);

        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.CENTRIFUGE.get(), 1), SEPARATION_TYPE);
        
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.FUSERY.get(), 1), FIXATION_TYPE);

        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.CIRCLE_FABRICATION.get(), 1), FABRICATION_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ItemInit.RUNE_RITUAL_METAL.get(), 1).setHoverName(Component.translatable("magichem:rituals/balanced_scales")), SUBLIMATION_RITUAL_TYPE);

        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.ALCHEMICAL_NEXUS.get(), 1), SUBLIMATION_TYPE);

        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.VARIEGATOR.get(), 1), COLORATION_TYPE);

        IModPlugin.super.registerRecipeCatalysts(registration);
    }
}
