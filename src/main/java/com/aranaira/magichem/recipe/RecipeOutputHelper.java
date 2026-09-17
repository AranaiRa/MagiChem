package com.aranaira.magichem.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

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

    /**
     * Compares recipe output stacks without treating an empty tag as meaningful NBT.
     *
     * Item tooltips are allowed to inspect their stack, and some item implementations
     * use getOrCreateTag() while doing so. That can leave a displayed recipe output
     * with an empty tag even though the recipe output has no authored NBT.
     */
    static boolean matches(ItemStack expected, ItemStack query) {
        if (!ItemStack.isSameItem(expected, query)) {
            return false;
        }

        return Objects.equals(normalizeTag(expected.getTag()), normalizeTag(query.getTag()));
    }

    private static CompoundTag normalizeTag(CompoundTag tag) {
        return tag == null || tag.isEmpty() ? null : tag;
    }
}
