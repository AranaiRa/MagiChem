package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.gson.JsonObject;
import com.mna.api.recipes.IMARecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class FulminationRecipe implements Recipe<SimpleContainer>, IMARecipe {
    private final ResourceLocation id;
    private final ItemStack input, result;

    public FulminationRecipe(ResourceLocation pID, ItemStack pInput, ItemStack pResult) {
        this.id = pID;
        this.input = pInput;
        this.result = pResult;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        return false;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getResult() {
        return result;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return result.copy();
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

    public static FulminationRecipe getFulminationRecipe(Level level, Item query) {
        FulminationRecipe recipeResult = null;
        List<FulminationRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);

        for(FulminationRecipe ar : allRecipes) {
            if(ar.input.getItem() == query) {
                recipeResult = ar;
                break;
            }
        }

        return recipeResult;
    }

    public static List<FulminationRecipe> getAllFulminationRecipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.id;
    }

    @Override
    public ItemStack getResultItem() {
        return result;
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        return result;
    }

    @Override
    public int getTier() {
        return 1;
    }

    public static class Type implements RecipeType<FulminationRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "fulmination";
    }

    public static class Serializer implements RecipeSerializer<FulminationRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(MagiChemMod.MODID, "fulmination");

        @Override
        public FulminationRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            
            String inputRL = GsonHelper.getAsString(pSerializedRecipe, "input");
            int inputCount = GsonHelper.getAsInt(pSerializedRecipe, "input_count");
            String resultRL = GsonHelper.getAsString(pSerializedRecipe, "result");
            int resultCount = GsonHelper.getAsInt(pSerializedRecipe, "result_count");

            Item inputAsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(inputRL));
            if(inputAsItem == null || inputAsItem == Items.AIR)
                inputAsItem = ItemRegistry.PROBLEMITE.get();
            Item resultAsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(resultRL));
            if(resultAsItem == null || resultAsItem == Items.AIR)
                resultAsItem = ItemRegistry.PROBLEMITE.get();

            return new FulminationRecipe(pRecipeId,
                    new ItemStack(inputAsItem, inputCount),
                    new ItemStack(resultAsItem, resultCount));
        }

        @Override
        public @Nullable FulminationRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            CompoundTag nbt = pBuffer.readNbt();

            Item inputAsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("input")));
            int inputCount = nbt.getInt("input_count");
            Item resultAsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("result")));
            int resultCount = nbt.getInt("result_count");

            return new FulminationRecipe(pRecipeId,
                    inputAsItem == null ? ItemStack.EMPTY : new ItemStack(inputAsItem, inputCount),
                    resultAsItem == null ? ItemStack.EMPTY : new ItemStack(resultAsItem, resultCount));
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, FulminationRecipe pRecipe) {
            CompoundTag nbt = new CompoundTag();

            nbt.putString("input", ForgeRegistries.ITEMS.getKey(pRecipe.input.getItem()).toString());
            nbt.putInt("input_count", pRecipe.input.getCount());
            nbt.putString("result", ForgeRegistries.ITEMS.getKey(pRecipe.result.getItem()).toString());
            nbt.putInt("result_count", pRecipe.result.getCount());

            pBuffer.writeNbt(nbt);
        }
    }
}
