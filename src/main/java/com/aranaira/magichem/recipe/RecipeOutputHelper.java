package com.aranaira.magichem.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

final class RecipeOutputHelper {
    private RecipeOutputHelper() {
    }

    static ItemStack applyNbt(ItemStack output, JsonObject outputJson, ResourceLocation recipeId) {
        return applyNbt(output, outputJson, recipeId, "nbt");
    }

    static ItemStack applyNbt(ItemStack output, JsonObject outputJson, ResourceLocation recipeId,
                              String key) {
        if(output.isEmpty() || outputJson == null || !outputJson.has(key))
            return output;

        String serializedNbt = GsonHelper.getAsString(outputJson, key);
        try {
            output.setTag(TagParser.parseTag(serializedNbt));
        } catch(CommandSyntaxException exception) {
            throw new JsonSyntaxException("Invalid output NBT in MagiChem recipe '" + recipeId + "': " + exception.getMessage(), exception);
        }

        return output;
    }
}
