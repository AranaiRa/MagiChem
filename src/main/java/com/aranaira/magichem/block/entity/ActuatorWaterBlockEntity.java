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

public class ActuatorWaterBlockEntity extends AbstractDirectionalPluginBlockEntity implements MenuProvider, IPluginDevice, IEldrinConsumerTile, IFluidHandler, IShlorpReceiver, IMateriaProvisionRequester {

    private static final int[]
            ELDRIN_POWER_USAGE = {0, 5, 15, 30, 50, 75, 105, 140, 180, 225, 275, 335, 410, 500},
            WATER_PER_OPERATION = {0, 140, 170, 200, 230, 260, 290, 320, 350, 380, 410, 440, 470, 500},
            STEAM_PER_PROCESS = {0, 3, 5, 7, 10, 13, 17, 22, 27, 33, 39, 46, 53, 61},
            EFFICIENCY_INCREASE = {0, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40};
    public static final int
            MAX_POWER_LEVEL = 13,
            SLOT_COUNT = 2,
            SLOT_ESSENTIA_INSERTION = 0, SLOT_BOTTLES = 1,
            TANK_ID_WATER = 0, TANK_ID_STEAM = 1,
            DATA_COUNT = 5, DATA_REMAINING_ELDRIN_TIME = 0, DATA_POWER_LEVEL = 1, DATA_FLAGS = 2, DATA_WATER = 3, DATA_STEAM = 4;
    public static final int
            FLAG_IS_SATISFIED = 1, FLAG_IS_PAUSED = 2;
    private int
            remainingWaterForSatisfaction = 1,
            flags;
    protected ContainerData data;
    private FluidStack
            containedWater, containedSteam;
    private final LazyOptional<IFluidHandler> fluidHandler;
    private static final MateriaItem ESSENTIA_WATER = ItemRegistry.getEssentiaMap(false, false).get("water");
    private static final Random random = new Random();

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

        this.containedWater = FluidStack.EMPTY;
        this.containedSteam = FluidStack.EMPTY;
        this.fluidHandler = LazyOptional.of(() -> this);
        this.flags = 0;

        itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot == SLOT_ESSENTIA_INSERTION) {
                    if(stack.getItem() == ItemRegistry.DEBUG_ORB.get())
                        return true;
                    else if(stack.getItem() instanceof MateriaItem mi) {
                        return mi == ESSENTIA_WATER;
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

    public int getWaterInTank() {
        return containedWater.getAmount();
    }

    public int getWaterPerOperation() {
        return WATER_PER_OPERATION[this.powerLevel];
    }

    public static int getWaterPerOperation(int pPowerLevel) {
        return WATER_PER_OPERATION[pPowerLevel];
    }

    public int getSteamInTank() {
        return containedSteam.getAmount();
    }

    public int getSteamPerProcess() {
        return STEAM_PER_PROCESS[this.powerLevel];
    }

    public static int getSteamPerProcess(int pPowerLevel) {
        return STEAM_PER_PROCESS[pPowerLevel];
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("remainingCycleTime", remainingCycleTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("storedMateria", storedMateria);
        nbt.putBoolean("drewEldrinThisCycle", drewEldrinThisCycle);
        nbt.putBoolean("drewEssentiaThisCycle", drewEssentiaThisCycle);
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
        this.drewEssentiaThisCycle = nbt.getBoolean("drewEssentiaThisCycle");
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
        nbt.putBoolean("drewEssentiaThisCycle", drewEssentiaThisCycle);
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

    public int getStoredMateria() {
        return storedMateria;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        boolean changed = AbstractDirectionalPluginBlockEntity.tick(level, pos, blockState, t, ActuatorWaterBlockEntity::getValue);

        if(t instanceof ActuatorWaterBlockEntity entity) {
            if(changed && !level.isClientSide())
                entity.syncAndSave();
            if(entity.itemHandler.getStackInSlot(SLOT_ESSENTIA_INSERTION).getItem() == ItemRegistry.DEBUG_ORB.get()) {
                int pre = entity.getWaterInTank();
                if(entity.containedWater.isEmpty()) {
                    entity.containedWater = new FluidStack(Fluids.WATER, Config.delugePurifierTankCapacity);
                } else {
                    entity.containedWater.setAmount(Config.delugePurifierTankCapacity);
                }

                if(pre != entity.getWaterInTank())
                    changed = true;

                if(changed) entity.syncAndSave();
            }

            //Particle work
            if(level.isClientSide()) {
                float water = entity.getWaterPercent();
                if (water > 0 && !entity.getPaused()) {
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
                    float steam = entity.containedSteam.getAmount();
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
        boolean changed = AbstractDirectionalPluginBlockEntity.delegatedTick(level, pos, state, entity,
                ActuatorWaterBlockEntity::getValue,
                ActuatorWaterBlockEntity::getAffinity,
                ActuatorWaterBlockEntity::getPowerDraw,
                ActuatorWaterBlockEntity::handleAuxiliaryRequirements);

        if(!entity.isPaused && entity.remainingCycleTime <= 0) {
            entity.remainingWaterForSatisfaction = entity.getWaterPerOperation();
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
        return (float)containedWater.getAmount() / Config.delugePurifierTankCapacity;
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
        return remainingCycleTime * ActuatorWaterScreen.SYMBOL_H / ((drewEssentiaThisCycle && drewEldrinThisCycle) ? Config.actuatorDoubleSuppliedPeriod : Config.actuatorSingleSuppliedPeriod);
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
        //Water is insert-only
        Fluid fluid = fluidStack.getFluid();
        int incomingAmount = fluidStack.getAmount();
        if(fluid == Fluids.WATER) {
            int extantAmount = containedWater.getAmount();
            int query = Config.delugePurifierTankCapacity - (incomingAmount + extantAmount);

            //Hit capacity
            if(query < 0) {
                int actualTransfer = Config.delugePurifierTankCapacity - extantAmount;
                if(fluidAction == FluidAction.EXECUTE) {
                    this.containedWater = new FluidStack(Fluids.WATER, extantAmount + actualTransfer);
                    syncAndSave();
                }
                return actualTransfer;
            } else {
                if(fluidAction == FluidAction.EXECUTE) {
                    this.containedWater = new FluidStack(Fluids.WATER, extantAmount + incomingAmount);
                    syncAndSave();
                }
                return incomingAmount;
            }
        }

        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
        //Steam is extract only
        Fluid fluid = fluidStack.getFluid();
        int incomingAmount = fluidStack.getAmount();
        if(fluid == Fluids.WATER) {
            int extantAmount = containedWater.getAmount();
            if(extantAmount >= incomingAmount) {
                if(fluidAction == FluidAction.EXECUTE) {
                    containedWater.shrink(incomingAmount);
                    syncAndSave();
                }
                return new FluidStack(fluid, incomingAmount);
            } else {
                if(fluidAction == FluidAction.EXECUTE) {
                    containedWater = FluidStack.EMPTY;
                    syncAndSave();
                }
                return new FluidStack(fluid, incomingAmount - extantAmount);
            }
        }
        else if(fluid == FluidRegistry.STEAM.get()) {
            int extantAmount = containedSteam.getAmount();
            if(extantAmount >= incomingAmount) {
                if(fluidAction == FluidAction.EXECUTE) {
                    containedSteam.shrink(incomingAmount);
                    syncAndSave();
                }
                return new FluidStack(fluid, incomingAmount);
            } else {
                if(incomingAmount - extantAmount > 0) {
                    if (fluidAction == FluidAction.EXECUTE) {
                        containedSteam = FluidStack.EMPTY;
                        syncAndSave();
                    }
                }
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

    public static Affinity getAffinity(Void v) {
        return Affinity.WATER;
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
            result.put(ESSENTIA_WATER, itemHandler.getSlotLimit(SLOT_ESSENTIA_INSERTION) - insertionStack.getCount());
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
        if(entity instanceof ActuatorWaterBlockEntity awbe) {
            if(!entity.isAuxiliaryRequirementSatisfied()) {
                if(awbe.containedWater.getAmount() > 0) {
                    if(awbe.remainingWaterForSatisfaction > 0) {
                        int consumption = Math.min(awbe.remainingWaterForSatisfaction, awbe.containedWater.getAmount());
                        awbe.containedWater.setAmount(awbe.containedWater.getAmount() - consumption);
                        awbe.remainingWaterForSatisfaction -= consumption;
                        if(awbe.remainingWaterForSatisfaction <= 0) {
                            awbe.satisfyAuxiliaryRequirements();
                            return true;
                        }
                    } else {
                        awbe.satisfyAuxiliaryRequirements();
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
