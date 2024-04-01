package com.aranaira.magichem.block.entity.ext;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.AlembicBlockEntity;
import com.aranaira.magichem.block.entity.interfaces.IMateriaProcessingDevice;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
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

public abstract class AbstractDistillationBlockEntity extends AbstractBlockEntityWithEfficiency implements IMateriaProcessingDevice {

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected ContainerData data;
    protected int progress = 0;

    protected final ItemStackHandler itemHandler;

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    protected AbstractDistillationBlockEntity(BlockEntityType pType, BlockPos pPos, int pEfficiency, BlockState pState) {
        super(pType, pPos, pEfficiency, pState);
        this.itemHandler = new ItemStackHandler(1);
    }

    //////////
    // BOILERPLATE CODE
    //////////

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

    //////////
    // CRAFTING HANDLERS
    //////////

    public static void tick(Level level, BlockPos pos, BlockState state, AbstractDistillationBlockEntity entity) {
        //skip all of this if grime is full
        if(GrimeProvider.getCapability(entity).getGrime() >= Config.alembicMaximumGrime)
            return;

        //figure out what slot and stack to target
        Pair<Integer, ItemStack> processing = getProcessingItem(entity);
        int processingSlot = processing.getFirst();
        ItemStack processingItem = processing.getSecond();

        AlchemicalCompositionRecipe recipe = getRecipeInSlot(entity, processingSlot);
        if(processingItem != ItemStack.EMPTY && recipe != null) {
            if(canCraftItem(entity, recipe)) {
                if (entity.progress > getOperationTicks(GrimeProvider.getCapability(entity).getGrime())) {
                    if (!level.isClientSide()) {
                        craftItem(entity, recipe, processingSlot);
                        entity.pushData();
                    }
                    if (!entity.isStalled)
                        entity.resetProgress();
                } else
                    entity.incrementProgress();
            }
        }
        else if(processingItem == ItemStack.EMPTY)
            entity.resetProgress();
    }

    protected static Pair<Integer, ItemStack> getProcessingItem(AbstractDistillationBlockEntity entity) {
        return new Pair<>(0, ItemStack.EMPTY);
    }

    @Override
    public SimpleContainer getContentsOfOutputSlots() {
        SimpleContainer output = new SimpleContainer(getVar(IDs.SLOT_OUTPUT_COUNT));

        for(int i=getVar(IDs.SLOT_OUTPUT_START); i<getVar(IDs.SLOT_OUTPUT_START)+getVar(IDs.SLOT_OUTPUT_COUNT); i++) {
            output.setItem(i-getVar(IDs.SLOT_OUTPUT_START), itemHandler.getStackInSlot(i));
        }

        return output;
    }

    @Override
    public void setContentsOfOutputSlots(SimpleContainer replacementInventory) {
        for(int i=getVar(IDs.SLOT_OUTPUT_START); i<getVar(IDs.SLOT_OUTPUT_START)+getVar(IDs.SLOT_OUTPUT_COUNT); i++) {
            itemHandler.setStackInSlot(i, replacementInventory.getItem(i-getVar(IDs.SLOT_OUTPUT_COUNT)));
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

    protected static boolean canCraftItem(AbstractDistillationBlockEntity entity, AlchemicalCompositionRecipe recipe) {
        SimpleContainer cont = new SimpleContainer(getVar(IDs.SLOT_OUTPUT_COUNT));
        for(int i=getVar(IDs.SLOT_OUTPUT_START); i<getVar(IDs.SLOT_OUTPUT_START)+getVar(IDs.SLOT_OUTPUT_COUNT); i++) {
            cont.setItem(i-getVar(IDs.SLOT_OUTPUT_START), entity.itemHandler.getStackInSlot(i).copy());
        }

        for(int i=0; i<recipe.getComponentMateria().size(); i++) {
            if(!cont.canAddItem(recipe.getComponentMateria().get(i).copy()))
                return false;
            cont.addItem(recipe.getComponentMateria().get(i).copy());
        }

        return true;
    }

    protected static void craftItem(AbstractDistillationBlockEntity entity, AlchemicalCompositionRecipe recipe, int processingSlot) {
        SimpleContainer outputSlots = new SimpleContainer(9);
        for(int i=0; i<getVar(IDs.SLOT_OUTPUT_COUNT); i++) {
            outputSlots.setItem(i, entity.itemHandler.getStackInSlot(getVar(IDs.SLOT_OUTPUT_START)+i));
        }

        Pair<Integer, NonNullList<ItemStack>> pair = applyEfficiencyToCraftingResult(recipe.getComponentMateria(), AlembicBlockEntity.getActualEfficiency(GrimeProvider.getCapability(entity).getGrime()), recipe.getOutputRate(), Config.alembicGrimeOnSuccess, Config.alembicGrimeOnFailure);
        int grimeToAdd = Math.round(pair.getFirst() * recipe.getOutputRate());
        NonNullList<ItemStack> componentMateria = pair.getSecond();

        for(ItemStack item : componentMateria) {
            if(outputSlots.canAddItem(item)) {
                outputSlots.addItem(item);
            }
            else {
                entity.isStalled = true;
                break;
            }
        }

        if(!entity.isStalled) {
            for(int i=0; i<9; i++) {
                entity.itemHandler.setStackInSlot(getVar(IDs.SLOT_OUTPUT_START) + i, outputSlots.getItem(i));
            }
            ItemStack processingSlotContents = entity.itemHandler.getStackInSlot(processingSlot);
            processingSlotContents.shrink(1);
            if(processingSlotContents.getCount() == 0)
                entity.itemHandler.setStackInSlot(processingSlot, ItemStack.EMPTY);
        }

        IGrimeCapability grimeCapability = GrimeProvider.getCapability(entity);
        grimeCapability.setGrime(Math.min(Math.max(grimeCapability.getGrime() + grimeToAdd, 0), Config.alembicMaximumGrime));
    }

    ////////////////////
    // DATA SLOT HANDLING
    ////////////////////

    protected abstract void pushData();

    public static float getTimeScalar(int grime) {
        float grimeScalar = Math.min(Math.max(Math.min(Math.max(getGrimePercent(grime) - 0.5f, 0f), 1f) * 2f, 0f), 1f);
        return 1f + grimeScalar * 3f;
    }

    public static int getOperationTicks(int grime) {
        return Math.round(getVar(IDs.CONFIG_OPERATION_TIME) * getTimeScalar(grime));
    }

    public static int getActualEfficiency(int grime) {
        float grimeScalar = 1f - Math.min(Math.max(Math.min(Math.max(getGrimePercent(grime) - 0.5f, 0f), 1f) * 2f, 0f), 1f);
        return Math.round(baseEfficiency * grimeScalar);
    }

    public static float getGrimePercent(int grime) {
        return (float)grime / (float)getVar(IDs.CONFIG_MAX_GRIME);
    }

    public static int getScaledProgress(int progress, int grime) {
        return getVar(IDs.GUI_PROGRESS_BAR_WIDTH) * progress / getOperationTicks(grime);
    }

    ////////////////////
    // FINAL VARIABLE RETRIEVAL
    ////////////////////

    protected static int getVar(IDs pID) {
        return 0;
    }

    protected enum IDs {
        SLOT_INPUT_START, SLOT_INPUT_COUNT,
        SLOT_OUTPUT_START, SLOT_OUTPUT_COUNT,
        CONFIG_OPERATION_TIME, CONFIG_MAX_GRIME, CONFIG_MAX_BURN_TIME,
        DATA_PROGRESS, DATA_GRIME, DATA_FUEL_TIME, DATA_FUEL_DURATION, DATA_EFFICIENCY_MOD, DATA_OPERATION_TIME_MOD,
        GUI_PROGRESS_BAR_WIDTH, GUI_BURN_TIME_HEIGHT, GUI_GRIME_BAR_WIDTH
    }
}
