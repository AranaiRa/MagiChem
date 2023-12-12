/**
 * This class is adapted from a ChemLib class. See the original here:
 * https://github.com/SmashingMods/ChemLib/blob/1.19.x/src/main/java/com/smashingmods/chemlib/registry/ChemicalRegistry.java
 */

package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.EssentiaItem;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

public class MateriaRegistry {
    public static final JsonObject ESSENTIA_JSON = getStreamAsJsonObject("/data/magichem/generator/essentia.json");
    public static final JsonObject ADMIXTURE_JSON = getStreamAsJsonObject("/data/magichem/generator/admixtures.json");

    public static JsonObject getStreamAsJsonObject(String pPath) {
        return JsonParser.parseReader(new BufferedReader(new InputStreamReader(Objects.requireNonNull(MagiChemMod.class.getResourceAsStream(pPath))))).getAsJsonObject();
    }

    public static void register(IEventBus eventBus) {
        registerEssentia();
    }

    public static void registerEssentia() {
        for(JsonElement json : ESSENTIA_JSON.getAsJsonArray("essentia")) {

            JsonObject object = json.getAsJsonObject();
            String essentiaName = object.get("name").getAsString();
            String abbreviation = object.get("abbreviation").getAsString();
            int wheelPos = object.get("wheel").getAsInt();
            String house = object.get("house").getAsString();
            String color = object.get("color").getAsString();

            ItemRegistry.ESSENTIA.register("essentia_"+essentiaName,
                    () -> new EssentiaItem(essentiaName, abbreviation, house, wheelPos, color));
            RegistryObject<Item> registryObject = ItemRegistry.getRegistryObject(ItemRegistry.ESSENTIA, "essentia_"+essentiaName);
        }
    }
}
