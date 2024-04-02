package com.aranaira.magichem.block.entity.ext;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.AlembicBlockEntity;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class AbstractDistillationBlockEntity extends AbstractBlockEntityWithEfficiency {

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected ContainerData data;
    protected int progress = 0;

    protected ItemStackHandler itemHandler;

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    protected AbstractDistillationBlockEntity(BlockEntityType pType, BlockPos pPos, int pEfficiency, BlockState pState) {
        super(pType, pPos, pEfficiency, pState);
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

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, AbstractDistillationBlockEntity pEntity, Function<IDs, Integer> pVarFunc) {
        //skip all of this if grime is full
        if(GrimeProvider.getCapability(pEntity).getGrime() >= Config.alembicMaximumGrime)
            return;

        //figure out what slot and stack to target
        Pair<Integer, ItemStack> processing = getProcessingItem(pEntity, pVarFunc);
        int processingSlot = processing.getFirst();
        ItemStack processingItem = processing.getSecond();

        AlchemicalCompositionRecipe recipe = getRecipeInSlot(pEntity, processingSlot);
        if(processingItem != ItemStack.EMPTY && recipe != null) {
            if(canCraftItem(pEntity, recipe, pVarFunc)) {
                if (pEntity.progress > getOperationTicks(GrimeProvider.getCapability(pEntity).getGrime(), pVarFunc)) {
                    if (!pLevel.isClientSide()) {
                        craftItem(pEntity, recipe, processingSlot, pVarFunc);
                        pEntity.pushData();
                    }
                    if (!pEntity.isStalled)
                        pEntity.resetProgress();
                } else
                    pEntity.incrementProgress();
            }
        }
        else if(processingItem == ItemStack.EMPTY)
            pEntity.resetProgress();
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

    protected static void craftItem(AbstractDistillationBlockEntity entity, AlchemicalCompositionRecipe recipe, int processingSlot, Function<IDs, Integer> pVarFunc) {
        SimpleContainer outputSlots = new SimpleContainer(9);
        for(int i=0; i<pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            outputSlots.setItem(i, entity.itemHandler.getStackInSlot(pVarFunc.apply(IDs.SLOT_OUTPUT_START)+i));
        }

        Pair<Integer, NonNullList<ItemStack>> pair = applyEfficiencyToCraftingResult(recipe.getComponentMateria(), AlembicBlockEntity.getActualEfficiency(GrimeProvider.getCapability(entity).getGrime(), pVarFunc), recipe.getOutputRate(), Config.alembicGrimeOnSuccess, Config.alembicGrimeOnFailure);
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
                entity.itemHandler.setStackInSlot(pVarFunc.apply(IDs.SLOT_OUTPUT_START) + i, outputSlots.getItem(i));
            }
            ItemStack processingSlotContents = entity.itemHandler.getStackInSlot(processingSlot);
            processingSlotContents.shrink(1);
            if(processingSlotContents.getCount() == 0)
                entity.itemHandler.setStackInSlot(processingSlot, ItemStack.EMPTY);
        }

        IGrimeCapability grimeCapability = GrimeProvider.getCapability(entity);
        grimeCapability.setGrime(Math.min(Math.max(grimeCapability.getGrime() + grimeToAdd, 0), pVarFunc.apply(IDs.CONFIG_MAX_GRIME)));
    }

    ////////////////////
    // DATA SLOT HANDLING
    ////////////////////

    protected abstract void pushData();

    public static float getTimeScalar(int pGrime, Function<IDs, Integer> pVarFunc) {
        float grimeScalar = Math.min(Math.max(Math.min(Math.max(getGrimePercent(pGrime, pVarFunc) - 0.5f, 0f), 1f) * 2f, 0f), 1f);
        return 1f + grimeScalar * 3f;
    }

    public static int getOperationTicks(int pGrime, Function<IDs, Integer> pVarFunc) {
        return Math.round(pVarFunc.apply(IDs.CONFIG_OPERATION_TIME) * getTimeScalar(pGrime, pVarFunc));
    }

    public static int getActualEfficiency(int pGrime, Function<IDs, Integer> pVarFunc) {
        float grimeScalar = 1f - Math.min(Math.max(Math.min(Math.max(getGrimePercent(pGrime, pVarFunc) - 0.5f, 0f), 1f) * 2f, 0f), 1f);
        return Math.round(baseEfficiency * grimeScalar);
    }

    public static float getGrimePercent(int pGrime, Function<IDs, Integer> pVarFunc) {
        return (float)pGrime / (float)pVarFunc.apply(IDs.CONFIG_MAX_GRIME);
    }

    public static int getScaledProgress(int pProgress, int pGrime, Function<IDs, Integer> pVarFunc) {
        return pVarFunc.apply(IDs.GUI_PROGRESS_BAR_WIDTH) * pProgress / getOperationTicks(pGrime, pVarFunc);
    }

    ////////////////////
    // FINAL VARIABLE RETRIEVAL
    ////////////////////

    public static int getVar(IDs pID) {
        return -2;
    }

    public enum IDs {
        SLOT_BOTTLES, SLOT_INPUT_START, SLOT_INPUT_COUNT, SLOT_OUTPUT_START, SLOT_OUTPUT_COUNT,
        CONFIG_OPERATION_TIME, CONFIG_MAX_GRIME, CONFIG_MAX_BURN_TIME,
        DATA_PROGRESS, DATA_GRIME, DATA_FUEL_TIME, DATA_FUEL_DURATION, DATA_EFFICIENCY_MOD, DATA_OPERATION_TIME_MOD,
        GUI_PROGRESS_BAR_WIDTH, GUI_BURN_TIME_HEIGHT, GUI_GRIME_BAR_WIDTH
    }
}
