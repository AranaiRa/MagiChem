package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mna.api.recipes.IMARecipe;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This recipe type is used by the Coloring Cauldron and the Variegator.
 */
public class ColorationRecipe implements Recipe<SimpleContainer>, IMARecipe {
    private final ResourceLocation id;
    private final int chargeUsage;
    private final float craftingTimeMultiplier;
    private final boolean validOnCauldron, validOnVariegator;
    private final ItemStack colorlessDefault;
    private final HashMap<DyeColor, ItemStack> potentialOutputs;
    private final boolean nbtAware, preserveNbtDefault;
    private final EnumMap<DyeColor, Boolean> preserveNbtByColor;
    private static final ArrayList<ColorationRecipe> ALL_COLORATION_RECIPES = new ArrayList<>();
    private static final ArrayList<Item> ALL_INPUT_ITEMS = new ArrayList<>();

    public ColorationRecipe(ResourceLocation id, int pChargeUsage, float pCraftingTimeMultiplier,
                            boolean pValidOnCauldron, boolean pValidOnVariegator,
                            ItemStack pColorlessDefault,
                            HashMap<DyeColor, ItemStack> pPotentialOutputs) {
        this(id, pChargeUsage, pCraftingTimeMultiplier, pValidOnCauldron,
                pValidOnVariegator, pColorlessDefault, pPotentialOutputs,
                false, false, new EnumMap<>(DyeColor.class));
    }

    public ColorationRecipe(ResourceLocation id, int pChargeUsage, float pCraftingTimeMultiplier,
                            boolean pValidOnCauldron, boolean pValidOnVariegator,
                            ItemStack pColorlessDefault, HashMap<DyeColor, ItemStack> pPotentialOutputs,
                            boolean pNbtAware, boolean pPreserveNbtDefault,
                            EnumMap<DyeColor, Boolean> pPreserveNbtByColor) {
        this.id = id;
        this.chargeUsage = pChargeUsage;
        this.craftingTimeMultiplier = pCraftingTimeMultiplier;
        this.validOnCauldron = pValidOnCauldron;
        this.validOnVariegator = pValidOnVariegator;
        this.colorlessDefault = pColorlessDefault;
        this.potentialOutputs = pPotentialOutputs;
        this.nbtAware = pNbtAware;
        this.preserveNbtDefault = pPreserveNbtDefault;
        this.preserveNbtByColor = pPreserveNbtByColor;
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

    public HashMap<DyeColor, ItemStack> getResultsAsMap(boolean copy) {
        if(!copy)
            return potentialOutputs;

        HashMap<DyeColor, ItemStack> resultsCopy = new HashMap<>();
        for(DyeColor color : potentialOutputs.keySet()) {
            resultsCopy.put(color, potentialOutputs.get(color).copy());
        }
        return resultsCopy;
    }

    public ItemStack getColorlessDefault() {
        return colorlessDefault.copy();
    }

    public int getChargeUsage() {
        return chargeUsage;
    }

    public float getCraftingTimeMultiplier() {
        return craftingTimeMultiplier;
    }

    public boolean isValidOnCauldron() {
        return validOnCauldron;
    }

    public boolean isValidOnVariegator() {
        return validOnVariegator;
    }

    public boolean isNbtAware() {
        return nbtAware;
    }

    public boolean isPreserveNbtDefault() {
        return preserveNbtDefault;
    }

    public boolean isPreserveNbt(DyeColor color) {
        return preserveNbtByColor.getOrDefault(color, preserveNbtDefault);
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
        if(level.getRecipeManager().getAllRecipesFor(ColorationRecipe.Type.INSTANCE)
                .stream().anyMatch(ColorationRecipe::isNbtAware))
            return ColorationNbtHelper.findRecipe(level, query, null);

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

    public static ColorationRecipe getFilteredColorationRecipe(Level level, ItemStack query, boolean filterForVariegator) {
        if(level.getRecipeManager().getAllRecipesFor(ColorationRecipe.Type.INSTANCE)
                .stream().anyMatch(ColorationRecipe::isNbtAware))
            return ColorationNbtHelper.findRecipe(level, query, filterForVariegator);

        ColorationRecipe result = null;
        List<ColorationRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(ColorationRecipe.Type.INSTANCE);

        for(ColorationRecipe cr : allRecipes) {
            if(cr.colorlessDefault.getItem() == query.getItem()) {
                if((filterForVariegator && cr.isValidOnVariegator()) || (!filterForVariegator && cr.isValidOnCauldron())) {
                    result = cr;
                    break;
                }
            }
            for(DyeColor color : cr.potentialOutputs.keySet()) {
                if(cr.potentialOutputs.get(color).getItem() == query.getItem()) {
                    if((filterForVariegator && cr.isValidOnVariegator()) || (!filterForVariegator && cr.isValidOnCauldron())) {
                        result = cr;
                        break;
                    }
                }
            }
            if(result != null)
                break;
        }

        return result;
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.id;
    }

    @Override
    public ItemStack getResultItem() {
        return getColorlessDefault();
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        return getColorlessDefault();
    }

    @Override
    public int getTier() {
        return validOnCauldron ? 1 : 3;
    }

    public static ArrayList<ColorationRecipe> getAllColorationRecipes(Level level) {
        if(ALL_COLORATION_RECIPES.size() == 0) {
            List<ColorationRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);
            ALL_COLORATION_RECIPES.addAll(allRecipes);
        }

        return ALL_COLORATION_RECIPES;
    }

    public static ArrayList<Item> getAllRecipeInputItems(Level level) {
        if(ALL_INPUT_ITEMS.size() == 0) {
            if (ALL_COLORATION_RECIPES.size() == 0) getAllColorationRecipes(level);

            for (ColorationRecipe cr : ALL_COLORATION_RECIPES) {
                for (ItemStack stack : cr.getResultsAsList()) {
                    if(!ALL_INPUT_ITEMS.contains(stack.getItem())) ALL_INPUT_ITEMS.add(stack.getItem());
                }
                if(!ALL_INPUT_ITEMS.contains(cr.getColorlessDefault().getItem()))
                    ALL_INPUT_ITEMS.add(cr.getColorlessDefault().getItem());
            }
        }

        return ALL_INPUT_ITEMS;
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
            boolean validOnCauldron = GsonHelper.getAsBoolean(pSerializedRecipe, "valid_on_cauldron", true);
            boolean validOnVariegator = GsonHelper.getAsBoolean(pSerializedRecipe, "valid_on_variegator", true);

            JsonObject colorlessObject = GsonHelper.getAsJsonObject(pSerializedRecipe, "colorless_default");
            if(colorlessObject.has("preserve_nbt"))
                throw RecipeNbtHelper.error(pRecipeId, "colorless_default cannot define preserve_nbt");

            boolean preserveNbtDefault = RecipeNbtHelper.readOptionalBoolean(pSerializedRecipe, pRecipeId);
            boolean nbtAware = pSerializedRecipe.has("preserve_nbt") || colorlessObject.has("nbt");
            ItemStack colorlessDefault = ShapedRecipe.itemStackFromJson(colorlessObject);
            if(colorlessDefault.getItem() == ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:air")))
                colorlessDefault = new ItemStack(ItemRegistry.PROBLEMITE.get());
            RecipeOutputHelper.applyNbt(colorlessDefault, colorlessObject, pRecipeId);

            JsonArray components = GsonHelper.getAsJsonArray(pSerializedRecipe, "outputs");
            HashMap<DyeColor, ItemStack> extractedOutputs = new HashMap<>();
            EnumMap<DyeColor, Boolean> preserveNbtByColor = new EnumMap<>(DyeColor.class);
            components.forEach(element -> {
                JsonObject outputObject = element.getAsJsonObject();
                String color = outputObject.get("color").getAsString();
                String item = outputObject.get("item").getAsString();
                DyeColor dyeColor = DyeColor.byName(color, null);
                if(dyeColor == null)
                    throw RecipeNbtHelper.error(pRecipeId, "unknown color '" + color + "'");
                if(extractedOutputs.containsKey(dyeColor))
                    throw RecipeNbtHelper.error(pRecipeId, "color '" + color + "' is declared more than once");

                ItemStack ing;

                Item query = ForgeRegistries.ITEMS.getValue(new ResourceLocation(item));
                if(query != null && query != Items.AIR) {
                    ing = new ItemStack(query);
                } else {
                    ing = new ItemStack(ItemRegistry.PROBLEMITE.get());
                    MagiChemMod.LOGGER.warn("&&& Couldn't find item \""+item+"\" for color \""+color+"\" in coloration recipe \""+pRecipeId+"\"");
                }

                if(outputObject.has("count"))
                    ing.setCount(outputObject.get("count").getAsInt());
                RecipeOutputHelper.applyNbt(ing, outputObject, pRecipeId);
                boolean preserve = outputObject.has("preserve_nbt")
                        ? RecipeNbtHelper.readOptionalBoolean(outputObject, pRecipeId)
                        : preserveNbtDefault;
                preserveNbtByColor.put(dyeColor, preserve);
                extractedOutputs.put(dyeColor, ing);
            });

            for(JsonElement element : components) {
                JsonObject outputObject = element.getAsJsonObject();
                nbtAware |= outputObject.has("nbt") || outputObject.has("preserve_nbt");
            }

            ColorationRecipe recipe = new ColorationRecipe(pRecipeId, chargeUsage,
                    craftingTimeMultiplier, validOnCauldron, validOnVariegator,
                    colorlessDefault, extractedOutputs, nbtAware, preserveNbtDefault,
                    preserveNbtByColor);
            ColorationNbtHelper.validate(recipe);
            return recipe;
        }

        @Override
        public @Nullable ColorationRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {

            CompoundTag nbt = buf.readNbt();
            if(nbt == null) return null;

            int chargeUsage = nbt.getInt("chargeUsage");
            float craftingTimeMultiplier = nbt.getFloat("craftingTimeMultiplier");
            boolean validOnCauldron = nbt.getBoolean("validOnCauldron");
            boolean validOnVariegator = nbt.getBoolean("validOnVariegator");

            ItemStack colorlessDefault = ItemStack.EMPTY;
            if(nbt.contains("colorlessDefault")) {
                colorlessDefault = ItemStack.of(nbt.getCompound("colorlessDefault"));
            }

            HashMap<DyeColor, ItemStack> outputs = new HashMap<>();
            EnumMap<DyeColor, Boolean> preserveNbtByColor = new EnumMap<>(DyeColor.class);
            if(nbt.contains("outputs")) {
                CompoundTag outputsTag = nbt.getCompound("outputs");

                for (DyeColor color : DyeColor.values()) {
                    if(outputsTag.contains(color.getName())) {
                        CompoundTag thisColorTag = outputsTag.getCompound(color.getName());

                        outputs.put(color, ItemStack.of(thisColorTag.getCompound("stack")));
                        preserveNbtByColor.put(color, thisColorTag.getBoolean("preserveNbt"));
                    }
                }
            }

            return new ColorationRecipe(id, chargeUsage, craftingTimeMultiplier,
                    validOnCauldron, validOnVariegator, colorlessDefault, outputs,
                    nbt.getBoolean("nbtAware"), nbt.getBoolean("preserveNbtDefault"),
                    preserveNbtByColor);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ColorationRecipe recipe) {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt("chargeUsage", recipe.chargeUsage);
            nbt.putFloat("craftingTimeMultiplier", recipe.craftingTimeMultiplier);
            nbt.putBoolean("validOnCauldron", recipe.validOnCauldron);
            nbt.putBoolean("validOnVariegator", recipe.validOnVariegator);

            nbt.putBoolean("nbtAware", recipe.nbtAware);
            nbt.putBoolean("preserveNbtDefault", recipe.preserveNbtDefault);
            nbt.put("colorlessDefault", recipe.getColorlessDefault().save(new CompoundTag()));

            CompoundTag outputs = new CompoundTag();
            for(DyeColor color : recipe.potentialOutputs.keySet()) {
                ItemStack stack = recipe.potentialOutputs.get(color);

                CompoundTag thisOutput = new CompoundTag();
                thisOutput.put("stack", stack.save(new CompoundTag()));
                thisOutput.putBoolean("preserveNbt", recipe.isPreserveNbt(color));

                outputs.put(color.getName(), thisOutput);
            }
            nbt.put("outputs", outputs);

            buf.writeNbt(nbt);
        }
    }
}
