package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.IMateriaProvisionRequester;
import com.aranaira.magichem.foundation.IPluginDevice;
import com.aranaira.magichem.foundation.IShlorpReceiver;
import com.aranaira.magichem.gui.ActuatorAirMenu;
import com.aranaira.magichem.gui.ActuatorAirScreen;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleOrbitMover;
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
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class ActuatorAirBlockEntity extends AbstractDirectionalPluginBlockEntity implements MenuProvider, IPluginDevice, IEldrinConsumerTile, IFluidHandler, IShlorpReceiver, IMateriaProvisionRequester {

    private static final int[]
            ELDRIN_POWER_USAGE = {0, 5, 140, 500},
            GAS_PER_PROCESS = {0, 0, 16, 32};
    private static final float[]
            POWER_PENALTY = {1.0f, 1.5f, 2.25f, 3.375f};
    public static final int
            SLOT_COUNT = 2, SLOT_ESSENTIA_INSERTION = 0, SLOT_BOTTLES = 1, MAX_POWER_LEVEL = 3,
            TANK_SMOKE = 0, TANK_STEAM = 1;
    private static final float
            FAN_ACCELERATION_RATE = 0.09f, FAN_TOP_SPEED = 24.0f;
    private boolean
            isGasSatisfied = false;
    private int
            flags;
    public float
            fanAngle = 0, fanSpeed = 0;
    protected ContainerData data;
    private FluidStack containedSmoke, containedSteam;
    private final LazyOptional<IFluidHandler> fluidHandler;
    private static final MateriaItem ESSENTIA_AIR = ItemRegistry.getEssentiaMap(false, false).get("air");

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public ActuatorAirBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        this.containedSmoke = FluidStack.EMPTY;
        this.containedSteam = FluidStack.EMPTY;
        this.fluidHandler = LazyOptional.of(() -> this);
        this.flags = 0;
    }

    public ActuatorAirBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ACTUATOR_AIR_BE.get(), pPos, pBlockState);

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

        this.containedSmoke = FluidStack.EMPTY;
        this.containedSteam = FluidStack.EMPTY;
        this.fluidHandler = LazyOptional.of(() -> this);
        this.flags = 0;

        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
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
                        return mi == ESSENTIA_AIR;
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

    public boolean getIsGasSatsified() {
        return isGasSatisfied;
    }

    public static int getRawBatchSize(int pPowerLevel) {
        return new int[]{0, 2, 4, 8}[pPowerLevel];
    }

    public int getBatchSize() {
        int gpp = getGasPerProcess(powerLevel);

        if(powerLevel == 2) {
            if(containedSteam.getAmount() < gpp && containedSmoke.getAmount() < gpp)
                return 1;
        } else if (powerLevel == 3) {
            if(containedSteam.getAmount() < gpp || containedSmoke.getAmount() < gpp)
                return 1;
        }
        return new int[]{0, 2, 4, 8}[powerLevel];
    }

    public float getPenaltyRate() {
        if(powerLevel > 1 && !isGasSatisfied)
            return 1;
        return POWER_PENALTY[this.powerLevel];
    }

    public static float getPenaltyRate(int pPowerLevel) {
        return POWER_PENALTY[pPowerLevel];
    }

    public static float getPenaltyRateFromBatchSize(int pBatchSize) {
        int index = 0;
        if(pBatchSize == 2) index = 1;
        else if(pBatchSize == 4) index = 2;
        else if(pBatchSize == 8) index = 3;
        return POWER_PENALTY[index];
    }

    public int getEldrinPowerUsage() {
        return ELDRIN_POWER_USAGE[this.powerLevel];
    }

    public static int getEldrinPowerUsage(int pPowerLevel) {
        return ELDRIN_POWER_USAGE[pPowerLevel];
    }

    public int getGasPerProcess() {
        return GAS_PER_PROCESS[this.powerLevel];
    }

    public static int getGasPerProcess(int pPowerLevel) {
        return GAS_PER_PROCESS[pPowerLevel];
    }

    public int getPowerLevel() {
        return this.powerLevel;
    }

    public void increasePowerLevel() {
        this.powerLevel = Math.min(3, this.powerLevel + 1);
    }

    public void decreasePowerLevel() {
        this.powerLevel = Math.max(1, this.powerLevel - 1);
    }

    @Override
    public void setPowerLevel(int pPowerLevel) {
        this.powerLevel = pPowerLevel;
    }

    public int getFlags() {
        return flags;
    }

    public int getSmokeInTank() {
        return containedSmoke.getAmount();
    }

    public int getSteamInTank() {
        return containedSteam.getAmount();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("remainingCycleTime", remainingCycleTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("storedMateria", storedMateria);
        nbt.putBoolean("drewEldrinThisCycle", drewEldrinThisCycle);
        nbt.putBoolean("drewEssentiaThisCycle", drewEssentiaThisCycle);
        nbt.putBoolean("isGasSatisfied", isGasSatisfied);
        nbt.putInt("tankSmoke", this.containedSmoke.getAmount());
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
        this.isGasSatisfied = nbt.getBoolean("isGasSatisfied");
        this.flags = nbt.getInt("flags");

        int nbtSmoke = nbt.getInt("tankSmoke");
        if(nbtSmoke > 0)
            this.containedSmoke = new FluidStack(FluidRegistry.SMOKE.get(), nbtSmoke);
        else
            this.containedSmoke = FluidStack.EMPTY;

        int nbtSteam = nbt.getInt("tankSteam");
        if(nbtSteam > 0)
            this.containedSteam = new FluidStack(FluidRegistry.STEAM.get(), nbtSteam);
        else
            this.containedSteam = FluidStack.EMPTY;

        this.flags = nbt.getInt("flags");

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
        nbt.putBoolean("isGasSatisfied", isGasSatisfied);
        nbt.putInt("tankSmoke", this.containedSmoke.getAmount());
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
        consumeGasses();
    }

    private boolean consumeGasses() {
        boolean changed = false;

        //Do nothing, there is no gas cost
        if (powerLevel == 1) {
            isGasSatisfied = true;
        }
        //Consume from the higher of Smoke or Steam
        else if (powerLevel == 2) {
            int cost = getGasPerProcess();

            if(containedSteam.getAmount() >= containedSmoke.getAmount()) {
                if(containedSteam.getAmount() >= cost) {
                    containedSteam.setAmount(Math.max(0,containedSteam.getAmount() - cost));
                    changed = true;
                    isGasSatisfied = true;
                } else {
                    isGasSatisfied = false;
                }
            } else {
                if(containedSmoke.getAmount() >= cost) {
                    containedSmoke.setAmount(Math.max(0,containedSmoke.getAmount() - cost));
                    changed = true;
                    isGasSatisfied = true;
                } else {
                    isGasSatisfied = false;
                }
            }

        }
        //Consume from both Smoke and Steam
        else if (powerLevel == 3) {
            int cost = getGasPerProcess();

            if(containedSmoke.getAmount() >= cost && containedSteam.getAmount() >= cost) {
                containedSmoke.setAmount(Math.max(0,containedSmoke.getAmount() - cost));
                containedSteam.setAmount(Math.max(0,containedSteam.getAmount() - cost));
                changed = true;
                isGasSatisfied = true;
            } else {
                isGasSatisfied = false;
            }
        }

        return changed;
    }

    public static <T extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pBlockState, T t) {
        boolean changed = AbstractDirectionalPluginBlockEntity.tick(pLevel, pPos, pBlockState, t, ActuatorAirBlockEntity::getValue);

        if(t instanceof ActuatorAirBlockEntity entity) {
            if(changed && !pLevel.isClientSide())
                entity.syncAndSave();

            if(pLevel.isClientSide()) {
                entity.handleAnimationDrivers();
            }

            if(entity.itemHandler.getStackInSlot(SLOT_ESSENTIA_INSERTION).getItem() == ItemRegistry.DEBUG_ORB.get()) {
                int preSteam = entity.getSteamInTank();
                if (entity.containedSteam.isEmpty()) {
                    entity.containedSteam = new FluidStack(FluidRegistry.STEAM.get(), Config.galePressurizerTankCapacity);
                } else {
                    entity.containedSteam.setAmount(Config.galePressurizerTankCapacity);
                }

                int preSmoke = entity.getSmokeInTank();
                if (entity.containedSmoke.isEmpty()) {
                    entity.containedSmoke = new FluidStack(FluidRegistry.SMOKE.get(), Config.galePressurizerTankCapacity);
                } else {
                    entity.containedSmoke.setAmount(Config.galePressurizerTankCapacity);
                }

                if((preSteam != entity.getSteamInTank()) || (preSmoke != entity.getSmokeInTank()))
                    changed = true;

                if(changed) entity.syncAndSave();
            }

            if(!entity.getPaused()) {
                //particle work goes here
                if (entity.getIsSatisfied()) {
                    int spawnModulus = 3;
                    Vector3f mid = new Vector3f(0f, 1.53125f, 0f);

                    Direction dir = pBlockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    if (dir == Direction.NORTH) {
                        mid.x = 0.5f;
                        mid.z = 0.6563f;
                    }
                    if (dir == Direction.EAST) {
                        mid.x = 0.3437f;
                        mid.z = 0.5f;
                    } else if (dir == Direction.SOUTH) {
                        mid.x = 0.5f;
                        mid.z = 0.3437f;
                    } else if (dir == Direction.WEST) {
                        mid.x = 0.6563f;
                        mid.z = 0.5f;
                    }

                    pLevel.addParticle(new MAParticleType(ParticleInit.AIR_ORBIT.get())
                                    .setMaxAge(15).setScale(0.0875f).setColor(32, 32, 32, 128)
                                    .setMover(new ParticleOrbitMover(
                                            pPos.getX() + mid.x, pPos.getY() + mid.y - 0.325f, pPos.getZ() + mid.z,
                                            0.425f, 0.01875f, 0.0001f, 0.1f
                                    )),
                            pPos.getX() + mid.x, pPos.getY() + mid.y - 0.325f, pPos.getZ() + mid.z,
                            0, 0, 0);

                    if (pLevel.getGameTime() % spawnModulus == 0) {
                        pLevel.addParticle(new MAParticleType(ParticleInit.AIR_ORBIT.get())
                                        .setMaxAge(20).setScale(0.01f).setColor(64, 64, 64, 196)
                                        .setMover(new ParticleOrbitMover(
                                                pPos.getX() + mid.x, pPos.getY() + mid.y - 0.325f, pPos.getZ() + mid.z,
                                                -0.4375f, 0.02f, 0.0001f, 0.125f
                                        )),
                                pPos.getX() + mid.x, pPos.getY() + mid.y - 0.325f, pPos.getZ() + mid.z,
                                0, 0, 0);
                    }
                }
            }
        }
    }

    public static void delegatedTick(Level level, BlockPos pos, BlockState state, ActuatorAirBlockEntity entity) {
        boolean changed = AbstractDirectionalPluginBlockEntity.delegatedTick(level, pos, state, entity,
                ActuatorAirBlockEntity::getValue,
                ActuatorAirBlockEntity::getAffinity,
                ActuatorAirBlockEntity::getPowerDraw,
                ActuatorAirBlockEntity::handleAuxiliaryRequirements);

        if(!entity.isPaused && entity.remainingCycleTime <= 0) {
            entity.isGasSatisfied = false;
        }

        if(changed)
            entity.syncAndSave();
    }

    public void handleAnimationDrivers() {
        if(this.getIsSatisfied() && !this.getPaused()) {
            if(fanSpeed == 0) fanSpeed += FAN_ACCELERATION_RATE * 4;
            fanSpeed = Math.min(fanSpeed + FAN_ACCELERATION_RATE, FAN_TOP_SPEED);
        } else {
            fanSpeed = Math.max(fanSpeed - FAN_ACCELERATION_RATE * 3.0f, 0f);
        }
        fanAngle = (fanAngle + fanSpeed) % 360.0f;
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
        return new ActuatorAirMenu(i, inventory, this, this.data);
    }

    public static float getSmokePercent(int pSmokeAmount) {
        return (float)pSmokeAmount * 100f / Config.galePressurizerTankCapacity;
    }

    public static int getScaledSmoke(int pSmokeAmount) {
        return pSmokeAmount * ActuatorAirScreen.FLUID_GAUGE_H / Config.galePressurizerTankCapacity;
    }

    public static float getSteamPercent(int pSteamAmount) {
        return (float)pSteamAmount * 100f / Config.galePressurizerTankCapacity;
    }

    public static int getScaledSteam(int pSteamAmount) {
        return pSteamAmount * ActuatorAirScreen.FLUID_GAUGE_H / Config.galePressurizerTankCapacity;
    }

    @Override
    public int getTanks() {
        return 2;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int i) {
        if(i == TANK_SMOKE) return containedSmoke;
        if(i == TANK_STEAM) return containedSteam;
        else return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int i) {
        return Config.galePressurizerTankCapacity;
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
        if(i == TANK_SMOKE) return fluidStack.getFluid() == FluidRegistry.SMOKE.get();
        if(i == TANK_STEAM) return fluidStack.getFluid() == FluidRegistry.STEAM.get();
        return false;
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction fluidAction) {
        Fluid fluid = fluidStack.getFluid();
        int incomingAmount = fluidStack.getAmount();

        if(fluid == FluidRegistry.SMOKE.get()) {
            int extantAmount = this.containedSmoke.getAmount();
            int query = Config.galePressurizerTankCapacity - incomingAmount - extantAmount;

            if (query < 0) {
                int actualTransfer = Config.galePressurizerTankCapacity - extantAmount;
                if(fluidAction == FluidAction.EXECUTE) {
                    this.containedSmoke = new FluidStack(fluid, extantAmount + actualTransfer);
                    syncAndSave();
                }
                return actualTransfer;
            } else {
                if (fluidAction == FluidAction.EXECUTE) {
                    this.containedSmoke = new FluidStack(fluid, extantAmount + incomingAmount);
                    syncAndSave();
                }
                return incomingAmount;
            }
        } else if(fluid == FluidRegistry.STEAM.get()) {
            int extantAmount = this.containedSteam.getAmount();
            int query = Config.galePressurizerTankCapacity - incomingAmount - extantAmount;

            if (query < 0) {
                int actualTransfer = Config.galePressurizerTankCapacity - extantAmount;
                if(fluidAction == FluidAction.EXECUTE) {
                    this.containedSteam = new FluidStack(fluid, extantAmount + actualTransfer);
                    syncAndSave();
                }
                return actualTransfer;
            } else {
                if (fluidAction == FluidAction.EXECUTE) {
                    this.containedSteam = new FluidStack(fluid, extantAmount + incomingAmount);
                    syncAndSave();
                }
                return incomingAmount;
            }
        } else {
            return 0;
        }
    }

    @Override
    public @NotNull FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
        //Gasses are insert-only
        return fluidStack;
    }

    @Override
    public @NotNull FluidStack drain(int i, FluidAction fluidAction) {
        return drain(new FluidStack(FluidRegistry.SMOKE.get(), i), fluidAction);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-1, 0, -1), getBlockPos().offset(1,2,1));
    }

    public static Affinity getAffinity(Void v) {
        return Affinity.WIND;
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
            result.put(ESSENTIA_AIR, itemHandler.getSlotLimit(SLOT_ESSENTIA_INSERTION) - insertionStack.getCount());
        }

        return result;
    }

    @Override
    public void setProvisioningInProgress(MateriaItem pMateriaItem) {
        if(pMateriaItem == ESSENTIA_AIR)
            activeProvisionRequests.add(pMateriaItem);
    }

    @Override
    public void cancelProvisioningInProgress(MateriaItem pMateriaItem) {
        activeProvisionRequests.remove(pMateriaItem);
    }

    @Override
    public void provide(ItemStack pStack) {
        if(pStack.getItem() == ESSENTIA_AIR) {
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
        if(pStack.getItem() == ESSENTIA_AIR) {
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
        if(entity instanceof ActuatorAirBlockEntity aabe) {
            if(!aabe.isGasSatisfied) {
                boolean changed = aabe.consumeGasses();
                if (aabe.getIsGasSatsified()) aabe.satisfyAuxiliaryRequirements();
                return changed;
            }
        }
        return false;
    }
}
