package com.aranaira.magichem.interop.mna;

import com.aranaira.magichem.interop.mna.guide.RecipeInfusionRitual;
import com.mna.guide.recipe.init.RecipeRenderers;

public class MnAPlugin {
    public static final String ALCHEMICAL_INFUSION_RITUAL = "alchemical_infusion_ritual";

    public static void register() {
        RecipeRenderers.registerRecipeRenderer(ALCHEMICAL_INFUSION_RITUAL, RecipeInfusionRitual.class);
    }
}
