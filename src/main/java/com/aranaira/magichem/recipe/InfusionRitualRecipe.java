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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class InfusionRitualRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final ItemStack result;
    private final int experienceCost;
    private final NonNullList<ItemStack> componentIngredients;
    private final NonNullList<ItemStack> componentMateria;

    public InfusionRitualRecipe(ResourceLocation pID, ItemStack pResult, int pExperienceCost, NonNullList<ItemStack> pComponentIngredients, NonNullList<ItemStack> pComponentMateria) {
        this.id = pID;
        this.result = pResult;
        this.experienceCost = pExperienceCost;
        this.componentIngredients = pComponentIngredients;
        this.componentMateria = pComponentMateria;
    }

    /**
     * Unused by this mod's devices, but has to be here for Recipe inheritance.
     * @param pContainer
     * @param pLevel
     * @return always false
     */
    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        return false;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return this.result;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return this.result;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    public int getExperienceCost() {
        return this.experienceCost;
    }

    public NonNullList<ItemStack> getComponentIngredients() {
        return this.componentIngredients;
    }

    public NonNullList<ItemStack> getComponentMateria() {
        return this.componentMateria;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<InfusionRitualRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "infusion_ritual";
    }

    public static class Serializer implements RecipeSerializer<InfusionRitualRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(
                MagiChemMod.MODID, "infusion_ritual");
        private static final HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(true, true);

        @Override
        public InfusionRitualRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ItemStack recipeResult = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "object"));
            if(recipeResult.getItem() == Items.AIR)
                recipeResult = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:barrier")));

            int experienceCost = GsonHelper.getAsInt(pSerializedRecipe, "experience");

            JsonArray ingredients = GsonHelper.getAsJsonArray(pSerializedRecipe, "ingredients");
            NonNullList<ItemStack> extractedIngredients = NonNullList.create();
            ingredients.forEach(element -> {
                String key = element.getAsJsonObject().get("item").getAsString();

                ItemStack stack = ItemStack.EMPTY;

                String[] keySplit = key.split(":");
                Item query = ForgeRegistries.ITEMS.getValue(new ResourceLocation(keySplit[0], keySplit[1]));
                if(query != null) {
                    stack = new ItemStack(query, 1);
                    extractedIngredients.add(stack);
                } else {
                    MagiChemMod.LOGGER.warn("&&& Couldn't find item \""+key+"\" for infusion_ritual recipe \""+pRecipeId+"\"");
                }
            });

            JsonArray components = GsonHelper.getAsJsonArray(pSerializedRecipe, "components");
            NonNullList<ItemStack> extractedComponents = NonNullList.create();
            ingredients.forEach(element -> {
                String key = element.getAsJsonObject().get("item").getAsString();

                ItemStack stack = ItemStack.EMPTY;

                MateriaItem query = materiaMap.get(key);
                if(query != null) {
                    stack = new ItemStack(query, 1);
                    extractedComponents.add(stack);
                } else {
                    MagiChemMod.LOGGER.warn("&&& Couldn't find materia \""+key+"\" for infusion_ritual recipe \""+pRecipeId+"\"");
                }
            });

            return new InfusionRitualRecipe(pRecipeId, recipeResult, experienceCost, extractedIngredients, extractedComponents);
        }

        @Override
        public @Nullable InfusionRitualRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            ItemStack readResult = pBuffer.readItem();

            int readExperienceCost = pBuffer.readInt();

            int readTotalIngredients = pBuffer.readInt();
            NonNullList<ItemStack> readIngredients = NonNullList.create();
            for(int i=0; i<readTotalIngredients; i++) {
                readIngredients.add(pBuffer.readItem());
            }

            int readTotalComponents = pBuffer.readInt();
            NonNullList<ItemStack> readComponents = NonNullList.create();
            for(int i=0; i<readTotalComponents; i++) {
                readComponents.add(pBuffer.readItem());
            }

            return new InfusionRitualRecipe(pRecipeId, readResult, readExperienceCost, readIngredients, readComponents);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, InfusionRitualRecipe pRecipe) {
            pBuffer.writeItemStack(pRecipe.result, true);

            pBuffer.writeInt(pRecipe.experienceCost);

            pBuffer.writeInt(pRecipe.componentIngredients.size());
            for(ItemStack stack : pRecipe.componentIngredients) {
                pBuffer.writeItemStack(stack, true);
            }

            pBuffer.writeInt(pRecipe.componentMateria.size());
            for(ItemStack stack : pRecipe.componentMateria) {
                pBuffer.writeItemStack(stack, true);
            }
        }
    }
}
