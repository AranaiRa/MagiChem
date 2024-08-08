package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.IBlockWithPowerLevel;
import com.aranaira.magichem.foundation.IPluginDevice;
import com.aranaira.magichem.gui.ActuatorArcaneMenu;
import com.aranaira.magichem.gui.ActuatorArcaneScreen;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.tile.IEldrinConsumerTile;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleVelocityMover;
import com.mna.tools.math.Vector3;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ActuatorArcaneBlockEntity extends DirectionalPluginBlockEntity implements MenuProvider, IBlockWithPowerLevel, IPluginDevice, IEldrinConsumerTile {

    private static final int[]
            ELDRIN_POWER_USAGE = {0, 5, 15, 30, 50, 75, 105, 140, 180, 225, 275, 335, 410, 500},
            SAND_PER_OPERATION = {0, 45, 50, 55, 60, 70, 80, 90, 105, 120, 135, 155, 175, 200},
            GRIME_REDUCTION = {0, 34, 37, 40, 43, 46, 49, 52, 55, 58, 61, 64, 67, 70};
    public static final int
            SLOT_COUNT = 2, SLOT_INPUT = 0, SLOT_OUTPUT = 1,
            DATA_COUNT = 4, DATA_REMAINING_ELDRIN_TIME = 0, DATA_POWER_LEVEL = 1, DATA_FLAGS = 2, DATA_SLURRY = 3,
            FLAG_IS_SATISFIED = 1, FLAG_IS_PAUSED = 2,
            STAMPER_ANIMATION_PERIOD = 10;
    private int
            powerLevel = 1,
            remainingEldrinTime = -1,
            containedSlurry = 0,
            flags;
    private float
            remainingEldrinForSatisfaction;
    public float
            stamperDepth = 0,
            stamperDepthNextTick = 0;
    protected ContainerData data;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(slot == SLOT_INPUT)
                return true;
            else if(slot == SLOT_OUTPUT)
                return true;
            return false;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

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
                    case DATA_SLURRY -> ActuatorArcaneBlockEntity.this.containedSlurry;
                    default -> -1;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case DATA_REMAINING_ELDRIN_TIME -> ActuatorArcaneBlockEntity.this.remainingEldrinTime = pValue;
                    case DATA_POWER_LEVEL -> ActuatorArcaneBlockEntity.this.powerLevel = pValue;
                    case DATA_FLAGS -> ActuatorArcaneBlockEntity.this.flags = pValue;
                    case DATA_SLURRY -> ActuatorArcaneBlockEntity.this.containedSlurry = pValue;
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

    public static int getSandPerOperation(int pPowerLevel) {
        return SAND_PER_OPERATION[pPowerLevel];
    }

    public int getGrimeReductionRate() {
        return GRIME_REDUCTION[this.powerLevel];
    }

    public static int getGrimeReductionRate(int pPowerLevel) {
        return GRIME_REDUCTION[pPowerLevel];
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
        nbt.putInt("containedSlurry", containedSlurry);
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
        this.containedSlurry = nbt.getInt("containedSlurry");
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
        nbt.putInt("remainingEldrinTime", remainingEldrinTime);
        nbt.putInt("powerLevel", powerLevel);
        nbt.putInt("containedSlurry", containedSlurry);
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
    public void processCompletedOperation() {
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
        if(t instanceof ActuatorArcaneBlockEntity aebe) {
            if (level.isClientSide()) {
                aebe.handleAnimationDrivers();
            }

            //fill and empty from the slurry buffer
        }
    }

    public static void delegatedTick(Level level, BlockPos pos, BlockState state, ActuatorArcaneBlockEntity entity) {
        Player ownerCheck = entity.getOwner();
        int powerDraw = entity.getEldrinPowerUsage();

        if(ownerCheck != null && !getIsPaused(entity)) {
            float consumption = entity.consume(ownerCheck, pos, pos.getCenter(), Affinity.ARCANE, Math.min(powerDraw, entity.remainingEldrinForSatisfaction));
            entity.remainingEldrinForSatisfaction -= consumption;

            //Eldrin processing
            if(entity.remainingEldrinTime <= 0) {
                if(entity.remainingEldrinForSatisfaction <= 0) {
                    entity.remainingEldrinForSatisfaction = powerDraw;
                    entity.remainingEldrinTime = Config.quakeRefineryOperationTime;
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
        return new ActuatorArcaneMenu(i, inventory, this, this.data);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-1, 0, -1), getBlockPos().offset(1,2,1));
    }
}
