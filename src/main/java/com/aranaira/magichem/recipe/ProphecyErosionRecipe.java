package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mna.api.recipes.IMARecipe;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ProphecyErosionRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final Block target;
    private final ArrayList<Block> resultOptions;
    private static final Random r = new Random();

    public ProphecyErosionRecipe(ResourceLocation pID, Block pTarget, ArrayList<Block> pResultOptions) {
        this.id = pID;
        this.target = pTarget;
        this.resultOptions = pResultOptions;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        return false;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public Block getTarget() {
        return target;
    }

    public ArrayList<Block> getResultOptions() {
        return resultOptions;
    }

    public Block pickResult() {
        if(resultOptions.size() == 1) return resultOptions.get(0);
        return resultOptions.get(r.nextInt(resultOptions.size()));
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return new ItemStack(resultOptions.get(0));
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return new ItemStack(resultOptions.get(0));
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

    public static ProphecyErosionRecipe getProphecyErosionRecipe(Level level, Block query) {
        ProphecyErosionRecipe recipeResult = null;
        List<ProphecyErosionRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);

        for(ProphecyErosionRecipe ar : allRecipes) {
            if(ar.target == query) {
                recipeResult = ar;
                break;
            }
        }

        return recipeResult;
    }

    public static List<ProphecyErosionRecipe> getAllProphecyErosionRecipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);
    }

    public static class Type implements RecipeType<ProphecyErosionRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "prophecy_erosion";
    }

    public static class Serializer implements RecipeSerializer<ProphecyErosionRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(MagiChemMod.MODID, "prophecy_erosion");
        private static final HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(true, true);

        @Override
        public ProphecyErosionRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            
            String targetRL = GsonHelper.getAsString(pSerializedRecipe, "targetBlock");
            final JsonArray resultOptionsAsArray = GsonHelper.getAsJsonArray(pSerializedRecipe, "resultOptions");

            Block targetAsBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(targetRL));
            ArrayList<Block> resultAsList = new ArrayList<>();
            for(JsonElement jo : resultOptionsAsArray.asList()) {
                resultAsList.add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(jo.getAsString())));
            }

            return new ProphecyErosionRecipe(pRecipeId, targetAsBlock, resultAsList);
        }

        @Override
        public @Nullable ProphecyErosionRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            CompoundTag nbt = pBuffer.readNbt();

            Block targetAsBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString("targetBlock")));

            ArrayList<Block> resultsAsList = new ArrayList<>();
            CompoundTag optionsTag = nbt.getCompound("resultOptions");
            for (String key : optionsTag.getAllKeys()) {
                resultsAsList.add(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(nbt.getString(key))));
            }

            return new ProphecyErosionRecipe(pRecipeId, targetAsBlock, resultsAsList);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, ProphecyErosionRecipe pRecipe) {
            CompoundTag nbt = new CompoundTag();

            nbt.putString("targetBlock", ForgeRegistries.BLOCKS.getKey(pRecipe.target).toString());

            CompoundTag optionsTag = new CompoundTag();
            for(Block b : pRecipe.resultOptions) {
                optionsTag.putBoolean(b.toString(), true);
            }

            nbt.put("resultOptions", optionsTag);

            pBuffer.writeNbt(nbt);
        }
    }
}
