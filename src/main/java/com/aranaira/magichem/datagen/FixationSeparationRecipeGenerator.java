package com.aranaira.magichem.datagen;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.NameCountPair;
import com.aranaira.magichem.registry.MateriaRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.*;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FixationSeparationRecipeGenerator {

    private static final String RECIPE_DIRECTORY = "C:\\Users\\arana\\Documents\\ForgeModding\\1.19.2\\MagnumOpus\\src\\generated\\resources\\data\\magichem\\recipes\\fixation_separation\\";

    private static List<AlchemicalRecipeData> recipeData = new ArrayList<>();

    public static void parseRecipeTable(){
        for(JsonElement json : MateriaRegistry.ADMIXTURE_JSON.getAsJsonArray("admixtures")) {

            JsonObject object = json.getAsJsonObject();
            String admixtureName = "admixture_"+object.get("name").getAsString();
            JsonArray components = object.getAsJsonArray("components");
            List<NameCountPair> outputComponents = new ArrayList<>();
            for(JsonElement component : components) {
                JsonObject componentObject = component.getAsJsonObject();

                byte componentCount = componentObject.has("count") ? componentObject.get("count").getAsByte() : 1;

                if(componentObject.has("essentia")) {
                    String componentName = componentObject.get("essentia").getAsString();
                    outputComponents.add(new NameCountPair("essentia_"+componentName, componentCount));
                }
                else if(componentObject.has("admixture")) {
                    String componentName = componentObject.get("admixture").getAsString();
                    outputComponents.add(new NameCountPair("admixture_"+componentName, componentCount));
                }

            }

            recipeData.add(new AlchemicalRecipeData(
                    admixtureName,
                    outputComponents
            ));
        }
    }

    public static void generateRecipes() {
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("javax.json.stream.JsonGenerator.prettyPrinting", Boolean.TRUE);

        for(AlchemicalRecipeData ard : recipeData) {
            JsonArrayBuilder componentsArray = Json.createArrayBuilder();
            for(NameCountPair ncp : ard.getComponents()) {
                componentsArray.add(Json.createObjectBuilder()
                        .add("item", ncp.getName())
                        .add("count", ncp.getCount())
                        .build()
                );
            }

            JsonBuilderFactory factory = Json.createBuilderFactory(config);
            javax.json.JsonObject output = factory.createObjectBuilder()
                    .add("type", "magichem:fixation_separation")
                    .add("object", ard.getAdmixture())
                    .add("components", componentsArray.build())
                    .build();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement element = JsonParser.parseString(output.toString());
            String formattedOutput = gson.toJson(element);

            saveJSON(ard.getAdmixture(), formattedOutput);
        }
    }

    public static void saveJSON(String filename, String contents) {
        PrintWriter outputFile = null;
        try {
            outputFile = new PrintWriter(new FileWriter(RECIPE_DIRECTORY + filename + ".json"));
            outputFile.write(contents);
            outputFile.close();
        } catch (IOException e) {
            MagiChemMod.LOGGER.error("&&& uH OH...! " + e.toString());
        }
    }
}

class AlchemicalRecipeData {
    private final String admixture;
    private final List<NameCountPair> components;

    public AlchemicalRecipeData(String pAdmixture, List<NameCountPair> pComponents) {
        this.admixture = pAdmixture;
        this.components = pComponents;
    }

    public String getAdmixture() {
        return admixture;
    }

    public List<NameCountPair> getComponents() {
        return components;
    }
}