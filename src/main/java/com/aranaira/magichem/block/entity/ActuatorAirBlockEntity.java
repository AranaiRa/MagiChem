package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.IBlockWithPowerLevel;
import com.aranaira.magichem.foundation.IPluginDevice;
import com.aranaira.magichem.gui.ActuatorAirMenu;
import com.aranaira.magichem.gui.ActuatorAirScreen;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ActuatorAirBlockEntity extends DirectionalPluginBlockEntity implements MenuProvider, IBlockWithPowerLevel, IPluginDevice, IEldrinConsumerTile, IFluidHandler {

    private static final int[]
            ELDRIN_POWER_USAGE = {0, 5, 140, 500},
            GAS_PER_PROCESS = {0, 0, 16, 32};
    private static final float[]
            POWER_PENALTY = {0, 2.5f, 6.25f, 15.625f};
    public static final int
            TANK_SMOKE = 0, TANK_STEAM = 1,
            DATA_COUNT = 5, DATA_REMAINING_ELDRIN_TIME = 0, DATA_POWER_LEVEL = 1, DATA_FLAGS = 2, DATA_SMOKE = 3, DATA_STEAM = 4,
            FLAG_IS_SATISFIED = 1, FLAG_REDUCTION_TYPE_POWER = 2;
    private static final float
            FAN_ACCELERATION = 0.002f;
    private int
            powerLevel = 1,
            remainingEldrinTime = -1,
            remainingFuelTime = -1,
            flags;
    private float
            remainingEldrinForSatisfaction,
            fanSpeed = 0;
    protected ContainerData data;
    private FluidStack containedSmoke, containedSteam;
    private final LazyOptional<IFluidHandler> fluidHandler;

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
                return switch(pIndex) {
                    case DATA_REMAINING_ELDRIN_TIME -> ActuatorAirBlockEntity.this.remainingEldrinTime;
                    case DATA_POWER_LEVEL -> ActuatorAirBlockEntity.this.powerLevel;
                    case DATA_FLAGS -> ActuatorAirBlockEntity.this.flags;
                    case DATA_SMOKE -> ActuatorAirBlockEntity.this.containedSmoke.getAmount();
                    case DATA_STEAM -> ActuatorAirBlockEntity.this.containedSteam.getAmount();
                    default -> -1;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case DATA_REMAINING_ELDRIN_TIME -> ActuatorAirBlockEntity.this.remainingEldrinTime = pValue;
                    case DATA_POWER_LEVEL -> ActuatorAirBlockEntity.this.powerLevel = pValue;
                    case DATA_FLAGS -> ActuatorAirBlockEntity.this.flags = pValue;
                    case DATA_SMOKE -> {
                        if(ActuatorAirBlockEntity.this.containedSmoke == FluidStack.EMPTY)
                            ActuatorAirBlockEntity.this.containedSmoke = new FluidStack(FluidRegistry.SMOKE.get(), pValue);
                        else
                            ActuatorAirBlockEntity.this.containedSmoke.setAmount(pValue);
                    }
                    case DATA_STEAM -> {
                        if(ActuatorAirBlockEntity.this.containedSteam == FluidStack.EMPTY)
                            ActuatorAirBlockEntity.this.containedSteam = new FluidStack(FluidRegistry.STEAM.get(), pValue);
                        else
                            ActuatorAirBlockEntity.this.containedSteam.setAmount(pValue);
                    }
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };

        this.containedSmoke = FluidStack.EMPTY;
        this.containedSteam = FluidStack.EMPTY;
        this.fluidHandler = LazyOptional.of(() -> this);
        this.flags = FLAG_IS_SATISFIED;
    }

    public boolean getIsPowerReductionMode() {
        return (this.flags & FLAG_REDUCTION_TYPE_POWER) == FLAG_REDUCTION_TYPE_POWER;
    }

    public static boolean getIsPowerReductionMode(int pFlags) {
        return (pFlags & FLAG_REDUCTION_TYPE_POWER) == FLAG_REDUCTION_TYPE_POWER;
    }

    public float getPenaltyRate() {
        return POWER_PENALTY[this.powerLevel];
    }

    public static float getPenaltyRate(int pPowerLevel) {
        return POWER_PENALTY[pPowerLevel];
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

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.putInt("remainingEldrinTime", remainingEldrinTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("tankSmoke", this.containedSmoke.getAmount());
        nbt.putInt("tankSteam", this.containedSteam.getAmount());
        if(ownerUUID != null)
            nbt.putUUID("owner", ownerUUID);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.remainingEldrinTime = nbt.getInt("remainingEldrinTime");
        this.powerLevel = nbt.getInt("powerLevel");

        int nbtSmoke = nbt.getInt("tankSmoke");
        if(nbtSmoke > 0)
            this.containedSmoke = new FluidStack(FluidRegistry.SMOKE.get(), nbtSmoke);
        else
            this.containedSmoke = FluidStack.EMPTY;

        int nbtSteam = nbt.getInt("tankSteam");
        if(nbtSteam > 0)
            this.containedSteam = new FluidStack(FluidRegistry.STEAM.get(), nbtSmoke);
        else
            this.containedSteam = FluidStack.EMPTY;

        if(nbt.contains("owner"))
            ownerUUID = nbt.getUUID("owner");
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("remainingEldrinTime", remainingEldrinTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("tankSmoke", this.containedSmoke.getAmount());
        nbt.putInt("tankSteam", this.containedSteam.getAmount());
        if(ownerUUID != null)
            nbt.putUUID("owner", ownerUUID);
        return nbt;
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void processCompletedOperation() {
        //TODO: consume steam and smoke if necessary
        syncAndSave();
    }

    public static boolean getIsSatisfied(ActuatorAirBlockEntity entity) {
        return (entity.flags & FLAG_IS_SATISFIED) == FLAG_IS_SATISFIED;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        if(t instanceof ActuatorAirBlockEntity afbe) {

            float smoke = afbe.data.get(DATA_SMOKE);
            if(smoke > 0) {
                float mappedSmokePercent = Math.max(0, ((smoke / Config.infernoEngineTankCapacity) - 0.5f) * 2);
                if (mappedSmokePercent > 0f) {
                    int spawnModulus = 5 - (int) Math.floor(mappedSmokePercent * 4);
                    Vector3f mid = new Vector3f(0f, 1.6875f, 0f);
                    Vector3f left = new Vector3f(0f, 2f, 0f);
                    Vector3f right = new Vector3f(0f, 2f, 0f);

                    Direction dir = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    if(dir == Direction.NORTH) {
                        mid.x = 0.5f;
                        mid.z = 0.1875f;
                        left.x = 0.6875f;
                        left.z = 0.0625f;
                        right.x = 0.3125f;
                        right.z = 0.0625f;
                    }
                    if(dir == Direction.EAST) {
                        mid.x = 0.8125f;
                        mid.z = 0.5f;
                        left.x = 0.9375f;
                        left.z = 0.3125f;
                        right.x = 0.9375f;
                        right.z = 0.6875f;
                    }
                    else if(dir == Direction.SOUTH) {
                        mid.x = 0.5f;
                        mid.z = 0.8125f;
                        left.x = 0.3125f;
                        left.z = 0.9375f;
                        right.x = 0.6875f;
                        right.z = 0.9375f;
                    }
                    else if(dir == Direction.WEST) {
                        mid.x = 0.1875f;
                        mid.z = 0.5f;
                        left.x = 0.0625f;
                        left.z = 0.6875f;
                        right.x = 0.0625f;
                        right.z = 0.3125f;
                    }

                    if (level.getGameTime() % spawnModulus == 0) {
                        level.addParticle(new MAParticleType(ParticleInit.COZY_SMOKE.get())
                        .setPhysics(true).setColor(0.2f, 0.2f, 0.2f).setScale(0.10f),
                        pos.getX() + mid.x, pos.getY() + mid.y, pos.getZ() + mid.z,
                        0, 0.04f, 0);

                        level.addParticle(new MAParticleType(ParticleInit.COZY_SMOKE.get())
                        .setPhysics(true).setColor(0.2f, 0.2f, 0.2f).setScale(0.05f),
                        pos.getX() + left.x, pos.getY() + left.y, pos.getZ() + left.z,
                        0, 0.03f, 0);

                        level.addParticle(new MAParticleType(ParticleInit.COZY_SMOKE.get())
                        .setPhysics(true).setColor(0.2f, 0.2f, 0.2f).setScale(0.05f),
                        pos.getX() + right.x, pos.getY() + right.y, pos.getZ() + right.z,
                        0, 0.03f, 0);
                    }
                }
            }
        }
    }

    public static void delegatedTick(Level level, BlockPos pos, BlockState state, ActuatorAirBlockEntity entity) {
        Player ownerCheck = entity.getOwner();
        int powerDraw = entity.getEldrinPowerUsage();

        if(ownerCheck != null) {
            float consumption = entity.consume(ownerCheck, pos, pos.getCenter(), Affinity.WIND, Math.min(powerDraw, entity.remainingEldrinForSatisfaction));
            entity.remainingEldrinForSatisfaction -= consumption;

            //Eldrin processing
            if(entity.remainingEldrinTime <= 0) {
                if(entity.remainingEldrinForSatisfaction <= 0) {
                    entity.remainingEldrinForSatisfaction = powerDraw;
                    entity.remainingEldrinTime = Config.infernoEngineOperationTime;
                    }

                    if(!getIsSatisfied(entity)) {
                        entity.syncAndSave();
                    }
                    //process fuel reduction if present
            }
            entity.remainingEldrinTime = Math.max(-1, entity.remainingEldrinTime - 1);

            if(entity.remainingEldrinTime >= 0) entity.flags = entity.flags | ActuatorAirBlockEntity.FLAG_IS_SATISFIED;
            else {
                if(getIsSatisfied(entity)) {
                    entity.flags = entity.flags & ~ActuatorAirBlockEntity.FLAG_IS_SATISFIED;
                    entity.syncAndSave();
                } else
                    entity.flags = entity.flags & ~ActuatorAirBlockEntity.FLAG_IS_SATISFIED;
            }
        }
    }

    public void handleAnimationDrivers() {

    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.FLUID_HANDLER) return fluidHandler.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
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
        FluidStack targetedStack;

        if(fluid == FluidRegistry.SMOKE.get()) {
            targetedStack = this.containedSmoke;
        } else if(fluid == FluidRegistry.STEAM.get()) {
            targetedStack = this.containedSteam;
        } else {
            return 0;
        }

        int extantAmount = targetedStack.getAmount();
        int query = Config.galePressurizerTankCapacity - (incomingAmount - extantAmount);

        //Hit capactiy
        if (query < 0) {
            int actualTransfer = Config.galePressurizerTankCapacity - extantAmount;
            if(fluidAction == FluidAction.EXECUTE)
                targetedStack = new FluidStack(fluid, extantAmount + actualTransfer);
            return actualTransfer;
        } else {
            if (fluidAction == FluidAction.EXECUTE)
                targetedStack = new FluidStack(fluid, extantAmount + incomingAmount);
            return incomingAmount;
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
}
