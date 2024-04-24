package com.aranaira.magichem.block.entity.ext;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ActuatorAirBlockEntity;
import com.aranaira.magichem.block.entity.ActuatorEarthBlockEntity;
import com.aranaira.magichem.block.entity.ActuatorFireBlockEntity;
import com.aranaira.magichem.block.entity.ActuatorWaterBlockEntity;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractSeparationBlockEntity extends AbstractBlockEntityWithEfficiency implements ICanTakePlugins {

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected ContainerData data;
    protected int
            progress = 0, batchSize = 0, remainingTorque = 0, remainingAnimus = 0, pluginLinkageCountdown = 3;

    protected ItemStackHandler itemHandler;
    protected List<DirectionalPluginBlockEntity> pluginDevices = new ArrayList<>();

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    public AbstractSeparationBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pState) {
        super(pType, pPos, pState);
    }

    ////////////////////
    // BOILERPLATE CODE
    ////////////////////

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    ////////////////////
    // CRAFTING HANDLERS
    ////////////////////

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, AbstractSeparationBlockEntity pEntity, Function<IDs, Integer> pVarFunc) {
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
            if (dpbe instanceof ActuatorAirBlockEntity air) {
                ActuatorAirBlockEntity.delegatedTick(pLevel, pPos, pState, air);
                if(ActuatorAirBlockEntity.getIsSatisfied(air)) {
                    pEntity.batchSize = ActuatorAirBlockEntity.getBatchSize(air.getPowerLevel());
                } else
                    pEntity.batchSize = 1;
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
            Pair<Integer, ItemStack> processing = getProcessingItem(pEntity, pVarFunc);
            int processingSlot = processing.getFirst();
            ItemStack processingItem = processing.getSecond();

            FixationSeparationRecipe recipe = getRecipeInSlot(pEntity, processingSlot);
            if (processingItem != ItemStack.EMPTY && recipe != null) {
                if (canCraftItem(pEntity, recipe, pVarFunc)) {

                    if (pEntity.progress > getOperationTicks(pEntity.getGrimeFromData(), pEntity.batchSize, pEntity.operationTimeMod*100, pVarFunc)) {
                        if (!pLevel.isClientSide()) {
                            craftItem(pEntity, recipe, processingSlot, pVarFunc);
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
                }
            } else if (processingItem == ItemStack.EMPTY)
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

    private static Pair<Integer, ItemStack> getProcessingItem(AbstractSeparationBlockEntity pEntity, Function<IDs, Integer> pVarFunc) {
        int processingSlot = pVarFunc.apply(IDs.SLOT_INPUT_START)+pVarFunc.apply(IDs.SLOT_INPUT_COUNT)-1;
        ItemStack processingItem;
        int outputSlot = processingSlot;
        ItemStack outputItem = ItemStack.EMPTY;

        while(processingSlot > pVarFunc.apply(IDs.SLOT_INPUT_START) - 1) {
            processingItem = pEntity.itemHandler.getStackInSlot(processingSlot);

            if(processingItem == ItemStack.EMPTY)  processingSlot--;
            else {
                outputSlot = processingSlot;
                outputItem = processingItem.copy();
                break;
            }
        }

        return new Pair<>(outputSlot, outputItem);
    }

    public SimpleContainer getContentsOfOutputSlots() {
        return getContentsOfOutputSlots(AbstractSeparationBlockEntity::getVar);
    }

    public SimpleContainer getContentsOfOutputSlots(Function<IDs, Integer> pVarFunc) {
        SimpleContainer output = new SimpleContainer(pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT));

        for(int i = pVarFunc.apply(IDs.SLOT_OUTPUT_START); i<pVarFunc.apply(IDs.SLOT_OUTPUT_START)+pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            output.setItem(i-pVarFunc.apply(IDs.SLOT_OUTPUT_START), itemHandler.getStackInSlot(i));
        }

        return output;
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

    protected static FixationSeparationRecipe getRecipeInSlot(AbstractSeparationBlockEntity pEntity, int pSlot) {
        Level level = pEntity.level;

        FixationSeparationRecipe recipe = FixationSeparationRecipe.getSeparatingRecipe(level, pEntity.itemHandler.getStackInSlot(pSlot));

        if(recipe != null) {
            return recipe;
        }

        return null;
    }

    protected static boolean canCraftItem(AbstractSeparationBlockEntity entity, FixationSeparationRecipe recipe, Function<IDs, Integer> pVarFunc) {
        if(entity.itemHandler.getStackInSlot(pVarFunc.apply(IDs.SLOT_BOTTLES_OUTPUT)).getCount() == 64)
            return false;

        SimpleContainer cont = new SimpleContainer(pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT));
        for(int i=pVarFunc.apply(IDs.SLOT_OUTPUT_START); i<pVarFunc.apply(IDs.SLOT_OUTPUT_START)+pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            cont.setItem(i-pVarFunc.apply(IDs.SLOT_OUTPUT_START), entity.itemHandler.getStackInSlot(i).copy());
        }

        for(int i=0; i<recipe.getComponentMateria().size(); i++) {
            if(!cont.canAddItem(recipe.getComponentMateria().get(i).copy()))
                return false;
            cont.addItem(recipe.getComponentMateria().get(i).copy());
        }

        return true;
    }

    protected static void craftItem(AbstractSeparationBlockEntity pEntity, FixationSeparationRecipe pRecipe, int pProcessingSlot, Function<IDs, Integer> pVarFunc) {
        int bottlesToInsert = 1;

        SimpleContainer outputSlots = new SimpleContainer(9);
        for(int i=0; i<pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            outputSlots.setItem(i, pEntity.itemHandler.getStackInSlot(pVarFunc.apply(IDs.SLOT_OUTPUT_START)+i));
        }

        int totalCycles = 0;
        for(int batch=0; batch< pEntity.batchSize; batch++) {
            totalCycles++;
            if (!canCraftItem(pEntity, pRecipe, pVarFunc)) {
                break;
            }
            Pair<Integer, NonNullList<ItemStack>> pair = applyEfficiencyToCraftingResult(pRecipe.getComponentMateria(), AbstractSeparationBlockEntity.getActualEfficiency(pEntity.efficiencyMod, GrimeProvider.getCapability(pEntity).getGrime(), pVarFunc), 1.0f, pVarFunc.apply(IDs.CONFIG_GRIME_ON_SUCCESS), pVarFunc.apply(IDs.CONFIG_GRIME_ON_FAILURE));
            int grimeToAdd = Math.round(pair.getFirst());
            NonNullList<ItemStack> componentMateria = pair.getSecond();

            for (ItemStack item : componentMateria) {
                if (outputSlots.canAddItem(item)) {
                    outputSlots.addItem(item);
                } else {
                    pEntity.isStalled = true;
                    break;
                }
            }

            if (!pEntity.isStalled) {
                for (int i = 0; i < pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
                    pEntity.itemHandler.setStackInSlot(pVarFunc.apply(IDs.SLOT_OUTPUT_START) + i, outputSlots.getItem(i));
                }
                ItemStack processingSlotContents = pEntity.itemHandler.getStackInSlot(pProcessingSlot);
                processingSlotContents.shrink(1);
                if (processingSlotContents.getCount() == 0)
                    pEntity.itemHandler.setStackInSlot(pProcessingSlot, ItemStack.EMPTY);
            }

            //Check to see if there's a Quake Refinery attached and shunt the grime over there if it exists
            for (DirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
                if (dpbe instanceof ActuatorEarthBlockEntity aebe) {
                    grimeToAdd = aebe.addGrimeToBuffer(grimeToAdd);
                }
            }

            if (grimeToAdd > 0) {
                IGrimeCapability grimeCapability = GrimeProvider.getCapability(pEntity);
                grimeCapability.setGrime(Math.min(Math.max(grimeCapability.getGrime() + grimeToAdd, 0), pVarFunc.apply(IDs.CONFIG_MAX_GRIME)));
            }
        }

        //TODO: Uncommment the original line once the bottle slot stack size has been expanded
        //Fill Bottle Slot
        //entity.itemHandler.insertItem(SLOT_BOTTLES, new ItemStack(Items.GLASS_BOTTLE, bottlesToInsert), false);
        //TODO: Remove this once the bottle slot stack size has been expanded
        ItemStack bottles = pEntity.itemHandler.insertItem(pVarFunc.apply(IDs.SLOT_BOTTLES_OUTPUT), new ItemStack(Items.GLASS_BOTTLE, bottlesToInsert * totalCycles), false);
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

        resolveActuators(pEntity);
    }

    ////////////////////
    // DATA SLOT HANDLING
    ////////////////////

    protected abstract void pushData();

    public static float getTimeScalar(int pGrime, Function<IDs, Integer> pVarFunc) {
        float grimeScalar = Math.min(Math.max(Math.min(Math.max(getGrimePercent(pGrime, pVarFunc) - 0.5f, 0f), 1f) * 2f, 0f), 1f);
        return 1f + grimeScalar * 3f;
    }

    public static int getOperationTicks(int pGrime, int pBatchSize, float pOperationTimeMod, Function<IDs, Integer> pVarFunc) {
        float otmScalar = (10000f - pOperationTimeMod) / 10000f;
        float batchScalar = ActuatorAirBlockEntity.getPenaltyRateFromBatchSize(pBatchSize);
        return Math.round(pVarFunc.apply(IDs.CONFIG_OPERATION_TIME) * getTimeScalar(pGrime, pVarFunc) * otmScalar * batchScalar);
    }

    public static int getActualEfficiency(int pMod, int pGrime, Function<IDs, Integer> pVarFunc) {
        float grimeScalar = 1f - Math.min(Math.max(Math.min(Math.max(getGrimePercent(pGrime, pVarFunc) - 0.5f, 0f), 1f) * 2f, 0f), 1f);
        return Math.round((pVarFunc.apply(IDs.CONFIG_BASE_EFFICIENCY) + pMod) * grimeScalar);
    }

    public static float getGrimePercent(int pGrime, Function<IDs, Integer> pVarFunc) {
        return (float)pGrime / (float)pVarFunc.apply(IDs.CONFIG_MAX_GRIME);
    }

    public static int getScaledProgress(int pProgress, int pGrime, int pBatchSize, float pOperationTimeMod, Function<IDs, Integer> pVarFunc) {
        return pVarFunc.apply(IDs.GUI_PROGRESS_BAR_WIDTH) * pProgress / getOperationTicks(pGrime, pBatchSize, pOperationTimeMod, pVarFunc);
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

    ////////////////////
    // ACTUATOR HANDLING
    ////////////////////

    protected static void updateActuatorValues(AbstractSeparationBlockEntity entity) {
        for(DirectionalPluginBlockEntity dpbe : entity.pluginDevices) {
            if(dpbe instanceof ActuatorWaterBlockEntity water) {
                entity.efficiencyMod = ActuatorWaterBlockEntity.getIsSatisfied(water) ? water.getEfficiencyIncrease() : 0;
            }
        }
    }

    public static void resolveActuators(AbstractSeparationBlockEntity pEntity) {
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
        SLOT_BOTTLES, SLOT_BOTTLES_OUTPUT, SLOT_INPUT_START, SLOT_INPUT_COUNT, SLOT_OUTPUT_START, SLOT_OUTPUT_COUNT,
        CONFIG_BASE_EFFICIENCY, CONFIG_OPERATION_TIME, CONFIG_MAX_GRIME, CONFIG_GRIME_ON_SUCCESS, CONFIG_GRIME_ON_FAILURE,
        CONFIG_NO_TORQUE_GRACE_PERIOD, CONFIG_TORQUE_GAIN_ON_ACTIVATION, CONFIG_ANIMUS_GAIN_ON_DUSTING,
        DATA_PROGRESS, DATA_GRIME, DATA_TORQUE, DATA_ANIMUS, DATA_EFFICIENCY_MOD, DATA_OPERATION_TIME_MOD,
        GUI_PROGRESS_BAR_WIDTH, GUI_GRIME_BAR_WIDTH
    }

}
