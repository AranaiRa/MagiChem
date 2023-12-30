package com.aranaira.magichem.datagen;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.NameCountPair;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.MateriaRegistry;
import com.aranaira.magichem.registry.RecipeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.json.*;
import java.util.HashMap;
import java.util.Map;

public class RecipeGenerator extends ItemModelProvider {

    public RecipeGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, MagiChemMod.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        generateFixationRecipes();
    }

    private void generateFixationRecipes() {
        Map<String, Object> config = new HashMap<>();
        config.put("javax.json.stream.JsonGenerator.prettyPrinting", Boolean.TRUE);

        for(String key : MateriaRegistry.ADMIXTURE_DATAGEN.keySet()) {

            JsonObjectBuilder components = Json.createObjectBuilder();

            for(NameCountPair nci : MateriaRegistry.ADMIXTURE_DATAGEN.get(key)) {

            }

            /*JsonBuilderFactory factory = Json.createBuilderFactory(config);
            JsonObject value = factory.createObjectBuilder()
                    .add("admixture", ai.getMateriaName())
                    .build();
            */

        }
    }
}
