package com.aranaira.magichem.interop.mna;

import com.aranaira.magichem.interop.mna.guide.RecipeInfusion;
import com.aranaira.magichem.interop.mna.guide.RecipeInfusionRitual;
import com.mna.guide.recipe.init.RecipeRenderers;

public class MnAPlugin {
    public static final String ALCHEMICAL_INFUSION_RITUAL = "sublimation_ritual";
    public static final String ALCHEMICAL_INFUSION = "sublimation";

    public static void register() {
        RecipeRenderers.registerRecipeRenderer(ALCHEMICAL_INFUSION_RITUAL, RecipeInfusionRitual.class);
        RecipeRenderers.registerRecipeRenderer(ALCHEMICAL_INFUSION, RecipeInfusion.class);
    }
}
