package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.GrandDistilleryBlock;
import com.aranaira.magichem.block.entity.ext.AbstractDistillationBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.block.entity.routers.GrandDistilleryRouterBlockEntity;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.foundation.*;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.GrandDistilleryRouterType;
import com.aranaira.magichem.gui.GrandDistilleryMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.IEnergyStoragePlus;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.HAS_LABORATORY_UPGRADE;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.IS_EMITTING_LIGHT;

public class GrandDistilleryBlockEntity extends AbstractDistillationBlockEntity implements MenuProvider, ICanTakePlugins, IPoweredAlchemyDevice, IRequiresRouterCleanupOnDestruction {
    public static final int
        SLOT_COUNT = 25,
        SLOT_BOTTLES = 0,
        SLOT_INPUT_START = 1, SLOT_INPUT_COUNT = 6,
        SLOT_OUTPUT_START = 7, SLOT_OUTPUT_COUNT  = 18,
        GUI_PROGRESS_BAR_WIDTH = 24, GUI_GRIME_BAR_WIDTH = 67, GUI_HEAT_GAUGE_HEIGHT = 16,
        DATA_COUNT = 6, DATA_PROGRESS = 0, DATA_GRIME = 1, DATA_POWER_SUFFICIENCY = 2, DATA_EFFICIENCY_MOD = 3, DATA_OPERATION_TIME_MOD = 4, DATA_BATCH_SIZE = 5;
    public static final float
            CIRCLE_FILL_RATE = 0.025f, PARTICLE_PERCENT_RATE = 0.05f;
    private int powerUsageSetting = 1;
    private boolean
            hasSufficientPower = false, redstonePaused = false;

    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();
    private static final int[] POWER_DRAW = { //TODO: Convert this to config
            300, 360, 450, 540, 660, 810, 990, 1200, 1440, 1740,
            2100, 2520, 3030, 3660, 4410, 5310, 6390, 7680, 9240, 11100,
            13320, 15990, 19200, 23040, 27660, 33210, 39870, 47850, 57420, 68910
    };
    private static final int[] OPERATION_TICKS = { //TODO: Convert this to config
            200, 177, 155, 136, 119, 104, 91, 80, 70, 61,
            53, 46, 40, 35, 30, 26, 22, 19, 16, 14,
            14, 12, 10, 8, 6, 5, 4, 3, 2, 1
    };

    public float circlePercent = 0f;
    public float particlePercent = 0f;
    private static final int[][] PARTICLE_COLORS = {
            {64, 2, 2},
            {32, 32, 2},
            {2, 64, 2},
            {2, 32, 32},
            {2, 2, 64},
            {32, 2, 32}
    };

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    public GrandDistilleryBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.GRAND_DISTILLERY_BE.get(), pos, state);

        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if(slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT) {
                    ItemStack item = super.extractItem(slot, amount, simulate);
                    item.removeTagKey("CustomModelData");
                    return item;
                }

                return super.extractItem(slot, amount, simulate);
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if((slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) || (slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)) {
                    isStalled = false;
                }
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == SLOT_BOTTLES)
                    return stack.getItem() == Items.GLASS_BOTTLE || stack.getItem() == ItemRegistry.DEBUG_ORB.get();
                if (slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT)
                    return !(stack.getItem() instanceof MateriaItem);
                if (slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)
                    return false;

                return super.isItemValid(slot, stack);
            }
        };

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                switch(pIndex) {
                    case DATA_PROGRESS: {
                        return GrandDistilleryBlockEntity.this.progress;
                    }
                    case DATA_GRIME: {
                        IGrimeCapability grime = GrimeProvider.getCapability(GrandDistilleryBlockEntity.this);
                        return grime.getGrime();
                    }
                    case DATA_POWER_SUFFICIENCY: {
                        return GrandDistilleryBlockEntity.this.hasSufficientPower ? 1 : 0;
                    }
                    case DATA_EFFICIENCY_MOD: {
                        return GrandDistilleryBlockEntity.this.efficiencyMod;
                    }
                    case DATA_OPERATION_TIME_MOD: {
                        return Math.round(GrandDistilleryBlockEntity.this.operationTimeMod * 100);
                    }
                    case DATA_BATCH_SIZE: {
                        return GrandDistilleryBlockEntity.this.batchSize;
                    }
                    default: return -1;
                }
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch(pIndex) {
                    case DATA_PROGRESS: {
                        GrandDistilleryBlockEntity.this.progress = pValue;
                        break;
                    }
                    case DATA_GRIME: {
                        IGrimeCapability grime = GrimeProvider.getCapability(GrandDistilleryBlockEntity.this);
                        grime.setGrime(pValue);
                        break;
                    }
                    case DATA_POWER_SUFFICIENCY: {
                        GrandDistilleryBlockEntity.this.hasSufficientPower = pValue == 1;
                        break;
                    }
                    case DATA_EFFICIENCY_MOD: {
                        efficiencyMod = pValue;
                        break;
                    }
                    case DATA_OPERATION_TIME_MOD: {
                        operationTimeMod = pValue / 100f;
                        break;
                    }
                    case DATA_BATCH_SIZE: {
                        batchSize = pValue;
                        break;
                    }
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };

        lazyEnergyHandler = LazyOptional.of(() -> ENERGY_STORAGE);
    }

    //////////
    // BOILERPLATE CODE
    //////////

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.grand_distillery");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GrandDistilleryMenu(id, inventory, this, this.data);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putBoolean("hasSufficientPower", this.hasSufficientPower);
        nbt.putInt("batchSize", this.batchSize);
        nbt.putInt("powerUsageSetting", this.powerUsageSetting);
        nbt.putBoolean("redstonePaused", this.redstonePaused);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("craftingProgress");
        hasSufficientPower = nbt.getBoolean("hasSufficientPower");
        batchSize = nbt.getInt("batchSize");
        powerUsageSetting = nbt.getInt("powerUsageSetting");
        redstonePaused = nbt.getBoolean("redstonePaused");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putBoolean("hasSufficientPower", this.hasSufficientPower);
        nbt.putInt("batchSize", this.batchSize);
        nbt.putInt("powerUsageSetting", this.powerUsageSetting);
        nbt.putBoolean("redstonePaused", this.redstonePaused);
        return nbt;
    }

    public void packInventoryToBlockItem() {
        ItemStack stack = new ItemStack(BlockRegistry.GRAND_DISTILLERY.get());
        IGrimeCapability grimeCap = GrimeProvider.getCapability(GrandDistilleryBlockEntity.this);

        CompoundTag nbt = new CompoundTag();
        nbt.putInt("grime", grimeCap.getGrime());
        nbt.put("inventory", itemHandler.serializeNBT());

        stack.setTag(nbt);

        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    public void unpackInventoryFromNBT(CompoundTag pInventoryTag) {
        itemHandler.deserializeNBT(pInventoryTag);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    ////////////////////
    // DATA SLOT HANDLING
    ////////////////////

    @Override
    public int getGrimeFromData() {
        return data.get(DATA_GRIME);
    }

    @Override
    public int getMaximumGrime() {
        return Config.grandDistilleryMaximumGrime;
    }

    public boolean getPowerSufficiency() {
        return hasSufficientPower;
    }

    public void setRedstonePaused(boolean pPaused) {
        redstonePaused = pPaused;
        syncAndSave();
    }

    @Override
    public int clean() {
        int grimeDetected = GrimeProvider.getCapability(this).getGrime();
        IGrimeCapability grimeCapability = GrimeProvider.getCapability(this);
        grimeCapability.setGrime(0);
        data.set(DATA_GRIME, 0);
        return grimeDetected / Config.grimePerWaste;
    }

    public static int getScaledGrime(int grime) {
        return Math.min(Config.grandDistilleryMaximumGrime, (GUI_GRIME_BAR_WIDTH * grime) / Config.grandDistilleryMaximumGrime);
    }

    @Override
    protected void pushData() {
        this.data.set(DATA_PROGRESS, progress);
        this.data.set(DATA_GRIME, GrimeProvider.getCapability(this).getGrime());
        this.data.set(DATA_POWER_SUFFICIENCY, hasSufficientPower ? 1 : 0);
        //TODO: push op time mod
    }

    ////////////////////
    // ACTUATOR HANDLERS
    ////////////////////

    public DevicePlugDirection getPlugDirection() {
        return DevicePlugDirection.NONE;
    }

    public BlockEntity getPlugEntity() {
        BlockPos target = getBlockPos();

        if(getPlugDirection() == DevicePlugDirection.NORTH) target = target.north();
        else if(getPlugDirection() == DevicePlugDirection.EAST) target = target.east();
        else if(getPlugDirection() == DevicePlugDirection.SOUTH) target = target.south();
        else if(getPlugDirection() == DevicePlugDirection.WEST) target = target.west();

        return getLevel().getBlockEntity(target);
    }

    @Override
    public void linkPlugins() {
        pluginDevices.clear();

        //Start by grabbing the actuator plugged into the main block
        if(getPlugEntity() instanceof AbstractDirectionalPluginBlockEntity dpbe)
            pluginDevices.add(dpbe);

        List<BlockEntity> query = new ArrayList<>();
        for(Triplet<BlockPos, GrandDistilleryRouterType, DevicePlugDirection> posAndType : GrandDistilleryBlock.getRouterOffsets(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))) {
            BlockEntity be = level.getBlockEntity(getBlockPos().offset(posAndType.getFirst()));
            if(be != null)
                query.add(be);
        }

        for(BlockEntity be : query) {
            if (be instanceof GrandDistilleryRouterBlockEntity gdrbe) {
                BlockEntity pe = gdrbe.getPlugEntity();
                if(pe instanceof AbstractDirectionalPluginBlockEntity dpbe) pluginDevices.add(dpbe);
            }
        }
    }

    ////////////////////
    // INTERACTION AND VFX
    ////////////////////

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-3, 0, -3), getBlockPos().offset(3,3,3));
    }

    public void handleAnimationDrivers() {
        if(particlePercent == 1) {
            circlePercent = Math.min(1, circlePercent + CIRCLE_FILL_RATE);
        } else if(particlePercent == 0) {
            circlePercent = Math.max(0, circlePercent - CIRCLE_FILL_RATE);
        }

        if(hasSufficientPower && !redstonePaused) {
            particlePercent = Math.min(1, particlePercent + PARTICLE_PERCENT_RATE);
        } else {
            particlePercent = Math.max(0, particlePercent - PARTICLE_PERCENT_RATE);
        }
    }

    ////////////////////
    // POWER
    ////////////////////

    public int getPowerUsageSetting() {
        return powerUsageSetting;
    }

    public int getPowerDraw() {
        int baseDraw = POWER_DRAW[MathUtils.clamp(powerUsageSetting, 1, 30)-1];
        float fireActuatorModifier = 1 - (operationTimeMod / 100.0f);

        int out = Math.round((float)baseDraw * fireActuatorModifier);

        return Math.round((float)baseDraw * fireActuatorModifier);
    }

    public int getOperationTicks() {
        return OPERATION_TICKS[MathUtils.clamp(powerUsageSetting, 1, 30)-1];
    }

    public int setPowerUsageSetting(int pPowerUsageSetting) {
        this.powerUsageSetting = pPowerUsageSetting;
        this.resetProgress();
        if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * Config.circlePowerBuffer)
            ENERGY_STORAGE.setEnergy(getPowerDraw() * Config.circlePowerBuffer);
        return this.powerUsageSetting;
    }

    public int incrementPowerUsageSetting() {
        if(powerUsageSetting + 1 < 31) {
            this.powerUsageSetting++;
            this.resetProgress();
            if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * Config.circlePowerBuffer)
                ENERGY_STORAGE.setEnergy(getPowerDraw() * Config.circlePowerBuffer);
        }
        return this.powerUsageSetting;
    }

    public int decrementPowerUsageSetting() {
        if(powerUsageSetting - 1 > 0) {
            this.powerUsageSetting--;
            this.resetProgress();
            if(ENERGY_STORAGE.getEnergyStored() > getPowerDraw() * Config.circlePowerBuffer)
                ENERGY_STORAGE.setEnergy(getPowerDraw() * Config.circlePowerBuffer);
        }
        return this.powerUsageSetting;
    }

    private final IEnergyStoragePlus ENERGY_STORAGE = new IEnergyStoragePlus(Integer.MAX_VALUE, Integer.MAX_VALUE) {
        @Override
        public void onEnergyChanged() {
            setChanged();
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {

            int powerToLimit = Math.max(0, (getPowerDraw() * Config.circlePowerBuffer) - getEnergyStored());
            int actualReceive = Math.min(maxReceive, powerToLimit);

            return super.receiveEnergy(actualReceive, simulate);
        }
    };

    ////////////////////
    // OVERRIDES
    ////////////////////

    public SimpleContainer getContentsOfOutputSlots() {
        return getContentsOfOutputSlots(GrandDistilleryBlockEntity::getVar);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, GrandDistilleryBlockEntity pEntity) {
        if(!pEntity.getLevel().isClientSide() && !pEntity.redstonePaused) {
            int powerDraw = pEntity.getPowerDraw();
            boolean sufficientThisTick = pEntity.ENERGY_STORAGE.getEnergyStored() >= powerDraw;

            if(sufficientThisTick != pEntity.hasSufficientPower) {
                pEntity.hasSufficientPower = sufficientThisTick;
                pEntity.syncAndSave();
            }
            if(sufficientThisTick) {
                pEntity.remainingHeat = 2;
            } else {
                pEntity.progress = Math.max(0, pEntity.progress - 2);
            }
            pEntity.ENERGY_STORAGE.extractEnergy(powerDraw, false);

            Direction facing = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);
            BlockPos daisPos = pPos;

            if(facing == Direction.NORTH) daisPos = daisPos.south();
            if(facing == Direction.SOUTH) daisPos = daisPos.north();
            if(facing == Direction.EAST) daisPos = daisPos.west();
            if(facing == Direction.WEST) daisPos = daisPos.east();

            BlockState daisState = pLevel.getBlockState(daisPos);
            boolean isDaisEmittingLight = daisState.getValue(IS_EMITTING_LIGHT);

            if((isDaisEmittingLight && pEntity.particlePercent == 0) || (!isDaisEmittingLight && pEntity.particlePercent == 1)) {
                BlockState newDaisState = pLevel.getBlockState(daisPos).setValue(IS_EMITTING_LIGHT, sufficientThisTick);

                pLevel.setBlock(daisPos, newDaisState, 3);
                pLevel.sendBlockUpdated(daisPos, daisState, newDaisState, 3);
            }
        }
        pEntity.handleAnimationDrivers();

        //particle stuff
        if(pEntity.getLevel().isClientSide() && pEntity.particlePercent > 0) {
            Vector3 center = Vector3.zero();
            Direction facing = pEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            if (facing == Direction.NORTH)
                center = new Vector3(pPos.getX() + 0.5, pPos.getY() + 1.375, pPos.getZ() + 1.5);
            else if (facing == Direction.EAST)
                center = new Vector3(pPos.getX() - 0.5, pPos.getY() + 1.375, pPos.getZ() + 0.5);
            else if (facing == Direction.SOUTH)
                center = new Vector3(pPos.getX() + 0.5, pPos.getY() + 1.375, pPos.getZ() - 0.5);
            else if (facing == Direction.WEST)
                center = new Vector3(pPos.getX() + 1.5, pPos.getY() + 1.375, pPos.getZ() + 0.5);

            int colorIndex = r.nextInt(6);
            if (pEntity.getLevel().getGameTime() % 8 == 0) {
                pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                .setColor(PARTICLE_COLORS[colorIndex][0], PARTICLE_COLORS[colorIndex][1], PARTICLE_COLORS[colorIndex][2])
                                .setScale(0.4f * pEntity.particlePercent).setMaxAge(80),
                        center.x, center.y, center.z,
                        0, 0, 0);
            }
            pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                            .setColor(255, 255, 255).setScale(0.2f * pEntity.particlePercent),
                    center.x, center.y, center.z,
                    0, 0, 0);

            if(pEntity.particlePercent == 1) {
                for (int i = 0; i < 2; i++) {
                    Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.3f);
                    pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                    .setColor(PARTICLE_COLORS[colorIndex][0], PARTICLE_COLORS[colorIndex][1], PARTICLE_COLORS[colorIndex][2], 128)
                                    .setScale(0.09f).setMaxAge(16)
                                    .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                            center.x + offset.x, center.y + offset.y, center.z + offset.z,
                            0, 0, 0);

                    pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_LERP_POINT.get())
                                    .setScale(0.015f).setMaxAge(16)
                                    .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                            center.x + offset.x, center.y + offset.y, center.z + offset.z,
                            0, 0, 0);
                }
            }
        }

        if(!pEntity.redstonePaused)
            AbstractDistillationBlockEntity.tick(pLevel, pPos, pState, pEntity, GrandDistilleryBlockEntity::getVar, pEntity::getPoweredOperationTime);
    }

    public Integer getPoweredOperationTime(Void unused) {
        return OPERATION_TICKS[this.powerUsageSetting-1];
    }

    public void applyLaboratoryCharm() {
        BlockPos rootPos = getBlockPos();
        BlockState rootState = getBlockState();
        BlockState newRootState = getBlockState().setValue(HAS_LABORATORY_UPGRADE, true);

        getLevel().setBlock(rootPos, newRootState, 3);
        getLevel().sendBlockUpdated(rootPos, rootState, newRootState, 2);

        for(int z=-1; z<=1; z++) {
            for(int y=0; y<=2; y++) {
                for (int x=-1; x <=1; x++) {
                    BlockPos routerPos = rootPos.offset(x, y, z);
                    BlockState routerState = getLevel().getBlockState(routerPos);

                    if(routerState.getBlock() == BlockRegistry.GRAND_DISTILLERY_ROUTER.get()) {
                        BlockState newRouterState = routerState.setValue(HAS_LABORATORY_UPGRADE, true);

                        getLevel().setBlock(routerPos, newRouterState, 3);
                        getLevel().sendBlockUpdated(routerPos, routerState, newRouterState, 2);
                    }
                }
            }
        }
    }

    public static int getVar(IDs pID) {
        return switch(pID) {
            case SLOT_BOTTLES -> SLOT_BOTTLES;
            case SLOT_INPUT_START -> SLOT_INPUT_START;
            case SLOT_INPUT_COUNT -> SLOT_INPUT_COUNT;
            case SLOT_OUTPUT_START -> SLOT_OUTPUT_START;
            case SLOT_OUTPUT_COUNT -> SLOT_OUTPUT_COUNT;

            case DATA_PROGRESS -> DATA_PROGRESS;
            case DATA_GRIME -> DATA_GRIME;
            case DATA_EFFICIENCY_MOD -> DATA_EFFICIENCY_MOD;
            case DATA_OPERATION_TIME_MOD -> DATA_OPERATION_TIME_MOD;

            case MODE_USES_RF -> 1;

            case GUI_PROGRESS_BAR_WIDTH -> GUI_PROGRESS_BAR_WIDTH;
            case GUI_GRIME_BAR_WIDTH -> GUI_GRIME_BAR_WIDTH;
            case GUI_HEAT_GAUGE_HEIGHT -> GUI_HEAT_GAUGE_HEIGHT;

            case CONFIG_BASE_EFFICIENCY -> Config.grandDistilleryEfficiency;
            case CONFIG_MAX_GRIME -> Config.grandDistilleryMaximumGrime;
            case CONFIG_GRIME_ON_SUCCESS -> Config.grandDistilleryGrimeOnSuccess;
            case CONFIG_GRIME_ON_FAILURE -> Config.grandDistilleryGrimeOnFailure;

            default -> -1;
        };
    }

    @Override
    public void destroyRouters() {
        if(getBlockState().getValue(HAS_LABORATORY_UPGRADE)) {
            ItemStack charmStack = new ItemStack(ItemRegistry.LABORATORY_CHARM.get());
            ItemEntity ie = new ItemEntity(getLevel(), getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), charmStack);
            getLevel().addFreshEntity(ie);
        }

        GrandDistilleryBlock.destroyRouters(getLevel(), getBlockPos(), getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
    }
}
