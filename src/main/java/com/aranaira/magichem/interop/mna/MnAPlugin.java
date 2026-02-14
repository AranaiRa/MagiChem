package com.aranaira.magichem.interop.mna;

import com.aranaira.magichem.interop.mna.guide.*;
import com.mna.guide.recipe.init.RecipeRenderers;

public class MnAPlugin {
    public static final String SUBLIMATION_RITUAL = "sublimation_ritual";
    public static final String SUBLIMATION = "sublimation";
    public static final String ANOINTING = "anointing";
    public static final String VITRIOLATION = "vitriolation";
    public static final String FULMINATION = "fulmination";
    public static final String ILLUMINATION = "illumination";
    public static final String EXALTATION = "exaltation";

    public static void register() {
        RecipeRenderers.registerRecipeRenderer(SUBLIMATION_RITUAL, RecipeSublimationRitual.class);
        RecipeRenderers.registerRecipeRenderer(SUBLIMATION, RecipeSublimation.class);
        RecipeRenderers.registerRecipeRenderer(ANOINTING, RecipeAnointing.class);
        RecipeRenderers.registerRecipeRenderer(VITRIOLATION, RecipeVitriolation.class);
        RecipeRenderers.registerRecipeRenderer(FULMINATION, RecipeFulmination.class);
        RecipeRenderers.registerRecipeRenderer(ILLUMINATION, RecipeIllumination.class);
        RecipeRenderers.registerRecipeRenderer(EXALTATION, RecipeExaltation.class);
    }
}
