package com.aranaira.magichem.block.entity.ext;

import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.foundation.ICanHaveUnbottledMateriaInInputTray;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
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
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractFixationBlockEntity extends AbstractBlockEntityWithEfficiency implements ICanTakePlugins, IFluidHandler, IMateriaProvisionRequester, ICanHaveUnbottledMateriaInInputTray {

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected LazyOptional<IFluidHandler> lazyFluidHandler;
    protected ContainerData data;
    protected int
            progress = 0, batchSize = 4, remainingTorque = 0, remainingAnimus = 0, pluginLinkageCountdown = 3, reductionRate = 0;
    public boolean clearRecipeAfterNextProcess = false;
    public boolean doDeferredRecipeCheck = false;

    protected ItemStackHandler itemHandler;
    protected List<AbstractDirectionalPluginBlockEntity> pluginDevices = new ArrayList<>();
    protected FluidStack containedSlurry;
    protected FixationSeparationRecipe currentRecipe;
    protected ResourceLocation deferredRecipeQuery = null;

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

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public void recalculateBatchSize() {
        int mult = 1;
        for (AbstractDirectionalPluginBlockEntity dpbe : pluginDevices) {
            if (dpbe instanceof ActuatorAirBlockEntity air) {
                if (air.getIsSatisfied() && !air.getPaused()) {
                    mult = air.getBatchSize();
                }
            }
        }

        int baseBatchSize = 4;
        int batchSizeCap = 32;
        if (currentRecipe != null && currentRecipe.getResultAdmixture().getItem() instanceof AdmixtureItem ai) {
            baseBatchSize = ai.getInitialBatchSize();
            batchSizeCap = ai.getBatchSizeCap();
        }

        batchSize = Math.min(batchSizeCap, baseBatchSize * mult);
    }

    ////////////////////
    // CRAFTING HANDLERS
    ////////////////////

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, AbstractFixationBlockEntity pEntity, Function<IDs, Integer> pVarFunc, Function<Void, Integer> pPoweredTimeFunc) {
        for (AbstractDirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
            if (dpbe instanceof ActuatorFireBlockEntity fire) {
                ActuatorFireBlockEntity.delegatedTick(pLevel, pPos, pState, fire);
                final boolean satisfied = fire.getIsSatisfied();
                final boolean paused = fire.getPaused();
                final float reductionRate = (paused ? 0 : (satisfied ? fire.getReductionRate() : 0));

                if(pEntity.operationTimeMod != reductionRate) {
                    if(pVarFunc.apply(IDs.MODE_USES_RF) == 0) pEntity.remainingTorque = (fire.isPaused || !fire.getIsSatisfied()) ? 0 : 100;
                    pEntity.operationTimeMod = reductionRate;
                    pEntity.syncAndSave();
                } else if (pVarFunc.apply(IDs.MODE_USES_RF) == 0 && satisfied && !paused && pEntity.remainingTorque < 20) {
                    pEntity.remainingTorque = 100;
                    pEntity.syncAndSave();
                }
            }
            else if (dpbe instanceof ActuatorEarthBlockEntity earth) {
                ActuatorEarthBlockEntity.delegatedTick(pLevel, pPos, pState, earth);
            }
            else if (dpbe instanceof ActuatorWaterBlockEntity water) {
                ActuatorWaterBlockEntity.delegatedTick(pLevel, pPos, pState, water);
            }
            else if (dpbe instanceof ActuatorAirBlockEntity air) {
                ActuatorAirBlockEntity.delegatedTick(pLevel, pPos, pState, air);
                int pre = pEntity.batchSize;
                int baseBatchSize = 4;
                int batchSizeCap = 32;
                if(pEntity.currentRecipe != null && pEntity.currentRecipe.getResultAdmixture().getItem() instanceof AdmixtureItem ai) {
                    baseBatchSize = ai.getInitialBatchSize();
                    batchSizeCap = ai.getBatchSizeCap();
                }

                if(air.getIsSatisfied() && !air.getPaused()) {
                    pEntity.batchSize = baseBatchSize * air.getBatchSize();
                } else
                    pEntity.batchSize = baseBatchSize;
                pEntity.batchSize = Math.min(batchSizeCap, pEntity.batchSize);

                if(pre != pEntity.batchSize)
                    pEntity.syncAndSave();
            }
            else if (dpbe instanceof ActuatorArcaneBlockEntity arcane) {
                ActuatorArcaneBlockEntity.delegatedTick(pLevel, pPos, pState, arcane, true);
                pEntity.reductionRate = arcane.getSlurryReductionRate();
            }
            else if (dpbe instanceof ActuatorEnderBlockEntity ender) {
                ActuatorEnderBlockEntity.delegatedTick(pLevel, pPos, pState, ender);
                if (ender.getIsSatisfied() && !ender.getPaused()) {
                    //exporting
                    if (ender.getMirrorTarget() != null) {
                        boolean instant = ender.getPowerLevel() == 2;
                        if (instant || pLevel.getGameTime() % 10 == 0) {
                            final SimpleContainer outputs = pEntity.getContentsOfOutputSlots(pVarFunc);
                            if (!outputs.isEmpty()) {
                                for (int i = 0; i < outputs.getContainerSize(); i++) {
                                    if (!outputs.getItem(i).isEmpty()) {
                                        final ItemStack outputStack = pEntity.itemHandler.getStackInSlot(pVarFunc.apply(AbstractFixationBlockEntity.IDs.SLOT_OUTPUT_START) + i);
                                        pEntity.itemHandler.setStackInSlot(pVarFunc.apply(AbstractFixationBlockEntity.IDs.SLOT_OUTPUT_START) + i, ItemStack.EMPTY);
                                        ender.createShlorpToTarget(outputStack, instant);
                                        break;
                                    }
                                }
                            }
                            if(pEntity.currentRecipe == null && pLevel.getGameTime() % 40 == 0) {
                                final SimpleContainer inputs = pEntity.getContentsOfInputSlots(pVarFunc);
                                for (int i = 0; i < inputs.getContainerSize(); i++) {
                                    final ItemStack inputQuery = inputs.getItem(i);
                                    if(!inputQuery.isEmpty() && InventoryHelper.hasCustomModelData(inputQuery)) {
                                        pEntity.itemHandler.setStackInSlot(pVarFunc.apply(AbstractFixationBlockEntity.IDs.SLOT_INPUT_START) + i, ItemStack.EMPTY);
                                        ender.createShlorpToTarget(inputQuery, instant);
                                    }
                                }
                            }
                        }
                    }
                    //importing
                    final Map<MateriaItem, Integer> provisioningNeeds = pEntity.getProvisioningNeeds();
                    if (provisioningNeeds != null && provisioningNeeds.size() > 0) {
                        if (ender.getMirrorTarget() instanceof AbstractMateriaStorageMultiTypeDynamicBlockEntity multi) {
                            for (MateriaItem mi : provisioningNeeds.keySet()) {
                                int requested = provisioningNeeds.get(mi);
                                int inStorage = multi.getCurrentStock(mi);
                                boolean instant = ender.getPowerLevel() == 2;

                                int actualDrain = Math.min(requested, inStorage);
                                if (actualDrain > 0) {
                                    multi.drain(mi, actualDrain, false);
                                    ender.createShlorpFromTarget(new ItemStack(mi, actualDrain), instant);

                                    pEntity.setProvisioningInProgress(mi);
                                }
                            }
                        }
                    }
                }
            }
            else if (dpbe instanceof ActuatorNeutralBlockEntity neutral && !(pEntity instanceof GrandFuseryBlockEntity)) {
                ActuatorNeutralBlockEntity.delegatedTick(pLevel, pPos, pState, neutral);

                boolean efficiencyChanged = false;
                boolean opTimeChanged = false;

                if(neutral.getIsSatisfied() && !neutral.isPaused) {
                    if(pEntity.operationTimeMod != 20f) {
                        pEntity.operationTimeMod = 20f;
                        opTimeChanged = true;
                    }
                }
                else {
                    if(pEntity.operationTimeMod == 20f) {
                        pEntity.operationTimeMod = 0;
                        opTimeChanged = true;
                    }
                }

                if(efficiencyChanged || opTimeChanged)
                    pEntity.syncAndSave();
            }
        }

        pEntity.remainingTorque = Math.max(-pVarFunc.apply(IDs.CONFIG_NO_TORQUE_GRACE_PERIOD), pEntity.remainingTorque - 1);
        pEntity.remainingAnimus = Math.max(-pVarFunc.apply(IDs.CONFIG_NO_TORQUE_GRACE_PERIOD), pEntity.remainingAnimus - 1);

        //skip all of this if grime is full
        if(GrimeProvider.getCapability(pEntity).getGrime() >= pVarFunc.apply(IDs.CONFIG_MAX_GRIME))
            return;

        updateActuatorValues(pEntity);

        //make sure we have enough torque (or animus) to operate
        if(pEntity.remainingTorque + pEntity.remainingAnimus > -pVarFunc.apply(IDs.CONFIG_NO_TORQUE_GRACE_PERIOD)) {

            //figure out what slot and stack to target
            if (canCraftItem(pEntity, pVarFunc)) {

                if (pEntity.progress > getOperationTicks(GrimeProvider.getCapability(pEntity).getGrime(), pEntity.batchSize, pEntity.operationTimeMod*100, pVarFunc, pPoweredTimeFunc)) {
                    if (!pLevel.isClientSide()) {
                        craftItem(pEntity, pVarFunc);
                        pEntity.syncAndSave();
                    }
                    if (!pEntity.isStalled)
                        pEntity.resetProgress();
                } else {
                    pEntity.incrementProgress();
                    //tick actuators
                    for(AbstractDirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
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

    public SimpleContainer getContentsOfOutputSlots() {
        return getContentsOfOutputSlots(AbstractFixationBlockEntity::getVar);
    }

    public SimpleContainer getContentsOfOutputSlots(Function<IDs, Integer> pVarFunc) {
        SimpleContainer output = new SimpleContainer(pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT));

        for(int i = pVarFunc.apply(IDs.SLOT_OUTPUT_START); i<pVarFunc.apply(IDs.SLOT_OUTPUT_START)+pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            output.setItem(i-pVarFunc.apply(IDs.SLOT_OUTPUT_START), itemHandler.getStackInSlot(i));
        }

        return output;
    }

    public SimpleContainer getContentsOfInputSlots() {
        return getContentsOfInputSlots(AbstractFixationBlockEntity::getVar);
    }

    public SimpleContainer getContentsOfInputSlots(Function<IDs, Integer> pVarFunc) {
        SimpleContainer input = new SimpleContainer(pVarFunc.apply(IDs.SLOT_INPUT_COUNT));

        for(int i = pVarFunc.apply(IDs.SLOT_INPUT_START); i<pVarFunc.apply(IDs.SLOT_INPUT_START)+pVarFunc.apply(IDs.SLOT_INPUT_COUNT); i++) {
            input.setItem(i-pVarFunc.apply(IDs.SLOT_INPUT_START), itemHandler.getStackInSlot(i));
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

    public void clearRecipe() {
        if(!clearRecipeAfterNextProcess) {
            clearRecipeAfterNextProcess = true;
            doDeferredRecipeCheck = false;
        } else {
            clearRecipeAfterNextProcess = false;
            doDeferredRecipeCheck = false;
            currentRecipe = null;
        }
        syncAndSave();
    }

    protected static boolean canCraftItem(AbstractFixationBlockEntity pEntity, Function<IDs, Integer> pVarFunc) {
        //Can't craft if there's no set recipe
        if(pEntity.currentRecipe == null)
            return false;

        final int slurryCost = Math.round(pEntity.currentRecipe.getSlurryCost() * ((100f - pEntity.reductionRate) / 100f));
        final int containedSlurry = pEntity.containedSlurry.getAmount();

        //Can't craft if there's not enough Academic Slurry
        if(slurryCost > containedSlurry)
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
            int needed = is.getCount();

            for(int i=0; i<input.getContainerSize(); i++) {
                ItemStack query = input.getItem(i);
                if(query.getItem() == is.getItem()) {
                    needed -= query.getCount();
                }
            }

            hasAllInputItems = hasAllInputItems & (needed <= 0);
        }

        return outputHasSpace && hasAllInputItems;
    }

    protected static void craftItem(AbstractFixationBlockEntity pEntity, Function<IDs, Integer> pVarFunc) {
        int bottlesToInsert = 0;

        //TODO: Handle case where both bottled and unbottled items are present at the same time
        for(ItemStack is : pEntity.currentRecipe.getComponentMateria()) {
            bottlesToInsert += is.getCount();
        }

        int totalCycles = 0;
        for(int batch=0; batch<pEntity.batchSize; batch++) {
            totalCycles++;
            if(!canCraftItem(pEntity, pVarFunc)) {
                break;
            }

            //Apply Efficiency
            NonNullList<ItemStack> preEfficiencyOutput = NonNullList.create();
            preEfficiencyOutput.add(pEntity.currentRecipe.getResultAdmixture());
            Triplet<Integer, NonNullList<ItemStack>, Integer> triplet = applyEfficiencyToCraftingResult(preEfficiencyOutput, AbstractFixationBlockEntity.getActualEfficiency(pEntity.efficiencyMod, GrimeProvider.getCapability(pEntity).getGrime(), pVarFunc), 1.0f, pVarFunc.apply(IDs.CONFIG_GRIME_ON_SUCCESS), pVarFunc.apply(IDs.CONFIG_GRIME_ON_FAILURE));
            NonNullList<ItemStack> postEfficiencyOutput = triplet.getSecond();
            boolean crafted = triplet.getThird() > 0;

            //Generate grime amount; Fixation uses the inputs to determine Grime rather than the output
            int grimeToAdd = 0;
            for (ItemStack is : pEntity.currentRecipe.getComponentMateria()) {
                grimeToAdd += is.getCount();
            }
            grimeToAdd *= postEfficiencyOutput.size() == 1 ? pVarFunc.apply(IDs.CONFIG_GRIME_ON_SUCCESS) : pVarFunc.apply(IDs.CONFIG_GRIME_ON_FAILURE);

            //Update output copy with the potentially-crafted item
            SimpleContainer output = pEntity.getContentsOfOutputSlots(pVarFunc);
            for (ItemStack item : postEfficiencyOutput) {
                CompoundTag nbt = item.getOrCreateTag();
                nbt.putInt("CustomModelData", 1);
                item.setTag(nbt);
                
                output.addItem(item);
            }

            //Remove component items from inputs
            if(crafted){
                for (ItemStack is : pEntity.currentRecipe.getComponentMateria()) {
                    ItemStack itemsToRemove = is.copy();
                    int remaining = itemsToRemove.getCount();

                    for (int i = pVarFunc.apply(IDs.SLOT_INPUT_START) + pVarFunc.apply(IDs.SLOT_INPUT_COUNT) - 1; i >= pVarFunc.apply(IDs.SLOT_INPUT_START); i--) {
                        ItemStack stackInSlot = pEntity.itemHandler.getStackInSlot(i);
                        if (stackInSlot.isEmpty())
                            continue;

                        if (stackInSlot.getItem() == itemsToRemove.getItem()) {
                            int removed = Math.min(remaining, stackInSlot.getCount());
                            if (stackInSlot.hasTag()) {
                                CompoundTag nbt = stackInSlot.getTag();
                                if (nbt.contains("CustomModelData")) {
                                    bottlesToInsert -= removed;
                                }
                            }
                            stackInSlot.shrink(removed);
//                        pEntity.itemHandler.setStackInSlot(i, stackInSlot);
                            remaining -= removed;

                            if (remaining == 0)
                                break;
                        }
                    }
                }
            }

            //Overwrite final output slots with the outcome
            for (int i = 0; i < pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
                pEntity.itemHandler.setStackInSlot(pVarFunc.apply(IDs.SLOT_OUTPUT_START) + i, output.getItem(i));
            }

            boolean hasProto = false;
            boolean hasQuake = false;
            //Check to see if there's a Quake Refinery attached and shunt the grime over there if it exists
            for (AbstractDirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
                if (dpbe instanceof ActuatorEarthBlockEntity aebe) {
                    grimeToAdd = aebe.addGrimeToBuffer(grimeToAdd);
                    hasQuake = true;
                }
                if (dpbe instanceof ActuatorNeutralBlockEntity neutral) {
                    hasProto = true;
                }
            }

            //Add grime to this device if there's no Quake Refinery
            if (grimeToAdd > 0) {
                if(!hasQuake && hasProto) grimeToAdd = Math.round((float)grimeToAdd * 0.8f);

                IGrimeCapability grimeCapability = GrimeProvider.getCapability(pEntity);
                grimeCapability.setGrime(Math.min(Math.max(grimeCapability.getGrime() + grimeToAdd, 0), ServerConfig.centrifugeMaximumGrime));
            }

            //Consume slurry
            int slurryCost = Math.round((float)pEntity.currentRecipe.getSlurryCost() * ((100f - pEntity.reductionRate) / 100f));
            float reducedSlurryCost = (1.0f - (ServerConfig.fixationFailureRefund / 100.0f)) * slurryCost;
            pEntity.containedSlurry.shrink(postEfficiencyOutput.size() == 1 ? slurryCost : (int) reducedSlurryCost);
            pEntity.syncAndSave();
        }

        //TODO: Uncommment the original line once the bottle slot stack size has been expanded
        //Fill Bottle Slot
        //entity.itemHandler.insertItem(SLOT_BOTTLES, new ItemStack(Items.GLASS_BOTTLE, bottlesToInsert), false);
        //TODO: Remove this once the bottle slot stack size has been expanded
        int slotLimit = pEntity.itemHandler.getSlotLimit(pVarFunc.apply(IDs.SLOT_BOTTLES_OUTPUT));
        ItemStack contained = pEntity.itemHandler.getStackInSlot(pVarFunc.apply(IDs.SLOT_BOTTLES_OUTPUT));
        int inserted = bottlesToInsert * totalCycles;
        ItemStack bottles;

        if(contained.isEmpty()) {
            if(inserted > 64) {
                bottles = new ItemStack(Items.GLASS_BOTTLE, inserted - 64);
                pEntity.itemHandler.setStackInSlot(pVarFunc.apply(IDs.SLOT_BOTTLES_OUTPUT), new ItemStack(Items.GLASS_BOTTLE, 64));
            } else {
                bottles = ItemStack.EMPTY;
                pEntity.itemHandler.setStackInSlot(pVarFunc.apply(IDs.SLOT_BOTTLES_OUTPUT), new ItemStack(Items.GLASS_BOTTLE, inserted));
            }
        } else {
            int remainingSpace = slotLimit - contained.getCount();
            if(inserted <= remainingSpace) {
                bottles = ItemStack.EMPTY;
                contained.grow(inserted);
            } else {
                bottles = new ItemStack(Items.GLASS_BOTTLE, remainingSpace - inserted);
                contained.setCount(slotLimit);
            }
        }

        SimpleContainer bottleSpill = new SimpleContainer(10);
        while (bottles.getCount() > 0) {
            int count = bottles.getCount();
            int thisPile;
            if (count > 64) {
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

        resolveActuators(pEntity, totalCycles);
        if(totalCycles > 0 && pEntity.clearRecipeAfterNextProcess) {
            pEntity.currentRecipe = null;
            pEntity.clearRecipeAfterNextProcess = false;
            pEntity.syncAndSave();
        }
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
        return drain(new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), i), fluidAction);
    }

    ////////////////////
    // PROVISION HANDLING
    ////////////////////

    @Override
    public boolean allowIncreasedDeliverySize() {
        return false;
    }

    @Override
    public boolean needsProvisioning() {
        return false;
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        return null;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {

    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {

    }

    @Override
    public void provide(ItemStack pStack) {

    }

    ////////////////////
    // DATA SLOT HANDLING
    ////////////////////

    protected abstract void pushData();

    public static float getTimeScalar(int pGrime, Function<IDs, Integer> pVarFunc) {
        float grimeScalar = Math.min(Math.max(Math.min(Math.max(getGrimePercent(pGrime, pVarFunc) - 0.5f, 0f), 1f) * 2f, 0f), 1f);
        return 1f + grimeScalar * 3f;
    }

    public static int getOperationTicks(int pGrime, int pBatchSize, float pOperationTimeMod, Function<IDs, Integer> pVarFunc, Function<Void, Integer> pPoweredTimeFunc) {
        int poweredOpTime = pPoweredTimeFunc.apply(null);
        float otmScalar;

        //RF-using devices reduce energy usage, not operation time
        if (pVarFunc.apply(IDs.MODE_USES_RF) == 0)
            otmScalar = (10000f - pOperationTimeMod) / 10000f;
        else
            otmScalar = 1;

        float batchScalar = ActuatorAirBlockEntity.getPenaltyRateFromBatchSize(pBatchSize);

        if(poweredOpTime == -1) {
            return Math.round(pVarFunc.apply(IDs.CONFIG_OPERATION_TIME) * getTimeScalar(pGrime, pVarFunc) * otmScalar * batchScalar);
        } else {
            return Math.round(poweredOpTime * getTimeScalar(pGrime, pVarFunc) * otmScalar * batchScalar);
        }
    }

    public static int getActualEfficiency(int pMod, int pGrime, Function<IDs, Integer> pVarFunc) {
        float grimeScalar = 1f - Math.min(Math.max(Math.min(Math.max(getGrimePercent(pGrime, pVarFunc) - 0.5f, 0f), 1f) * 2f, 0f), 1f);
        return Math.round((pVarFunc.apply(IDs.CONFIG_BASE_EFFICIENCY) + pMod) * grimeScalar);
    }

    public static float getGrimePercent(int pGrime, Function<IDs, Integer> pVarFunc) {
        return (float)pGrime / (float)pVarFunc.apply(IDs.CONFIG_MAX_GRIME);
    }

    public static int getScaledProgress(int pProgress, int pGrime, int pBatchSize, float pOperationTimeMod, Function<IDs, Integer> pVarFunc, Function<Void, Integer> pPoweredTimeFunc) {
        return Math.min(pVarFunc.apply(IDs.GUI_PROGRESS_BAR_WIDTH), pVarFunc.apply(IDs.GUI_PROGRESS_BAR_WIDTH) * pProgress / getOperationTicks(pGrime, pBatchSize, pOperationTimeMod, pVarFunc, pPoweredTimeFunc));
    }

    @Override
    public int getMaximumGrime() {
        return 0;
    }

    @Override
    public int clean() {
        return 0;
    }

    public ItemStack getRecipeItem() {
        return currentRecipe == null ? ItemStack.EMPTY : currentRecipe.getResultItem().copy();
    }

    public ItemStack getRecipeItem(boolean pMakeCopy) {
        return currentRecipe == null ? ItemStack.EMPTY : pMakeCopy ? currentRecipe.getResultItem().copy() : currentRecipe.getResultItem();
    }

    ////////////////////
    // ACTUATOR HANDLING
    ////////////////////

    protected static void updateActuatorValues(AbstractFixationBlockEntity entity) {
        entity.efficiencyMod = 0;
        for(AbstractDirectionalPluginBlockEntity dpbe : entity.pluginDevices) {
            if(dpbe instanceof ActuatorWaterBlockEntity water) {
                entity.efficiencyMod = (water.getIsSatisfied() && !water.getPaused() && water.isAuxiliaryRequirementSatisfied()) ? water.getEfficiencyIncrease() : 0;
            } else if(dpbe instanceof ActuatorNeutralBlockEntity neutral) {
                entity.efficiencyMod = Math.max(neutral.getIsSatisfied() ? 10 : 0, entity.efficiencyMod);
            }
        }
    }

    public static void resolveActuators(AbstractFixationBlockEntity pEntity, int pCyclesCompleted) {
        for(AbstractDirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
            dpbe.processCompletedOperation(pCyclesCompleted);
        }
    }

    @Override
    public void linkPlugins() {
        pluginDevices.clear();
        pluginLinkageCountdown = 1;
    }

    @Override
    public void removePlugin(AbstractDirectionalPluginBlockEntity pPlugin) {
        this.pluginDevices.remove(pPlugin);
        if(pPlugin instanceof ActuatorWaterBlockEntity) {
            efficiencyMod = 0;
        }
        if(pPlugin instanceof ActuatorAirBlockEntity) {
            batchSize = 1;
        }
        if(pPlugin instanceof ActuatorFireBlockEntity) {
            operationTimeMod = 0;
        }
        if(pPlugin instanceof ActuatorNeutralBlockEntity) {
            efficiencyMod = 0;
            operationTimeMod = 0;
        }
        syncAndSave();
    }

    @Override
    public void linkPluginsDeferred() {
        pluginLinkageCountdown = 1;
    }

    ////////////////////
    // FINAL VARIABLE RETRIEVAL
    ////////////////////

    public static int getVar(IDs pID) {
        return -2;
    }

    public Integer getPoweredOperationTime(Void unused) {
        return -1;
    }

    public enum IDs {
        SLOT_BOTTLES, SLOT_BOTTLES_OUTPUT, SLOT_INPUT_START, SLOT_INPUT_COUNT, SLOT_OUTPUT_START, SLOT_OUTPUT_COUNT,
        CONFIG_BASE_EFFICIENCY, CONFIG_OPERATION_TIME, CONFIG_MAX_GRIME, CONFIG_GRIME_ON_SUCCESS, CONFIG_GRIME_ON_FAILURE,
        MODE_USES_RF,
        CONFIG_NO_TORQUE_GRACE_PERIOD, CONFIG_TORQUE_GAIN_ON_ACTIVATION, CONFIG_ANIMUS_GAIN_ON_DUSTING, CONFIG_TANK_CAPACITY,
        DATA_PROGRESS, DATA_GRIME, DATA_TORQUE, DATA_ANIMUS, DATA_EFFICIENCY_MOD, DATA_OPERATION_TIME_MOD,
        GUI_PROGRESS_BAR_WIDTH, GUI_GRIME_BAR_WIDTH
    }
}
