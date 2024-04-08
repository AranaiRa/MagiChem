package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.DistilleryBlock;
import com.aranaira.magichem.block.entity.ext.AbstractDistillationBlockEntity;
import com.aranaira.magichem.block.entity.routers.CentrifugeRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.DistilleryRouterBlockEntity;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.foundation.enums.CentrifugeRouterType;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.DistilleryRouterType;
import com.aranaira.magichem.gui.DistilleryMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DistilleryBlockEntity extends AbstractDistillationBlockEntity implements MenuProvider, ICanTakePlugins {
    public static final int
        SLOT_COUNT = 26,
        SLOT_BOTTLES = 0, SLOT_FUEL = 1,
        SLOT_INPUT_START = 2, SLOT_INPUT_COUNT = 6,
        SLOT_OUTPUT_START = 8, SLOT_OUTPUT_COUNT  = 18,
        GUI_PROGRESS_BAR_WIDTH = 24, GUI_GRIME_BAR_WIDTH = 50, GUI_HEAT_GAUGE_HEIGHT = 16,
        DATA_COUNT = 6, DATA_PROGRESS = 0, DATA_GRIME = 1, DATA_REMAINING_HEAT = 2, DATA_HEAT_DURATION = 3, DATA_EFFICIENCY_MOD = 4, DATA_OPERATION_TIME_MOD = 5;
    private DevicePlugDirection plugDirection = DevicePlugDirection.NONE;

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    public DistilleryBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.DISTILLERY_BE.get(), pos, state);

        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == SLOT_BOTTLES)
                    return stack.getItem() == Items.GLASS_BOTTLE;
                if (slot == SLOT_FUEL)
                    return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
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
                        return DistilleryBlockEntity.this.progress;
                    }
                    case DATA_GRIME: {
                        IGrimeCapability grime = GrimeProvider.getCapability(DistilleryBlockEntity.this);
                        return grime.getGrime();
                    }
                    case DATA_REMAINING_HEAT: {
                        return DistilleryBlockEntity.this.remainingHeat;
                    }
                    case DATA_HEAT_DURATION: {
                        return DistilleryBlockEntity.this.heatDuration;
                    }
                    case DATA_EFFICIENCY_MOD: {
                        return DistilleryBlockEntity.this.efficiencyMod;
                    }
                    case DATA_OPERATION_TIME_MOD: {
                        return Math.round(DistilleryBlockEntity.this.operationTimeMod * 100);
                    }
                    default: return -1;
                }
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch(pIndex) {
                    case DATA_PROGRESS: {
                        DistilleryBlockEntity.this.progress = pValue;
                        break;
                    }
                    case DATA_GRIME: {
                        IGrimeCapability grime = GrimeProvider.getCapability(DistilleryBlockEntity.this);
                        grime.setGrime(pValue);
                        break;
                    }
                    case DATA_REMAINING_HEAT: {
                        DistilleryBlockEntity.this.remainingHeat = pValue;
                        break;
                    }
                    case DATA_HEAT_DURATION: {
                        DistilleryBlockEntity.this.heatDuration = pValue;
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
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        switch(facing) {
            case NORTH -> plugDirection = DevicePlugDirection.EAST;
            case EAST -> plugDirection = DevicePlugDirection.SOUTH;
            case SOUTH -> plugDirection = DevicePlugDirection.WEST;
            case WEST -> plugDirection = DevicePlugDirection.NORTH;
        }
    }

    //////////
    // BOILERPLATE CODE
    //////////

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.distillery");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new DistilleryMenu(id, inventory, this, this.data);
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
        nbt.putInt("remainingHeat", this.remainingHeat);
        nbt.putInt("heatDuration", this.heatDuration);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("craftingProgress");
        remainingHeat = nbt.getInt("remainingHeat");
        heatDuration = nbt.getInt("heatDuration");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("remainingHeat", this.remainingHeat);
        nbt.putInt("heatDuration", this.heatDuration);
        return nbt;
    }

    public void dropInventoryToWorld() {
        //Drop items in input slots, bottle slot, and processing slot as-is
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()+4);
        for (int i = 0; i < SLOT_INPUT_COUNT + 1; i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);


        //Convert items in the output slots to alchemical waste
        SimpleContainer waste = new SimpleContainer(itemHandler.getSlots()+4);
        for (int i = 0; i < SLOT_OUTPUT_COUNT; i++) {
            ItemStack stack = itemHandler.getStackInSlot(SLOT_INPUT_START + i);
            waste.setItem(i, new ItemStack(ItemRegistry.ALCHEMICAL_WASTE.get(), stack.getCount()));
        }

        Containers.dropContents(this.level, this.worldPosition, waste);
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
        return Config.distilleryMaximumGrime;
    }

    public int getHeatFromData() {
        return data.get(DATA_REMAINING_HEAT);
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
        return (GUI_GRIME_BAR_WIDTH * grime) / Config.distilleryMaximumGrime;
    }

    @Override
    protected void pushData() {
        this.data.set(DATA_PROGRESS, progress);
        this.data.set(DATA_GRIME, GrimeProvider.getCapability(this).getGrime());
        this.data.set(DATA_REMAINING_HEAT, remainingHeat);
        this.data.set(DATA_HEAT_DURATION, heatDuration);
        //TODO: push op time mod
    }

    ////////////////////
    // ACTUATOR HANDLERS
    ////////////////////

    public DevicePlugDirection getPlugDirection() {
        return this.plugDirection;
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
        if(getPlugEntity() instanceof DirectionalPluginBlockEntity dpbe)
            pluginDevices.add(dpbe);

        List<BlockEntity> query = new ArrayList<>();
        for(Triplet<BlockPos, DistilleryRouterType, DevicePlugDirection> posAndType : DistilleryBlock.getRouterOffsets(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))) {
            BlockEntity be = level.getBlockEntity(getBlockPos().offset(posAndType.getFirst()));
            if(be != null)
                query.add(be);
        }

        for(BlockEntity be : query) {
            if (be instanceof DistilleryRouterBlockEntity crbe) {
                BlockEntity pe = crbe.getPlugEntity();
                if(pe instanceof DirectionalPluginBlockEntity dpbe) pluginDevices.add(dpbe);
            }
        }
    }

    ////////////////////
    // OVERRIDES
    ////////////////////

    public SimpleContainer getContentsOfOutputSlots() {
        return getContentsOfOutputSlots(DistilleryBlockEntity::getVar);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, DistilleryBlockEntity pEntity) {
        if(pEntity.remainingHeat <= 0) {
            ItemStack fuelStack = pEntity.itemHandler.getStackInSlot(SLOT_FUEL);
            if(fuelStack != ItemStack.EMPTY) {
                int burnTime = ForgeHooks.getBurnTime(new ItemStack(fuelStack.getItem()), RecipeType.SMELTING);
                pEntity.remainingHeat = burnTime;
                pEntity.heatDuration = burnTime;
                pEntity.pushData();

                fuelStack.shrink(1);
                pEntity.itemHandler.setStackInSlot(SLOT_FUEL, fuelStack);

                pEntity.syncAndSave();
            }
        }

        AbstractDistillationBlockEntity.tick(pLevel, pPos, pState, pEntity, DistilleryBlockEntity::getVar);
    }

    public static int getVar(IDs pID) {
        return switch(pID) {
            case SLOT_BOTTLES -> SLOT_BOTTLES;
            case SLOT_FUEL -> SLOT_FUEL;
            case SLOT_INPUT_START -> SLOT_INPUT_START;
            case SLOT_INPUT_COUNT -> SLOT_INPUT_COUNT;
            case SLOT_OUTPUT_START -> SLOT_OUTPUT_START;
            case SLOT_OUTPUT_COUNT -> SLOT_OUTPUT_COUNT;

            case DATA_PROGRESS -> DATA_PROGRESS;
            case DATA_GRIME -> DATA_GRIME;
            case DATA_REMAINING_HEAT -> DATA_REMAINING_HEAT;
            case DATA_HEAT_DURATION -> DATA_HEAT_DURATION;
            case DATA_EFFICIENCY_MOD -> DATA_EFFICIENCY_MOD;
            case DATA_OPERATION_TIME_MOD -> DATA_OPERATION_TIME_MOD;

            case GUI_PROGRESS_BAR_WIDTH -> GUI_PROGRESS_BAR_WIDTH;
            case GUI_GRIME_BAR_WIDTH -> GUI_GRIME_BAR_WIDTH;
            case GUI_HEAT_GAUGE_HEIGHT -> GUI_HEAT_GAUGE_HEIGHT;

            case CONFIG_BASE_EFFICIENCY -> Config.distilleryEfficiency;
            case CONFIG_MAX_GRIME -> Config.distilleryMaximumGrime;
            case CONFIG_OPERATION_TIME -> Config.distilleryOperationTime;

            default -> -1;
        };
    }
}
