package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.enums.DistillationSourceCategory;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mna.api.recipes.IMARecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

/**
 * This recipe type is used by both the Circle of Fabrication in a straightforward manner and the Alembic/Distillery in reverse.
 */
public class FluidDistillationFabricationRecipe implements Recipe<SimpleContainer>, IMARecipe {
    private final ResourceLocation id;
    private final FluidStack alchemyFluid;
    private final ItemStack bucket;
    private final NonNullList<ItemStack> componentMateria;
    private final byte wisdom, categories, batchSize;
    private final float outputRate;
    private final ResourceLocation requiredAdvancement, forbiddenAdvancement;

    public FluidDistillationFabricationRecipe(ResourceLocation pID, FluidStack pAlchemyFluid,
                                              NonNullList<ItemStack> pComponentMateria, byte pWisdom, byte pCategories, byte pBatchSize, float pOutputRate,
                                              ResourceLocation pRequiredAdvancement, ResourceLocation pForbiddenAdvancement) {
        this.id = pID;
        this.alchemyFluid = pAlchemyFluid;
        this.componentMateria = pComponentMateria;
        this.wisdom = pWisdom;
        this.categories = pCategories;
        this.outputRate = pOutputRate;
        this.batchSize = pBatchSize;
        this.requiredAdvancement = pRequiredAdvancement;
        this.forbiddenAdvancement = pForbiddenAdvancement;
        this.bucket = new ItemStack(alchemyFluid.getFluid().getBucket());
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

    public byte getWisdom() {
        return wisdom;
    }

    public byte getBatchSize() {
        return batchSize;
    }

    public boolean getIsDistillOnly() {
        return wisdom > 5;
    }

    public FluidStack getAlchemyFluid() {
        return alchemyFluid;
    }

    public float getOutputRate() { return outputRate; }

    public boolean isAdvancementRequired() {
        return requiredAdvancement != null;
    }

    public ResourceLocation getRequiredAdvancement() {
        return requiredAdvancement;
    }

    public boolean isForbiddenByAdvancement() {
        return forbiddenAdvancement != null;
    }

    public ResourceLocation getForbiddenAdvancement() {
        return forbiddenAdvancement;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return ItemStack.EMPTY;
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

    public static FluidDistillationFabricationRecipe getDistillingRecipe(Level level, FluidStack query) {
        return getDistillingRecipe(level, query.getFluid());
    }

    public static FluidDistillationFabricationRecipe getDistillingRecipe(Level level, Fluid query) {
        FluidDistillationFabricationRecipe result = null;
        List<FluidDistillationFabricationRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);

        for(FluidDistillationFabricationRecipe acr : allRecipes) {
            if(acr.alchemyFluid.getFluid() == query) {
                result = acr;
                break;
            }
        }

        return result;
    }

    public static List<FluidDistillationFabricationRecipe> getAllDistillingRecipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);
    }

    public static FluidDistillationFabricationRecipe getFabricatingRecipe(Level level, FluidStack query) {
        return getFabricatingRecipe(level, query.getFluid());
    }

    public static FluidDistillationFabricationRecipe getFabricatingRecipe(Level level, Fluid query) {
        FluidDistillationFabricationRecipe result = null;
        List<FluidDistillationFabricationRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);

        for(FluidDistillationFabricationRecipe acr : allRecipes) {
            if(acr.alchemyFluid.getFluid() == query) {
                result = acr;
                if(acr.wisdom > 5)
                    return null;
                break;
            }
        }

        return result;
    }

    public static final byte
        FLAG_CRAFTABLE = 1, FLAG_GATHERABLE = 2, FLAG_FARMABLE = 4, FLAG_RENEWABLE = 8, FLAG_TROPHY = 16, FLAG_RARE = 32;
    public boolean hasSourceCategory(DistillationSourceCategory pSource) {
        if(pSource == DistillationSourceCategory.CRAFTABLE) return (categories & FLAG_CRAFTABLE) == FLAG_CRAFTABLE;
        if(pSource == DistillationSourceCategory.GATHERABLE) return (categories & FLAG_GATHERABLE) == FLAG_GATHERABLE;
        if(pSource == DistillationSourceCategory.FARMABLE) return (categories & FLAG_FARMABLE) == FLAG_FARMABLE;
        if(pSource == DistillationSourceCategory.RENEWABLE) return (categories & FLAG_RENEWABLE) == FLAG_RENEWABLE;
        if(pSource == DistillationSourceCategory.TROPHY) return (categories & FLAG_TROPHY) == FLAG_TROPHY;
        if(pSource == DistillationSourceCategory.RARE) return (categories & FLAG_RARE) == FLAG_RARE;
        return false;
    }

    public NonNullList<DistillationSourceCategory> getSourceCategories() {
        NonNullList<DistillationSourceCategory> out = NonNullList.create();

        if((categories & FLAG_CRAFTABLE) == FLAG_CRAFTABLE) out.add(DistillationSourceCategory.CRAFTABLE);
        if((categories & FLAG_GATHERABLE) == FLAG_GATHERABLE) out.add(DistillationSourceCategory.GATHERABLE);
        if((categories & FLAG_FARMABLE) == FLAG_FARMABLE) out.add(DistillationSourceCategory.FARMABLE);
        if((categories & FLAG_RENEWABLE) == FLAG_RENEWABLE) out.add(DistillationSourceCategory.RENEWABLE);
        if((categories & FLAG_TROPHY) == FLAG_TROPHY) out.add(DistillationSourceCategory.TROPHY);
        if((categories & FLAG_RARE) == FLAG_RARE) out.add(DistillationSourceCategory.RARE);

        return out;
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.id;
    }

    @Override
    public ItemStack getResultItem() {
        return bucket;
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        return bucket;
    }

    @Override
    public int getTier() {
        return Math.min(5, wisdom + 1);
    }

    public static class Type implements RecipeType<FluidDistillationFabricationRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "fluid_distillation_fabrication";
    }

    public static class Serializer implements RecipeSerializer<FluidDistillationFabricationRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MagiChemMod.MODID, "fluid_distillation_fabrication");
        private static final HashMap<String, MateriaItem> materiaMap = ItemRegistry.getMateriaMap(true, true);

        @Override
        public FluidDistillationFabricationRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {

            String fluidRL = GsonHelper.getAsString(pSerializedRecipe, "fluid");

            final Fluid fluidType = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidRL));
            FluidStack recipeFluid = FluidStack.EMPTY;
            if(fluidType != null) {
                recipeFluid = new FluidStack(fluidType, 1000);
            }

            byte wisdom = GsonHelper.getAsByte(pSerializedRecipe, "wisdom");
            byte categories = GsonHelper.getAsByte(pSerializedRecipe, "categories");
            byte batchSize = GsonHelper.getAsByte(pSerializedRecipe, "batch_size");
            float rate = GsonHelper.getAsFloat(pSerializedRecipe, "output_rate");

            ResourceLocation requiredAdvancementRL = null;
            if(pSerializedRecipe.has("required_advancement"))
                requiredAdvancementRL = new ResourceLocation(GsonHelper.getAsString(pSerializedRecipe, "required_advancement"));

            ResourceLocation forbiddenAdvancementRL = null;
            if(pSerializedRecipe.has("forbidden_advancement"))
                forbiddenAdvancementRL = new ResourceLocation(GsonHelper.getAsString(pSerializedRecipe, "forbidden_advancement"));

            JsonArray components = GsonHelper.getAsJsonArray(pSerializedRecipe, "components");
            NonNullList<ItemStack> extractedIngredients = NonNullList.create();
            components.forEach(element -> {
                String key = element.getAsJsonObject().get("item").getAsString();

                ItemStack ing = ItemStack.EMPTY;

                MateriaItem matQuery = materiaMap.get(key);
                if(matQuery != null) {
                    ing = new ItemStack(matQuery);
                } else {
                    MagiChemMod.LOGGER.warn("&&& Couldn't find materia \""+key+"\" for distillation_fabrication recipe \""+pRecipeId);
                }

                if(element.getAsJsonObject().has("count"))
                    ing.setCount(element.getAsJsonObject().get("count").getAsInt());
                extractedIngredients.add(ing);
            });

            if(categories == 0) {
                MagiChemMod.LOGGER.warn("Fluid Distillation/Fabrication recipe for \""+pRecipeId.getNamespace()+":"+pRecipeId.getPath()+"\" has no assigned Categories. Is this intentional?");
            }

            return new FluidDistillationFabricationRecipe(pRecipeId, recipeFluid, extractedIngredients, wisdom, categories, batchSize, rate, requiredAdvancementRL, forbiddenAdvancementRL);
        }

        @Override
        public @Nullable FluidDistillationFabricationRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            CompoundTag nbt = buf.readNbt();
            if(nbt == null) return null;

            byte readWisdom = nbt.getByte("wisdom");
            byte readCategories = nbt.getByte("categories");
            byte readBatchSize = nbt.getByte("batchSize");
            float readOutputRate = nbt.getFloat("outputRate");
            ResourceLocation alchemyFluidRL = new ResourceLocation(nbt.getString("alchemyFluid"));
            Fluid alchemyFluidFluid = ForgeRegistries.FLUIDS.getValue(alchemyFluidRL);
            FluidStack alchemyFluid = FluidStack.EMPTY;
            if(alchemyFluidFluid != null)
                alchemyFluid = new FluidStack(alchemyFluidFluid, 1000);
            int componentTotal = nbt.getInt("componentCount");

            ResourceLocation requiredAdvancementRL = null;
            if(nbt.contains("requiredAdvancement"))
                requiredAdvancementRL = new ResourceLocation(nbt.getString("requiredAdvancement"));
            ResourceLocation forbiddenAdvancementRL = null;
            if(nbt.contains("forbiddenAdvancement"))
                forbiddenAdvancementRL = new ResourceLocation(nbt.getString("forbiddenAdvancement"));

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

            return new FluidDistillationFabricationRecipe(id, alchemyFluid, readComponentMateria, readWisdom, readCategories, readBatchSize, readOutputRate, requiredAdvancementRL, forbiddenAdvancementRL);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, FluidDistillationFabricationRecipe recipe) {
            CompoundTag nbt = new CompoundTag();
            nbt.putByte("wisdom", recipe.wisdom);
            nbt.putByte("categories", recipe.categories);
            nbt.putByte("batchSize", recipe.batchSize);
            if(recipe.requiredAdvancement != null)
                nbt.putString("requiredAdvancement", recipe.requiredAdvancement.toString());
            if(recipe.forbiddenAdvancement != null)
                nbt.putString("forbiddenAdvancement", recipe.forbiddenAdvancement.toString());
            nbt.putFloat("outputRate", recipe.outputRate);
            nbt.putString("alchemyFluid", ForgeRegistries.FLUIDS.getKey(recipe.getAlchemyFluid().getFluid()).toString());
            nbt.putInt("componentCount", recipe.componentMateria.size());
            for(int i=0;i<recipe.componentMateria.size(); i++) {
                nbt.putString("component"+i+"Item", ForgeRegistries.ITEMS.getKey(recipe.componentMateria.get(i).getItem()).toString());
                nbt.putInt("component"+i+"Count", recipe.componentMateria.get(i).getCount());
            }

            buf.writeNbt(nbt);
        }
    }
}