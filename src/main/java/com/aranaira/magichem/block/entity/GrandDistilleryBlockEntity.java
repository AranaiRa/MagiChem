package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.DistilleryBlock;
import com.aranaira.magichem.block.entity.ext.AbstractDistillationBlockEntity;
import com.aranaira.magichem.block.entity.routers.DistilleryRouterBlockEntity;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.DistilleryRouterType;
import com.aranaira.magichem.gui.GrandDistilleryMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.items.ItemInit;
import com.mna.particles.types.movers.ParticleLerpMover;
import com.mna.tools.math.Vector3;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GrandDistilleryBlockEntity extends AbstractDistillationBlockEntity implements MenuProvider, ICanTakePlugins {
    public static final int
        SLOT_COUNT = 26,
        SLOT_BOTTLES = 0, SLOT_FUEL = 1,
        SLOT_INPUT_START = 2, SLOT_INPUT_COUNT = 6,
        SLOT_OUTPUT_START = 8, SLOT_OUTPUT_COUNT  = 18,
        GUI_PROGRESS_BAR_WIDTH = 24, GUI_GRIME_BAR_WIDTH = 50, GUI_HEAT_GAUGE_HEIGHT = 16,
        DATA_COUNT = 7, DATA_PROGRESS = 0, DATA_GRIME = 1, DATA_REMAINING_HEAT = 2, DATA_HEAT_DURATION = 3, DATA_EFFICIENCY_MOD = 4, DATA_OPERATION_TIME_MOD = 5, DATA_BATCH_SIZE = 6;
    public static final float
            CIRCLE_FILL_RATE = 0.025f, PARTICLE_PERCENT_RATE = 0.05f;
    private DevicePlugDirection plugDirection = DevicePlugDirection.NONE;

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
            protected void onContentsChanged(int slot) {
                setChanged();
                if((slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) || (slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)) {
                    isStalled = false;
                }
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot == SLOT_BOTTLES)
                    return stack.getItem() == Items.GLASS_BOTTLE;
                if (slot == SLOT_FUEL) {
                    if(stack.getItem() == ItemInit.FLUID_JUG_INFINITE_LAVA.get() ||
                       stack.getItem() == ItemInit.FLUID_JUG.get())
                        return true;
                    return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
                }
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
                    case DATA_REMAINING_HEAT: {
                        return GrandDistilleryBlockEntity.this.remainingHeat;
                    }
                    case DATA_HEAT_DURATION: {
                        return GrandDistilleryBlockEntity.this.heatDuration;
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
                    case DATA_REMAINING_HEAT: {
                        GrandDistilleryBlockEntity.this.remainingHeat = pValue;
                        break;
                    }
                    case DATA_HEAT_DURATION: {
                        GrandDistilleryBlockEntity.this.heatDuration = pValue;
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
        nbt.putInt("remainingHeat", this.remainingHeat);
        nbt.putInt("heatDuration", this.heatDuration);
        nbt.putInt("batchSize", this.batchSize);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("craftingProgress");
        remainingHeat = nbt.getInt("remainingHeat");
        heatDuration = nbt.getInt("heatDuration");
        batchSize = nbt.getInt("batchSize");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("remainingHeat", this.remainingHeat);
        nbt.putInt("heatDuration", this.heatDuration);
        nbt.putInt("batchSize", this.batchSize);
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
    // INTERACTION AND VFX
    ////////////////////

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-1, 0, -1), getBlockPos().offset(1,1,1));
    }

    public void handleAnimationDrivers() {
        if(circlePercent == 1) {
            particlePercent = Math.min(1, particlePercent + PARTICLE_PERCENT_RATE);
        } else if(circlePercent == 0) {
            particlePercent = Math.max(0, particlePercent - PARTICLE_PERCENT_RATE);
        }

        //check to see if we have power
        if(true) {
            circlePercent = Math.min(1, circlePercent + CIRCLE_FILL_RATE);
        } else {
            circlePercent = Math.max(0, circlePercent - CIRCLE_FILL_RATE);
        }
    }

    ////////////////////
    // OVERRIDES
    ////////////////////

    public SimpleContainer getContentsOfOutputSlots() {
        return getContentsOfOutputSlots(GrandDistilleryBlockEntity::getVar);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, GrandDistilleryBlockEntity pEntity) {
        if(!pEntity.getLevel().isClientSide()) {
            for (DirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
                if (dpbe instanceof ActuatorFireBlockEntity fire) {
                    if (!pEntity.itemHandler.getStackInSlot(SLOT_FUEL).isEmpty()) {
                        //try to push fuel to an open slot in an inferno engine if there is one
                        pEntity.itemHandler.setStackInSlot(SLOT_FUEL,fire.tryPushFuel(pEntity.itemHandler.getStackInSlot(SLOT_FUEL)));
                    }
                }
            }
        } else {
            pEntity.handleAnimationDrivers();
        }

        if(pEntity.remainingHeat <= 0) {
            ItemStack fuelStack = pEntity.itemHandler.getStackInSlot(SLOT_FUEL);
            if(fuelStack != ItemStack.EMPTY) {
                int burnTime = ForgeHooks.getBurnTime(new ItemStack(fuelStack.getItem()), RecipeType.SMELTING);

                if(fuelStack.getItem() == ItemInit.FLUID_JUG.get()) {
                    LazyOptional<IFluidHandlerItem> cap = fuelStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
                    AtomicReference<Integer> mi = new AtomicReference<>(0);
                    cap.ifPresent(handler -> {
                        FluidStack fluidInTank = handler.getFluidInTank(0);
                        if(fluidInTank.getAmount() > 0) {
                            if (fluidInTank.getFluid() == Fluids.LAVA || fluidInTank.getFluid() == Fluids.FLOWING_LAVA) {
                                FluidStack operation = handler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                                float proportion = operation.getAmount() / 1000f;
                                int lavaBurnTime = ForgeHooks.getBurnTime(new ItemStack(Items.LAVA_BUCKET), RecipeType.SMELTING);
                                mi.set((int) (lavaBurnTime * proportion));
                            }
                        }
                    });
                    burnTime = mi.get();
                } else if(fuelStack.getItem() == ItemInit.FLUID_JUG_INFINITE_LAVA.get()) {
                    burnTime = ForgeHooks.getBurnTime(new ItemStack(Items.LAVA_BUCKET), RecipeType.SMELTING);
                } else if(fuelStack.getItem() == Items.LAVA_BUCKET) {
                    fuelStack = new ItemStack(Items.BUCKET);
                } else {
                    fuelStack.shrink(1);
                }

                pEntity.itemHandler.setStackInSlot(SLOT_FUEL, fuelStack);
                pEntity.remainingHeat = burnTime;
                pEntity.heatDuration = burnTime;
                pEntity.pushData();

                pEntity.syncAndSave();
            }
        }

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
                                .setColor(PARTICLE_COLORS[colorIndex][0], PARTICLE_COLORS[colorIndex][1], PARTICLE_COLORS[colorIndex][2]).setScale(0.4f * pEntity.particlePercent)
                                .setMaxAge(80),
                        center.x, center.y, center.z,
                        0, 0, 0);
            }
            pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                            .setColor(255, 255, 255).setScale(0.2f * pEntity.particlePercent),
                    center.x, center.y, center.z,
                    0, 0, 0);

            for (int i = 0; i < 2; i++) {
                Vector3 offset = new Vector3(r.nextFloat() - 0.5, r.nextFloat() - 0.5, r.nextFloat() - 0.5).normalize().scale(0.3f);
                pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.ARCANE_LERP.get())
                                .setColor(PARTICLE_COLORS[colorIndex][0], PARTICLE_COLORS[colorIndex][1], PARTICLE_COLORS[colorIndex][2], 72)
                                .setScale(0.09f * pEntity.particlePercent).setMaxAge(16)
                                .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                        center.x + offset.x, center.y + offset.y, center.z + offset.z,
                        0, 0, 0);

                pEntity.getLevel().addParticle(new MAParticleType(ParticleInit.SPARKLE_LERP_POINT.get())
                                .setScale(0.015f * pEntity.particlePercent).setMaxAge(16)
                                .setMover(new ParticleLerpMover(center.x + offset.x, center.y + offset.y, center.z + offset.z, center.x, center.y, center.z)),
                        center.x + offset.x, center.y + offset.y, center.z + offset.z,
                        0, 0, 0);
            }
        }

        AbstractDistillationBlockEntity.tick(pLevel, pPos, pState, pEntity, GrandDistilleryBlockEntity::getVar);
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

            case MODE_USES_RF -> 1;

            case GUI_PROGRESS_BAR_WIDTH -> GUI_PROGRESS_BAR_WIDTH;
            case GUI_GRIME_BAR_WIDTH -> GUI_GRIME_BAR_WIDTH;
            case GUI_HEAT_GAUGE_HEIGHT -> GUI_HEAT_GAUGE_HEIGHT;

            case CONFIG_BASE_EFFICIENCY -> Config.distilleryEfficiency;
            case CONFIG_MAX_GRIME -> Config.distilleryMaximumGrime;
            case CONFIG_OPERATION_TIME -> Config.distilleryOperationTime;
            case CONFIG_GRIME_ON_SUCCESS -> Config.distilleryGrimeOnSuccess;
            case CONFIG_GRIME_ON_FAILURE -> Config.distilleryGrimeOnFailure;

            default -> -1;
        };
    }
}
