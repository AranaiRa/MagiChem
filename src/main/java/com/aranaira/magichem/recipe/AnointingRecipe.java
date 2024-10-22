package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

public class AnointingRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final MateriaItem materia;
    private final float chance;
    private final Block target, result;

    public AnointingRecipe(ResourceLocation pID, MateriaItem pMateria, float pChance, Block pTarget, Block pResult) {
        this.id = pID;
        this.materia = pMateria;
        this.chance = pChance;
        this.target = pTarget;
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

    public MateriaItem getMateria() {
        return materia;
    }

    public float getChance() {
        return chance;
    }

    public Block getTarget() {
        return target;
    }

    public Block getResult() {
        return result;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return new ItemStack(result.asItem());
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return new ItemStack(result.asItem());
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

    public static AnointingRecipe getAnointingRecipe(Level level, Block query) {
        AnointingRecipe recipeResult = null;
        List<AnointingRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);

        for(AnointingRecipe ar : allRecipes) {
            if(ar.target == query) {
                recipeResult = ar;
                break;
            }
        }

        return recipeResult;
    }

    public static class Type implements RecipeType<AnointingRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "anointing";
    }

    public static class Serializer implements RecipeSerializer<AnointingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(MagiChemMod.MODID, "anointing");
        private static final HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(true, true);

        @Override
        public AnointingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            
            String targetRL = GsonHelper.getAsString(pSerializedRecipe, "targetBlock");
            String materiaRL = GsonHelper.getAsString(pSerializedRecipe, "materia");
            float chance = GsonHelper.getAsFloat(pSerializedRecipe, "chance");
            String resultRL = GsonHelper.getAsString(pSerializedRecipe, "result");

            Block targetAsBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(targetRL));
            Block resultAsBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(resultRL));
            MateriaItem materiaItem = materiaMap.get(materiaRL);

            return new AnointingRecipe(pRecipeId, materiaItem, chance, targetAsBlock, resultAsBlock);
        }

        @Override
        public @Nullable AnointingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            CompoundTag nbt = pBuffer.readNbt();

            Block targetAsBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("targetBlock")));
            float chance = nbt.getFloat("chance");
            MateriaItem materiaItem = materiaMap.get(nbt.getString("materia"));
            Block resultAsBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("result"));

            return new AnointingRecipe(pRecipeId, materiaItem, chance, targetAsBlock, resultAsBlock);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, AnointingRecipe pRecipe) {
            CompoundTag nbt = new CompoundTag();

            nbt.putString("targetBlock", ForgeRegistries.BLOCKS.getKey(pRecipe.target).toString());
            nbt.putString("materia", ForgeRegistries.ITEMS.getKey(pRecipe.materia).toString());
            nbt.putFloat("chance", pRecipe.chance);
            nbt.putString("result", ForgeRegistries.BLOCKS.getKey(pRecipe.result).toString());

            pBuffer.writeNbt(nbt);
        }
    }
}
