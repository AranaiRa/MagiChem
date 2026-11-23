package com.aranaira.magichem.block.entity.ext;

import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.foundation.ICanHaveUnbottledMateriaInInputTray;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.item.PhilosophersStoneItem;
import com.aranaira.magichem.recipe.DistillationFabricationRecipe;
import com.aranaira.magichem.recipe.FluidDistillationFabricationRecipe;
import com.aranaira.magichem.util.InventoryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractFabricationBlockEntity extends BlockEntity implements ICanTakePlugins, IMateriaProvisionRequester, IFluidHandler, ICanHaveUnbottledMateriaInInputTray {

    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected ContainerData data;
    protected int
            progress = 0, operationTicks = 0, pluginLinkageCountdown = 3, batchSize = 1;
    protected boolean
            isFESatisfied = false, doDeferredRecipeCheck = false, deferredRecipeIsFluid = false;

    protected ItemStackHandler itemHandler;
    protected FluidStack outputTank = FluidStack.EMPTY.copy();
    protected List<AbstractDirectionalPluginBlockEntity> pluginDevices = new ArrayList<>();
    protected DistillationFabricationRecipe currentItemRecipe;
    protected FluidDistillationFabricationRecipe currentFluidRecipe;
    protected ResourceLocation deferredRecipeQuery = null;
    protected static final HashMap<Item, DistillationFabricationRecipe> allItemRecipes = new HashMap<>();
    protected static final HashMap<Fluid, FluidDistillationFabricationRecipe> allFluidRecipes = new HashMap<>();

    public boolean clearRecipeAfterNextProcess = false;

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

        if(pEntity instanceof GrandCircleFabricationBlockEntity grand && grand.isFESatisfied && !grand.isRedstonePaused()) {
            for (AbstractDirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
                if (dpbe instanceof ActuatorArcaneBlockEntity arcane) {
                    ActuatorArcaneBlockEntity.delegatedTick(pLevel, pPos, pState, arcane, false);
                }
                if (dpbe instanceof ActuatorEnderBlockEntity ender) {
                    ActuatorEnderBlockEntity.delegatedTick(pLevel, pPos, pState, ender);
                    if (ender.getIsSatisfied() && !ender.getPaused()) {
                        boolean instant = ender.getPowerLevel() == 2;
                        //importing
                        final Map<MateriaItem, Integer> provisioningNeeds = pEntity.getProvisioningNeeds();
                        if (provisioningNeeds != null && provisioningNeeds.size() > 0) {
                            if (ender.getMirrorTarget() instanceof AbstractMateriaStorageMultiTypeDynamicBlockEntity multi) {
                                for (MateriaItem mi : provisioningNeeds.keySet()) {
                                    int requested = provisioningNeeds.get(mi);
                                    int inStorage = multi.getCurrentStock(mi);

                                    int actualDrain = Math.min(requested, inStorage);
                                    if (actualDrain > 0) {
                                        multi.drain(mi, actualDrain, false);
                                        ender.createShlorpFromTarget(new ItemStack(mi, actualDrain), instant);

                                        pEntity.setProvisioningInProgress(mi);
                                    }
                                }
                            }
                        }
                        if (pEntity.currentItemRecipe == null && pEntity.currentFluidRecipe == null && pLevel.getGameTime() % 40 == 0) {
                            final SimpleContainer inputs = pEntity.getContentsOfInputSlots(pVarFunc);
                            for (int i = 0; i < inputs.getContainerSize(); i++) {
                                final ItemStack inputQuery = inputs.getItem(i);
                                if (!inputQuery.isEmpty() && InventoryHelper.hasCustomModelData(inputQuery)) {
                                    pEntity.itemHandler.setStackInSlot(pVarFunc.apply(AbstractFabricationBlockEntity.IDs.SLOT_INPUT_START) + i, ItemStack.EMPTY);
                                    ender.createShlorpToTarget(inputQuery, instant);
                                }
                            }
                        }
                    }
                }
            }
        }

        if(pEntity.isFESatisfied && pEntity.operationTicks > 0) {

            if(pEntity.currentItemRecipe != null) {
                if (canCraftItem(pEntity, pEntity.currentItemRecipe, pVarFunc)) {
                    if (pEntity.progress > pEntity.operationTicks) {
                        if (!pLevel.isClientSide()) {
                            craftItem(pEntity, pEntity.currentItemRecipe, pVarFunc);
                            pEntity.resetProgress();
                            changed = true;
                        }
                    } else {
                        pEntity.incrementProgress();
                    }
                } else {
                    pEntity.resetProgress();
                }
            } else if(pEntity.currentFluidRecipe != null) {
                if (canCraftFluid(pEntity, pEntity.currentFluidRecipe, pVarFunc)) {
                    if (pEntity.progress > pEntity.operationTicks) {
                        if (!pLevel.isClientSide()) {
                            craftFluid(pEntity, pEntity.currentFluidRecipe, pVarFunc);
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

    public SimpleContainer getContentsOfInputSlots() {
        return getContentsOfInputSlots(AbstractFabricationBlockEntity::getVar);
    }

    public SimpleContainer getContentsOfInputSlots(Function<IDs, Integer> pVarFunc) {
        SimpleContainer output = new SimpleContainer(pVarFunc.apply(IDs.SLOT_INPUT_COUNT));

        for(int i = pVarFunc.apply(IDs.SLOT_INPUT_START); i<pVarFunc.apply(IDs.SLOT_INPUT_START)+pVarFunc.apply(IDs.SLOT_INPUT_COUNT); i++) {
            output.setItem(i-pVarFunc.apply(IDs.SLOT_INPUT_START), itemHandler.getStackInSlot(i));
        }

        return output;
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

    public int getCurrentWisdom(Function<IDs, Integer> pVarFunc) {
        int slot = pVarFunc.apply(IDs.SLOT_STONE);
        if(slot >= 0) {
            final Item itemQuery = itemHandler.getStackInSlot(slot).getItem();
            if (itemQuery instanceof PhilosophersStoneItem stone) {
                return stone.getWisdom();
            }
        }
        return 0;
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

    public void setBatchSize(int pNewBatchSize) {
        this.batchSize = pNewBatchSize;
        this.resetProgress();
        if(level != null && !level.isClientSide())
            this.syncAndSave();
    }

    public int getBatchSize() {
        return this.batchSize;
    }

    ////////////////////
    // RECIPE HANDLING
    ////////////////////

    public ItemStack getRecipeItem() {
        return currentItemRecipe == null ? ItemStack.EMPTY.copy() : currentItemRecipe.getResultItem().copy();
    }

    public ItemStack getRecipeItem(boolean pMakeCopy) {
        return currentItemRecipe == null ? ItemStack.EMPTY.copy() : pMakeCopy ? currentItemRecipe.getResultItem().copy() : currentItemRecipe.getResultItem();
    }

    public void clearRecipe() {
        if(!clearRecipeAfterNextProcess) {
            clearRecipeAfterNextProcess = true;
            doDeferredRecipeCheck = false;
        } else {
            clearRecipeAfterNextProcess = false;
            doDeferredRecipeCheck = false;
            currentItemRecipe = null;
            currentFluidRecipe = null;
        }
        syncAndSave();
    }

    protected DistillationFabricationRecipe getRecipeForItem(Item pItem) {
        if(allItemRecipes.size() == 0) {
            for(DistillationFabricationRecipe recipe : DistillationFabricationRecipe.getAllDistillingRecipes(level)) {
                if(recipe.getWisdom() < 6) allItemRecipes.put(recipe.getAlchemyObject().getItem(), recipe);
            }
        }

        return allItemRecipes.get(pItem);
    }

    protected FluidDistillationFabricationRecipe getRecipeForFluid(Fluid pFluid) {
        if(allFluidRecipes.size() == 0) {
            for(FluidDistillationFabricationRecipe recipe : FluidDistillationFabricationRecipe.getAllDistillingRecipes(level)) {
                if(recipe.getWisdom() < 6) allFluidRecipes.put(recipe.getAlchemyFluid().getFluid(), recipe);
            }
        }

        return allFluidRecipes.get(pFluid);
    }

    protected static boolean canCraftItem(AbstractFabricationBlockEntity pEntity, DistillationFabricationRecipe pRecipe, Function<IDs, Integer> pVarFunc) {
        //Has all inputs?
        SimpleContainer inputSlots = new SimpleContainer(pVarFunc.apply(IDs.SLOT_INPUT_COUNT));
        for (int i = 0; i < pVarFunc.apply(IDs.SLOT_INPUT_COUNT); i++) {
            inputSlots.setItem(i, pEntity.itemHandler.getStackInSlot(pVarFunc.apply(IDs.SLOT_INPUT_START) + i));
        }

        for(ItemStack query : pRecipe.getComponentMateria()) {
            int remaining = query.getCount() * pEntity.batchSize;
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

        return cont.canAddItem(new ItemStack(pRecipe.getAlchemyObject().getItem(), Math.round(pRecipe.getAlchemyObject().getCount() * pEntity.batchSize * (1/ pEntity.currentItemRecipe.getOutputRate()))));
    }

    protected static boolean canCraftFluid(AbstractFabricationBlockEntity pEntity, FluidDistillationFabricationRecipe pRecipe, Function<IDs, Integer> pVarFunc) {
        //Has all inputs?
        SimpleContainer inputSlots = new SimpleContainer(pVarFunc.apply(IDs.SLOT_INPUT_COUNT));
        for (int i = 0; i < pVarFunc.apply(IDs.SLOT_INPUT_COUNT); i++) {
            inputSlots.setItem(i, pEntity.itemHandler.getStackInSlot(pVarFunc.apply(IDs.SLOT_INPUT_START) + i));
        }

        for(ItemStack query : pRecipe.getComponentMateria()) {
            int remaining = query.getCount() * pEntity.batchSize;
            for(int i=0; i<inputSlots.getContainerSize(); i++) {
                ItemStack stackInSlot = inputSlots.getItem(i);
                if(stackInSlot.getItem() == query.getItem())
                    remaining -= stackInSlot.getCount();
            }

            if(remaining > 0)
                return false;
        }

        //Space for output?
        if(!pEntity.outputTank.isEmpty()) {
            if(pEntity.outputTank.getFluid() != pRecipe.getAlchemyFluid().getFluid()) return false;
            if(pEntity.outputTank.getAmount() > pEntity.getTankCapacity(0) - 1000 * pEntity.batchSize) return false;
        }

        return true;
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
        int materiaCreated = 0;
        for (ItemStack item : pRecipe.getComponentMateria()) {
            int totalThisIngredient = item.getCount() * pEntity.batchSize;
            materiaCreated += totalThisIngredient;

            //tally up bottles
            for (int i=0; i<inputSlots.getContainerSize(); i++) {
                ItemStack stackInSlot = inputSlots.getItem(i);
                if(stackInSlot.getItem() == item.getItem()) {
                    if(!InventoryHelper.hasCustomModelData(stackInSlot)) {
                        int limit = Math.min(totalThisIngredient, stackInSlot.getCount());
                        bottlesGenerated += limit;
                        totalThisIngredient -= limit;
                    }
                }
            }

            inputSlots.removeItemType(item.getItem(), item.getCount() * pEntity.batchSize);
        }

        outputSlots.addItem(new ItemStack(pRecipe.getAlchemyObject().getItem(), Math.round(pRecipe.getAlchemyObject().getCount() * pEntity.batchSize * (1/pEntity.currentItemRecipe.getOutputRate()))));

        for (int i = 0; i < pVarFunc.apply(IDs.SLOT_OUTPUT_COUNT); i++) {
            pEntity.itemHandler.setStackInSlot(pVarFunc.apply(IDs.SLOT_OUTPUT_START) + i, outputSlots.getItem(i));
        }

        resolveActuators(pEntity, materiaCreated);
        if(pEntity.clearRecipeAfterNextProcess) {
            pEntity.currentItemRecipe = null;
            pEntity.currentFluidRecipe = null;
            pEntity.doDeferredRecipeCheck = false;
            pEntity.clearRecipeAfterNextProcess = false;
            pEntity.syncAndSave();
        }

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

    protected static void craftFluid(AbstractFabricationBlockEntity pEntity, FluidDistillationFabricationRecipe pRecipe, Function<IDs, Integer> pVarFunc) {
        SimpleContainer inputSlots = new SimpleContainer(pVarFunc.apply(IDs.SLOT_INPUT_COUNT));
        for (int i = 0; i < pVarFunc.apply(IDs.SLOT_INPUT_COUNT); i++) {
            inputSlots.setItem(i, pEntity.itemHandler.getStackInSlot(pVarFunc.apply(IDs.SLOT_INPUT_START) + i));
        }

        int bottlesGenerated = 0;
        int materiaCreated = 0;
        for (ItemStack item : pRecipe.getComponentMateria()) {
            int totalThisIngredient = item.getCount() * pEntity.batchSize;
            materiaCreated += totalThisIngredient;

            //tally up bottles
            for (int i=0; i<inputSlots.getContainerSize(); i++) {
                ItemStack stackInSlot = inputSlots.getItem(i);
                if(stackInSlot.getItem() == item.getItem()) {
                    if(!InventoryHelper.hasCustomModelData(stackInSlot)) {
                        int limit = Math.min(totalThisIngredient, stackInSlot.getCount());
                        bottlesGenerated += limit;
                        totalThisIngredient -= limit;
                    }
                }
            }

            inputSlots.removeItemType(item.getItem(), item.getCount() * pEntity.batchSize);
        }

        if(pEntity.outputTank.isEmpty()) {
            pEntity.outputTank = pRecipe.getAlchemyFluid().copy();
            pEntity.outputTank.setAmount(pEntity.batchSize * 1000);
        } else {
            pEntity.outputTank.setAmount(Math.min(pEntity.getTankCapacity(0), pEntity.outputTank.getAmount() + 1000 * pEntity.batchSize));
        }

        resolveActuators(pEntity, materiaCreated);
        if(pEntity.clearRecipeAfterNextProcess) {
            pEntity.currentItemRecipe = null;
            pEntity.currentFluidRecipe = null;
            pEntity.clearRecipeAfterNextProcess = false;
            pEntity.syncAndSave();
        }

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

    public static void resolveActuators(AbstractFabricationBlockEntity pEntity, int pMateriaCreated) {
        for(AbstractDirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
            dpbe.processCompletedOperation(dpbe instanceof ActuatorArcaneBlockEntity ? pMateriaCreated : 1);
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
        SLOT_BOTTLES, SLOT_INPUT_START, SLOT_INPUT_COUNT, SLOT_OUTPUT_START, SLOT_OUTPUT_COUNT, SLOT_STONE
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
        return outputTank;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        //Tank is withdraw-only
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        //Tank is withdraw-only
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if(outputTank.isEmpty())
            return FluidStack.EMPTY;

        if(resource.getFluid() == outputTank.getFluid() || outputTank.isEmpty()) {
            int extracted = Math.min(resource.getAmount(), outputTank.getAmount());
            FluidStack output = outputTank.copy();
            output.setAmount(extracted);
            if(action == FluidAction.EXECUTE) {
                outputTank.shrink(extracted);
                syncAndSave();
            }
            return output;
        }

        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if(outputTank.isEmpty())
            return FluidStack.EMPTY;

        int extracted = Math.min(maxDrain, outputTank.getAmount());
        FluidStack output = outputTank.copy();
        output.setAmount(extracted);
        if(action == FluidAction.EXECUTE) {
            outputTank.shrink(extracted);
            syncAndSave();
        }
        return output;
    }
}
