package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

/**
 * This recipe type is used by both the Circle of Fabrication in a straightforward manner and the Alembic/Distillery in reverse.
 */
public class AlchemicalCompositionRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final ItemStack alchemyObject;
    private final NonNullList<ItemStack> componentMateria;
    private final boolean distillOnly;
    private final float outputRate;

    public AlchemicalCompositionRecipe(ResourceLocation id, ItemStack alchemyObject,
                                    NonNullList<ItemStack> componentMateria, boolean distillOnly, float outputRate) {
        this.id = id;
        this.alchemyObject = alchemyObject;
        this.componentMateria = componentMateria;
        this.distillOnly = distillOnly;
        this.outputRate = outputRate;
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
        /*if(pLevel.isClientSide()) {
            return false;
        }

        return componentMateria.get(0).test(pContainer.getItem(1));*/
        return false;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public NonNullList<ItemStack> getComponentMateria() {
        return componentMateria;
    }

    public boolean getIsDistillOnly() {
        return distillOnly;
    }

    public ItemStack getAlchemyObject() {
        return alchemyObject;
    }

    public float getOutputRate() { return outputRate; }

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

    public static AlchemicalCompositionRecipe getDistillingRecipe(Level level, ItemStack query) {
        AlchemicalCompositionRecipe result = null;
        List<AlchemicalCompositionRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);

        for(AlchemicalCompositionRecipe acr : allRecipes) {
            if(acr.alchemyObject.getItem() == query.getItem()) {
                result = acr;
                break;
            }
        }

        return result;
    }

    public static class Type implements RecipeType<AlchemicalCompositionRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "alchemical_composition";
    }


    public static class Serializer implements RecipeSerializer<AlchemicalCompositionRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MagiChemMod.MODID, "alchemical_composition");
        private static final HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(true, true);

        @Override
        public AlchemicalCompositionRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {

            ItemStack recipeObject = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "object"));
            if(recipeObject.getItem() == ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:air")))
                recipeObject = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:barrier")));

            boolean distillOnly = GsonHelper.getAsBoolean(pSerializedRecipe, "distill_only");
            float rate = GsonHelper.getAsFloat(pSerializedRecipe, "output_rate");

            JsonArray components = GsonHelper.getAsJsonArray(pSerializedRecipe, "components");
            NonNullList<ItemStack> extractedIngredients = NonNullList.create();
            components.forEach(element -> {
                String key = element.getAsJsonObject().get("item").getAsString();

                ItemStack ing = ItemStack.EMPTY;

                MateriaItem matQuery = materiaMap.get(key);
                if(matQuery != null) {
                    ing = new ItemStack(matQuery);
                } else {
                    MagiChemMod.LOGGER.warn("&&& Couldn't find materia \""+key+"\" for alchemical_composition recipe \""+pRecipeId);
                }

                if(element.getAsJsonObject().has("count"))
                    ing.setCount(element.getAsJsonObject().get("count").getAsInt());
                extractedIngredients.add(ing);
            });

            return new AlchemicalCompositionRecipe(pRecipeId, recipeObject, extractedIngredients, distillOnly, rate);
        }

        @Override
        public @Nullable AlchemicalCompositionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            CompoundTag nbt = buf.readNbt();
            if(nbt == null) return null;

            boolean readDistillOnly = nbt.getBoolean("distillOnly");
            float readOutputRate = nbt.getFloat("outputRate");
            ResourceLocation alchemyObjectRL = new ResourceLocation(nbt.getString("alchemyObject"));
            Item alchemyObjectItem = ForgeRegistries.ITEMS.getValue(alchemyObjectRL);
            ItemStack alchemyObject = ItemStack.EMPTY;
            if(alchemyObjectItem != null)
                alchemyObject = new ItemStack(alchemyObjectItem, 1);
            int componentTotal = nbt.getInt("componentCount");

            NonNullList<ItemStack> readComponentMateria = NonNullList.create();
            for(int i=0; i<componentTotal; i++) {
                ResourceLocation componentItemRL = new ResourceLocation(nbt.getString("component"+i+"Item"));
                int componentCount = nbt.getInt("component"+i+"Count");
                Item componentItem = ForgeRegistries.ITEMS.getValue(componentItemRL);
                ItemStack componentStack = ItemStack.EMPTY;
                if(componentItem != null)
                    componentStack = new ItemStack(componentItem, componentCount);
                readComponentMateria.add(componentStack);
            }

            return new AlchemicalCompositionRecipe(id, alchemyObject, readComponentMateria, readDistillOnly, readOutputRate);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, AlchemicalCompositionRecipe recipe) {
            CompoundTag nbt = new CompoundTag();
            nbt.putBoolean("distillOnly", recipe.distillOnly);
            nbt.putFloat("outputRate", recipe.outputRate);
            nbt.putString("alchemyObject", ForgeRegistries.ITEMS.getKey(recipe.getAlchemyObject().getItem()).toString());
            nbt.putInt("componentCount", recipe.componentMateria.size());
            for(int i=0;i<recipe.componentMateria.size(); i++) {
                nbt.putString("component"+i+"Item", ForgeRegistries.ITEMS.getKey(recipe.componentMateria.get(i).getItem()).toString());
                nbt.putInt("component"+i+"Count", recipe.componentMateria.get(i).getCount());
            }

            buf.writeNbt(nbt);
        }
    }
}