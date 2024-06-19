package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

/**
 * This recipe type is used by the Ritual of the Balanced Scales.
 */
public class AlchemicalInfusionRitualRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final ItemStack alchemyObject;
    private final ItemStack componentMateriaOne, componentMateriaTwo;
    private final NonNullList<ItemStack> ingredients;
    private static final NonNullList<ItemStack> allPossibleOutputs = NonNullList.create();

    public AlchemicalInfusionRitualRecipe(ResourceLocation pID, ItemStack pAlchemyObject,
                                          ItemStack pComponentMateriaOne, ItemStack pComponentMateriaTwo,
                                          NonNullList<ItemStack> pIngredients) {
        this.id = pID;
        this.alchemyObject = pAlchemyObject;
        this.componentMateriaOne = pComponentMateriaOne;
        this.componentMateriaTwo = pComponentMateriaTwo;
        this.ingredients = pIngredients;
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
    public boolean isSpecial() {
        return true;
    }

    public Pair<ItemStack, ItemStack> getComponentMateria() {
        return new Pair<>(this.componentMateriaOne, this.componentMateriaTwo);
    }

    public ItemStack getAlchemyObject() {
        return alchemyObject;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return alchemyObject;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return alchemyObject;
    }

    public NonNullList<ItemStack> getIngredientItemStacks() {
        return ingredients;
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

    public static AlchemicalInfusionRitualRecipe getInfusionRitualRecipe(Level level, ItemStack query) {
        AlchemicalInfusionRitualRecipe result = null;
        List<AlchemicalInfusionRitualRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);

        for(AlchemicalInfusionRitualRecipe airr : allRecipes) {
            if(airr.alchemyObject.getItem() == query.getItem()) {
                result = airr;
                break;
            }
        }

        return result;
    }

    public static NonNullList<ItemStack> getAllOutputs(Level pLevel) {
        if(allPossibleOutputs.size() > 0)
            return allPossibleOutputs;

        List<AlchemicalInfusionRitualRecipe> allRecipes = pLevel.getRecipeManager().getAllRecipesFor(Type.INSTANCE);
        for(AlchemicalInfusionRitualRecipe airr : allRecipes) {
            allPossibleOutputs.add(airr.alchemyObject.copy());
        }

        return allPossibleOutputs;
    }

    public static class Type implements RecipeType<AlchemicalInfusionRitualRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "alchemical_infusion_ritual";
    }


    public static class Serializer implements RecipeSerializer<AlchemicalInfusionRitualRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MagiChemMod.MODID, "alchemical_infusion_ritual");
        private static final HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(true, true);

        @Override
        public AlchemicalInfusionRitualRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {

            ItemStack recipeObject = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "object"));
            if(recipeObject.getItem() == ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:air")))
                recipeObject = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:barrier")));

            ItemStack compOne = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "materia_type_one"));
            ItemStack compTwo = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "materia_type_two"));

            JsonArray components = GsonHelper.getAsJsonArray(pSerializedRecipe, "components");
            NonNullList<ItemStack> extractedIngredients = NonNullList.create();
            components.forEach(element -> {
                String key = element.getAsJsonObject().get("item").getAsString();

                ItemStack ing = ItemStack.EMPTY;

                Item itemQuery = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key));
                if(itemQuery != null) {
                    ing = new ItemStack(itemQuery);
                } else {
                    MagiChemMod.LOGGER.warn("&&& Couldn't find item \""+key+"\" for alchemical_infusion_ritual recipe \""+pRecipeId);
                }

                extractedIngredients.add(ing);
            });

            return new AlchemicalInfusionRitualRecipe(pRecipeId, recipeObject, compOne, compTwo, extractedIngredients);
        }

        @Override
        public @Nullable AlchemicalInfusionRitualRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            CompoundTag nbt = buf.readNbt();
            if(nbt == null) return null;

            ResourceLocation alchemyObjectRL = new ResourceLocation(nbt.getString("alchemyObjectItem"));
            Item alchemyObjectItem = ForgeRegistries.ITEMS.getValue(alchemyObjectRL);
            ItemStack alchemyObject = ItemStack.EMPTY;
            if(alchemyObjectItem != null) {
                if(nbt.contains("alchemyObjectCount"))
                    alchemyObject = new ItemStack(alchemyObjectItem, nbt.getInt("alchemyObjectCount"));
                else
                    alchemyObject = new ItemStack(alchemyObjectItem, 1);
            }

            ResourceLocation materiaTypeOneRL = new ResourceLocation(nbt.getString("materiaTypeOneItem"));
            int materiaTypeOneCount = nbt.getInt("materiaTypeOneCount");
            Item materiaTypeOneItem = materiaMap.get(materiaTypeOneRL.toString());
            ItemStack materiaTypeOne = new ItemStack(materiaTypeOneItem, materiaTypeOneCount);

            ResourceLocation materiaTypeTwoRL = new ResourceLocation(nbt.getString("materiaTypeTwoItem"));
            int materiaTypeTwoCount = nbt.getInt("materiaTypeTwoCount");
            Item materiaTypeTwoItem = materiaMap.get(materiaTypeTwoRL.toString());
            ItemStack materiaTypeTwo = new ItemStack(materiaTypeTwoItem, materiaTypeTwoCount);

            int componentTotal = nbt.getInt("componentCount");
            NonNullList<ItemStack> readIngredients = NonNullList.create();
            for(int i=0; i<componentTotal; i++) {
                ResourceLocation componentItemRL = new ResourceLocation(nbt.getString("component"+i+"Item"));
                int componentCount = nbt.getInt("component"+i+"Count");
                Item componentItem = ForgeRegistries.ITEMS.getValue(componentItemRL);
                ItemStack componentStack = ItemStack.EMPTY;
                if(componentItem != null)
                    componentStack = new ItemStack(componentItem, componentCount);
                readIngredients.add(componentStack);
            }

            return new AlchemicalInfusionRitualRecipe(id, alchemyObject, materiaTypeOne, materiaTypeTwo, readIngredients);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, AlchemicalInfusionRitualRecipe recipe) {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("alchemyObjectItem", ForgeRegistries.ITEMS.getKey(recipe.getAlchemyObject().getItem()).toString());
            nbt.putInt("alchemyObjectCount", recipe.getAlchemyObject().getCount());

            ItemStack first = recipe.getComponentMateria().getFirst();
            nbt.putString("materiaTypeOneItem", ForgeRegistries.ITEMS.getKey(first.getItem()).toString());
            nbt.putInt("materiaTypeOneCount", first.getCount());

            ItemStack second = recipe.getComponentMateria().getSecond();
            nbt.putString("materiaTypeTwoItem", ForgeRegistries.ITEMS.getKey(second.getItem()).toString());
            nbt.putInt("materiaTypeTwoCount", second.getCount());

            nbt.putInt("componentCount", recipe.ingredients.size());
            for(int i=0;i<recipe.ingredients.size(); i++) {
                nbt.putString("component"+i+"Item", ForgeRegistries.ITEMS.getKey(recipe.ingredients.get(i).getItem()).toString());
                nbt.putInt("component"+i+"Count", recipe.ingredients.get(i).getCount());
            }

            buf.writeNbt(nbt);
        }
    }
}