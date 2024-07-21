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

            CompoundTag nbtAlchemyObject = nbt.getCompound("alchemyObject");
            CompoundTag nbtIngredients = nbt.getCompound("ingredients");
            CompoundTag nbtMateria1 = nbt.getCompound("materia1");
            CompoundTag nbtMateria2 = nbt.getCompound("materia2");

            ResourceLocation alchemyObjectRL = new ResourceLocation(nbtAlchemyObject.getString("item"));
            Item alchemyObjectItem = ForgeRegistries.ITEMS.getValue(alchemyObjectRL);
            ItemStack alchemyObject = ItemStack.EMPTY;
            if(alchemyObjectItem != null) {
                if(nbtAlchemyObject.contains("count"))
                    alchemyObject = new ItemStack(alchemyObjectItem, nbtAlchemyObject.getInt("count"));
                else
                    alchemyObject = new ItemStack(alchemyObjectItem, 1);
            }

            ResourceLocation materiaTypeOneRL = new ResourceLocation(nbtMateria1.getString("item"));
            int materiaTypeOneCount = nbtMateria1.getInt("count");
            Item materiaTypeOneItem = materiaMap.get(materiaTypeOneRL.toString());
            ItemStack materiaTypeOne = new ItemStack(materiaTypeOneItem, materiaTypeOneCount);

            ResourceLocation materiaTypeTwoRL = new ResourceLocation(nbtMateria2.getString("item"));
            int materiaTypeTwoCount = nbtMateria2.getInt("count");
            Item materiaTypeTwoItem = materiaMap.get(materiaTypeTwoRL.toString());
            ItemStack materiaTypeTwo = new ItemStack(materiaTypeTwoItem, materiaTypeTwoCount);

            int componentTotal = nbtIngredients.getInt("total");
            NonNullList<ItemStack> readIngredients = NonNullList.create();
            for(int i=0; i<componentTotal; i++) {
                CompoundTag nbtThisIngredient = nbtIngredients.getCompound("ingredient"+i);
                ResourceLocation componentItemRL = new ResourceLocation(nbtThisIngredient.getString("item"));
                int componentCount = nbtThisIngredient.getInt("count");
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

            CompoundTag nbtAlchemyObject = new CompoundTag();
            nbtAlchemyObject.putString("item", ForgeRegistries.ITEMS.getKey(recipe.getAlchemyObject().getItem()).toString());
            nbtAlchemyObject.putInt("count", recipe.getAlchemyObject().getCount());
            nbt.put("alchemyObject", nbtAlchemyObject);

            CompoundTag nbtMateria1 = new CompoundTag();
            ItemStack first = recipe.getComponentMateria().getFirst();
            nbtMateria1.putString("item", ForgeRegistries.ITEMS.getKey(first.getItem()).toString());
            nbtMateria1.putInt("count", first.getCount());
            nbt.put("materia1", nbtMateria1);

            CompoundTag nbtMateria2 = new CompoundTag();
            ItemStack second = recipe.getComponentMateria().getSecond();
            nbtMateria2.putString("item", ForgeRegistries.ITEMS.getKey(second.getItem()).toString());
            nbtMateria2.putInt("count", second.getCount());
            nbt.put("materia2", nbtMateria2);

            CompoundTag nbtIngredients = new CompoundTag();
            nbtIngredients.putInt("total", recipe.ingredients.size());
            for(int i=0;i<recipe.ingredients.size(); i++) {
                CompoundTag thisIngredient = new CompoundTag();
                thisIngredient.putString("item", ForgeRegistries.ITEMS.getKey(recipe.ingredients.get(i).getItem()).toString());
                thisIngredient.putInt("count", recipe.ingredients.get(i).getCount());
                nbtIngredients.put("ingredient"+i, thisIngredient);
            }
            nbt.put("ingredients", nbtIngredients);

            buf.writeNbt(nbt);
        }
    }
}