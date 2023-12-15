package com.aranaira.magichem.interop;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.interop.jei.CompositionRecipeCategory;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static RecipeType<AlchemicalCompositionRecipe> COMPOSITION_TYPE =
            new RecipeType<>(CompositionRecipeCategory.UID, AlchemicalCompositionRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(MagiChemMod.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new
                CompositionRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager rm = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();
        List<AlchemicalCompositionRecipe> recipesDistillation = rm.getAllRecipesFor(AlchemicalCompositionRecipe.Type.INSTANCE);
        registration.addRecipes(COMPOSITION_TYPE, recipesDistillation);
    }
}
