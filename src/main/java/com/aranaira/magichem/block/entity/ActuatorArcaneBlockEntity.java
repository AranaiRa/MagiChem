package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.routers.AlchemicalNexusRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.FuseryRouterBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.*;
import com.aranaira.magichem.gui.ActuatorArcaneMenu;
import com.aranaira.magichem.gui.ActuatorArcaneScreen;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.items.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ActuatorArcaneBlockEntity extends AbstractDirectionalPluginBlockEntity implements MenuProvider, IFluidHandler, IPluginDevice, IEldrinConsumerTile, IShlorpReceiver, IMateriaProvisionRequester {

    private static final int[]
            ELDRIN_POWER_USAGE = {0, 5, 15, 30, 50, 75, 105, 140, 180, 225, 275, 335, 410, 500},
            SLURRY_PER_OPERATION = {0, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18, 20},
            SLURRY_REDUCTION = {0, 34, 37, 40, 43, 46, 49, 52, 55, 58, 61, 64, 67, 70};
    public static final int
            MAX_POWER_LEVEL = 13,
            SLOT_COUNT = 4, SLOT_INPUT = 0, SLOT_OUTPUT = 1, SLOT_ESSENTIA_INSERTION = 2, SLOT_BOTTLES = 3,
            FLAG_IS_REDUCTION_MODE = 1;
    private int
            flags;
    protected ContainerData data;
    private static final MateriaItem ESSENTIA_ARCANE = ItemRegistry.getEssentiaMap(false, false).get("arcane");

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    private FluidStack containedSlurry = FluidStack.EMPTY;

    public ActuatorArcaneBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        this.flags = 0;
    }

    public ActuatorArcaneBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ACTUATOR_ARCANE_BE.get(), pPos, pBlockState);

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return 0;
            }

            @Override
            public void set(int pIndex, int pValue) {

            }

            @Override
            public int getCount() {
                return 0;
            }
        };

        this.flags = 0;

        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot == SLOT_INPUT || slot == SLOT_OUTPUT) {
                    boolean hasFluidCap = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
                    boolean isCrystalOfMemories = stack.getItem() == ItemInit.CRYSTAL_OF_MEMORIES.get();
                    boolean isDebugOrb = stack.getItem() == ItemRegistry.DEBUG_ORB.get();
                    return hasFluidCap || isCrystalOfMemories || (slot == SLOT_INPUT && isDebugOrb);
                } else if(slot == SLOT_ESSENTIA_INSERTION) {
                    if(stack.getItem() instanceof MateriaItem mi) {
                        return mi == ESSENTIA_ARCANE;
                    }
                }
                return false;
            }

            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if(slot == SLOT_ESSENTIA_INSERTION) {
                    if(InventoryHelper.isMateriaUnbottled(itemHandler.getStackInSlot(SLOT_ESSENTIA_INSERTION)))
                        return ItemStack.EMPTY;
                }

                return super.extractItem(slot, amount, simulate);
            }
        };
    }

    public int getEldrinPowerUsage() {
        return ELDRIN_POWER_USAGE[this.powerLevel];
    }

    public static int getEldrinPowerUsage(int pPowerLevel) {
        return ELDRIN_POWER_USAGE[pPowerLevel];
    }

    public int getSlurryGeneratedPerOperation() {
        return SLURRY_PER_OPERATION[this.powerLevel];
    }

    public static int getSlurryGeneratedPerOperation(int pPowerLevel) {
        return SLURRY_PER_OPERATION[pPowerLevel];
    }

    public int getSlurryReductionRate() {
        return SLURRY_REDUCTION[getPaused() ? 0 : this.powerLevel];
    }

    public static int getSlurryReductionRate(int pPowerLevel) {
        return SLURRY_REDUCTION[pPowerLevel];
    }

    public static float getSlurryPercent(int pSlurryAmount) {
        return (float)pSlurryAmount * 100f / Config.occultMatrixTankCapacity;
    }

    public float getSlurryPercent() {
        return (float)containedSlurry.getAmount() / Config.occultMatrixTankCapacity;
    }

    public static int getScaledSlurry(int pSlurryAmount) {
        return pSlurryAmount * ActuatorArcaneScreen.FLUID_GAUGE_H / Config.occultMatrixTankCapacity;
    }

    public int getSlurryInTank() {
        return containedSlurry.getAmount();
    }

    public boolean getIsReductionMode() {
        return (flags & FLAG_IS_REDUCTION_MODE) == FLAG_IS_REDUCTION_MODE;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("remainingCycleTime", remainingCycleTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("storedMateria", storedMateria);
        nbt.putBoolean("drewEldrinThisCycle", drewEldrinThisCycle);
        nbt.putBoolean("drewEssentiaThisCycle", drewEssentiaThisCycle);
        nbt.putInt("containedSlurry", containedSlurry.getAmount());
        nbt.putInt("flags", flags);
        if(ownerUUID != null)
            nbt.putUUID("owner", ownerUUID);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        this.remainingCycleTime = nbt.getInt("remainingCycleTime");
        this.powerLevel = nbt.getInt("powerLevel");
        this.storedMateria = nbt.getInt("storedMateria");
        this.drewEldrinThisCycle = nbt.getBoolean("drewEldrinThisCycle");
        this.drewEssentiaThisCycle = nbt.getBoolean("drewEssentiaThisCycle");
        if(this.containedSlurry == FluidStack.EMPTY)
            this.containedSlurry = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), nbt.getInt("containedSlurry"));
        else
            this.containedSlurry.setAmount(nbt.getInt("containedSlurry"));
        this.flags = nbt.getInt("flags");

        if(nbt.contains("owner"))
            ownerUUID = nbt.getUUID("owner");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyFluidHandler = LazyOptional.of(() -> this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("remainingCycleTime", remainingCycleTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("storedMateria", storedMateria);
        nbt.putBoolean("drewEldrinThisCycle", drewEldrinThisCycle);
        nbt.putBoolean("drewEssentiaThisCycle", drewEssentiaThisCycle);
        nbt.putInt("containedSlurry", containedSlurry.getAmount());
        nbt.putInt("flags", flags);
        if(ownerUUID != null)
            nbt.putUUID("owner", ownerUUID);
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void processCompletedOperation(int pCyclesCompleted) {
        if((flags & FLAG_IS_REDUCTION_MODE) == 0 && !this.getPaused() && getIsSatisfied()) {
            fill(new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), getSlurryGeneratedPerOperation() * pCyclesCompleted), FluidAction.EXECUTE);
        }

        syncAndSave();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        boolean changed = AbstractDirectionalPluginBlockEntity.tick(level, pos, blockState, t, ActuatorArcaneBlockEntity::getValue);

        if(t instanceof ActuatorArcaneBlockEntity entity) {
            if(changed && !level.isClientSide())
                entity.syncAndSave();

            if (level.isClientSide()) {
                entity.handleAnimationDrivers();
            }

            if (!level.isClientSide()) {
                //Handle item slots
                {
                    ItemStack inputItem = entity.itemHandler.getStackInSlot(SLOT_INPUT);
                    ItemStack outputItem = entity.itemHandler.getStackInSlot(SLOT_OUTPUT);

                    //Fill the slurry buffer from a waiting item
                    if (inputItem != ItemStack.EMPTY) {
                        LazyOptional<IFluidHandlerItem> inputCap = inputItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);

                        if (inputItem.getItem() == ItemInit.CRYSTAL_OF_MEMORIES.get()) {
                            CompoundTag crystalNBT = inputItem.getOrCreateTag();
                            if (crystalNBT.contains("stored_xp")) {
                                int availableExperiencePoints = crystalNBT.getInt("stored_xp");
                                int availableTankCapacity = entity.getTankCapacity(0) - entity.containedSlurry.getAmount();

                                int pointsToConvert = Math.min(availableExperiencePoints, availableTankCapacity / Config.fluidPerXPPoint);
                                entity.fill(new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), pointsToConvert * Config.fluidPerXPPoint), FluidAction.EXECUTE);
                                crystalNBT.putInt("stored_xp", availableExperiencePoints - pointsToConvert);
                                inputItem.setTag(crystalNBT);
                            }
                        } else if (inputCap.isPresent()) {
                            IFluidHandlerItem handler = inputCap.resolve().get();

                            if (handler.getFluidInTank(0).getFluid() == FluidRegistry.ACADEMIC_SLURRY.get()) {
                                FluidStack simulatedDrain = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
                                FluidStack executedFill = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), simulatedDrain.getAmount());
                                executedFill.setAmount(entity.fill(executedFill, FluidAction.EXECUTE));
                                handler.drain(executedFill, FluidAction.EXECUTE);
                            }
                        } else if (inputItem.getItem() == ItemRegistry.DEBUG_ORB.get()) {
                            if(entity.containedSlurry.getFluid() == FluidRegistry.ACADEMIC_SLURRY.get())
                                entity.containedSlurry.setAmount(entity.getTankCapacity(0));
                            else
                                entity.containedSlurry = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), entity.getTankCapacity(0));
                        }
                    }

                    //Empty the slurry buffer into a waiting item
                    if (outputItem != ItemStack.EMPTY && entity.containedSlurry.getAmount() > 0) {
                        LazyOptional<IFluidHandlerItem> outputCap = outputItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);

                        if (outputItem.getItem() == ItemInit.CRYSTAL_OF_MEMORIES.get()) {
                            CompoundTag crystalNBT = outputItem.getOrCreateTag();

                            int availableSlurry = entity.containedSlurry.getAmount();
                            int availableCrystalCapacity = 20000;
                            int currentCrystalFill = 0;

                            if (crystalNBT.contains("stored_xp")) {
                                currentCrystalFill = crystalNBT.getInt("stored_xp");
                                availableCrystalCapacity = Math.max(0, 20000 - currentCrystalFill);
                            }

                            int availableSlurryAsPoints = availableSlurry / Config.fluidPerXPPoint;
                            int pointsToConvert = Math.min(availableSlurryAsPoints, availableCrystalCapacity);
                            if (currentCrystalFill + pointsToConvert > availableCrystalCapacity) {
                                pointsToConvert = currentCrystalFill + pointsToConvert - availableCrystalCapacity;
                            }

                            entity.drain(pointsToConvert * Config.fluidPerXPPoint, FluidAction.EXECUTE);
                            int newCrystalFill = Math.min(20000, Math.max(0, currentCrystalFill + pointsToConvert));

                            crystalNBT.putInt("stored_xp", newCrystalFill);
                            outputItem.setTag(crystalNBT);
                        } else if (outputCap.isPresent()) {
                            IFluidHandlerItem handler = outputCap.resolve().get();

                            if (handler.getFluidInTank(0) == FluidStack.EMPTY || handler.getFluidInTank(0).getFluid() == FluidRegistry.ACADEMIC_SLURRY.get()) {
                                FluidStack simulatedDrain = entity.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
                                simulatedDrain.setAmount(Integer.MAX_VALUE - simulatedDrain.getAmount());
                                FluidStack executedFill = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), simulatedDrain.getAmount());
                                executedFill.setAmount(handler.fill(executedFill, FluidAction.EXECUTE));
                                entity.drain(executedFill, FluidAction.EXECUTE);
                            }
                        }
                    }
                }

                //Handle pushing to device reservoirs
                if((entity.flags & FLAG_IS_REDUCTION_MODE) == FLAG_IS_REDUCTION_MODE) {
                    ICanTakePlugins targetMachine = entity.getTargetMachine();
                    LazyOptional<IFluidHandler> fluidCap = LazyOptional.empty();

                    if(targetMachine instanceof FuseryRouterBlockEntity frbe) {
                        fluidCap = frbe.getCapability(ForgeCapabilities.FLUID_HANDLER);
                    } else if(targetMachine instanceof AlchemicalNexusRouterBlockEntity anrbe) {
                        fluidCap = anrbe.getCapability(ForgeCapabilities.FLUID_HANDLER);
                    }

                    if(fluidCap.isPresent()) {
                        IFluidHandler handler = fluidCap.resolve().get();
                        int simulatedFill = handler.fill(entity.containedSlurry, FluidAction.SIMULATE);
                        if(simulatedFill > 0) {
                            handler.fill(entity.containedSlurry, FluidAction.EXECUTE);
                            entity.drain(simulatedFill, FluidAction.EXECUTE);
                        }
                    }
                }
            }
        }
    }

    public static void delegatedTick(Level level, BlockPos pos, BlockState state, ActuatorArcaneBlockEntity entity, boolean reductionMode) {
        boolean changed = AbstractDirectionalPluginBlockEntity.delegatedTick(level, pos, state, entity,
                ActuatorArcaneBlockEntity::getValue,
                ActuatorArcaneBlockEntity::getAffinity,
                ActuatorArcaneBlockEntity::getPowerDraw,
                ActuatorArcaneBlockEntity::handleAuxiliaryRequirements);

        int pre = entity.flags;
        if(reductionMode) entity.flags = entity.flags | FLAG_IS_REDUCTION_MODE;
        else entity.flags = entity.flags & ~FLAG_IS_REDUCTION_MODE;
        if(pre != entity.flags) changed = true;

        if(changed) entity.syncAndSave();
    }

    public void handleAnimationDrivers() {

    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();
        if(cap == ForgeCapabilities.FLUID_HANDLER) return lazyFluidHandler.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.lazyItemHandler.invalidate();
        this.lazyFluidHandler.invalidate();
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ActuatorArcaneMenu(i, inventory, this, this.data);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-1, 0, -1), getBlockPos().offset(1,2,1));
    }

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
        return Config.occultMatrixTankCapacity;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return stack.getFluid() == FluidRegistry.ACADEMIC_SLURRY.get();
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction fluidAction) {
        if(fluidAction.execute()) {
            setChanged();
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }

        Fluid fluid = fluidStack.getFluid();
        int incomingAmount = fluidStack.getAmount();
        if(fluid == FluidRegistry.ACADEMIC_SLURRY.get()) {
            int extantAmount = containedSlurry.getAmount();
            int query = Config.occultMatrixTankCapacity - (incomingAmount + extantAmount);

            //Hit capacity
            if(query < 0) {
                int actualTransfer = Config.occultMatrixTankCapacity - extantAmount;
                if(fluidAction == FluidAction.EXECUTE)
                    this.containedSlurry = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), extantAmount + actualTransfer);
                return actualTransfer;
            } else {
                if(fluidAction == FluidAction.EXECUTE)
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

        //Steam is extract only
        Fluid fluid = fluidStack.getFluid();
        int incomingAmount = fluidStack.getAmount();
        if(fluid == FluidRegistry.ACADEMIC_SLURRY.get()) {
            int extantAmount = containedSlurry.getAmount();
            if(extantAmount >= incomingAmount) {
                if(fluidAction == FluidAction.EXECUTE)
                    containedSlurry.shrink(incomingAmount);
                setChanged();
                return new FluidStack(fluid, incomingAmount);
            } else {
                if(fluidAction == FluidAction.EXECUTE)
                    containedSlurry = FluidStack.EMPTY;
                if(incomingAmount - extantAmount > 0)
                    setChanged();
                return new FluidStack(fluid, incomingAmount - extantAmount);
            }
        }
        return fluidStack;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction fluidAction) {
        if(containedSlurry.getAmount() > 0)
            setChanged();
        return drain(new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), maxDrain), fluidAction);
    }

    public static Affinity getAffinity(Void v) {
        return Affinity.ARCANE;
    }

    ////////////////////
    // PROVISIONING AND SHLORPS
    ////////////////////

    private final NonNullList<MateriaItem> activeProvisionRequests = NonNullList.create();

    @Override
    public boolean allowIncreasedDeliverySize() {
        return false;
    }

    @Override
    public boolean needsProvisioning() {
        if(activeProvisionRequests.size() > 0)
            return false;
        ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_ESSENTIA_INSERTION);
        if(InventoryHelper.isMateriaUnbottled(insertionStack)) {
            return insertionStack.getCount() < itemHandler.getSlotLimit(SLOT_ESSENTIA_INSERTION);
        }
        return insertionStack.isEmpty();
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        Map<MateriaItem, Integer> result = new HashMap<>();

        ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_ESSENTIA_INSERTION);

        if(insertionStack.getCount() < itemHandler.getSlotLimit(SLOT_ESSENTIA_INSERTION) / 2) {
            result.put(ESSENTIA_ARCANE, itemHandler.getSlotLimit(SLOT_ESSENTIA_INSERTION) - insertionStack.getCount());
        }

        return result;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        if(pMateriaItem == ESSENTIA_ARCANE)
            activeProvisionRequests.add(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        activeProvisionRequests.remove(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        if(pStack.getItem() == ESSENTIA_ARCANE) {
            ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_ESSENTIA_INSERTION);

            if(insertionStack.isEmpty()) {
                insertionStack = pStack.copy();
                CompoundTag nbt = new CompoundTag();
                nbt.putInt("CustomModelData", 1);
                insertionStack.setTag(nbt);
            } else {
                insertionStack.grow(pStack.getCount());
            }
            itemHandler.setStackInSlot(SLOT_ESSENTIA_INSERTION, insertionStack);

            syncAndSave();

            activeProvisionRequests.remove((MateriaItem)pStack.getItem());
        }
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        if(pStack.getItem() == ESSENTIA_ARCANE) {
            return 0;
        }
        return pStack.getCount();
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        provide(pStack);
        return 0;
    }

    ////////////////////
    // STATIC RETRIEVAL
    ////////////////////

    public static int getValue(IDs id) {
        return switch (id) {
            case SLOT_COUNT -> SLOT_COUNT;
            case SLOT_ESSENTIA_INSERTION -> SLOT_ESSENTIA_INSERTION;
            case SLOT_BOTTLES -> SLOT_BOTTLES;
            case MAX_POWER_LEVEL -> MAX_POWER_LEVEL;
        };
    }

    public static int getPowerDraw(AbstractDirectionalPluginBlockEntity entity) {
        if(entity == null)
            return 1;
        return ELDRIN_POWER_USAGE[entity.getPowerLevel()];
    }

    public static boolean handleAuxiliaryRequirements(AbstractDirectionalPluginBlockEntity entity) {
        entity.satisfyAuxiliaryRequirements();
        return true;
    }
}
