package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

/**
 * This recipe type is used by the Conjurer
 */
public class ConjurationRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final int craftingPeriodPassive, craftingPeriodSupplied, chargeUsageSupplied;
    private final Item catalyst;
    private final MateriaItem materia;
    private final ItemStack resultPassive, resultSupplied;

    public ConjurationRecipe(ResourceLocation pID, Item pCatalyst, MateriaItem pMateria, ItemStack pResultPassive, int pCraftingPeriodPassive, ItemStack pResultSupplied, int pCraftingPeriodSupplied, int pChargeUsageSupplied) {
        this.id = pID;
        this.catalyst = pCatalyst;
        this.materia = pMateria;
        this.resultPassive = pResultPassive;
        this.craftingPeriodPassive = pCraftingPeriodPassive;
        this.resultSupplied = pResultSupplied;
        this.craftingPeriodSupplied = pCraftingPeriodSupplied;
        this.chargeUsageSupplied = pChargeUsageSupplied;
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
        return resultPassive.copy();
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
        return ItemStack.EMPTY;
    }

    public Item getCatalyst() {
        return catalyst;
    }

    public MateriaItem getMateria() {
        return materia;
    }

    public Pair<ItemStack, Integer> getPassiveData(boolean pMakeCopy) {
        if(pMakeCopy)
            return new Pair<>(resultPassive.copy(), craftingPeriodPassive);
        return new Pair<>(resultPassive, craftingPeriodPassive);
    }

    public Triplet<ItemStack, Integer, Integer> getSuppliedData(boolean pMakeCopy) {
        if(pMakeCopy)
            return new Triplet<>(resultSupplied.copy(), craftingPeriodSupplied, chargeUsageSupplied);
        return new Triplet<>(resultSupplied, craftingPeriodSupplied, chargeUsageSupplied);
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

    public static ConjurationRecipe getConjurationRecipe(Level level, ItemStack query) {
        ConjurationRecipe result = null;
        List<ConjurationRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(ConjurationRecipe.Type.INSTANCE);

        for(ConjurationRecipe cr : allRecipes) {
            if(cr.catalyst == query.getItem()) {
                result = cr;
                break;
            }
        }

        return result;
    }

    public static class Type implements RecipeType<ConjurationRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "conjuration";
    }

    public static class Serializer implements RecipeSerializer<ConjurationRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MagiChemMod.MODID, "conjuration");
        private static final HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(true, true);

        @Override
        public ConjurationRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {

            String catalystQuery = GsonHelper.getAsString(pSerializedRecipe, "catalyst", "minecraft:air");
            String materiaQuery = GsonHelper.getAsString(pSerializedRecipe, "materia", "minecraft:air");
            JsonObject passiveData = GsonHelper.getAsJsonObject(pSerializedRecipe, "passive");
            JsonObject suppliedData = GsonHelper.getAsJsonObject(pSerializedRecipe, "supplied");

            String passiveItemQuery = GsonHelper.getAsString(passiveData, "item");
            int passiveCount = GsonHelper.getAsInt(passiveData, "count");
            int passivePeriod = GsonHelper.getAsInt(passiveData, "period");

            String suppliedItemQuery = GsonHelper.getAsString(suppliedData, "item");
            int suppliedCount = GsonHelper.getAsInt(suppliedData, "count");
            int suppliedPeriod = GsonHelper.getAsInt(suppliedData, "period");
            int suppliedChargeUsage = GsonHelper.getAsInt(suppliedData, "charge_usage");

            Item catalyst = ForgeRegistries.ITEMS.getValue(new ResourceLocation(catalystQuery));
            Item passiveItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(passiveItemQuery));
            Item suppliedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(suppliedItemQuery));
            MateriaItem materia = materiaMap.get(materiaQuery);

            return new ConjurationRecipe(pRecipeId, catalyst, materia, new ItemStack(passiveItem, passiveCount), passivePeriod, new ItemStack(suppliedItem, suppliedCount), suppliedPeriod, suppliedChargeUsage);
        }

        @Override
        public @Nullable ConjurationRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {

            CompoundTag nbt = buf.readNbt();
            if(nbt == null) return null;

            String catalystQuery = nbt.getString("catalyst");
            String materiaQuery = nbt.getString("materia");

            CompoundTag passiveData = nbt.getCompound("passive");
            String passiveItemQuery = passiveData.getString("item");
            int passiveCount = passiveData.getInt("count");
            int passivePeriod = passiveData.getInt("period");

            CompoundTag suppliedData = nbt.getCompound("passive");
            String suppliedItemQuery = suppliedData.getString("item");
            int suppliedCount = suppliedData.getInt("count");
            int suppliedPeriod = suppliedData.getInt("period");
            int suppliedChargeUsage = suppliedData.getInt("charge_usage");

            Item catalyst = ForgeRegistries.ITEMS.getValue(new ResourceLocation(catalystQuery));
            Item passiveItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(passiveItemQuery));
            Item suppliedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(suppliedItemQuery));
            MateriaItem materia = materiaMap.get(materiaQuery);

            return new ConjurationRecipe(id, catalyst, materia, new ItemStack(passiveItem, passiveCount), passivePeriod, new ItemStack(suppliedItem, suppliedCount), suppliedPeriod, suppliedChargeUsage);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ConjurationRecipe recipe) {
            CompoundTag nbt = new CompoundTag();

            nbt.putString("catalyst", ForgeRegistries.ITEMS.getKey(recipe.catalyst).toString());
            nbt.putString("materia", ForgeRegistries.ITEMS.getKey(recipe.materia).toString());

            CompoundTag passiveData = new CompoundTag();
            passiveData.putString("item", ForgeRegistries.ITEMS.getKey(recipe.resultPassive.getItem()).toString());
            passiveData.putInt("count", recipe.resultPassive.getCount());
            passiveData.putInt("period", recipe.craftingPeriodPassive);
            nbt.put("passive", passiveData);

            CompoundTag suppliedData = new CompoundTag();
            suppliedData.putString("item", ForgeRegistries.ITEMS.getKey(recipe.resultSupplied.getItem()).toString());
            suppliedData.putInt("count", recipe.resultSupplied.getCount());
            suppliedData.putInt("period", recipe.craftingPeriodSupplied);
            suppliedData.putInt("charge_usage", recipe.craftingPeriodSupplied);
            nbt.put("supplied", suppliedData);

            buf.writeNbt(nbt);
        }
    }
}