package com.aranaira.magichem.block.entity.ext;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.AlembicBlock;
import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public abstract class AbstractDistillationBlockEntity extends AbstractBlockEntityWithEfficiency implements ICanTakePlugins {

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected ContainerData data;
    protected int
            progress = 0, batchSize = 1, remainingHeat = 0, heatDuration = 0, pluginLinkageCountdown = 3;

    protected ItemStackHandler itemHandler;
    protected List<DirectionalPluginBlockEntity> pluginDevices = new ArrayList<>();

    private static HashMap<String, AdmixtureItem> admixturesMap = ItemRegistry.getAdmixturesMap(false, true);
    private static final NonNullList<AdmixtureItem> admixturesForRandomSelection = NonNullList.create();
    private static final Random random = new Random();

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    protected AbstractDistillationBlockEntity(BlockEntityType pType, BlockPos pPos, BlockState pState) {
        super(pType, pPos, pState);

        if(admixturesForRandomSelection.stream().count() == 0) {
            for(AdmixtureItem ai : admixturesMap.values()) {
                for(int i=0; i < Math.pow(5 - ai.getDepth(), 2); i++) {
                    admixturesForRandomSelection.add(ai);
                }
            }
        }
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

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    ////////////////////
    // CRAFTING HANDLERS
    ////////////////////

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, AbstractDistillationBlockEntity pEntity, Function<IDs, Integer> pVarFunc, Function<Void, Integer> pPoweredTimeFunc) {
        for (DirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
            if (dpbe instanceof ActuatorFireBlockEntity fire) {
                ActuatorFireBlockEntity.delegatedTick(pLevel, pPos, pState, fire);
                if (ActuatorFireBlockEntity.getIsSatisfied(fire) && pEntity.remainingHeat <= 20) {
                    if(!(pEntity instanceof GrandDistilleryBlockEntity)) {
                        pEntity.remainingHeat = 1000;
                        pEntity.heatDuration = 1000;
                    }
                    pEntity.operationTimeMod = fire.getReductionRate();
                    pEntity.syncAndSave();
                }
            }
            if (dpbe instanceof ActuatorEarthBlockEntity earth) {
                ActuatorEarthBlockEntity.delegatedTick(pLevel, pPos, pState, earth);
            }
            if (dpbe instanceof ActuatorWaterBlockEntity water) {
                ActuatorWaterBlockEntity.delegatedTick(pLevel, pPos, pState, water);
            }
            if (dpbe instanceof ActuatorAirBlockEntity air) {
                ActuatorAirBlockEntity.delegatedTick(pLevel, pPos, pState, air);
                int pre = pEntity.batchSize;

                if(ActuatorAirBlockEntity.getIsSatisfied(air)) {
                    pEntity.batchSize = air.getBatchSize();
                } else
                    pEntity.batchSize = 1;

                if(pre != pEntity.batchSize)
                    pEntity.syncAndSave();
            }
            if (dpbe instanceof ActuatorArcaneBlockEntity arcane) {
                ActuatorArcaneBlockEntity.delegatedTick(pLevel, pPos, pState, arcane, false);
            }
        }

        pEntity.remainingHeat = Math.max(0, pEntity.remainingHeat - 1);

        //skip all of this if grime is full
        if(GrimeProvider.getCapability(pEntity).getGrime() >= pVarFunc.apply(IDs.CONFIG_MAX_GRIME))
            return;

        updateActuatorValues(pEntity);

        //make sure we have enough heat (or passive heat) to operate
        boolean hasPassiveHeat = false;
        boolean doubleSpeed = false;
        if(pEntity instanceof AlembicBlockEntity abe) {
            hasPassiveHeat = pEntity.getBlockState().getValue(MagiChemBlockStateProperties.HAS_PASSIVE_HEAT);
            BlockState below = pLevel.getBlockState(pPos.below());
            doubleSpeed = ((below.getBlock() == Blocks.SMOKER) && below.getValue(BlockStateProperties.LIT)) || ((below.getBlock() == Blocks.BLAST_FURNACE) && below.getValue(BlockStateProperties.LIT));
        }

        if(pEntity.remainingHeat > 0 || hasPassiveHeat) {
            int operationTicks = getOperationTicks(GrimeProvider.getCapability(pEntity).getGrime(), pEntity.batchSize, pEntity.operationTimeMod * 100, pVarFunc, pPoweredTimeFunc);
            boolean halfSpeed = hasPassiveHeat && (pEntity.remainingHeat <= 0);

            //figure out what slot and stack to target
            Pair<Integer, ItemStack> processing = getProcessingItem(pEntity, pVarFunc);
            int processingSlot = processing.getFirst();
            ItemStack processingItem = processing.getSecond();

            AlchemicalCompositionRecipe recipe = getRecipeInSlot(pEntity, processingSlot);
            if(recipe != null) {
                if (canCraftItem(pEntity, recipe, pVarFunc)) {
                    if (pEntity.progress > operationTicks) {
                        if (!pLevel.isClientSide()) {
                            craftItem(pEntity, recipe, processingSlot, pVarFunc);
                            pEntity.pushData();
                        }
                        if (!pEntity.isStalled)
                            pEntity.resetProgress();
                    } else {
                        if (doubleSpeed) {
                            pEntity.incrementProgress();
                            pEntity.incrementProgress();
                        } else if (!halfSpeed) {
                            pEntity.incrementProgress();
                        } else if (pLevel.getGameTime() % 2 == 0)
                            pEntity.incrementProgress();
                    }
                }
            } else if(processingItem.getItem() == ItemRegistry.RAREFIED_WASTE.get()) {
                if (canCraftRandom(pEntity, pVarFunc)) {
                    if (pEntity.progress > operationTicks) {
                        if (!pLevel.isClientSide()) {
                            craftRandomAdmixture(pEntity, processingSlot, pVarFunc);
                            pEntity.pushData();
                        }
                        if (!pEntity.isStalled)
                            pEntity.resetProgress();
                    } else {
                        if (doubleSpeed) {
                            pEntity.incrementProgress();
                            pEntity.incrementProgress();
                        } else if (!halfSpeed) {
                            pEntity.incrementProgress();
                        } else if (pLevel.getGameTime() % 2 == 0)
                            pEntity.incrementProgress();
                    }
                }
            }
            if (processingItem == ItemStack.EMPTY)
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

    protected static Pair<Integer, ItemStack> getProcessingItem(AbstractDistillationBlockEntity entity, Function<IDs, Integer> pVarFunc) {
        int processingSlot = pVarFunc.apply(IDs.SLOT_INPUT_START)+pVarFunc.apply(IDs.SLOT_INPUT_COUNT)-1;
        ItemStack processingItem;
        int outputSlot = processingSlot;
        ItemStack outputItem = ItemStack.EMPTY;

        while(processingSlot > pVarFunc.apply(IDs.SLOT_INPUT_START) - 1) {
            processingItem = entity.itemHandler.getStackInSlot(processingSlot);

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
        return getContentsOfOutputSlots(AbstractDistillationBlockEntity::getVar);
    }

    public SimpleContainer getContentsOfOutputSlots(Function<IDs, Integer> pVarFunc) {
        SimpleContainer output = new SimpleContainer(pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT));

        for(int i=pVarFunc.apply(IDs.SLOT_OUTPUT_START); i<pVarFunc.apply(IDs.SLOT_OUTPUT_START)+pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            output.setItem(i-pVarFunc.apply(IDs.SLOT_OUTPUT_START), itemHandler.getStackInSlot(i));
        }

        return output;
    }

    public void setContentsOfOutputSlots(SimpleContainer replacementInventory, Function<IDs, Integer> pVarFunc) {
        for(int i=pVarFunc.apply(IDs.SLOT_OUTPUT_START); i<pVarFunc.apply(IDs.SLOT_OUTPUT_START)+pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            itemHandler.setStackInSlot(i, replacementInventory.getItem(i-pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT)));
        }
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

    protected static AlchemicalCompositionRecipe getRecipeInSlot(AbstractDistillationBlockEntity entity, int slot) {
        Level level = entity.level;

        AlchemicalCompositionRecipe recipe = AlchemicalCompositionRecipe.getDistillingRecipe(level, entity.itemHandler.getStackInSlot(slot));

        if(recipe != null) {
            return recipe;
        }

        return null;
    }

    protected static boolean canCraftItem(AbstractDistillationBlockEntity pEntity, AlchemicalCompositionRecipe pRecipe, Function<IDs, Integer> pVarFunc) {
        SimpleContainer cont = new SimpleContainer(pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT));
        for(int i=pVarFunc.apply(IDs.SLOT_OUTPUT_START); i<pVarFunc.apply(IDs.SLOT_OUTPUT_START)+pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            cont.setItem(i-pVarFunc.apply(IDs.SLOT_OUTPUT_START), pEntity.itemHandler.getStackInSlot(i).copy());
        }

        for(int i=0; i<pRecipe.getComponentMateria().size(); i++) {
            if(!cont.canAddItem(pRecipe.getComponentMateria().get(i).copy()))
                return false;
            cont.addItem(pRecipe.getComponentMateria().get(i).copy());
        }

        return true;
    }

    protected static boolean canCraftRandom(AbstractDistillationBlockEntity pEntity, Function<IDs, Integer> pVarFunc) {
        boolean hasSpace = false;
        for(int i=pVarFunc.apply(IDs.SLOT_OUTPUT_START); i<pVarFunc.apply(IDs.SLOT_OUTPUT_START)+pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            if(pEntity.itemHandler.getStackInSlot(i).isEmpty()) {
                hasSpace = true;
                break;
            }
        }
        return hasSpace;
    }

    protected static void craftItem(AbstractDistillationBlockEntity pEntity, AlchemicalCompositionRecipe pRecipe, int pProcessingSlot, Function<IDs, Integer> pVarFunc) {
        SimpleContainer outputSlots = new SimpleContainer(pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT));
        for(int i=0; i<pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            outputSlots.setItem(i, pEntity.itemHandler.getStackInSlot(pVarFunc.apply(IDs.SLOT_OUTPUT_START)+i));
        }

        int totalCycles = 0;
        for(int batch=0; batch<pEntity.batchSize; batch++) {
            totalCycles++;
            if(!canCraftItem(pEntity, pRecipe, pVarFunc)) {
                break;
            } else if (pEntity.itemHandler.getStackInSlot(pProcessingSlot).isEmpty()) {
                break;
            }

            Pair<Integer, NonNullList<ItemStack>> pair = applyEfficiencyToCraftingResult(pRecipe.getComponentMateria(), AbstractDistillationBlockEntity.getActualEfficiency(pEntity.efficiencyMod, GrimeProvider.getCapability(pEntity).getGrime(), pVarFunc), pRecipe.getOutputRate(), pVarFunc.apply(IDs.CONFIG_GRIME_ON_SUCCESS), pVarFunc.apply(IDs.CONFIG_GRIME_ON_FAILURE));
            int grimeToAdd = Math.round(pair.getFirst() * pRecipe.getOutputRate());
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
                if (dpbe instanceof ActuatorArcaneBlockEntity aabe) {
                    aabe.generateAcademicSlurry();
                }
            }

            if (grimeToAdd > 0) {
                IGrimeCapability grimeCapability = GrimeProvider.getCapability(pEntity);
                grimeCapability.setGrime(Math.min(Math.max(grimeCapability.getGrime() + grimeToAdd, 0), pVarFunc.apply(IDs.CONFIG_MAX_GRIME)));
            }
        }

        resolveActuators(pEntity, totalCycles);
    }

    protected static void craftRandomAdmixture(AbstractDistillationBlockEntity pEntity, int pProcessingSlot, Function<IDs, Integer> pVarFunc) {
        SimpleContainer outputSlots = new SimpleContainer(pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT));
        for(int i=0; i<pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            outputSlots.setItem(i, pEntity.itemHandler.getStackInSlot(pVarFunc.apply(IDs.SLOT_OUTPUT_START)+i));
        }

        int totalCycles = 0;
        for(int batch=0; batch<pEntity.batchSize; batch++) {
            totalCycles++;
            NonNullList<ItemStack> randomAdmixtureList = NonNullList.create();

            AdmixtureItem ai = admixturesForRandomSelection.get(random.nextInt(admixturesForRandomSelection.size()));
            randomAdmixtureList.add(new ItemStack(ai, 1));

            Pair<Integer, NonNullList<ItemStack>> pair = applyEfficiencyToCraftingResult(randomAdmixtureList, AbstractDistillationBlockEntity.getActualEfficiency(pEntity.efficiencyMod, GrimeProvider.getCapability(pEntity).getGrime(), pVarFunc), 1.0f, pVarFunc.apply(IDs.CONFIG_GRIME_ON_SUCCESS), pVarFunc.apply(IDs.CONFIG_GRIME_ON_FAILURE));
            NonNullList<ItemStack> componentMateria = pair.getSecond();

            boolean canCraft = true;
            for (ItemStack item : componentMateria) {
                if (outputSlots.canAddItem(item)) {
                    outputSlots.addItem(item);
                } else {
                    canCraft = false;
                    break;
                }
            }

            if (!pEntity.isStalled && canCraft) {
                for (int i = 0; i < pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
                    pEntity.itemHandler.setStackInSlot(pVarFunc.apply(IDs.SLOT_OUTPUT_START) + i, outputSlots.getItem(i));
                }
                ItemStack processingSlotContents = pEntity.itemHandler.getStackInSlot(pProcessingSlot);
                processingSlotContents.shrink(1);
                if (processingSlotContents.getCount() == 0)
                    pEntity.itemHandler.setStackInSlot(pProcessingSlot, ItemStack.EMPTY);
            } else {
                break;
            }
        }

        resolveActuators(pEntity, totalCycles);
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
        int maxWidth = pVarFunc.apply(IDs.GUI_PROGRESS_BAR_WIDTH);
        return Math.min(maxWidth, maxWidth * pProgress / getOperationTicks(pGrime, pBatchSize, pOperationTimeMod, pVarFunc, pPoweredTimeFunc));
    }

    public static int getScaledHeat(int pHeat, int pHeatDuration, Function<IDs, Integer> pVarFunc) {
        return 1 + (pVarFunc.apply(IDs.GUI_HEAT_GAUGE_HEIGHT) * pHeat / pHeatDuration);
    }

    public void setHeat(int pNewHeatTicks) {
        remainingHeat = pNewHeatTicks;
        heatDuration = pNewHeatTicks;
        syncAndSave();
    }

    ////////////////////
    // ACTUATOR HANDLING
    ////////////////////

    protected static void updateActuatorValues(AbstractDistillationBlockEntity entity) {
        for(DirectionalPluginBlockEntity dpbe : entity.pluginDevices) {
            if(dpbe instanceof ActuatorWaterBlockEntity water) {
                entity.efficiencyMod = ActuatorWaterBlockEntity.getIsSatisfied(water) ? water.getEfficiencyIncrease() : 0;
            }
        }
    }

    public static void resolveActuators(AbstractDistillationBlockEntity pEntity, int pCyclesCompleted) {
        for(DirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
            dpbe.processCompletedOperation(pCyclesCompleted);
        }
    }

    @Override
    public void linkPlugins() {
        pluginDevices.clear();
        pluginLinkageCountdown = 3;
    }

    @Override
    public void removePlugin(DirectionalPluginBlockEntity pPlugin) {
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
        syncAndSave();
    }

    @Override
    public void linkPluginsDeferred() {
        pluginLinkageCountdown = 3;
    }

    ////////////////////
    // FINAL VARIABLE RETRIEVAL
    ////////////////////

    public Integer getPoweredOperationTime(Void unused) {
        return -1;
    }

    public static int getVar(IDs pID) {
        return -2;
    }

    public enum IDs {
        SLOT_BOTTLES, SLOT_FUEL, SLOT_INPUT_START, SLOT_INPUT_COUNT, SLOT_OUTPUT_START, SLOT_OUTPUT_COUNT,
        CONFIG_BASE_EFFICIENCY, CONFIG_OPERATION_TIME, CONFIG_MAX_GRIME, CONFIG_MAX_BURN_TIME, CONFIG_GRIME_ON_SUCCESS, CONFIG_GRIME_ON_FAILURE,
        MODE_USES_RF,
        DATA_PROGRESS, DATA_GRIME, DATA_REMAINING_HEAT, DATA_HEAT_DURATION, DATA_EFFICIENCY_MOD, DATA_OPERATION_TIME_MOD,
        GUI_PROGRESS_BAR_WIDTH, GUI_HEAT_GAUGE_HEIGHT, GUI_GRIME_BAR_WIDTH
    }
}
