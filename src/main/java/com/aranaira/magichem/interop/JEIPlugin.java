package com.aranaira.magichem.interop;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.interop.jei.*;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static RecipeType<AlchemicalCompositionRecipe> DISTILLATION_TYPE =
            new RecipeType<>(DistillationRecipeCategory.UID, AlchemicalCompositionRecipe.class);
    public static RecipeType<AlchemicalCompositionRecipe> FABRICATION_TYPE =
            new RecipeType<>(FabricationRecipeCategory.UID, AlchemicalCompositionRecipe.class);
    public static RecipeType<FixationSeparationRecipe> FIXATION_TYPE =
            new RecipeType<>(FixationRecipeCategory.UID, FixationSeparationRecipe.class);
    public static RecipeType<FixationSeparationRecipe> SEPARATION_TYPE =
            new RecipeType<>(SeparationRecipeCategory.UID, FixationSeparationRecipe.class);

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
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager rm = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();

        List<AlchemicalCompositionRecipe> recipesDistillation = rm.getAllRecipesFor(AlchemicalCompositionRecipe.Type.INSTANCE);
        registration.addRecipes(DISTILLATION_TYPE, recipesDistillation);

        List<AlchemicalCompositionRecipe> recipesFabrication = new ArrayList<>();
        for(AlchemicalCompositionRecipe acr : recipesDistillation) {
            if(!acr.getIsDistillOnly())  recipesFabrication.add(acr);
        }
        registration.addRecipes(FABRICATION_TYPE, recipesFabrication);

        List<FixationSeparationRecipe> recipesFixationSeparation = rm.getAllRecipesFor(FixationSeparationRecipe.Type.INSTANCE);
        registration.addRecipes(FIXATION_TYPE, recipesFixationSeparation);
        registration.addRecipes(SEPARATION_TYPE, recipesFixationSeparation);
    }
}
