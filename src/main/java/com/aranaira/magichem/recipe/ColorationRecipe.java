package com.aranaira.magichem.recipe;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.AdmixtureItem;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

/**
 * This recipe type is used by the Admixer/Fusion Crucible in a straightforward manner and the Centrifuge/Excision Engine in reverse.
 */
public class ColorationRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final int chargeUsage;
    private final float craftingTimeMultiplier;
    private final ItemStack colorlessDefault;
    private final HashMap<DyeColor, ItemStack> potentialOutputs;

    public ColorationRecipe(ResourceLocation id, int pChargeUsage, float pCraftingTimeMultiplier, ItemStack pColorlessDefault, HashMap<DyeColor, ItemStack> pPotentialOutputs) {
        this.id = id;
        this.chargeUsage = pChargeUsage;
        this.craftingTimeMultiplier = pCraftingTimeMultiplier;
        this.colorlessDefault = pColorlessDefault;
        this.potentialOutputs = new HashMap<>();
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
        return colorlessDefault;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public NonNullList<ItemStack> getResultsAsList() {
        NonNullList<ItemStack> componentsCopy = NonNullList.create();
        for(ItemStack is : potentialOutputs.values()) {
            componentsCopy.add(is.copy());
        }
        return componentsCopy;
    }

    public HashMap<DyeColor, ItemStack> getResultsAsMap() {
        HashMap<DyeColor, ItemStack> resultsCopy = new HashMap<>();
        for(DyeColor color : potentialOutputs.keySet()) {
            resultsCopy.put(color, potentialOutputs.get(color).copy());
        }
        return resultsCopy;
    }

    public ItemStack getColorlessDefault() {
        return colorlessDefault.copy();
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

    public static ColorationRecipe getColorationRecipe(Level level, ItemStack query) {
        ColorationRecipe result = null;
        List<ColorationRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(ColorationRecipe.Type.INSTANCE);

        for(ColorationRecipe cr : allRecipes) {
            if(cr.colorlessDefault.getItem() == query.getItem()) {
                result = cr;
                break;
            }
            for(DyeColor color : cr.potentialOutputs.keySet()) {
                if(cr.potentialOutputs.get(color).getItem() == query.getItem()) {
                    result = cr;
                    break;
                }
            }
            if(result != null)
                break;
        }

        return result;
    }

    public static class Type implements RecipeType<ColorationRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "coloration";
    }

    public static class Serializer implements RecipeSerializer<ColorationRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MagiChemMod.MODID, "coloration");
        private static final HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(true, true);

        @Override
        public ColorationRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {

            int chargeUsage = GsonHelper.getAsInt(pSerializedRecipe, "charge_usage", 1);
            float craftingTimeMultiplier = GsonHelper.getAsFloat(pSerializedRecipe, "crafting_time_multiplier", 1.0f);

            ItemStack colorlessDefault = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "colorless_default"));
            if(colorlessDefault.getItem() == ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:air")))
                colorlessDefault = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:barrier")));

            JsonArray components = GsonHelper.getAsJsonArray(pSerializedRecipe, "outputs");
            HashMap<DyeColor, ItemStack> extractedOutputs = new HashMap<>();
            components.forEach(element -> {
                String color = element.getAsJsonObject().get("color").getAsString();
                String item = element.getAsJsonObject().get("item").getAsString();

                ItemStack ing = ItemStack.EMPTY;

                Item query = ForgeRegistries.ITEMS.getValue(new ResourceLocation(item));
                if(query != null) {
                    ing = new ItemStack(query);
                } else {
                    MagiChemMod.LOGGER.warn("&&& Couldn't find item \""+item+"\" for color \""+color+"\" in coloration recipe \""+pRecipeId+"\"");
                }

                if(element.getAsJsonObject().has("count"))
                    ing.setCount(element.getAsJsonObject().get("count").getAsInt());
                extractedOutputs.put(DyeColor.byName(color, DyeColor.WHITE), ing);
            });

            return new ColorationRecipe(pRecipeId, chargeUsage, craftingTimeMultiplier, colorlessDefault, extractedOutputs);
        }

        @Override
        public @Nullable ColorationRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {

            CompoundTag nbt = buf.readNbt();
            if(nbt == null) return null;

            int chargeUsage = nbt.getInt("chargeUsage");
            float craftingTimeMultiplier = nbt.getFloat("craftingTimeMultiplier");

            ItemStack colorlessDefault = ItemStack.EMPTY;
            if(nbt.contains("colorlessDefault")) {
                CompoundTag colorlessDefaultTag = nbt.getCompound("colorlessDefault");
                String query = colorlessDefaultTag.getString("item");
                Item queriedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(query));
                int queriedCount = colorlessDefaultTag.getInt("count");

                if(queriedItem != null) colorlessDefault = new ItemStack(queriedItem, queriedCount);
            }

            HashMap<DyeColor, ItemStack> outputs = new HashMap<>();
            if(nbt.contains("outputs")) {
                CompoundTag outputsTag = nbt.getCompound("outputs");

                for (DyeColor color : DyeColor.values()) {
                    if(outputsTag.contains(color.getName())) {
                        CompoundTag thisColorTag = outputsTag.getCompound(color.getName());

                        String query = thisColorTag.getString("item");
                        Item queriedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(query));
                        int queriedCount = thisColorTag.getInt("count");

                        if(queriedItem != null) outputs.put(color, new ItemStack(queriedItem, queriedCount));
                    }
                }
            }

            return new ColorationRecipe(id, chargeUsage, craftingTimeMultiplier, colorlessDefault, outputs);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ColorationRecipe recipe) {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("chargeUsage", recipe.chargeUsage);
            nbt.putFloat("craftingTimeMultiplier", recipe.craftingTimeMultiplier);

            CompoundTag colorlessDefault = new CompoundTag();
            colorlessDefault.putString("item", ForgeRegistries.ITEMS.getKey(recipe.getColorlessDefault().getItem()).toString());
            colorlessDefault.putInt("count", recipe.getColorlessDefault().getCount());
            nbt.put("colorlessDefault", colorlessDefault);

            CompoundTag outputs = new CompoundTag();
            for(DyeColor color : recipe.potentialOutputs.keySet()) {
                ItemStack stack = recipe.potentialOutputs.get(color);

                CompoundTag thisOutput = new CompoundTag();
                thisOutput.putString("item", ForgeRegistries.ITEMS.getKey(stack.getItem()).toString());
                thisOutput.putInt("count", stack.getCount());

                outputs.put(color.getName(), thisOutput);
            }
            nbt.put("outputs", outputs);

            buf.writeNbt(nbt);
        }
    }
}