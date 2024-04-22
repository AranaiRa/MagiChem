package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.IBlockWithPowerLevel;
import com.aranaira.magichem.foundation.IPluginDevice;
import com.aranaira.magichem.gui.ActuatorWaterMenu;
import com.aranaira.magichem.gui.ActuatorWaterScreen;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleLerpMover;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Random;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class ActuatorWaterBlockEntity extends DirectionalPluginBlockEntity implements MenuProvider, IBlockWithPowerLevel, IPluginDevice, IEldrinConsumerTile, IFluidHandler {

    private static final int[]
            ELDRIN_POWER_USAGE = {0, 5, 15, 30, 50, 75, 105, 140, 180, 225, 275, 335, 410, 500},
            WATER_PER_OPERATION = {0, 140, 170, 200, 230, 260, 290, 320, 350, 380, 410, 440, 470, 500},
            STEAM_PER_PROCESS = {0, 3, 5, 7, 10, 13, 17, 22, 27, 33, 39, 46, 53, 61},
            EFFICIENCY_INCREASE = {0, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40};
    public static final int
            TANK_ID_WATER = 0, TANK_ID_STEAM = 1,
            DATA_COUNT = 5, DATA_REMAINING_ELDRIN_TIME = 0, DATA_POWER_LEVEL = 1, DATA_FLAGS = 2, DATA_WATER = 3, DATA_STEAM = 4;
    public static final int
            FLAG_IS_SATISFIED = 1;
    private int
            powerLevel = 1,
            remainingEldrinTime,
            flags;
    private float
            remainingEldrinForSatisfaction;
    protected ContainerData data;
    private FluidStack
            containedWater, containedSteam;
    private final LazyOptional<IFluidHandler> fluidHandler;
    private static final Random random = new Random();

    public ActuatorWaterBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        this.containedWater = FluidStack.EMPTY;
        this.containedSteam = FluidStack.EMPTY;
        this.fluidHandler = LazyOptional.of(() -> this);
        this.flags = FLAG_IS_SATISFIED;
    }

    public ActuatorWaterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ACTUATOR_WATER_BE.get(), pPos, pBlockState);

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch(pIndex) {
                    case DATA_REMAINING_ELDRIN_TIME -> ActuatorWaterBlockEntity.this.remainingEldrinTime;
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
                    case DATA_REMAINING_ELDRIN_TIME -> ActuatorWaterBlockEntity.this.remainingEldrinTime = pValue;
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
        nbt.putInt("remainingEldrinTime", remainingEldrinTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("tankWater", this.containedWater.getAmount());
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
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("remainingEldrinTime", remainingEldrinTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("tankWater", this.containedWater.getAmount());
        nbt.putInt("tankSteam", this.containedSteam.getAmount());
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
    public void processCompletedOperation() {
        int newTotal = Math.min(Config.delugePurifierTankCapacity, containedSteam.getAmount() + getSteamPerProcess());
        containedSteam = new FluidStack(FluidRegistry.STEAM.get(), Math.min(newTotal, Config.delugePurifierTankCapacity));
    }

    public static boolean getIsSatisfied(ActuatorWaterBlockEntity entity) {
        return (entity.flags & FLAG_IS_SATISFIED) == FLAG_IS_SATISFIED;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        if(t instanceof ActuatorWaterBlockEntity awbe) {

            float water = awbe.getWaterPercent();
            if(water > 0) {
                Vector3f mid = new Vector3f(0f, 1.5625f, 0f);
                Vector3f left = new Vector3f(0f, 1.03125f, 0f);
                Vector3f right = new Vector3f(0f, 1.03125f, 0f);

                Vector2f leftSpeed = new Vector2f();

                Direction dir = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
                if(dir == Direction.NORTH) {
                    mid.x = 0.5f;
                    mid.z = 0.6875f;
                    left.x = 0.09375f;
                    left.z = 0.3125f;
                    right.x = 0.90625f;
                    right.z = 0.3125f;
                    leftSpeed.x = -0.04f;
                    leftSpeed.y = 0.0f;
                }
                if(dir == Direction.EAST) {
                    mid.x = 0.3125f;
                    mid.z = 0.5f;
                    left.x = 0.6875f;
                    left.z = 0.09375f;
                    right.x = 0.6875f;
                    right.z = 0.90625f;
                    leftSpeed.x = 0.0f;
                    leftSpeed.y = -0.04f;
                }
                if(dir == Direction.SOUTH) {
                    mid.x = 0.5f;
                    mid.z = 0.3125f;
                    left.x = 0.90625f;
                    left.z = 0.6875f;
                    right.x = 0.09375f;
                    right.z = 0.6875f;
                    leftSpeed.x = 0.04f;
                    leftSpeed.y = 0.0f;
                }
                if(dir == Direction.WEST) {
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
                for(int i=0; i<3; i++) {
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
                float steam = awbe.data.get(DATA_STEAM);
                if((int)steam > Config.delugePurifierTankCapacity / 2) {
                    float mappedSteamPercent = Math.max(0, ((steam / Config.delugePurifierTankCapacity) - 0.5f) * 2) * 2;
                    int spawnModulus = (int)Math.ceil(mappedSteamPercent * 5);

                    long time = level.getGameTime();

                    if(time % 20 <= spawnModulus) {
                        level.addParticle(new MAParticleType(ParticleInit.COZY_SMOKE.get())
                                        .setPhysics(true).setScale(0.065f).setMaxAge(10),
                                pos.getX() + left.x, pos.getY() + left.y, pos.getZ() + left.z,
                                leftSpeed.x, 0.075f, leftSpeed.y);
                    }

                    if((time + 10) % 20 <= spawnModulus) {
                        level.addParticle(new MAParticleType(ParticleInit.COZY_SMOKE.get())
                                        .setPhysics(true).setScale(0.065f).setMaxAge(10),
                                pos.getX() + right.x, pos.getY() + right.y, pos.getZ() + right.z,
                                -leftSpeed.x, 0.075f, -leftSpeed.y);
                    }
                }
            }
        }
    }

    public static void delegatedTick(Level level, BlockPos pos, BlockState state, ActuatorWaterBlockEntity entity) {
        Player ownerCheck = entity.getOwner();
        int powerDraw = entity.getEldrinPowerUsage();

        if(ownerCheck != null) {
            float consumption = entity.consume(ownerCheck, pos, pos.getCenter(), Affinity.WATER, Math.min(powerDraw, entity.remainingEldrinForSatisfaction));
            entity.remainingEldrinForSatisfaction -= consumption;

            if(entity.remainingEldrinTime <= 0) {
                if(entity.remainingEldrinForSatisfaction <= 0) {
                    int fluidOp = getWaterPerOperation(entity.powerLevel);
                    if(entity.containedWater.getAmount() >= fluidOp) {
                        entity.drain(new FluidStack(Fluids.WATER, fluidOp), FluidAction.EXECUTE);
                        entity.remainingEldrinForSatisfaction = powerDraw;
                        entity.remainingEldrinTime = Config.delugePurifierOperationTime;
                    }
                }
            } else {
                entity.remainingEldrinTime--;
            }

            if(entity.remainingEldrinTime > 0) entity.flags = entity.flags | ActuatorWaterBlockEntity.FLAG_IS_SATISFIED;
            else if(entity.remainingEldrinForSatisfaction == 0 && entity.containedWater.getAmount() > getWaterPerOperation(entity.powerLevel)) entity.flags = entity.flags | ActuatorWaterBlockEntity.FLAG_IS_SATISFIED;
            else entity.flags = entity.flags & ~ActuatorWaterBlockEntity.FLAG_IS_SATISFIED;
        }
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
                return new FluidStack(fluid, incomingAmount - extantAmount);
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
}
