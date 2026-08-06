package com.aranaira.magichem.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public final class RecipeNbtHelper {
    private static final String JSON_KEY = "preserve_nbt";
    private static final String NETWORK_KEY = "preserveNbtSource";

    private RecipeNbtHelper() {
    }

    public static boolean readOptionalBoolean(JsonObject recipeJson, ResourceLocation recipeId) {
        if(!recipeJson.has(JSON_KEY)) return false;

        JsonElement element = recipeJson.get(JSON_KEY);
        if(!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean())
            throw error(recipeId, "'preserve_nbt' must be a boolean");

        return element.getAsBoolean();
    }

    @Nullable
    public static Item readOptionalSource(JsonObject recipeJson, ResourceLocation recipeId) {
        if(!recipeJson.has(JSON_KEY)) return null;

        JsonElement element = recipeJson.get(JSON_KEY);
        if(!element.isJsonObject())
            throw error(recipeId, "'preserve_nbt' must be an object containing an 'item'");

        JsonObject object = element.getAsJsonObject();
        if(!object.has("item") || !object.get("item").isJsonPrimitive())
            throw error(recipeId, "'preserve_nbt.item' must be a concrete item id");

        ResourceLocation id = ResourceLocation.tryParse(object.get("item").getAsString());
        Item source = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
        if(source == null || !ForgeRegistries.ITEMS.containsKey(id))
            throw error(recipeId, "unknown preserve_nbt item '" + object.get("item").getAsString() + "'");

        return source;
    }

    public static void writeNetworkSource(CompoundTag nbt, NbtPreservingRecipe recipe) {
        Item source = recipe.getNbtSource();
        ResourceLocation id = source == null ? null : ForgeRegistries.ITEMS.getKey(source);
        if(id != null) nbt.putString(NETWORK_KEY, id.toString());
    }

    @Nullable
    public static Item readNetworkSource(CompoundTag nbt) {
        if(!nbt.contains(NETWORK_KEY, CompoundTag.TAG_STRING)) return null;

        ResourceLocation id = ResourceLocation.tryParse(nbt.getString(NETWORK_KEY));
        if(id == null || !ForgeRegistries.ITEMS.containsKey(id)) return null;
        return ForgeRegistries.ITEMS.getValue(id);
    }

    public static ItemStack createOutput(NbtPreservingRecipe recipe, ItemStack recipeOutput,
                                         ItemStack source, String... transientKeys) {
        Item expectedSource = recipe.getNbtSource();
        if(expectedSource == null || source.isEmpty() || source.getItem() != expectedSource)
            return recipeOutput.copy();

        ItemStack result = recipeOutput.copy();
        CompoundTag outputNbt = result.getTag() == null ? null : result.getTag().copy();
        CompoundTag mergedNbt = source.getTag() == null ? new CompoundTag() : source.getTag().copy();
        if(outputNbt != null) mergedNbt.merge(outputNbt);
        for(String key : transientKeys) mergedNbt.remove(key);

        result.setTag(mergedNbt.isEmpty() ? null : mergedNbt);
        return result;
    }

    public static ItemStack copyOne(ItemStack source) {
        ItemStack copy = source.copy();
        copy.setCount(1);
        return copy;
    }

    public static JsonSyntaxException error(ResourceLocation recipeId, String message) {
        return new JsonSyntaxException("Invalid NBT-preserving MagiChem recipe '" + recipeId + "': " + message);
    }
}
