package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.*;
import com.aranaira.magichem.gui.ActuatorWaterMenu;
import com.aranaira.magichem.gui.ActuatorWaterScreen;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleLerpMover;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Random;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class ActuatorWaterBlockEntity extends AbstractDirectionalPluginBlockEntity implements MenuProvider, IBlockWithPowerLevel, IPluginDevice, IEldrinConsumerTile, IFluidHandler, IShlorpReceiver, IMateriaProvisionRequester {

    private static final int[]
            ELDRIN_POWER_USAGE = {0, 5, 15, 30, 50, 75, 105, 140, 180, 225, 275, 335, 410, 500},
            WATER_PER_OPERATION = {0, 140, 170, 200, 230, 260, 290, 320, 350, 380, 410, 440, 470, 500},
            STEAM_PER_PROCESS = {0, 3, 5, 7, 10, 13, 17, 22, 27, 33, 39, 46, 53, 61},
            EFFICIENCY_INCREASE = {0, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40};
    public static final int
            SLOT_COUNT = 2,
            SLOT_MATERIA_INSERTION = 0, SLOT_BOTTLES = 1,
            TANK_ID_WATER = 0, TANK_ID_STEAM = 1,
            DATA_COUNT = 5, DATA_REMAINING_ELDRIN_TIME = 0, DATA_POWER_LEVEL = 1, DATA_FLAGS = 2, DATA_WATER = 3, DATA_STEAM = 4;
    public static final int
            FLAG_IS_SATISFIED = 1, FLAG_IS_PAUSED = 2;
    public int
            powerLevel = 1,
            remainingCycleTime,
            flags,
            storedMateria = 0,
            remainingMateriaForSatisfaction;
    private float
            remainingEldrinForSatisfaction;
    private boolean
            drewEldrinThisCycle = false,
            drewMateriaThisCycle = false;
    protected ContainerData data;
    private FluidStack
            containedWater, containedSteam;
    private final LazyOptional<IFluidHandler> fluidHandler;
    private static final MateriaItem ESSENTIA_WATER = ItemRegistry.getEssentiaMap(false, false).get("water");
    private static final Random random = new Random();
    
    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(slot == SLOT_MATERIA_INSERTION) {
                if(stack.getItem() instanceof MateriaItem mi) {
                    return mi == ESSENTIA_WATER;
                }
            }
            return false;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if(slot == SLOT_MATERIA_INSERTION) {
                if(InventoryHelper.isMateriaUnbottled(itemHandler.getStackInSlot(SLOT_MATERIA_INSERTION)))
                    return ItemStack.EMPTY;
            }

            return super.extractItem(slot, amount, simulate);
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public ActuatorWaterBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        this.containedWater = FluidStack.EMPTY;
        this.containedSteam = FluidStack.EMPTY;
        this.fluidHandler = LazyOptional.of(() -> this);
    }

    public ActuatorWaterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ACTUATOR_WATER_BE.get(), pPos, pBlockState);

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch(pIndex) {
                    case DATA_REMAINING_ELDRIN_TIME -> ActuatorWaterBlockEntity.this.remainingCycleTime;
                    case DATA_POWER_LEVEL -> ActuatorWaterBlockEntity.this.powerLevel;
                    case DATA_FLAGS -> ActuatorWaterBlockEntity.this.flags;
                    case DATA_WATER -> ActuatorWaterBlockEntity.this.containedWater.getAmount();
                    case DATA_STEAM -> ActuatorWaterBlockEntity.this.containedSteam.getAmount();
                    default -> -1;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case DATA_REMAINING_ELDRIN_TIME -> ActuatorWaterBlockEntity.this.remainingCycleTime = pValue;
                    case DATA_POWER_LEVEL -> ActuatorWaterBlockEntity.this.powerLevel = pValue;
                    case DATA_FLAGS -> ActuatorWaterBlockEntity.this.flags = pValue;
                    case DATA_WATER -> {
                        if(ActuatorWaterBlockEntity.this.containedWater == FluidStack.EMPTY)
                            ActuatorWaterBlockEntity.this.containedWater = new FluidStack(Fluids.WATER, pValue);
                        else
                            ActuatorWaterBlockEntity.this.containedWater.setAmount(pValue);
                    }
                    case DATA_STEAM -> {
                        if(ActuatorWaterBlockEntity.this.containedSteam == FluidStack.EMPTY)
                            ActuatorWaterBlockEntity.this.containedSteam = new FluidStack(FluidRegistry.STEAM.get(), pValue);
                        else
                            ActuatorWaterBlockEntity.this.containedSteam.setAmount(pValue);
                    }
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };

        this.containedWater = FluidStack.EMPTY;
        this.containedSteam = FluidStack.EMPTY;
        this.fluidHandler = LazyOptional.of(() -> this);
        this.flags = FLAG_IS_SATISFIED;
    }

    public int getEfficiencyIncrease() {
        return EFFICIENCY_INCREASE[this.powerLevel];
    }

    public static int getEfficiencyIncrease(int pPowerLevel) {
        return EFFICIENCY_INCREASE[pPowerLevel];
    }

    public int getEldrinPowerUsage() {
        return ELDRIN_POWER_USAGE[this.powerLevel];
    }

    public static int getEldrinPowerUsage(int pPowerLevel) {
        return ELDRIN_POWER_USAGE[pPowerLevel];
    }

    public int getWaterPerOperation() {
        return WATER_PER_OPERATION[this.powerLevel];
    }

    public static int getWaterPerOperation(int pPowerLevel) {
        return WATER_PER_OPERATION[pPowerLevel];
    }

    public int getSteamPerProcess() {
        return STEAM_PER_PROCESS[this.powerLevel];
    }

    public static int getSteamPerProcess(int pPowerLevel) {
        return STEAM_PER_PROCESS[pPowerLevel];
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
        nbt.putInt("remainingCycleTime", remainingCycleTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("storedMateria", storedMateria);
        nbt.putBoolean("drewEldrinThisCycle", drewEldrinThisCycle);
        nbt.putBoolean("drewMateriaThisCycle", drewMateriaThisCycle);
        nbt.putInt("tankWater", this.containedWater.getAmount());
        nbt.putInt("tankSteam", this.containedSteam.getAmount());
        nbt.putInt("flags", this.flags);
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
        this.drewMateriaThisCycle = nbt.getBoolean("drewMateriaThisCycle");
        this.flags = nbt.getInt("flags");

        int nbtWater = nbt.getInt("tankWater");
        if(nbtWater > 0)
            this.containedWater = new FluidStack(Fluids.WATER, nbtWater);
        else
            this.containedWater = FluidStack.EMPTY;

        int nbtSteam = nbt.getInt("tankSteam");
        if(nbtSteam > 0)
            this.containedSteam = new FluidStack(FluidRegistry.STEAM.get(), nbtSteam);
        else
            this.containedSteam = FluidStack.EMPTY;

        if(nbt.contains("owner"))
            ownerUUID = nbt.getUUID("owner");
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("remainingCycleTime", remainingCycleTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("storedMateria", storedMateria);
        nbt.putBoolean("drewEldrinThisCycle", drewEldrinThisCycle);
        nbt.putBoolean("drewMateriaThisCycle", drewMateriaThisCycle);
        nbt.putInt("tankWater", this.containedWater.getAmount());
        nbt.putInt("tankSteam", this.containedSteam.getAmount());
        nbt.putInt("flags", this.flags);
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
        int newTotal = Math.min(Config.delugePurifierTankCapacity, containedSteam.getAmount() + getSteamPerProcess() * pCyclesCompleted);
        containedSteam = new FluidStack(FluidRegistry.STEAM.get(), Math.min(newTotal, Config.delugePurifierTankCapacity));
    }

    public static boolean getIsSatisfied(ActuatorWaterBlockEntity entity) {
        boolean satisfied = (entity.flags & FLAG_IS_SATISFIED) == FLAG_IS_SATISFIED;
        boolean paused = (entity.flags & FLAG_IS_PAUSED) == FLAG_IS_PAUSED;
        return satisfied && !paused;
    }

    public static boolean getIsPaused(ActuatorWaterBlockEntity entity) {
        return (entity.flags & FLAG_IS_PAUSED) == FLAG_IS_PAUSED;
    }

    public static void setPaused(ActuatorWaterBlockEntity entity, boolean pauseState) {
        if(pauseState) {
            entity.flags = entity.flags | FLAG_IS_PAUSED;
        } else {
            entity.flags = entity.flags & ~FLAG_IS_PAUSED;
        }
        entity.syncAndSave();
    }

    public int getStoredMateria() {
        return storedMateria;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        if(t instanceof ActuatorWaterBlockEntity entity) {
            //Try inserting materia
            if(!level.isClientSide()) {
                if (entity.storedMateria < Config.actuatorMateriaBufferMaximum) {
                    ItemStack insertionStack = entity.itemHandler.getStackInSlot(SLOT_MATERIA_INSERTION);
                    ItemStack bottleStack = entity.itemHandler.getStackInSlot(SLOT_BOTTLES);

                    if (!insertionStack.isEmpty()) {
                        if (bottleStack.getCount() < entity.itemHandler.getSlotLimit(SLOT_BOTTLES)) {
                            if(!InventoryHelper.isMateriaUnbottled(insertionStack)){
                                if (bottleStack.isEmpty())
                                    bottleStack = new ItemStack(Items.GLASS_BOTTLE);
                                else
                                    bottleStack.grow(1);
                            }

                            insertionStack.shrink(1);
                            entity.storedMateria += Config.actuatorMateriaUnitsPerDram;

                            entity.itemHandler.setStackInSlot(SLOT_MATERIA_INSERTION, insertionStack);
                            entity.itemHandler.setStackInSlot(SLOT_BOTTLES, bottleStack);
                            entity.syncAndSave();
                        }
                    }
                }
            }
            //Particle work
            else {
                float water = entity.getWaterPercent();
                if (water > 0 && !getIsPaused(entity)) {
                    Vector3f mid = new Vector3f(0f, 1.5625f, 0f);
                    Vector3f left = new Vector3f(0f, 1.03125f, 0f);
                    Vector3f right = new Vector3f(0f, 1.03125f, 0f);

                    Vector2f leftSpeed = new Vector2f();

                    Direction dir = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    if (dir == Direction.NORTH) {
                        mid.x = 0.5f;
                        mid.z = 0.6875f;
                        left.x = 0.09375f;
                        left.z = 0.3125f;
                        right.x = 0.90625f;
                        right.z = 0.3125f;
                        leftSpeed.x = -0.04f;
                        leftSpeed.y = 0.0f;
                    }
                    if (dir == Direction.EAST) {
                        mid.x = 0.3125f;
                        mid.z = 0.5f;
                        left.x = 0.6875f;
                        left.z = 0.09375f;
                        right.x = 0.6875f;
                        right.z = 0.90625f;
                        leftSpeed.x = 0.0f;
                        leftSpeed.y = -0.04f;
                    }
                    if (dir == Direction.SOUTH) {
                        mid.x = 0.5f;
                        mid.z = 0.3125f;
                        left.x = 0.90625f;
                        left.z = 0.6875f;
                        right.x = 0.09375f;
                        right.z = 0.6875f;
                        leftSpeed.x = 0.04f;
                        leftSpeed.y = 0.0f;
                    }
                    if (dir == Direction.WEST) {
                        mid.x = 0.6875f;
                        mid.z = 0.5f;
                        left.x = 0.3125f;
                        left.z = 0.09375f;
                        right.x = 0.3125f;
                        right.z = 0.90625f;
                        leftSpeed.x = 0.0f;
                        leftSpeed.y = -0.04f;
                    }

                    //Spawn drip particles
                    for (int i = 0; i < 3; i++) {
                        Vector2f dripShift = new Vector2f((random.nextFloat() * 2.0f - 1.0f) * 0.05f, (random.nextFloat() * 2.0f - 1.0f) * 0.05f);

                        double dripX = pos.getX() + mid.x + dripShift.x;
                        double dripY = pos.getY() + mid.y;
                        double dripZ = pos.getZ() + mid.z + dripShift.y;

                        level.addParticle(new MAParticleType(ParticleInit.DRIP.get()).setMaxAge(13)
                                        .setMover(new ParticleLerpMover(dripX, dripY, dripZ, pos.getX() + mid.x, dripY - 1.0f, pos.getZ() + mid.z)),
                                dripX, dripY, dripZ,
                                0, 0, 0);
                    }

                    //Spawn steam jets
                    float steam = entity.data.get(DATA_STEAM);
                    if ((int) steam > Config.delugePurifierTankCapacity / 2) {
                        float mappedSteamPercent = Math.max(0, ((steam / Config.delugePurifierTankCapacity) - 0.5f) * 2) * 2;
                        int spawnModulus = (int) Math.ceil(mappedSteamPercent * 5);

                        long time = level.getGameTime();

                        if (time % 20 <= spawnModulus) {
                            level.addParticle(new MAParticleType(ParticleInit.COZY_SMOKE.get())
                                            .setPhysics(true).setScale(0.065f).setMaxAge(10),
                                    pos.getX() + left.x, pos.getY() + left.y, pos.getZ() + left.z,
                                    leftSpeed.x, 0.075f, leftSpeed.y);
                        }

                        if ((time + 10) % 20 <= spawnModulus) {
                            level.addParticle(new MAParticleType(ParticleInit.COZY_SMOKE.get())
                                            .setPhysics(true).setScale(0.065f).setMaxAge(10),
                                    pos.getX() + right.x, pos.getY() + right.y, pos.getZ() + right.z,
                                    -leftSpeed.x, 0.075f, -leftSpeed.y);
                        }
                    }
                }
            }
        }
    }

    public static void delegatedTick(Level level, BlockPos pos, BlockState state, ActuatorWaterBlockEntity entity) {
        Player ownerCheck = entity.getOwner();
        int powerDraw = entity.getEldrinPowerUsage();
        boolean changed = false;

        if(ownerCheck != null) {
            float consumption = entity.consume(ownerCheck, pos, pos.getCenter(), Affinity.WATER, Math.min(powerDraw, entity.remainingEldrinForSatisfaction));
            if(consumption > 0)
                entity.remainingEldrinForSatisfaction -= consumption;
            if (entity.remainingMateriaForSatisfaction > 0) {
                int materiaConsumption = Math.min(entity.remainingMateriaForSatisfaction, entity.storedMateria);
                if(materiaConsumption > 0) {
                    entity.storedMateria -= materiaConsumption;
                    entity.remainingMateriaForSatisfaction -= materiaConsumption;
                    changed = true;
                }
            }

            if(!getIsPaused(entity)) {
                //Eldrin processing
                if (entity.remainingCycleTime <= 0) {
                    if (entity.remainingEldrinForSatisfaction <= 0) {
                        entity.remainingEldrinForSatisfaction = powerDraw;
                        entity.drewEldrinThisCycle = true;
                    } else {
                        entity.drewEldrinThisCycle = false;
                    }

                    if (entity.remainingMateriaForSatisfaction <= 0) {
                        entity.remainingMateriaForSatisfaction = powerDraw;
                        entity.drewMateriaThisCycle = true;
                    } else {
                        entity.drewMateriaThisCycle = false;
                    }

                    if (entity.drewEldrinThisCycle && entity.drewMateriaThisCycle) {
                        entity.remainingCycleTime = Config.actuatorDoubleSuppliedPeriod;
                    } else if (entity.drewEldrinThisCycle || entity.drewMateriaThisCycle)
                        entity.remainingCycleTime = Config.actuatorSingleSuppliedPeriod;

                    if (entity.drewEldrinThisCycle || entity.drewMateriaThisCycle)
                        changed = true;
                }
            }
            entity.remainingCycleTime = Math.max(-1, entity.remainingCycleTime - 1);

            if (entity.drewMateriaThisCycle || entity.drewEldrinThisCycle)
                entity.flags = entity.flags | ActuatorWaterBlockEntity.FLAG_IS_SATISFIED;
            else {
                if (getIsSatisfied(entity)) {
                    entity.flags = entity.flags & ~ActuatorWaterBlockEntity.FLAG_IS_SATISFIED;
                    changed = true;
                } else
                    entity.flags = entity.flags & ~ActuatorWaterBlockEntity.FLAG_IS_SATISFIED;
            }
        }

        if(changed) entity.syncAndSave();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();
        if(cap == ForgeCapabilities.FLUID_HANDLER) return fluidHandler.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.lazyItemHandler.invalidate();
        this.fluidHandler.invalidate();
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ActuatorWaterMenu(i, inventory, this, this.data);
    }

    public static float getWaterPercent(int pWaterAmount) {
        return (float)pWaterAmount * 100f / Config.delugePurifierTankCapacity;
    }

    public float getWaterPercent() {
        return (float)this.data.get(DATA_WATER) / Config.delugePurifierTankCapacity;
    }

    public static int getScaledWater(int pWaterAmount) {
        return pWaterAmount * ActuatorWaterScreen.FLUID_GAUGE_H / Config.delugePurifierTankCapacity;
    }

    public static float getSteamPercent(int pSteamAmount) {
        return (float)pSteamAmount * 100f / Config.delugePurifierTankCapacity;
    }

    public static int getScaledSteam(int pSteamAmount) {
        return pSteamAmount * ActuatorWaterScreen.FLUID_GAUGE_H / Config.delugePurifierTankCapacity;
    }

    public int getScaledCycleTime() {
        return remainingCycleTime * ActuatorWaterScreen.SYMBOL_H / ((drewMateriaThisCycle && drewEldrinThisCycle) ? Config.actuatorDoubleSuppliedPeriod : Config.actuatorSingleSuppliedPeriod);
    }

    @Override
    public int getTanks() {
        return 2;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int i) {
        if(i == TANK_ID_WATER) return containedWater;
        if(i == TANK_ID_STEAM) return containedSteam;
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int i) {
        return Config.delugePurifierTankCapacity;
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
        if(i == TANK_ID_WATER) {
            return fluidStack.getFluid() == Fluids.WATER;
        } else if(i == TANK_ID_STEAM) {
            return fluidStack.getFluid() == FluidRegistry.STEAM.get();
        }
        return false;
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction fluidAction) {
        if(fluidAction.execute()) {
            setChanged();
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }

        //Water is insert-only
        Fluid fluid = fluidStack.getFluid();
        int incomingAmount = fluidStack.getAmount();
        if(fluid == Fluids.WATER) {
            int extantAmount = containedWater.getAmount();
            int query = Config.delugePurifierTankCapacity - (incomingAmount + extantAmount);

            //Hit capacity
            if(query < 0) {
                int actualTransfer = Config.delugePurifierTankCapacity - extantAmount;
                if(fluidAction == FluidAction.EXECUTE)
                    this.containedWater = new FluidStack(Fluids.WATER, extantAmount + actualTransfer);
                return actualTransfer;
            } else {
                if(fluidAction == FluidAction.EXECUTE)
                    this.containedWater = new FluidStack(Fluids.WATER, extantAmount + incomingAmount);
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
        if(fluid == Fluids.WATER) {
            int extantAmount = containedWater.getAmount();
            if(extantAmount >= incomingAmount) {
                if(fluidAction == FluidAction.EXECUTE)
                    containedWater.shrink(incomingAmount);
                return new FluidStack(fluid, incomingAmount);
            } else {
                if(fluidAction == FluidAction.EXECUTE)
                    containedWater = FluidStack.EMPTY;
                return new FluidStack(fluid, incomingAmount - extantAmount);
            }
        }
        else if(fluid == FluidRegistry.STEAM.get()) {
            int extantAmount = containedSteam.getAmount();
            if(extantAmount >= incomingAmount) {
                if(fluidAction == FluidAction.EXECUTE)
                    containedSteam.shrink(incomingAmount);
                setChanged();
                return new FluidStack(fluid, incomingAmount);
            } else {
                if(fluidAction == FluidAction.EXECUTE)
                    containedSteam = FluidStack.EMPTY;
                if(incomingAmount - extantAmount > 0)
                    setChanged();
                return new FluidStack(fluid, Math.min(incomingAmount, extantAmount));
            }
        }
        return fluidStack;
    }

    @Override
    public @NotNull FluidStack drain(int i, FluidAction fluidAction) {
        //Assume removed fluid is steam if not specified
        if(containedSteam.getAmount() > 0)
            setChanged();
        return drain(new FluidStack(FluidRegistry.STEAM.get(), i), fluidAction);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-1, 0, -1), getBlockPos().offset(1,2,1));
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
        ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_MATERIA_INSERTION);
        if(InventoryHelper.isMateriaUnbottled(insertionStack)) {
            return insertionStack.getCount() < itemHandler.getSlotLimit(SLOT_MATERIA_INSERTION);
        }
        return insertionStack.isEmpty();
    }

    @Override
    public Map<MateriaItem, Integer> getProvisioningNeeds() {
        Map<MateriaItem, Integer> result = new HashMap<>();

        ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_MATERIA_INSERTION);

        if(insertionStack.getCount() < itemHandler.getSlotLimit(SLOT_MATERIA_INSERTION) / 2) {
            result.put(ESSENTIA_WATER, itemHandler.getSlotLimit(SLOT_MATERIA_INSERTION) - insertionStack.getCount());
        }

        return result;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        if(pMateriaItem == ESSENTIA_WATER)
            activeProvisionRequests.add(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        activeProvisionRequests.remove(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        if(pStack.getItem() == ESSENTIA_WATER) {
            ItemStack insertionStack = itemHandler.getStackInSlot(SLOT_MATERIA_INSERTION);

            if(insertionStack.isEmpty()) {
                insertionStack = pStack.copy();
                CompoundTag nbt = new CompoundTag();
                nbt.putInt("CustomModelData", 1);
                insertionStack.setTag(nbt);
                itemHandler.setStackInSlot(SLOT_MATERIA_INSERTION, insertionStack);
            } else {
                insertionStack.grow(pStack.getCount());
                itemHandler.setStackInSlot(SLOT_MATERIA_INSERTION, insertionStack);
            }

            syncAndSave();

            activeProvisionRequests.remove((MateriaItem)pStack.getItem());
        }
    }

    @Override
    public int canAcceptStackFromShlorp(ItemStack pStack) {
        if(pStack.getItem() == ESSENTIA_WATER) {
            return 0;
        }
        return pStack.getCount();
    }

    @Override
    public int insertStackFromShlorp(ItemStack pStack) {
        provide(pStack);
        return 0;
    }
}
