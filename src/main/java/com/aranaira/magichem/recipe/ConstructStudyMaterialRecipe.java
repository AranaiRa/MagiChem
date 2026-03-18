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

public class ConstructStudyMaterialRecipe implements Recipe<SimpleContainer>, IMARecipe {
    private final ResourceLocation id;
    private final Item item;
    private final int experience;
    private final boolean consumed;

    public ConstructStudyMaterialRecipe(ResourceLocation pID, Item pItem, int pExperience, boolean pConsumed) {
        this.id = pID;
        this.item = pItem;
        this.experience = pExperience;
        this.consumed = pConsumed;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        return false;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public Item getItem() {
        return item;
    }

    public int getExperience() {
        return experience;
    }

    public boolean isConsumed() {
        return consumed;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return ItemStack.EMPTY;
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

    public static ConstructStudyMaterialRecipe getConstructStudyMaterialRecipe(Level level, Item query) {
        ConstructStudyMaterialRecipe recipeResult = null;
        List<ConstructStudyMaterialRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);

        for(ConstructStudyMaterialRecipe ar : allRecipes) {
            if(ar.item == query) {
                recipeResult = ar;
                break;
            }
        }

        return recipeResult;
    }

    public static List<ConstructStudyMaterialRecipe> getAllConstructStudyMaterialRecipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.id;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        return new ItemStack(item);
    }

    @Override
    public int getTier() {
        return 1;
    }

    public static class Type implements RecipeType<ConstructStudyMaterialRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "construct_study_material";
    }

    public static class Serializer implements RecipeSerializer<ConstructStudyMaterialRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(MagiChemMod.MODID, "construct_study_material");

        @Override
        public ConstructStudyMaterialRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            
            String itemRL = GsonHelper.getAsString(pSerializedRecipe, "item");
            Item itemAsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemRL));
            if(itemAsItem == null || itemAsItem == Items.AIR)
                itemAsItem = ItemRegistry.PROBLEMITE.get();

            int experience = GsonHelper.getAsInt(pSerializedRecipe, "experience");
            boolean consumed = GsonHelper.getAsBoolean(pSerializedRecipe, "consumed");

            return new ConstructStudyMaterialRecipe(pRecipeId, itemAsItem, experience, consumed);
        }

        @Override
        public @Nullable ConstructStudyMaterialRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            CompoundTag nbt = pBuffer.readNbt();

            Item itemAsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("item")));
            int experience = nbt.getInt("experience");
            boolean consumed = nbt.getBoolean("consumed");

            return new ConstructStudyMaterialRecipe(pRecipeId, itemAsItem, experience, consumed);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, ConstructStudyMaterialRecipe pRecipe) {
            CompoundTag nbt = new CompoundTag();

            nbt.putString("item", ForgeRegistries.ITEMS.getKey(pRecipe.item).toString());
            nbt.putInt("experience", pRecipe.experience);
            nbt.putBoolean("consumed", pRecipe.consumed);

            pBuffer.writeNbt(nbt);
        }
    }
}
