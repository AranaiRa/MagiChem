package com.aranaira.magichem.recipe;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.fluid.AcidFluidType;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.google.gson.JsonObject;
import com.klikli_dev.modonomicon.fluid.ForgeFluidHelper;
import com.mna.api.recipes.IMARecipe;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
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

public class VitriolationRecipe implements Recipe<SimpleContainer>, IMARecipe, NbtPreservingRecipe {
    private final ResourceLocation id;
    private final ItemStack inputItem, resultItem, outputForCodex;
    private final FluidStack resultFluid;
    private final Fluid inputFluidOverride;
    @Nullable
    private final Item nbtSource;
    private final int craftTicks, minimumAcidStrength, mBConsumed;
    private static final HashMap<Integer, ArrayList<FluidType>> allAcids = new HashMap();
    private static final HashMap<Integer, ArrayList<Fluid>> allAcidsAsFluids = new HashMap();

    public VitriolationRecipe(ResourceLocation pID, ItemStack pInputItem, ItemStack pResultItem, FluidStack pResultFluid, int pCraftTicks, int pMinimumAcidStrength, int pMBConsumed, Fluid pInputFluidOverride, ItemStack pOutputForCodex) {
        this(pID, pInputItem, pResultItem, pResultFluid, pCraftTicks, pMinimumAcidStrength,
                pMBConsumed, pInputFluidOverride, pOutputForCodex, null);
    }

    public VitriolationRecipe(ResourceLocation pID, ItemStack pInputItem, ItemStack pResultItem, FluidStack pResultFluid, int pCraftTicks, int pMinimumAcidStrength, int pMBConsumed, Fluid pInputFluidOverride, ItemStack pOutputForCodex, @Nullable Item pNbtSource) {
        this.id = pID;
        this.inputItem = pInputItem;
        this.resultItem = pResultItem;
        this.resultFluid = pResultFluid;
        this.craftTicks = pCraftTicks;
        this.minimumAcidStrength = pMinimumAcidStrength;
        this.mBConsumed = pMBConsumed;
        this.inputFluidOverride = pInputFluidOverride;
        this.outputForCodex = pOutputForCodex;
        this.nbtSource = pNbtSource;

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

    @Override
    public @Nullable Item getNbtSource() {
        return nbtSource;
    }

    @Override
    public ResourceLocation getRegistryId() {
        return this.id;
    }

    @Override
    public ItemStack getResultItem() {
        return hasResultItem() ? resultItem : new ItemStack(resultFluid.getFluid().getBucket());
    }

    @Override
    public ItemStack getGuiRepresentationStack() {
        return hasResultItem() ? resultItem : new ItemStack(resultFluid.getFluid().getBucket());
    }

    @Override
    public int getTier() {
        return hasInputFluidOverride() ? 1 : minimumAcidStrength;
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

        return myStrength - pTargetStrength;
    }

    public static int getFluidAcidStrength(Fluid pFluid) {
        int myStrength = -1;
        for(int i=0; i<=5; i++) {
            if(allAcids.get(i).contains(pFluid.getFluidType()) && pFluid.getFluidType() instanceof AcidFluidType aft) {
                myStrength = aft.getAcidStrength();
                break;
            }
        }
        return myStrength;
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

            if(inputItemAsItem == null || inputItemAsItem == Items.AIR) {
                inputItemAsItem = ItemRegistry.PROBLEMITE.get();
                MagiChemMod.LOGGER.warn("&&&&& Vitriolation recipe couldn't find input item \""+inputItemRL.toString()+"\"!");
            }

            Item resultItemAsItem = null;
            String resultItemRL = null;
            int resultItemCount = 0;
            if(resultItemObject != null) {
                resultItemRL = GsonHelper.getAsString(resultItemObject, "item");
                resultItemCount = GsonHelper.getAsInt(resultItemObject, "count");
                resultItemAsItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(resultItemRL));

                if(resultItemAsItem == null || resultItemAsItem == Items.AIR) {
                    resultItemAsItem = ItemRegistry.PROBLEMITE.get();
                    MagiChemMod.LOGGER.warn("&&&&& Vitriolation recipe couldn't find result item \""+resultItemRL.toString()+"\"!");
                }
            }

            Fluid resultFluidAsFluid = null;
            String resultFluidRL = null;
            int resultFluidCount = 0;
            if(resultFluidObject != null) {
                resultFluidRL = GsonHelper.getAsString(resultFluidObject, "fluid");
                resultFluidCount = GsonHelper.getAsInt(resultFluidObject, "amount");
                resultFluidAsFluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(resultFluidRL));

                if(resultFluidAsFluid == null || resultFluidAsFluid == Fluids.EMPTY) MagiChemMod.LOGGER.warn("&&&&& Vitriolation recipe couldn't find result fluid \""+resultFluidRL.toString()+"\"!");
            }

            Fluid inputFluidOverrideAsFluid = null;
            if(inputFluidOverrideString != null) {
                ResourceLocation inputFluidOverrideRL = new ResourceLocation(inputFluidOverrideString);
                inputFluidOverrideAsFluid = ForgeRegistries.FLUIDS.getValue(inputFluidOverrideRL);

                if(inputFluidOverrideAsFluid == null || inputFluidOverrideAsFluid == Fluids.EMPTY) MagiChemMod.LOGGER.warn("&&&&& Vitriolation recipe couldn't find input fluid override \""+inputFluidOverrideRL.toString()+"\"!");
            }

            Item outputForCodexAsItem = null;
            if(outputForCodexString != null) {
                ResourceLocation outputForCodexRL = new ResourceLocation(outputForCodexString);
                outputForCodexAsItem = ForgeRegistries.ITEMS.getValue(outputForCodexRL);
            }

            if(outputForCodexAsItem == null || outputForCodexAsItem == Items.AIR) {
                if(resultItemAsItem != null && resultItemAsItem != Items.AIR) {
                    outputForCodexAsItem = resultItemAsItem;
                } else if(resultFluidAsFluid != null && resultFluidAsFluid != Fluids.EMPTY) {
                    outputForCodexAsItem = resultFluidAsFluid.getBucket();
                } else {
                    outputForCodexAsItem = ItemRegistry.PROBLEMITE.get();
                }
            }

            ItemStack resultItem = resultItemAsItem == null
                    ? ItemStack.EMPTY
                    : RecipeOutputHelper.applyNbt(new ItemStack(resultItemAsItem, resultItemCount), resultItemObject, pRecipeId);

            Item nbtSource = null;
            if(RecipeNbtHelper.readOptionalBoolean(pSerializedRecipe, pRecipeId)) {
                if(resultItem.isEmpty())
                    throw RecipeNbtHelper.error(pRecipeId,
                            "preserve_nbt requires an item output, not a fluid-only output");
                if(inputItemCount != 1 || resultItem.getCount() != 1)
                    throw RecipeNbtHelper.error(pRecipeId,
                            "vitriolation input and output counts must both be exactly one");
                nbtSource = inputItemAsItem;
            }

            return new VitriolationRecipe(pRecipeId,
                    new ItemStack(inputItemAsItem, inputItemCount),
                    resultItem,
                    resultFluidAsFluid == null ? FluidStack.EMPTY : new FluidStack(resultFluidAsFluid, resultFluidCount),
                    craftTicks, minimumAcidStrength, mBConsumed,
                    inputFluidOverrideAsFluid,
                    new ItemStack((outputForCodexAsItem == null || outputForCodexAsItem == Items.AIR) ? ItemRegistry.PROBLEMITE.get() : outputForCodexAsItem),
                    nbtSource
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

            ItemStack resultItem = !hasResultItem ? ItemStack.EMPTY : ItemStack.of(nbt.getCompound("resultItem"));
            Item nbtSource = RecipeNbtHelper.readNetworkSource(nbt);

            return new VitriolationRecipe(pRecipeId,
                    inputAsItem == null ? ItemStack.EMPTY : new ItemStack(inputAsItem, inputCount),
                    resultItem,
                    !hasResultFluid ? FluidStack.EMPTY : new FluidStack(resultFluidAsFluid, resultFluidCount),
                    craftTicks, minimumAcidStrength, mBConsumed,
                    inputFluidOverrideAsFluid,
                    !hasOutputForCodex ? ItemStack.EMPTY : new ItemStack(outputForCodexAsItem),
                    nbtSource
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
                nbt.put("resultItem", pRecipe.resultItem.serializeNBT());
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
            RecipeNbtHelper.writeNetworkSource(nbt, pRecipe);

            pBuffer.writeNbt(nbt);
        }
    }
}
