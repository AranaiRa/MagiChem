package com.aranaira.magichem.block.entity.ext;

import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.recipe.DistillationFabricationRecipe;
import com.aranaira.magichem.util.InventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import java.util.Random;
import java.util.function.Function;

public abstract class AbstractFabricationBlockEntity extends BlockEntity implements ICanTakePlugins {

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected ContainerData data;
    protected int
            progress = 0, operationTicks = 0, pluginLinkageCountdown = 3;
    protected boolean
            isFESatisfied = false;

    protected ItemStackHandler itemHandler;
    protected List<AbstractDirectionalPluginBlockEntity> pluginDevices = new ArrayList<>();
    protected DistillationFabricationRecipe recipe;

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    protected AbstractFabricationBlockEntity(BlockEntityType pType, BlockPos pPos, BlockState pState) {
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

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    ////////////////////
    // CRAFTING HANDLERS
    ////////////////////

    public static boolean tick(Level pLevel, BlockPos pPos, BlockState pState, AbstractFabricationBlockEntity pEntity, Function<IDs, Integer> pVarFunc) {
        boolean changed = false;

        for (AbstractDirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
            if (dpbe instanceof ActuatorArcaneBlockEntity arcane) {
                ActuatorArcaneBlockEntity.delegatedTick(pLevel, pPos, pState, arcane, false);
            }
        }

        if(pEntity.isFESatisfied && pEntity.operationTicks > 0) {

            if(pEntity.recipe != null) {
                if (canCraftItem(pEntity, pEntity.recipe, pVarFunc)) {
                    if (pEntity.progress > pEntity.operationTicks) {
                        if (!pLevel.isClientSide()) {
                            craftItem(pEntity, pEntity.recipe, pVarFunc);
                            pEntity.resetProgress();
                            changed = true;
                        }
                    } else {
                        pEntity.incrementProgress();
                    }
                } else {
                    pEntity.resetProgress();
                }
            } else {
                pEntity.resetProgress();
            }
        } else {
            pEntity.decrementProgress();
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

        return changed;
    }

    public SimpleContainer getContentsOfOutputSlots() {
        return getContentsOfOutputSlots(AbstractFabricationBlockEntity::getVar);
    }

    public SimpleContainer getContentsOfOutputSlots(Function<IDs, Integer> pVarFunc) {
        SimpleContainer output = new SimpleContainer(pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT));

        for(int i = pVarFunc.apply(IDs.SLOT_OUTPUT_START); i<pVarFunc.apply(IDs.SLOT_OUTPUT_START)+pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            output.setItem(i-pVarFunc.apply(IDs.SLOT_OUTPUT_START), itemHandler.getStackInSlot(i));
        }

        return output;
    }

    public void setContentsOfOutputSlots(SimpleContainer replacementInventory, Function<IDs, Integer> pVarFunc) {
        for(int i = pVarFunc.apply(IDs.SLOT_OUTPUT_START); i<pVarFunc.apply(IDs.SLOT_OUTPUT_START)+pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            itemHandler.setStackInSlot(i, replacementInventory.getItem(i-pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT)));
        }
    }

    protected void resetProgress() {
        progress = 0;
    }

    protected void incrementProgress() {
        progress++;
    }

    protected void decrementProgress() {
        progress = Math.max(0, progress - 1);
    }

    ////////////////////
    // RECIPE HANDLING
    ////////////////////

    protected static boolean canCraftItem(AbstractFabricationBlockEntity pEntity, DistillationFabricationRecipe pRecipe, Function<IDs, Integer> pVarFunc) {
        //Has all inputs?
        SimpleContainer inputSlots = new SimpleContainer(pVarFunc.apply(IDs.SLOT_INPUT_COUNT));
        for (int i = 0; i < pVarFunc.apply(IDs.SLOT_INPUT_COUNT); i++) {
            inputSlots.setItem(i, pEntity.itemHandler.getStackInSlot(pVarFunc.apply(IDs.SLOT_INPUT_START) + i));
        }

        for(ItemStack query : pRecipe.getComponentMateria()) {
            int remaining = query.getCount();
            for(int i=0; i<inputSlots.getContainerSize(); i++) {
                ItemStack stackInSlot = inputSlots.getItem(i);
                if(stackInSlot.getItem() == query.getItem())
                    remaining -= stackInSlot.getCount();
            }

            if(remaining > 0)
                return false;
        }

        //Space for output?
        SimpleContainer cont = new SimpleContainer(pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT));
        for(int i = pVarFunc.apply(IDs.SLOT_OUTPUT_START); i<pVarFunc.apply(IDs.SLOT_OUTPUT_START)+pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            cont.setItem(i-pVarFunc.apply(IDs.SLOT_OUTPUT_START), pEntity.itemHandler.getStackInSlot(i).copy());
        }

        return cont.canAddItem(pRecipe.getAlchemyObject().copy());
    }

    protected static void craftItem(AbstractFabricationBlockEntity pEntity, DistillationFabricationRecipe pRecipe, Function<IDs, Integer> pVarFunc) {
        SimpleContainer inputSlots = new SimpleContainer(pVarFunc.apply(IDs.SLOT_INPUT_COUNT));
        for (int i = 0; i < pVarFunc.apply(IDs.SLOT_INPUT_COUNT); i++) {
            inputSlots.setItem(i, pEntity.itemHandler.getStackInSlot(pVarFunc.apply(IDs.SLOT_INPUT_START) + i));
        }

        SimpleContainer outputSlots = new SimpleContainer(pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT));
        for (int i = 0; i < pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            outputSlots.setItem(i, pEntity.itemHandler.getStackInSlot(pVarFunc.apply(IDs.SLOT_OUTPUT_START) + i));
        }

        int bottlesGenerated = 0;
        for (ItemStack item : pRecipe.getComponentMateria()) {
            int totalThisIngredient = item.getCount();

            //tally up bottles
            for (int i=0; i<inputSlots.getContainerSize(); i++) {
                ItemStack stackInSlot = inputSlots.getItem(i);
                if(stackInSlot.getItem() == item.getItem()) {
                    if(!InventoryHelper.isMateriaUnbottled(stackInSlot)) {
                        int limit = Math.min(totalThisIngredient, stackInSlot.getCount());
                        bottlesGenerated += limit;
                        totalThisIngredient -= limit;
                    }
                }
            }

            inputSlots.removeItemType(item.getItem(), item.getCount());
        }

        outputSlots.addItem(pRecipe.getAlchemyObject().copy());

        for (int i = 0; i < pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            pEntity.itemHandler.setStackInSlot(pVarFunc.apply(IDs.SLOT_OUTPUT_START) + i, outputSlots.getItem(i));
        }

        resolveActuators(pEntity);

        //Put bottles into output slot, eject the rest
        ItemStack bottleStack = pEntity.itemHandler.getStackInSlot(pVarFunc.apply(IDs.SLOT_BOTTLES));
        int spillCount;
        int limit = pEntity.itemHandler.getSlotLimit(pVarFunc.apply(IDs.SLOT_BOTTLES));
        if(bottleStack.getCount() >= limit) {
            spillCount = bottlesGenerated;
        } else if(bottleStack.getCount() > 0) {
            int delta = Math.min(limit - bottleStack.getCount(), bottlesGenerated);
            bottleStack.grow(delta);
            spillCount = bottlesGenerated - delta;
        } else {
            pEntity.itemHandler.setStackInSlot(pVarFunc.apply(IDs.SLOT_BOTTLES), new ItemStack(Items.GLASS_BOTTLE, Math.min(limit, bottlesGenerated)));
            spillCount = Math.max(0, bottlesGenerated - limit);
        }

        while(spillCount > 0) {
            ItemStack spillStack = new ItemStack(Items.GLASS_BOTTLE);
            int maxStackSizeBottles = spillStack.getMaxStackSize();
            int delta = Math.min(spillCount, maxStackSizeBottles);
            spillStack.setCount(delta);

            ItemEntity ie = new ItemEntity(pEntity.getLevel(), pEntity.getBlockPos().getX() + 0.5, pEntity.getBlockPos().getY() + 0.5, pEntity.getBlockPos().getZ() + 0.5, spillStack);
            pEntity.getLevel().addFreshEntity(ie);

            spillCount -= delta;

        }
    }

    ////////////////////
    // DATA HANDLING
    ////////////////////

    public boolean hasSufficientPower() {
        return isFESatisfied;
    }

    ////////////////////
    // ACTUATOR HANDLING
    ////////////////////

    public static void resolveActuators(AbstractFabricationBlockEntity pEntity) {
        for(AbstractDirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
            dpbe.processCompletedOperation(1);
        }
    }

    @Override
    public void linkPlugins() {
        pluginDevices.clear();
        pluginLinkageCountdown = 3;
    }

    @Override
    public void removePlugin(AbstractDirectionalPluginBlockEntity pPlugin) {
        this.pluginDevices.remove(pPlugin);
        syncAndSave();
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
        SLOT_RECIPE, SLOT_BOTTLES, SLOT_INPUT_START, SLOT_INPUT_COUNT, SLOT_OUTPUT_START, SLOT_OUTPUT_COUNT
    }
}
