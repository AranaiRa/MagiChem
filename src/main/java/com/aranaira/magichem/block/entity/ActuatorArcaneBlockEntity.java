package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.routers.AlchemicalNexusRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.FuseryRouterBlockEntity;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.IBlockWithPowerLevel;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.foundation.IPluginDevice;
import com.aranaira.magichem.gui.ActuatorArcaneMenu;
import com.aranaira.magichem.gui.ActuatorArcaneScreen;
import com.aranaira.magichem.gui.ActuatorWaterScreen;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.items.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
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

public class ActuatorArcaneBlockEntity extends DirectionalPluginBlockEntity implements MenuProvider, IFluidHandler, IBlockWithPowerLevel, IPluginDevice, IEldrinConsumerTile {

    private static final int[]
            ELDRIN_POWER_USAGE = {0, 5, 15, 30, 50, 75, 105, 140, 180, 225, 275, 335, 410, 500},
            SLURRY_PER_OPERATION = {0, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18, 20},
            SLURRY_REDUCTION = {0, 34, 37, 40, 43, 46, 49, 52, 55, 58, 61, 64, 67, 70};
    public static final int
            SLOT_COUNT = 2, SLOT_INPUT = 0, SLOT_OUTPUT = 1,
            DATA_COUNT = 4, DATA_REMAINING_ELDRIN_TIME = 0, DATA_POWER_LEVEL = 1, DATA_FLAGS = 2, DATA_SLURRY = 3,
            FLAG_IS_SATISFIED = 1, FLAG_IS_PAUSED = 2, FLAG_IS_REDUCTION_MODE = 4;
    private int
            powerLevel = 1,
            remainingEldrinTime = -1,
            flags;
    private float
            remainingEldrinForSatisfaction;
    protected ContainerData data;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            boolean hasFluidCap = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
            boolean isCrystalOfMemories = stack.getItem() == ItemInit.CRYSTAL_OF_MEMORIES.get();
            boolean isDebugOrb = stack.getItem() == ItemRegistry.DEBUG_ORB.get();
            return hasFluidCap || isCrystalOfMemories || (slot == SLOT_INPUT && isDebugOrb);
        }
    };

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
                return switch(pIndex) {
                    case DATA_REMAINING_ELDRIN_TIME -> ActuatorArcaneBlockEntity.this.remainingEldrinTime;
                    case DATA_POWER_LEVEL -> ActuatorArcaneBlockEntity.this.powerLevel;
                    case DATA_FLAGS -> ActuatorArcaneBlockEntity.this.flags;
                    case DATA_SLURRY -> ActuatorArcaneBlockEntity.this.containedSlurry.getAmount();
                    default -> -1;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case DATA_REMAINING_ELDRIN_TIME -> ActuatorArcaneBlockEntity.this.remainingEldrinTime = pValue;
                    case DATA_POWER_LEVEL -> ActuatorArcaneBlockEntity.this.powerLevel = pValue;
                    case DATA_FLAGS -> ActuatorArcaneBlockEntity.this.flags = pValue;
                    case DATA_SLURRY -> {
                        if(ActuatorArcaneBlockEntity.this.containedSlurry == FluidStack.EMPTY) {
                            ActuatorArcaneBlockEntity.this.containedSlurry = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), pValue);
                        } else {
                            ActuatorArcaneBlockEntity.this.containedSlurry.setAmount(pValue);
                        }
                    }
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };

        this.flags = FLAG_IS_SATISFIED;
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
        return SLURRY_REDUCTION[this.powerLevel];
    }

    public static int getSlurryReductionRate(int pPowerLevel) {
        return SLURRY_REDUCTION[pPowerLevel];
    }

    public static float getSlurryPercent(int pSlurryAmount) {
        return (float)pSlurryAmount * 100f / Config.occultMatrixTankCapacity;
    }

    public float getSlurryPercent() {
        return (float)this.data.get(DATA_SLURRY) / Config.occultMatrixTankCapacity;
    }

    public static int getScaledSlurry(int pSlurryAmount) {
        return pSlurryAmount * ActuatorArcaneScreen.FLUID_GAUGE_H / Config.occultMatrixTankCapacity;
    }

    public int getPowerLevel() {
        return this.powerLevel;
    }

    public void increasePowerLevel() {
        this.powerLevel = Math.min(13, this.powerLevel + 1);
    }

    public void decreasePowerLevel() {
        this.powerLevel = Math.max(1, this.powerLevel - 1);
    }

    @Override
    public void setPowerLevel(int pPowerLevel) {
        this.powerLevel = pPowerLevel;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("remainingEldrinTime", remainingEldrinTime);
        nbt.putInt("powerLevel", powerLevel);
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
        this.remainingEldrinTime = nbt.getInt("remainingEldrinTime");
        this.powerLevel = nbt.getInt("powerLevel");
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
        nbt.putInt("remainingEldrinTime", remainingEldrinTime);
        nbt.putInt("powerLevel", powerLevel);
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
        if((flags & FLAG_IS_REDUCTION_MODE) == 0) {
            fill(new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), getSlurryGeneratedPerOperation() * pCyclesCompleted), FluidAction.EXECUTE);
        }

        syncAndSave();
    }

    public static boolean getIsSatisfied(ActuatorArcaneBlockEntity entity) {
        boolean satisfied = (entity.flags & FLAG_IS_SATISFIED) == FLAG_IS_SATISFIED;
        boolean paused = (entity.flags & FLAG_IS_PAUSED) == FLAG_IS_PAUSED;
        return satisfied && !paused;
    }

    public static boolean getIsPaused(ActuatorArcaneBlockEntity entity) {
        return (entity.flags & FLAG_IS_PAUSED) == FLAG_IS_PAUSED;
    }

    public static void setPaused(ActuatorArcaneBlockEntity entity, boolean pauseState) {
        if(pauseState) {
            entity.flags = entity.flags | FLAG_IS_PAUSED;
        } else {
            entity.flags = entity.flags & ~FLAG_IS_PAUSED;
        }
        entity.syncAndSave();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        if(t instanceof ActuatorArcaneBlockEntity aabe) {
            if (level.isClientSide()) {
                aabe.handleAnimationDrivers();
            }

            if (!level.isClientSide()) {
                //Handle item slots
                {
                    ItemStack inputItem = aabe.itemHandler.getStackInSlot(SLOT_INPUT);
                    ItemStack outputItem = aabe.itemHandler.getStackInSlot(SLOT_OUTPUT);

                    //Fill the slurry buffer from a waiting item
                    if (inputItem != ItemStack.EMPTY) {
                        LazyOptional<IFluidHandlerItem> inputCap = inputItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);

                        if (inputItem.getItem() == ItemInit.CRYSTAL_OF_MEMORIES.get()) {
                            CompoundTag crystalNBT = inputItem.getOrCreateTag();
                            if (crystalNBT.contains("stored_xp")) {
                                int availableExperiencePoints = crystalNBT.getInt("stored_xp");
                                int availableTankCapacity = aabe.getTankCapacity(0) - aabe.containedSlurry.getAmount();

                                int pointsToConvert = Math.min(availableExperiencePoints, availableTankCapacity / Config.fluidPerXPPoint);
                                aabe.fill(new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), pointsToConvert * Config.fluidPerXPPoint), FluidAction.EXECUTE);
                                crystalNBT.putInt("stored_xp", availableExperiencePoints - pointsToConvert);
                                inputItem.setTag(crystalNBT);
                            }
                        } else if (inputCap.isPresent()) {
                            IFluidHandlerItem handler = inputCap.resolve().get();

                            if (handler.getFluidInTank(0).getFluid() == FluidRegistry.ACADEMIC_SLURRY.get()) {
                                FluidStack simulatedDrain = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
                                FluidStack executedFill = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), simulatedDrain.getAmount());
                                executedFill.setAmount(aabe.fill(executedFill, FluidAction.EXECUTE));
                                handler.drain(executedFill, FluidAction.EXECUTE);
                            }
                        } else if (inputItem.getItem() == ItemRegistry.DEBUG_ORB.get()) {
                            if(aabe.containedSlurry.getFluid() == FluidRegistry.ACADEMIC_SLURRY.get())
                                aabe.containedSlurry.setAmount(aabe.getTankCapacity(0));
                            else
                                aabe.containedSlurry = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), aabe.getTankCapacity(0));
                        }
                    }

                    //Empty the slurry buffer into a waiting item
                    if (outputItem != ItemStack.EMPTY && aabe.containedSlurry.getAmount() > 0) {
                        LazyOptional<IFluidHandlerItem> outputCap = outputItem.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);

                        if (outputItem.getItem() == ItemInit.CRYSTAL_OF_MEMORIES.get()) {
                            CompoundTag crystalNBT = outputItem.getOrCreateTag();

                            int availableSlurry = aabe.containedSlurry.getAmount();
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

                            aabe.drain(pointsToConvert * Config.fluidPerXPPoint, FluidAction.EXECUTE);
                            int newCrystalFill = Math.min(20000, Math.max(0, currentCrystalFill + pointsToConvert));

                            crystalNBT.putInt("stored_xp", newCrystalFill);
                            outputItem.setTag(crystalNBT);
                        } else if (outputCap.isPresent()) {
                            IFluidHandlerItem handler = outputCap.resolve().get();

                            if (handler.getFluidInTank(0) == FluidStack.EMPTY || handler.getFluidInTank(0).getFluid() == FluidRegistry.ACADEMIC_SLURRY.get()) {
                                FluidStack simulatedDrain = aabe.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
                                simulatedDrain.setAmount(Integer.MAX_VALUE - simulatedDrain.getAmount());
                                FluidStack executedFill = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), simulatedDrain.getAmount());
                                executedFill.setAmount(handler.fill(executedFill, FluidAction.EXECUTE));
                                aabe.drain(executedFill, FluidAction.EXECUTE);
                            }
                        }
                    }
                }

                //Handle pushing to device reservoirs
                if((aabe.flags & FLAG_IS_REDUCTION_MODE) == FLAG_IS_REDUCTION_MODE) {
                    ICanTakePlugins targetMachine = aabe.getTargetMachine();
                    LazyOptional<IFluidHandler> fluidCap = LazyOptional.empty();

                    if(targetMachine instanceof FuseryRouterBlockEntity frbe) {
                        fluidCap = frbe.getCapability(ForgeCapabilities.FLUID_HANDLER);
                    } else if(targetMachine instanceof AlchemicalNexusRouterBlockEntity anrbe) {
                        fluidCap = anrbe.getCapability(ForgeCapabilities.FLUID_HANDLER);
                    }

                    if(fluidCap.isPresent()) {
                        IFluidHandler handler = fluidCap.resolve().get();
                        int simulatedFill = handler.fill(aabe.containedSlurry, FluidAction.SIMULATE);
                        if(simulatedFill > 0) {
                            handler.fill(aabe.containedSlurry, FluidAction.EXECUTE);
                            aabe.drain(simulatedFill, FluidAction.EXECUTE);
                        }
                    }
                }
            }
        }
    }

    public static void delegatedTick(Level level, BlockPos pos, BlockState state, ActuatorArcaneBlockEntity entity, boolean reductionMode) {
        Player ownerCheck = entity.getOwner();
        int powerDraw = entity.getEldrinPowerUsage();

        if(reductionMode) entity.flags = entity.flags | FLAG_IS_REDUCTION_MODE;
        else entity.flags = entity.flags & ~FLAG_IS_REDUCTION_MODE;

        if(ownerCheck != null && !getIsPaused(entity)) {
            float consumption = entity.consume(ownerCheck, pos, pos.getCenter(), Affinity.ARCANE, Math.min(powerDraw, entity.remainingEldrinForSatisfaction));
            entity.remainingEldrinForSatisfaction -= consumption;

            //Eldrin processing
            if(entity.remainingEldrinTime <= 0) {
                if(entity.remainingEldrinForSatisfaction <= 0) {
                    entity.remainingEldrinForSatisfaction = powerDraw;
                    entity.remainingEldrinTime = Config.occultMatrixOperationTime;
                }

                if(!getIsSatisfied(entity)) {
                    entity.syncAndSave();
                }
            }
            entity.remainingEldrinTime = Math.max(-1, entity.remainingEldrinTime - 1);

            if(entity.remainingEldrinTime >= 0) entity.flags = entity.flags | ActuatorArcaneBlockEntity.FLAG_IS_SATISFIED;
            else {
                if(getIsSatisfied(entity)) {
                    entity.flags = entity.flags & ~ActuatorArcaneBlockEntity.FLAG_IS_SATISFIED;
                    entity.syncAndSave();
                } else
                    entity.flags = entity.flags & ~ActuatorArcaneBlockEntity.FLAG_IS_SATISFIED;
            }
        }
    }

    public void handleAnimationDrivers() {

    }

    public void generateAcademicSlurry() {

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
}
