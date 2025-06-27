package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.gson.JsonObject;
import com.mna.api.recipes.IMARecipe;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

/**
 * This recipe type isn't used to craft anything, it's just an intermediary for information panels when you look up items that don't have a traditional recipe in JEI.
 */
public class InfoPanelRecipe implements Recipe<SimpleContainer>, IMARecipe {
    private final ResourceLocation id;
    private final ItemStack item;
    private final String info;

    public InfoPanelRecipe(ResourceLocation pID, ItemStack pItem, String pInfo) {
        this.id = pID;
        this.item = pItem;
        this.info = pInfo;
    }

    /**
     * Unused by this mod's devices, but has to be here for Recipe inheritance.
     * @param pContainer
     * @param pLevel
     * @return always false
     */
    @Deprecated
    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        return false;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return item.copy();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return item;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.id;
    }

    @Override
    public ItemStack getResultItem() {
        return item;
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        return item;
    }

    @Override
    public int getTier() {
        return 1;
    }

    public static class Type implements RecipeType<InfoPanelRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "info_panel";
    }

    public static class Serializer implements RecipeSerializer<InfoPanelRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MagiChemMod.MODID, "info_panel");

        @Override
        public InfoPanelRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {

            String infoParse = GsonHelper.getAsString(pSerializedRecipe, "info", "INFO NOT PARSED");
            String itemQuery = GsonHelper.getAsString(pSerializedRecipe, "output", "minecraft:air");

            Item recipeItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemQuery));

            return new InfoPanelRecipe(pRecipeId, new ItemStack(recipeItem), infoParse);
        }

        @Override
        public @Nullable InfoPanelRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {

            CompoundTag nbt = buf.readNbt();
            if(nbt == null) return null;

            String itemQuery = nbt.getString("output");
            String infoParse = nbt.getString("info");

            Item recipeItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemQuery));

            return new InfoPanelRecipe(id, new ItemStack(recipeItem), infoParse);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, InfoPanelRecipe recipe) {
            CompoundTag nbt = new CompoundTag();

            nbt.putString("output", ForgeRegistries.ITEMS.getKey(recipe.getItem().getItem()).toString());
            nbt.putString("info", recipe.info);

            buf.writeNbt(nbt);
        }
    }
}