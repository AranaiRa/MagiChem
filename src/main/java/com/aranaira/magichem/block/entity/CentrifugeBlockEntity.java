package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.CentrifugeBlock;
import com.aranaira.magichem.block.DistilleryBlock;
import com.aranaira.magichem.block.entity.ext.AbstractSeparationBlockEntity;
import com.aranaira.magichem.block.entity.routers.CentrifugeRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.DistilleryRouterBlockEntity;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.foundation.enums.CentrifugeRouterType;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.DistilleryRouterType;
import com.aranaira.magichem.gui.CentrifugeMenu;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
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
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CentrifugeBlockEntity extends AbstractSeparationBlockEntity implements MenuProvider {
    public static final int
        SLOT_COUNT = 14,
        SLOT_BOTTLES = 13, SLOT_BOTTLES_OUTPUT = 0,
        SLOT_INPUT_START = 1, SLOT_INPUT_COUNT = 3,
        SLOT_OUTPUT_START = 4, SLOT_OUTPUT_COUNT  = 9,
        GRIME_BAR_WIDTH = 50, PROGRESS_BAR_WIDTH = 24,
        DATA_COUNT = 7, DATA_PROGRESS = 0, DATA_GRIME = 1, DATA_TORQUE = 2, DATA_ANIMUS = 3, DATA_EFFICIENCY_MOD = 4, DATA_OPERATION_TIME_MOD = 5, DATA_BATCH_SIZE = 6,
        NO_TORQUE_GRACE_PERIOD = 20, TORQUE_GAIN_ON_COG_ACTIVATION = 36, ANIMUS_GAIN_ON_DUSTING = 12000;
    public static final float
        WHEEL_ACCELERATION_RATE = 0.375f, WHEEL_DECELERATION_RATE = 0.625f, WHEEL_TOP_SPEED = 20.0f,
        COG_ACCELERATION_RATE = 0.5f, COG_DECELERATION_RATE = 0.375f, COG_TOP_SPEED = 10.0f;

    public float
            wheelAngle, wheelSpeed, cogAngle, cogSpeed;

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CENTRIFUGE_BE.get(), pos, state);

        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                if((slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) || (slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)) {
                    isStalled = false;
                }
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot == SLOT_BOTTLES)
                    return stack.getItem() == Items.GLASS_BOTTLE || stack.getItem() == ItemRegistry.DEBUG_ORB.get();
                if(slot == SLOT_BOTTLES_OUTPUT)
                    return false;
                if(slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT)
                    return stack.getItem() instanceof AdmixtureItem;
                if(slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)
                    return false;

                return super.isItemValid(slot, stack);
            }
        };

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                switch(pIndex) {
                    case DATA_PROGRESS: {
                        return CentrifugeBlockEntity.this.progress;
                    }
                    case DATA_GRIME: {
                        IGrimeCapability grime = GrimeProvider.getCapability(CentrifugeBlockEntity.this);
                        return grime.getGrime();
                    }
                    case DATA_TORQUE: {
                        return CentrifugeBlockEntity.this.remainingTorque;
                    }
                    case DATA_ANIMUS: {
                        return CentrifugeBlockEntity.this.remainingAnimus;
                    }
                    case DATA_EFFICIENCY_MOD: {
                        return CentrifugeBlockEntity.this.efficiencyMod;
                    }
                    case DATA_OPERATION_TIME_MOD: {
                        return Math.round(CentrifugeBlockEntity.this.operationTimeMod * 100);
                    }
                    case DATA_BATCH_SIZE: {
                        return CentrifugeBlockEntity.this.batchSize;
                    }
                    default: return -1;
                }
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch(pIndex) {
                    case DATA_PROGRESS: {
                        CentrifugeBlockEntity.this.progress = pValue;
                        break;
                    }
                    case DATA_GRIME: {
                        IGrimeCapability grime = GrimeProvider.getCapability(CentrifugeBlockEntity.this);
                        grime.setGrime(pValue);
                        break;
                    }
                    case DATA_TORQUE: {
                        remainingTorque = pValue;
                        break;
                    }
                    case DATA_ANIMUS: {
                        remainingAnimus = pValue;
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
    }

    ////////////////////
    // BOILERPLATE CODE
    ////////////////////

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.centrifuge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CentrifugeMenu(id, inventory, this, this.data);
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
        nbt.putInt("remainingTorque", this.remainingTorque);
        nbt.putInt("remainingAnimus", this.remainingAnimus);
        nbt.putInt("batchSize", this.batchSize);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("craftingProgress");
        remainingTorque = nbt.getInt("remainingTorque");
        remainingAnimus = nbt.getInt("remainingAnimus");
        batchSize = nbt.getInt("batchSize");
        updateActuatorValues(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("remainingTorque", this.remainingTorque);
        nbt.putInt("remainingAnimus", this.remainingAnimus);
        nbt.putInt("batchSize", this.batchSize);
        return nbt;
    }

    public void packInventoryToBlockItem() {
        ItemStack stack = new ItemStack(BlockRegistry.CENTRIFUGE.get());
        IGrimeCapability grimeCap = GrimeProvider.getCapability(CentrifugeBlockEntity.this);

        CompoundTag nbt = new CompoundTag();
        nbt.putInt("grime", grimeCap.getGrime());
        nbt.put("inventory", itemHandler.serializeNBT());

        stack.setTag(nbt);

        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    public void unpackInventoryFromNBT(CompoundTag pInventoryTag) {
        itemHandler.deserializeNBT(pInventoryTag);
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
        return getVar(IDs.CONFIG_MAX_GRIME);
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
        return (GRIME_BAR_WIDTH * grime) / Config.centrifugeMaximumGrime;
    }

    @Override
    protected void pushData() {
        this.data.set(DATA_PROGRESS, progress);
        this.data.set(DATA_GRIME, GrimeProvider.getCapability(this).getGrime());
        this.data.set(DATA_TORQUE, remainingTorque);
        this.data.set(DATA_ANIMUS, remainingAnimus);
        //todo: push op time mod
    }

    ////////////////////
    // OVERRIDES
    ////////////////////

    public SimpleContainer getContentsOfOutputSlots() {
        return getContentsOfOutputSlots(CentrifugeBlockEntity::getVar);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, CentrifugeBlockEntity pEntity) {
        if(pLevel.isClientSide()) {
            pEntity.handleAnimationDrivers();
        }
        AbstractSeparationBlockEntity.tick(pLevel, pPos, pState, pEntity, CentrifugeBlockEntity::getVar);
    }

    public static int getVar(IDs pID) {
        return switch(pID) {
            case SLOT_BOTTLES -> SLOT_BOTTLES;
            case SLOT_BOTTLES_OUTPUT -> SLOT_BOTTLES_OUTPUT;
            case SLOT_INPUT_START -> SLOT_INPUT_START;
            case SLOT_INPUT_COUNT -> SLOT_INPUT_COUNT;
            case SLOT_OUTPUT_START -> SLOT_OUTPUT_START;
            case SLOT_OUTPUT_COUNT -> SLOT_OUTPUT_COUNT;

            case DATA_PROGRESS -> DATA_PROGRESS;
            case DATA_GRIME -> DATA_GRIME;
            case DATA_TORQUE -> DATA_TORQUE;
            case DATA_ANIMUS -> DATA_ANIMUS;
            case DATA_EFFICIENCY_MOD -> DATA_EFFICIENCY_MOD;
            case DATA_OPERATION_TIME_MOD -> DATA_OPERATION_TIME_MOD;

            case GUI_PROGRESS_BAR_WIDTH -> PROGRESS_BAR_WIDTH;
            case GUI_GRIME_BAR_WIDTH -> GRIME_BAR_WIDTH;

            case CONFIG_BASE_EFFICIENCY -> Config.centrifugeEfficiency;
            case CONFIG_MAX_GRIME -> Config.centrifugeMaximumGrime;
            case CONFIG_GRIME_ON_SUCCESS -> Config.centrifugeGrimeOnSuccess;
            case CONFIG_GRIME_ON_FAILURE -> Config.centrifugeGrimeOnFailure;
            case CONFIG_OPERATION_TIME -> Config.centrifugeOperationTime;
            case CONFIG_TORQUE_GAIN_ON_ACTIVATION -> TORQUE_GAIN_ON_COG_ACTIVATION;
            case CONFIG_ANIMUS_GAIN_ON_DUSTING -> ANIMUS_GAIN_ON_DUSTING;
            case CONFIG_NO_TORQUE_GRACE_PERIOD -> NO_TORQUE_GRACE_PERIOD;

            default -> -1;
        };
    }

    @Override
    public void linkPlugins() {
        pluginDevices.clear();

        List<BlockEntity> query = new ArrayList<>();
        for(Triplet<BlockPos, CentrifugeRouterType, DevicePlugDirection> posAndType : CentrifugeBlock.getRouterOffsets(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))) {
            BlockEntity be = level.getBlockEntity(getBlockPos().offset(posAndType.getFirst()));
            if(be != null)
                query.add(be);
        }

        for(BlockEntity be : query) {
            if (be instanceof CentrifugeRouterBlockEntity crbe) {
                BlockEntity pe = crbe.getPlugEntity();
                if(pe instanceof DirectionalPluginBlockEntity dpbe) pluginDevices.add(dpbe);
            }
        }
    }

    ////////////////////
    // INTERACTION AND VFX
    ////////////////////

    private void handleAnimationDrivers() {
        if(remainingTorque + remainingAnimus > 0) {
            if(wheelSpeed == 0) wheelSpeed += WHEEL_ACCELERATION_RATE * 4;
            wheelSpeed = Math.min(wheelSpeed + WHEEL_ACCELERATION_RATE, WHEEL_TOP_SPEED);
            cogSpeed = Math.min(cogSpeed + COG_ACCELERATION_RATE, COG_TOP_SPEED);
        } else {
            wheelSpeed = Math.max(wheelSpeed - WHEEL_DECELERATION_RATE, 0f);
            cogSpeed = Math.max(cogSpeed - COG_DECELERATION_RATE, 0f);
        }
        wheelAngle = (wheelAngle + wheelSpeed) % 360.0f;
        cogAngle = (cogAngle + cogSpeed) % 360.0f;
    }

    public void activateCog() {
        activateCog(false);
    }

    public void activateCog(boolean isFakePlayer){
        if(remainingAnimus < TORQUE_GAIN_ON_COG_ACTIVATION) {
            int torqueMultiplier = isFakePlayer ? 3 : 1;
            remainingTorque = Math.max(remainingTorque, TORQUE_GAIN_ON_COG_ACTIVATION * torqueMultiplier);
            setChanged();
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void dustCog() {
        remainingAnimus += ANIMUS_GAIN_ON_DUSTING;
        setChanged();
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-2, 0, -2), getBlockPos().offset(2,1,2));
    }
}
