package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.fluid.AcidFluidType;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.gson.JsonObject;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class VitriolationRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final ItemStack inputItem, resultItem, outputForCodex;
    private final FluidStack resultFluid;
    private final Fluid inputFluidOverride;
    private final int craftTicks, minimumAcidStrength, mBConsumed;
    private static final HashMap<Integer, ArrayList<FluidType>> allAcids = new HashMap();
    private static final HashMap<Integer, ArrayList<Fluid>> allAcidsAsFluids = new HashMap();

    public VitriolationRecipe(ResourceLocation pID, ItemStack pInputItem, ItemStack pResultItem, FluidStack pResultFluid, int pCraftTicks, int pMinimumAcidStrength, int pMBConsumed, Fluid pInputFluidOverride, ItemStack pOutputForCodex) {
        this.id = pID;
        this.inputItem = pInputItem;
        this.resultItem = pResultItem;
        this.resultFluid = pResultFluid;
        this.craftTicks = pCraftTicks;
        this.minimumAcidStrength = pMinimumAcidStrength;
        this.mBConsumed = pMBConsumed;
        this.inputFluidOverride = pInputFluidOverride;
        this.outputForCodex = pOutputForCodex;

        ArrayList<FluidType>[] acidLists = new ArrayList[]{
                new ArrayList<FluidType>(),
                new ArrayList<FluidType>(),
                new ArrayList<FluidType>(),
                new ArrayList<FluidType>(),
                new ArrayList<FluidType>(),
                new ArrayList<FluidType>(),
        };
        acidLists[0].add(Fluids.WATER.getFluidType());

        if(allAcids.size() == 0) {
            for (FluidType ft : ForgeRegistries.FLUID_TYPES.get().getValues()) {
                if (ft instanceof AcidFluidType aft) {
                    acidLists[aft.getAcidStrength()].add(ft);
                }
            }

            for (int i = 0; i <= 5; i++) {
                allAcids.put(i, acidLists[i]);
            }
        }
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        return false;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public ItemStack getInputItem() {
        return inputItem;
    }

    public ItemStack getResultItem() {
        return resultItem;
    }

    public boolean hasResultItem() {
        return !(resultItem == null || resultItem.isEmpty());
    }

    public boolean hasResultFluid() {
        return !(resultFluid == null || resultFluid.isEmpty());
    }

    public FluidStack getResultFluid() {
        return resultFluid;
    }

    public boolean hasInputFluidOverride() {
        return inputFluidOverride != null;
    }

    public Fluid getInputFluidOverride() {
        return inputFluidOverride;
    }

    public int getMinimumAcidStrength() {
        return minimumAcidStrength;
    }

    public boolean canCraftWithAcid(int pAcidStrength) {
        return pAcidStrength >= minimumAcidStrength;
    }

    public int getFluidConsumed(int pAcidStrength) {
        if(pAcidStrength == minimumAcidStrength) return mBConsumed;
        else if(pAcidStrength == minimumAcidStrength + 1) return mBConsumed / 4;
        else if(pAcidStrength >= minimumAcidStrength + 2) return 0;
        return -1;
    }

    public int getBaseFluidConsumed() {
        return mBConsumed;
    }

    public int getCraftTicks() {
        return craftTicks;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        if(outputForCodex == null || outputForCodex.isEmpty())
            return resultItem.copy();
        else
            return outputForCodex;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        if(outputForCodex == null || outputForCodex.isEmpty())
            return resultItem.copy();
        else
            return outputForCodex;
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

    public static VitriolationRecipe getVitriolationRecipe(Level level, Item query) {
        VitriolationRecipe recipeResult = null;
        List<VitriolationRecipe> allRecipes = level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);

        for(VitriolationRecipe ar : allRecipes) {
            if(ar.inputItem.getItem() == query) {
                recipeResult = ar;
                break;
            }
        }

        return recipeResult;
    }

    public static List<VitriolationRecipe> getAllVitriolationRecipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(Type.INSTANCE);
    }

    public static ArrayList<FluidType> getAllFluidTypesOfAcidStrength(int pStrength) {
        return allAcids.get(pStrength);
    }

    public static ArrayList<Fluid> getAllFluidsOfAcidStrength(int pStrength) {
        if(allAcidsAsFluids.size() > 0) return allAcidsAsFluids.get(pStrength);

        final Collection<Fluid> allFluids = ForgeRegistries.FLUIDS.getValues();
        for(int i=1; i<=5; i++) {
            ArrayList<Fluid> out = new ArrayList<>();
            for (FluidType ft : allAcids.get(i)) {
                for (Fluid f : allFluids) {
                    if (f.getFluidType() == ft) {
                        out.add(f);
                    }
                }
            }
            allAcidsAsFluids.put(i, out);
        }

        return allAcidsAsFluids.get(pStrength);
    }

    public static boolean isFluidOfAcidStrength(Fluid pFluid, int pStrength) {
        return allAcids.get(pStrength).contains(pFluid.getFluidType());
    }

    public static boolean isFluidAcid(Fluid fluid) {
        boolean isAcid = false;

        for(ArrayList<FluidType> listByStrength : allAcids.values()) {
            for(FluidType ft : listByStrength) {
                isAcid = fluid.getFluidType() == ft;
                if(isAcid) break;
            }
            if(isAcid) break;
        }

        return isAcid;
    }

    public static int getFluidAcidStrengthDifference(Fluid pFluid, int pTargetStrength) {
        int myStrength = -1;
        for(int i=0; i<=5; i++) {
            if(allAcids.get(i).contains(pFluid.getFluidType()) && pFluid.getFluidType() instanceof AcidFluidType aft) {
                myStrength = aft.getAcidStrength();
                break;
            }
        }
        if(myStrength == -1) return -1;

        return pTargetStrength - myStrength;
    }

    public static class Type implements RecipeType<VitriolationRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "vitriolation";
    }

    public static class Serializer implements RecipeSerializer<VitriolationRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(MagiChemMod.MODID, "vitriolation");

        @Override
        public VitriolationRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {

            int craftTicks = GsonHelper.getAsInt(pSerializedRecipe, "duration");
            int minimumAcidStrength = GsonHelper.getAsInt(pSerializedRecipe, "minimumAcidStrength");
            int mBConsumed = GsonHelper.getAsInt(pSerializedRecipe, "acidConsumed");

            JsonObject inputItemObject = GsonHelper.getAsJsonObject(pSerializedRecipe, "inputItem");
            JsonObject resultItemObject = GsonHelper.getAsJsonObject(pSerializedRecipe, "resultItem", null);
            JsonObject resultFluidObject = GsonHelper.getAsJsonObject(pSerializedRecipe, "resultFluid", null);
            String outputForCodexString = GsonHelper.getAsString(pSerializedRecipe, "outputForCodex", null);
            String inputFluidOverrideString = GsonHelper.getAsString(pSerializedRecipe, "inputFluidOverride", null);

            String inputItemRL = GsonHelper.getAsString(inputItemObject, "item");
            int inputItemCount = GsonHelper.getAsInt(inputItemObject, "count");
            Item inputItemAsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(inputItemRL));

            Item resultItemAsItem = null;
            String resultItemRL = null;
            int resultItemCount = 0;
            if(resultItemObject != null) {
                resultItemRL = GsonHelper.getAsString(resultItemObject, "item");
                resultItemCount = GsonHelper.getAsInt(resultItemObject, "count");
                resultItemAsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(resultItemRL));
            }

            Fluid resultFluidAsFluid = null;
            String resultFluidRL = null;
            int resultFluidCount = 0;
            if(resultFluidObject != null) {
                resultFluidRL = GsonHelper.getAsString(resultFluidObject, "fluid");
                resultFluidCount = GsonHelper.getAsInt(resultFluidObject, "amount");
                resultFluidAsFluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(resultFluidRL));
            }

            Fluid inputFluidOverrideAsFluid = null;
            if(inputFluidOverrideString != null) {
                inputFluidOverrideAsFluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(inputFluidOverrideString));
            }

            Item outputForCodexAsItem = null;
            if(outputForCodexString != null) {
                outputForCodexAsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(outputForCodexString));
            }

            return new VitriolationRecipe(pRecipeId,
                    new ItemStack(inputItemAsItem, inputItemCount),
                    resultItemAsItem == null ? ItemStack.EMPTY : new ItemStack(resultItemAsItem, resultItemCount),
                    resultFluidAsFluid == null ? FluidStack.EMPTY : new FluidStack(resultFluidAsFluid, resultFluidCount),
                    craftTicks, minimumAcidStrength, mBConsumed,
                    inputFluidOverrideAsFluid,
                    outputForCodexAsItem == null ? ItemStack.EMPTY : new ItemStack(outputForCodexAsItem)
            );
        }

        @Override
        public @Nullable VitriolationRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            CompoundTag nbt = pBuffer.readNbt();

            int craftTicks = nbt.getInt("craftTicks");
            int minimumAcidStrength = nbt.getInt("minimumAcidStrength");
            int mBConsumed = nbt.getInt("mBConsumed");

            CompoundTag inputItemTag = nbt.getCompound("inputItem");
            Item inputAsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(inputItemTag.getString("item")));
            int inputCount = inputItemTag.getInt("count");

            boolean hasResultItem = nbt.contains("resultItem");
            Item resultItemAsItem = null;
            int resultItemCount = 0;
            if(hasResultItem) {
                CompoundTag resultItemTag = nbt.getCompound("resultItem");
                resultItemAsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(resultItemTag.getString("item")));
                resultItemCount = resultItemTag.getInt("count");
            }

            boolean hasResultFluid = nbt.contains("resultFluid");
            Fluid resultFluidAsFluid = null;
            int resultFluidCount = 0;
            if(hasResultFluid) {
                CompoundTag resultFluidTag = nbt.getCompound("resultFluid");
                resultFluidAsFluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(resultFluidTag.getString("fluid")));
                resultFluidCount = resultFluidTag.getInt("count");
                new FluidStack(resultFluidAsFluid, 1000);
            }

            boolean hasInputFluidOverride = nbt.contains("inputFluidOverride");
            Fluid inputFluidOverrideAsFluid = null;
            if(hasInputFluidOverride) {
                inputFluidOverrideAsFluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(nbt.getString("inputFluidOverride")));
            }

            boolean hasOutputForCodex = nbt.contains("outputForCodex");
            Item outputForCodexAsItem = null;
            if(hasOutputForCodex) {
                outputForCodexAsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("outputForCodex")));
            }

            return new VitriolationRecipe(pRecipeId,
                    inputAsItem == null ? ItemStack.EMPTY : new ItemStack(inputAsItem, inputCount),
                    !hasResultItem ? ItemStack.EMPTY : new ItemStack(resultItemAsItem, resultItemCount),
                    !hasResultFluid ? FluidStack.EMPTY : new FluidStack(resultFluidAsFluid, resultFluidCount),
                    craftTicks, minimumAcidStrength, mBConsumed,
                    inputFluidOverrideAsFluid,
                    !hasOutputForCodex ? ItemStack.EMPTY : new ItemStack(outputForCodexAsItem)
            );
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, VitriolationRecipe pRecipe) {
            CompoundTag nbt = new CompoundTag();

            CompoundTag inputItemTag = new CompoundTag();
            inputItemTag.putString("item", ForgeRegistries.ITEMS.getKey(pRecipe.inputItem.getItem()).toString());
            inputItemTag.putInt("count", pRecipe.inputItem.getCount());
            nbt.put("inputItem", inputItemTag);

            if(pRecipe.hasResultItem()) {
                CompoundTag resultItemTag = new CompoundTag();
                resultItemTag.putString("item", ForgeRegistries.ITEMS.getKey(pRecipe.resultItem.getItem()).toString());
                resultItemTag.putInt("count", pRecipe.resultItem.getCount());
                nbt.put("resultItem", resultItemTag);
            }

            if(pRecipe.hasResultFluid()) {
                CompoundTag resultFluidTag = new CompoundTag();
                resultFluidTag.putString("fluid", ForgeRegistries.FLUIDS.getKey(pRecipe.resultFluid.getFluid()).toString());
                resultFluidTag.putInt("count", pRecipe.resultFluid.getAmount());
                nbt.put("resultFluid", resultFluidTag);
            }

            if(pRecipe.hasInputFluidOverride()) {
                nbt.putString("inputFluidOverride", ForgeRegistries.FLUIDS.getKey(pRecipe.inputFluidOverride).toString());
            }

            nbt.putInt("craftTicks", pRecipe.craftTicks);
            nbt.putInt("mBConsumed", pRecipe.mBConsumed);
            nbt.putInt("minimumAcidStrength", pRecipe.minimumAcidStrength);

            if(pRecipe.outputForCodex != null && !pRecipe.outputForCodex.isEmpty()) {
                nbt.putString("outputForCodex", ForgeRegistries.ITEMS.getKey(pRecipe.outputForCodex.getItem()).toString());
            }

            pBuffer.writeNbt(nbt);
        }
    }
}
