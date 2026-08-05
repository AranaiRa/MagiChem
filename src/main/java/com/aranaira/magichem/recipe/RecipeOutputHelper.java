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
        if(output.isEmpty() || outputJson == null || !outputJson.has("nbt"))
            return output;

        String serializedNbt = GsonHelper.getAsString(outputJson, "nbt");
        try {
            output.setTag(TagParser.parseTag(serializedNbt));
        } catch(CommandSyntaxException exception) {
            throw new JsonSyntaxException("Invalid output NBT in MagiChem recipe '" + recipeId + "': " + exception.getMessage(), exception);
        }

        return output;
    }
}
