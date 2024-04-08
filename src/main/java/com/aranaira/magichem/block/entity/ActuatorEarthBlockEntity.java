package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.IBlockWithPowerLevel;
import com.aranaira.magichem.foundation.IPluginDevice;
import com.aranaira.magichem.gui.ActuatorEarthMenu;
import com.aranaira.magichem.gui.ActuatorEarthScreen;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeHooks;
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

public class ActuatorEarthBlockEntity extends DirectionalPluginBlockEntity implements MenuProvider, IBlockWithPowerLevel, IPluginDevice, IEldrinConsumerTile {

    private static final int[]
            ELDRIN_POWER_USAGE = {0, 1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 67, 82, 100},
            SAND_PER_OPERATION = {0, 140, 170, 200, 230, 260, 290, 320, 350, 380, 410, 440, 470, 500},
            GRIME_REDUCTION = {0, 34, 37, 40, 43, 46, 49, 52, 55, 58, 61, 64, 67, 70};
    public static final int
            SLOT_COUNT = 1,
            DATA_COUNT = 5, DATA_REMAINING_ELDRIN_TIME = 0, DATA_POWER_LEVEL = 1, DATA_FLAGS = 2, DATA_SAND = 3, DATA_GRIME = 4,
            FLAG_IS_SATISFIED = 1, FLAG_REDUCTION_TYPE_POWER = 2, FLAG_FUEL_NORMAL = 4, FLAG_FUEL_SUPER = 8, FLAG_FUEL_SATISFACTION_TYPE = 12;
    private static final float
            PIPE_VIBRATION_ACCELERATION = 0.002f;
    private int
            powerLevel = 1,
            remainingEldrinTime = -1,
            remainingSand = 0,
            currentGrime = 0,
            flags;
    private float
            remainingEldrinForSatisfaction,
            pipeVibrationIntensity = 0;
    protected ContainerData data;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public ActuatorEarthBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        this.flags = 0;
    }

    public ActuatorEarthBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ACTUATOR_EARTH_BE.get(), pPos, pBlockState);

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch(pIndex) {
                    case DATA_REMAINING_ELDRIN_TIME -> ActuatorEarthBlockEntity.this.remainingEldrinTime;
                    case DATA_POWER_LEVEL -> ActuatorEarthBlockEntity.this.powerLevel;
                    case DATA_FLAGS -> ActuatorEarthBlockEntity.this.flags;
                    case DATA_GRIME -> ActuatorEarthBlockEntity.this.currentGrime;
                    case DATA_SAND -> ActuatorEarthBlockEntity.this.remainingSand;
                    default -> -1;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case DATA_REMAINING_ELDRIN_TIME -> ActuatorEarthBlockEntity.this.remainingEldrinTime = pValue;
                    case DATA_POWER_LEVEL -> ActuatorEarthBlockEntity.this.powerLevel = pValue;
                    case DATA_FLAGS -> ActuatorEarthBlockEntity.this.flags = pValue;
                    case DATA_SAND -> ActuatorEarthBlockEntity.this.remainingSand = pValue;
                    case DATA_GRIME -> ActuatorEarthBlockEntity.this.currentGrime = pValue;
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

    public int getSandPerOperation() {
        return SAND_PER_OPERATION[this.powerLevel];
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
        nbt.putInt("remainingSand", remainingSand);
        nbt.putInt("currentGrime", currentGrime);
        if(ownerUUID != null)
            nbt.putUUID("owner", ownerUUID);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.remainingEldrinTime = nbt.getInt("remainingEldrinTime");
        this.powerLevel = nbt.getInt("powerLevel");
        this.remainingSand = nbt.getInt("remainingSand");
        this.currentGrime = nbt.getInt("currentGrime");

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
        nbt.putInt("remainingEldrinTime", remainingEldrinTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("remainingSand", remainingSand);
        nbt.putInt("currentGrime", currentGrime);
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
        syncAndSave();
    }

    public static boolean getIsSatisfied(ActuatorEarthBlockEntity entity) {
        return (entity.flags & FLAG_IS_SATISFIED) == FLAG_IS_SATISFIED;
    }

    public static boolean getIsFuelled(ActuatorEarthBlockEntity entity) {
        return (entity.flags & FLAG_FUEL_NORMAL) == FLAG_FUEL_NORMAL;
    }

    public static boolean getIsSuperFuelled(ActuatorEarthBlockEntity entity) {
        return (entity.flags & FLAG_FUEL_SUPER) == FLAG_FUEL_SUPER;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        if(t instanceof ActuatorEarthBlockEntity afbe) {

            float smoke = afbe.data.get(DATA_SAND);
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

    public static void delegatedTick(Level level, BlockPos pos, BlockState state, ActuatorEarthBlockEntity entity) {
        Player ownerCheck = entity.getOwner();
        int powerDraw = entity.getEldrinPowerUsage();

        if(ownerCheck != null) {
            float consumption = entity.consume(ownerCheck, pos, pos.getCenter(), Affinity.FIRE, Math.min(powerDraw, entity.remainingEldrinForSatisfaction), 1);
            entity.remainingEldrinForSatisfaction -= consumption;
        }
    }

    public void handleAnimationDrivers() {

    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.lazyItemHandler.invalidate();
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new ActuatorEarthMenu(i, inventory, this, this.data);
    }

    public static float getSandPercent(int pSandAmount) {
        return (float)pSandAmount * 100f / Config.quakeRefinerySandCapacity;
    }

    public float getSandPercent() {
        return (float)remainingSand * 100f / Config.quakeRefinerySandCapacity;
    }

    public static int getScaledSand(int pSandAmount) {
        return pSandAmount * ActuatorEarthScreen.FLUID_GAUGE_H / Config.quakeRefinerySandCapacity;
    }

    public static float getGrimePercent(int pGrimeAmount) {
        return (float)pGrimeAmount * 100f / Config.quakeRefineryGrimeCapacity;
    }

    public static int getScaledGrime(int pGrimeAmount) {
        return pGrimeAmount * ActuatorEarthScreen.FLUID_GAUGE_H / Config.quakeRefineryGrimeCapacity;
    }
}
