package com.aranaira.magichem.block.entity.ext;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ActuatorEarthBlockEntity;
import com.aranaira.magichem.block.entity.ActuatorFireBlockEntity;
import com.aranaira.magichem.block.entity.ActuatorWaterBlockEntity;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.FluidRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractFixationBlockEntity extends AbstractBlockEntityWithEfficiency implements ICanTakePlugins, IFluidHandler {

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected LazyOptional<IFluidHandler> lazyFluidHandler;
    protected ContainerData data;
    protected int
            progress = 0, remainingTorque = 0, remainingAnimus = 0, pluginLinkageCountdown = 3;

    protected ItemStackHandler itemHandler;
    protected List<DirectionalPluginBlockEntity> pluginDevices = new ArrayList<>();
    protected FluidStack containedSlurry;
    protected FixationSeparationRecipe currentRecipe;

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    public AbstractFixationBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pState) {
        super(pType, pPos, pState);
        containedSlurry = FluidStack.EMPTY;
    }

    ////////////////////
    // BOILERPLATE CODE
    ////////////////////

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        } else if(cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyFluidHandler.invalidate();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    ////////////////////
    // CRAFTING HANDLERS
    ////////////////////

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, AbstractFixationBlockEntity pEntity, Function<IDs, Integer> pVarFunc) {
        for (DirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
            if (dpbe instanceof ActuatorFireBlockEntity fire) {
                ActuatorFireBlockEntity.delegatedTick(pLevel, pPos, pState, fire);
                if (ActuatorFireBlockEntity.getIsSatisfied(fire) && pEntity.remainingTorque <= 20) {
                    pEntity.remainingTorque = 100;
                    pEntity.operationTimeMod = fire.getReductionRate();
                    pEntity.syncAndSave();
                }
            }
            if (dpbe instanceof ActuatorEarthBlockEntity earth) {
                ActuatorEarthBlockEntity.delegatedTick(pLevel, pPos, pState, earth);
            }
        }

        pEntity.remainingTorque = Math.max(-pVarFunc.apply(IDs.CONFIG_NO_TORQUE_GRACE_PERIOD), pEntity.remainingTorque - 1);
        pEntity.remainingAnimus = Math.max(-pVarFunc.apply(IDs.CONFIG_NO_TORQUE_GRACE_PERIOD), pEntity.remainingAnimus - 1);

        //skip all of this if grime is full
        if(GrimeProvider.getCapability(pEntity).getGrime() >= Config.centrifugeMaximumGrime)
            return;

        updateActuatorValues(pEntity);

        //make sure we have enough torque (or animus) to operate
        if(pEntity.remainingTorque + pEntity.remainingAnimus > -pVarFunc.apply(IDs.CONFIG_NO_TORQUE_GRACE_PERIOD)) {

            //figure out what slot and stack to target
            if (canCraftItem(pEntity, pVarFunc)) {

                if (pEntity.progress > getOperationTicks(pEntity.getGrimeFromData(), pEntity.operationTimeMod*100, pVarFunc)) {
                    if (!pLevel.isClientSide()) {
                        craftItem(pEntity, pVarFunc);
                    }
                    if (!pEntity.isStalled)
                        pEntity.resetProgress();
                } else {
                    pEntity.incrementProgress();
                    //tick actuators
                    for(DirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
                        if(dpbe instanceof ActuatorWaterBlockEntity water) {
                            ActuatorWaterBlockEntity.delegatedTick(pLevel, pPos, pState, water);
                        }
                    }
                }
            } else
                pEntity.resetProgress();
        }

        //deferred plugin linkage
        if(!pLevel.isClientSide()) {
            if (pEntity.pluginLinkageCountdown == 0) {
                pEntity.pluginLinkageCountdown = -1;
                pEntity.linkPlugins();
            } else if (pEntity.pluginLinkageCountdown > 0) {
                pEntity.pluginLinkageCountdown--;
            }
        }
    }

    public SimpleContainer getContentsOfOutputSlots(Function<IDs, Integer> pVarFunc) {
        SimpleContainer output = new SimpleContainer(pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT));

        for(int i = pVarFunc.apply(IDs.SLOT_OUTPUT_START); i<pVarFunc.apply(IDs.SLOT_OUTPUT_START)+pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            output.setItem(i-pVarFunc.apply(IDs.SLOT_OUTPUT_START), itemHandler.getStackInSlot(i).copy());
        }

        return output;
    }

    public SimpleContainer getContentsOfInputSlots(Function<IDs, Integer> pVarFunc) {
        SimpleContainer input = new SimpleContainer(pVarFunc.apply(IDs.SLOT_INPUT_COUNT));

        for(int i = pVarFunc.apply(IDs.SLOT_INPUT_START); i<pVarFunc.apply(IDs.SLOT_INPUT_START)+pVarFunc.apply(IDs.SLOT_INPUT_COUNT); i++) {
            input.setItem(i-pVarFunc.apply(IDs.SLOT_INPUT_START), itemHandler.getStackInSlot(i).copy());
        }

        return input;
    }

    protected void resetProgress() {
        progress = 0;
    }

    protected void incrementProgress() {
        progress++;
    }

    ////////////////////
    // RECIPE HANDLING
    ////////////////////

    protected static boolean canCraftItem(AbstractFixationBlockEntity pEntity, Function<IDs, Integer> pVarFunc) {
        //Can't craft if there's no set recipe
        if(pEntity.currentRecipe == null)
            return false;

        //Can't craft if the bottle output is full
        if(pEntity.itemHandler.getStackInSlot(pVarFunc.apply(IDs.SLOT_BOTTLES_OUTPUT)).getCount() == 64)
            return false;

        //Check to see if the output area has space to add the item
        SimpleContainer output = pEntity.getContentsOfOutputSlots(pVarFunc);
        boolean outputHasSpace = output.canAddItem(pEntity.currentRecipe.getResultAdmixture());

        //Check to see if all input items are present
        SimpleContainer input = pEntity.getContentsOfInputSlots(pVarFunc);
        boolean hasAllInputItems = true;
        for(ItemStack is : pEntity.currentRecipe.getComponentMateria()) {
            Predicate<ItemStack> testCase = i -> (i.getItem() == is.getItem() && i.getCount() >= is.getCount());
            hasAllInputItems = hasAllInputItems & input.hasAnyMatching(testCase);
        }

        return outputHasSpace && hasAllInputItems;
    }

    protected static void craftItem(AbstractFixationBlockEntity pEntity, Function<IDs, Integer> pVarFunc) {
        int bottlesToInsert = 0;
        SimpleContainer inputs = pEntity.getContentsOfInputSlots(pVarFunc);

        for(ItemStack is : pEntity.currentRecipe.getComponentMateria()) {
            bottlesToInsert += is.getCount();
        }

        //TODO: Uncommment the original line once the bottle slot stack size has been expanded
        //Fill Bottle Slot
        //entity.itemHandler.insertItem(SLOT_BOTTLES, new ItemStack(Items.GLASS_BOTTLE, bottlesToInsert), false);
        //TODO: Remove this once the bottle slot stack size has been expanded
        ItemStack bottles = pEntity.itemHandler.insertItem(pVarFunc.apply(IDs.SLOT_BOTTLES_OUTPUT), new ItemStack(Items.GLASS_BOTTLE, bottlesToInsert), false);
        SimpleContainer bottleSpill = new SimpleContainer(5);
        while(bottles.getCount() > 0) {
            int count = bottles.getCount();
            int thisPile;
            if(count > 64) {
                thisPile = 64;
                count -= 64;
            } else {
                thisPile = count;
                count = 0;
            }
            ItemStack bottlesToDrop = new ItemStack(Items.GLASS_BOTTLE, thisPile);
            bottleSpill.addItem(bottlesToDrop);
            bottles.setCount(count);
        }
        Containers.dropContents(pEntity.getLevel(), pEntity.getBlockPos(), bottleSpill);

        //Apply Efficiency
        NonNullList<ItemStack> preEfficiencyOutput = NonNullList.create();
        preEfficiencyOutput.add(pEntity.currentRecipe.getResultAdmixture());
        Pair<Integer, NonNullList<ItemStack>> pair = applyEfficiencyToCraftingResult(preEfficiencyOutput, AbstractFixationBlockEntity.getActualEfficiency(pEntity.efficiencyMod, GrimeProvider.getCapability(pEntity).getGrime(), pVarFunc), 1.0f, pVarFunc.apply(IDs.CONFIG_GRIME_ON_SUCCESS), pVarFunc.apply(IDs.CONFIG_GRIME_ON_FAILURE));
        NonNullList<ItemStack> postEfficiencyOutput = pair.getSecond();

        //Generate grime amount; Fixation uses the inputs to determine Grime rather than the output
        int grimeToAdd = 0;
        for(ItemStack is : pEntity.currentRecipe.getComponentMateria()) {
            grimeToAdd += is.getCount();
        }
        grimeToAdd *= postEfficiencyOutput.size() == 1 ? pVarFunc.apply(IDs.CONFIG_GRIME_ON_SUCCESS) : pVarFunc.apply(IDs.CONFIG_GRIME_ON_FAILURE);

            //Update output copy with the potentially-crafted item
        SimpleContainer output = pEntity.getContentsOfOutputSlots(pVarFunc);
        for(ItemStack is : postEfficiencyOutput) {
            output.addItem(is);
        }

        //Remove component items from inputs
        for(ItemStack is : pEntity.currentRecipe.getComponentMateria()) {
            inputs.removeItemType(is.getItem(), is.getCount());
        }

        //Overwrite final output slots with the outcome
        for(int i=0; i<pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            pEntity.itemHandler.setStackInSlot(pVarFunc.apply(IDs.SLOT_OUTPUT_START) + i, output.getItem(i));
        }

        //Overwrite final input slots with the outcome
        for(int i=0; i<pVarFunc.apply(IDs.SLOT_INPUT_COUNT); i++) {
            pEntity.itemHandler.setStackInSlot(pVarFunc.apply(IDs.SLOT_INPUT_START) + i, inputs.getItem(i));
        }

        //Check to see if there's a Quake Refinery attached and shunt the grime over there if it exists
        for(DirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
            if(dpbe instanceof ActuatorEarthBlockEntity aebe) {
                grimeToAdd = aebe.addGrimeToBuffer(grimeToAdd);
            }
        }

        if(grimeToAdd > 0) {
            IGrimeCapability grimeCapability = GrimeProvider.getCapability(pEntity);
            grimeCapability.setGrime(Math.min(Math.max(grimeCapability.getGrime() + grimeToAdd, 0), Config.centrifugeMaximumGrime));
        }

        resolveActuators(pEntity);
    }

    ////////////////////
    // FLUID HANDLING
    ////////////////////

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return containedSlurry;
    }

    @Override
    public int getTankCapacity(int tank) {
        return 100;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack fluidAction) {
        return fluidAction.getFluid() == FluidRegistry.ACADEMIC_SLURRY.get();
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction action) {
        if(action.execute()) {
            setChanged();
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }

        Fluid fluid = fluidStack.getFluid();
        int incomingAmount = fluidStack.getAmount();
        if(fluid == FluidRegistry.ACADEMIC_SLURRY.get()) {
            int extantAmount = containedSlurry.getAmount();

            //Hit capacity
            if(incomingAmount + extantAmount > getTankCapacity(0)) {
                int actualTransfer = getTankCapacity(0) - extantAmount;
                if(action == FluidAction.EXECUTE)
                    this.containedSlurry = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), getTankCapacity(0));
                return actualTransfer;
            } else {
                if(action == FluidAction.EXECUTE)
                    this.containedSlurry = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), extantAmount + incomingAmount);
                return incomingAmount;
            }
        }
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
        if(fluidAction.execute()) {
            setChanged();
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }

        Fluid fluid = fluidStack.getFluid();
        int incomingAmount = fluidStack.getAmount();
        if(fluid == FluidRegistry.ACADEMIC_SLURRY.get()) {
            int extantAmount = containedSlurry.getAmount();
            if(extantAmount >= incomingAmount) {
                if(fluidAction == FluidAction.EXECUTE)
                    containedSlurry.shrink(incomingAmount);
                return new FluidStack(fluid, incomingAmount);
            } else {
                if(fluidAction == FluidAction.EXECUTE)
                    containedSlurry = FluidStack.EMPTY;
                return new FluidStack(fluid, extantAmount);
            }
        }
        return fluidStack;
    }

    @Override
    public @NotNull FluidStack drain(int i, FluidAction fluidAction) {
        //Assume removed fluid is steam if not specified
        return drain(new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), i), fluidAction);
    }

    ////////////////////
    // DATA SLOT HANDLING
    ////////////////////

    protected abstract void pushData();

    public static float getTimeScalar(int pGrime, Function<IDs, Integer> pVarFunc) {
        float grimeScalar = Math.min(Math.max(Math.min(Math.max(getGrimePercent(pGrime, pVarFunc) - 0.5f, 0f), 1f) * 2f, 0f), 1f);
        return 1f + grimeScalar * 3f;
    }

    public static int getOperationTicks(int pGrime, float pOperationTimeMod, Function<IDs, Integer> pVarFunc) {
        float otmScalar = (10000f - pOperationTimeMod) / 10000f;
        return Math.round(pVarFunc.apply(IDs.CONFIG_OPERATION_TIME) * getTimeScalar(pGrime, pVarFunc) * otmScalar);
    }

    public static int getActualEfficiency(int pMod, int pGrime, Function<IDs, Integer> pVarFunc) {
        float grimeScalar = 1f - Math.min(Math.max(Math.min(Math.max(getGrimePercent(pGrime, pVarFunc) - 0.5f, 0f), 1f) * 2f, 0f), 1f);
        return Math.round((pVarFunc.apply(IDs.CONFIG_BASE_EFFICIENCY) + pMod) * grimeScalar);
    }

    public static float getGrimePercent(int pGrime, Function<IDs, Integer> pVarFunc) {
        return (float)pGrime / (float)pVarFunc.apply(IDs.CONFIG_MAX_GRIME);
    }

    public static int getScaledProgress(int pProgress, int pGrime, float pOperationTimeMod, Function<IDs, Integer> pVarFunc) {
        return pVarFunc.apply(IDs.GUI_PROGRESS_BAR_WIDTH) * pProgress / getOperationTicks(pGrime, pOperationTimeMod, pVarFunc);
    }

    @Override
    public int getGrimeFromData() {
        return 0;
    }

    @Override
    public int getMaximumGrime() {
        return 0;
    }

    @Override
    public int clean() {
        return 0;
    }

    public ItemStack getRecipeItem(Function<IDs, Integer> pVarFunc) {
        return itemHandler.getStackInSlot(pVarFunc.apply(IDs.SLOT_RECIPE));
    }

    ////////////////////
    // ACTUATOR HANDLING
    ////////////////////

    protected static void updateActuatorValues(AbstractFixationBlockEntity entity) {
        for(DirectionalPluginBlockEntity dpbe : entity.pluginDevices) {
            if(dpbe instanceof ActuatorWaterBlockEntity water) {
                entity.efficiencyMod = ActuatorWaterBlockEntity.getIsSatisfied(water) ? water.getEfficiencyIncrease() : 0;
            }
        }
    }

    public static void resolveActuators(AbstractFixationBlockEntity pEntity) {
        for(DirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
            dpbe.processCompletedOperation();
        }
    }

    @Override
    public void linkPlugins() {
        pluginDevices.clear();
    }

    @Override
    public void removePlugin(DirectionalPluginBlockEntity pPlugin) {
        this.pluginDevices.remove(pPlugin);
        if(pPlugin instanceof ActuatorWaterBlockEntity) {
            efficiencyMod = 0;
        }
    }

    @Override
    public void linkPluginsDeferred() {
        pluginLinkageCountdown = 3;
    }

    ////////////////////
    // FINAL VARIABLE RETRIEVAL
    ////////////////////

    public static int getVar(IDs pID) {
        return -2;
    }

    public enum IDs {
        SLOT_BOTTLES, SLOT_BOTTLES_OUTPUT, SLOT_INPUT_START, SLOT_INPUT_COUNT, SLOT_OUTPUT_START, SLOT_OUTPUT_COUNT, SLOT_RECIPE,
        CONFIG_BASE_EFFICIENCY, CONFIG_OPERATION_TIME, CONFIG_MAX_GRIME, CONFIG_GRIME_ON_SUCCESS, CONFIG_GRIME_ON_FAILURE,
        CONFIG_NO_TORQUE_GRACE_PERIOD, CONFIG_TORQUE_GAIN_ON_ACTIVATION, CONFIG_ANIMUS_GAIN_ON_DUSTING, CONFIG_TANK_CAPACITY,
        DATA_PROGRESS, DATA_GRIME, DATA_TORQUE, DATA_ANIMUS, DATA_EFFICIENCY_MOD, DATA_OPERATION_TIME_MOD,
        GUI_PROGRESS_BAR_WIDTH, GUI_GRIME_BAR_WIDTH
    }
}
