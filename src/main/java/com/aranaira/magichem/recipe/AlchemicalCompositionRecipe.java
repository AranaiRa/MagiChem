package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
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

    /**
     * Unused by this recipe type, use getComponentMateria() instead
     * @return always null
     */
    @Deprecated
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return null;
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
            boolean readDistillOnly = buf.readBoolean();

            float readOutputRate = buf.readFloat();

            ItemStack readAlchemyObject = buf.readItem();

            int totalComponents = buf.readInt();
            NonNullList<ItemStack> readComponentMateria = NonNullList.create();
            for(int i=0; i<totalComponents; i++) {
                readComponentMateria.add(buf.readItem());
            }

            return new AlchemicalCompositionRecipe(id, readAlchemyObject, readComponentMateria, readDistillOnly, readOutputRate);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, AlchemicalCompositionRecipe recipe) {
            //Parameter 0: Distill Only
            buf.writeBoolean(recipe.distillOnly);

            //Parameter 1: Output Rate
            buf.writeFloat(recipe.outputRate);

            //Parameter 2: Alchemy Object
            buf.writeItemStack(recipe.alchemyObject, true);

            //Parameter 3: Total number of component materia
            buf.writeFloat(recipe.componentMateria.size());

            //Parameter 4...: Component materia
            for(ItemStack stack : recipe.componentMateria) {
                buf.writeItemStack(stack, true);
            }
        }
    }
}