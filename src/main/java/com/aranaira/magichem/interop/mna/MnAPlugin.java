package com.aranaira.magichem.interop.mna;

import com.aranaira.magichem.interop.mna.guide.RecipeAnointing;
import com.aranaira.magichem.interop.mna.guide.RecipeSublimation;
import com.aranaira.magichem.interop.mna.guide.RecipeSublimationRitual;
import com.mna.guide.recipe.init.RecipeRenderers;

public class MnAPlugin {
    public static final String SUBLIMATION_RITUAL = "sublimation_ritual";
    public static final String SUBLIMATION = "sublimation";
    public static final String ANOINTING = "anointing";

    public static void register() {
        RecipeRenderers.registerRecipeRenderer(SUBLIMATION_RITUAL, RecipeSublimationRitual.class);
        RecipeRenderers.registerRecipeRenderer(SUBLIMATION, RecipeSublimation.class);
        RecipeRenderers.registerRecipeRenderer(ANOINTING, RecipeAnointing.class);
    }
}
