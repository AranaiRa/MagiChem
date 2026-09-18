package com.aranaira.magichem.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

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

    /**
     * Reads an item stack from a recipe-sync tag using Forge's item registry.
     *
     * ItemStack.of resolves ids through the vanilla built-in registry. MagiChem's
     * recipe serializers have historically used ForgeRegistries.ITEMS instead,
     * which is the registry used while parsing the recipe JSON. Keep that lookup
     * path for synced recipe outputs and carry the optional stack tag alongside it.
     */
    static ItemStack readNetworkStack(CompoundTag parent, String key) {
        CompoundTag stackTag = parent.getCompound(key);
        if (stackTag.isEmpty()) {
            return ItemStack.EMPTY;
        }

        String itemName = stackTag.getString("item");
        if (itemName.isEmpty()) {
            itemName = stackTag.getString("id");
        }

        ResourceLocation itemId = ResourceLocation.tryParse(itemName);
        Item item = itemId == null ? null : ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        int count = stackTag.contains("count")
                ? stackTag.getInt("count")
                : stackTag.contains("Count") ? stackTag.getByte("Count") : 1;
        if (count <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(item, count);
        if (stackTag.contains("tag", CompoundTag.TAG_COMPOUND)) {
            result.setTag(stackTag.getCompound("tag").copy());
        }
        return result;
    }

    /**
     * Writes the legacy MagiChem network item format plus the optional stack NBT.
     * The legacy fields keep recipe synchronization compatible with the rest of
     * MagiChem, while the nested tag carries recipe-authored output NBT.
     */
    static void writeNetworkStack(CompoundTag parent, String key, ItemStack stack) {
        CompoundTag stackTag = new CompoundTag();
        if (!stack.isEmpty()) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (itemId != null) {
                stackTag.putString("item", itemId.toString());
                stackTag.putInt("count", stack.getCount());
                if (stack.getTag() != null && !stack.getTag().isEmpty()) {
                    stackTag.put("tag", stack.getTag().copy());
                }
            }
        }
        parent.put(key, stackTag);
    }

    private static CompoundTag normalizeTag(CompoundTag tag) {
        return tag == null || tag.isEmpty() ? null : tag;
    }
}
