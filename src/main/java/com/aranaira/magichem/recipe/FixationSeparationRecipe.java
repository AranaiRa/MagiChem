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

import java.util.HashMap;
import java.util.List;

/**
 * This recipe type is used by the Admixer/Fusion Crucible in a straightforward manner and the Centrifuge/Excision Engine in reverse.
 */
public class FixationSeparationRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final ItemStack resultAdmixture;
    private final NonNullList<ItemStack> componentMateria;

    public FixationSeparationRecipe(ResourceLocation id, ItemStack alchemyObject, NonNullList<ItemStack> componentMateria) {
        this.id = id;
        this.resultAdmixture = alchemyObject;
        this.componentMateria = componentMateria;
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
        return resultAdmixture;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public NonNullList<ItemStack> getComponentMateria() {
        NonNullList<ItemStack> componentsCopy = NonNullList.create();
        for(ItemStack is : componentMateria) {
            componentsCopy.add(is.copy());
        }
        return componentsCopy;
    }

    public ItemStack getResultAdmixture() {
        return resultAdmixture.copy();
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

    public static FixationSeparationRecipe getSeparatingRecipe(Level level, ItemStack query) {
        FixationSeparationRecipe result = null;
        List<FixationSeparationRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(FixationSeparationRecipe.Type.INSTANCE);

        for(FixationSeparationRecipe fsr : allRecipes) {
            if(fsr.resultAdmixture.getItem() == query.getItem()) {
                result = fsr;
                break;
            }
        }

        return result;
    }

    public static class Type implements RecipeType<FixationSeparationRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "fixation_separation";
    }

    public static class Serializer implements RecipeSerializer<FixationSeparationRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MagiChemMod.MODID, "fixation_separation");
        private static final HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(true, true);

        @Override
        public FixationSeparationRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {

            ItemStack recipeObject = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "object"));
            if(recipeObject.getItem() == ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:air")))
                recipeObject = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:barrier")));

            JsonArray components = GsonHelper.getAsJsonArray(pSerializedRecipe, "components");
            NonNullList<ItemStack> extractedIngredients = NonNullList.create();
            components.forEach(element -> {
                String key = element.getAsJsonObject().get("item").getAsString();

                ItemStack ing = ItemStack.EMPTY;

                MateriaItem matQuery = materiaMap.get(key);
                if(matQuery != null) {
                    ing = new ItemStack(matQuery);
                } else {
                    MagiChemMod.LOGGER.warn("&&& Couldn't find materia \""+key+"\" for fixation_separation recipe \""+pRecipeId+"\"");
                }

                if(element.getAsJsonObject().has("count"))
                    ing.setCount(element.getAsJsonObject().get("count").getAsInt());
                extractedIngredients.add(ing);
            });

            return new FixationSeparationRecipe(pRecipeId, recipeObject, extractedIngredients);
        }

        @Override
        public @Nullable FixationSeparationRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {

            CompoundTag nbt = buf.readNbt();
            if(nbt == null) return null;

            ResourceLocation resultAdmixtureRL = new ResourceLocation(nbt.getString("resultAdmixture"));
            Item resultAdmixtureItem = ForgeRegistries.ITEMS.getValue(resultAdmixtureRL);
            ItemStack resultAdmixture = ItemStack.EMPTY;
            if(resultAdmixtureItem != null)
                resultAdmixture = new ItemStack(resultAdmixtureItem, 1);

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

            return new FixationSeparationRecipe(id, resultAdmixture, readComponentMateria);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, FixationSeparationRecipe recipe) {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("resultAdmixture", ForgeRegistries.ITEMS.getKey(recipe.getResultAdmixture().getItem()).toString());
            nbt.putInt("componentCount", recipe.componentMateria.size());
            for(int i=0;i<recipe.componentMateria.size(); i++) {
                nbt.putString("component"+i+"Item", ForgeRegistries.ITEMS.getKey(recipe.componentMateria.get(i).getItem()).toString());
                nbt.putInt("component"+i+"Count", recipe.componentMateria.get(i).getCount());
            }

            buf.writeNbt(nbt);
        }
    }
}