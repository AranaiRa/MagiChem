package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
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

    public ItemStack getResultAdmixture() {
        return resultAdmixture;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer) {
        return resultAdmixture;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem() {
        return resultAdmixture.copy();
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
                    MagiChemMod.LOGGER.warn("&&& Couldn't find materia \""+key+"\" for fixation_separation recipe \""+pRecipeId);
                }

                if(element.getAsJsonObject().has("count"))
                    ing.setCount(element.getAsJsonObject().get("count").getAsInt());
                extractedIngredients.add(ing);
            });

            return new FixationSeparationRecipe(pRecipeId, recipeObject, extractedIngredients);
        }

        @Override
        public @Nullable FixationSeparationRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {

            ItemStack readAlchemyObject = buf.readItem();

            int totalComponents = buf.readInt();
            NonNullList<ItemStack> readComponentMateria = NonNullList.create();
            for(int i=0; i<totalComponents; i++) {
                buf.readItem();
            }

            return new FixationSeparationRecipe(id, readAlchemyObject, readComponentMateria);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, FixationSeparationRecipe recipe) {

            //Parameter 1: Alchemy Object
            buf.writeItemStack(recipe.resultAdmixture, true);

            //Parameter 2: Total number of component materia
            buf.writeFloat(recipe.componentMateria.size());

            //Parameter 3...: Component materia
            for(ItemStack stack : recipe.componentMateria) {
                buf.writeItemStack(stack, true);
            }
        }
    }
}