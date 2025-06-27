package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.enums.LuminType;
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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IlluminationRecipe implements Recipe<SimpleContainer>, IMARecipe {
    private final ResourceLocation id;
    private final ItemStack inputItem, resultItem;
    private final LuminType luminType;
    private final int craftTime;

    public IlluminationRecipe(ResourceLocation pID, ItemStack pInputItem, ItemStack pResultItem, LuminType pLuminType, int pCraftTime) {
        this.id = pID;
        this.inputItem = pInputItem;
        this.resultItem = pResultItem;
        this.luminType = pLuminType;
        this.craftTime = pCraftTime;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        return false;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public ItemStack getInputItem() {
        return inputItem;
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.id;
    }

    public ItemStack getResultItem() {
        return resultItem;
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        return resultItem;
    }

    @Override
    public int getTier() {
        return 3;
    }

    public LuminType getLuminType() {
        return luminType;
    }

    public int getCraftTime() {
        return craftTime;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return resultItem.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return resultItem.copy();
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

    public static IlluminationRecipe getIlluminationRecipe(Level level, Item query, LuminType luminType) {
        if(luminType == LuminType.NONE) return null;

        IlluminationRecipe recipeResult = null;
        List<IlluminationRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);

        for(IlluminationRecipe ar : allRecipes) {
            if(ar.inputItem.getItem() == query && ar.luminType == luminType) {
                recipeResult = ar;
                break;
            }
        }

        return recipeResult;
    }

    public static List<IlluminationRecipe> getAllIlluminationRecipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);
    }

    public static class Type implements RecipeType<IlluminationRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "illumination";
    }

    public static class Serializer implements RecipeSerializer<IlluminationRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(MagiChemMod.MODID, "illumination");

        @Override
        public IlluminationRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {

            String inputItemRL = GsonHelper.getAsString(pSerializedRecipe, "input");
            Item inputItemAsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(inputItemRL));

            JsonObject luminObject = GsonHelper.getAsJsonObject(pSerializedRecipe, "lumins", null);
            JsonObject resultItemObject = GsonHelper.getAsJsonObject(pSerializedRecipe, "result", null);

            int luminType = 0;
            LuminType luminTypeAsType = LuminType.NONE;
            int craftTime = 0;
            if(luminObject != null) {
                luminType = GsonHelper.getAsInt(luminObject, "type");
                if(luminType == 1) luminTypeAsType = LuminType.SOLAR;
                else if(luminType == 2) luminTypeAsType = LuminType.LUNAR;
                else if(luminType == 3) luminTypeAsType = LuminType.SIDEREAL;
                craftTime = GsonHelper.getAsInt(luminObject, "minutes");
            }

            Item resultItemAsItem = null;
            String resultItemRL = null;
            int resultItemCount = 0;
            if(resultItemObject != null) {
                resultItemRL = GsonHelper.getAsString(resultItemObject, "item");
                resultItemCount = GsonHelper.getAsInt(resultItemObject, "count");
                resultItemAsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(resultItemRL));
            }

            return new IlluminationRecipe(pRecipeId,
                    inputItemAsItem == null ? ItemStack.EMPTY : new ItemStack(inputItemAsItem),
                    resultItemAsItem == null ? ItemStack.EMPTY : new ItemStack(resultItemAsItem, resultItemCount),
                    luminTypeAsType, craftTime
            );
        }

        @Override
        public @Nullable IlluminationRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            CompoundTag nbt = pBuffer.readNbt();

            Item inputAsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("inputItem")));

            int luminType = nbt.getInt("luminType");
            LuminType luminTypeAsType = LuminType.NONE;
            if(luminType == 1) luminTypeAsType = LuminType.SOLAR;
            else if(luminType == 2) luminTypeAsType = LuminType.LUNAR;
            else if(luminType == 3) luminTypeAsType = LuminType.SIDEREAL;

            int craftTime = nbt.getInt("minutes");

            CompoundTag resultItemTag = nbt.getCompound("resultItem");
            Item resultItemAsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(resultItemTag.getString("item")));
            int resultItemCount = resultItemTag.getInt("count");

            return new IlluminationRecipe(pRecipeId,
                    inputAsItem == null ? ItemStack.EMPTY : new ItemStack(inputAsItem),
                    resultItemAsItem == null ? ItemStack.EMPTY : new ItemStack(resultItemAsItem, resultItemCount),
                    luminTypeAsType, craftTime
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, IlluminationRecipe pRecipe) {
            CompoundTag nbt = new CompoundTag();

            nbt.putString("inputItem", ForgeRegistries.ITEMS.getKey(pRecipe.inputItem.getItem()).toString());
            nbt.putInt("luminType",pRecipe.luminType.ordinal());
            nbt.putInt("minutes",pRecipe.craftTime);

            CompoundTag resultItemTag = new CompoundTag();
            resultItemTag.putString("item", ForgeRegistries.ITEMS.getKey(pRecipe.resultItem.getItem()).toString());
            resultItemTag.putInt("count", pRecipe.resultItem.getCount());
            nbt.put("resultItem", resultItemTag);

            pBuffer.writeNbt(nbt);
        }
    }
}
