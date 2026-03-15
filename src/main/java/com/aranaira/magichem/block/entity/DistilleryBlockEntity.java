package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.FuseryBlock;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.block.DistilleryBlock;
import com.aranaira.magichem.block.entity.ext.AbstractDistillationBlockEntity;
import com.aranaira.magichem.block.entity.routers.DistilleryRouterBlockEntity;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.capabilities.grime.IGrimeCapability;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.*;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.DistilleryRouterType;
import com.aranaira.magichem.foundation.enums.FuseryRouterType;
import com.aranaira.magichem.gui.DistilleryMenu;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.items.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DistilleryBlockEntity extends AbstractDistillationBlockEntity implements MenuProvider, ICanTakePlugins, IRequiresRouterCleanupOnDestruction, IMateriaSortingRequester, IKeepsInventoryOnBreak {
    public static final int
        SLOT_COUNT = 26,
        SLOT_BOTTLES = 0, SLOT_FUEL = 1,
        SLOT_INPUT_START = 2, SLOT_INPUT_COUNT = 6,
        SLOT_OUTPUT_START = 8, SLOT_OUTPUT_COUNT  = 18,
        GUI_PROGRESS_BAR_WIDTH = 24, GUI_GRIME_BAR_WIDTH = 50, GUI_HEAT_GAUGE_HEIGHT = 16,
        DATA_COUNT = 7, DATA_PROGRESS = 0, DATA_GRIME = 1, DATA_REMAINING_HEAT = 2, DATA_HEAT_DURATION = 3, DATA_EFFICIENCY_MOD = 4, DATA_OPERATION_TIME_MOD = 5, DATA_BATCH_SIZE = 6;
    private DevicePlugDirection plugDirection = DevicePlugDirection.NONE;
    private int analogSignalLastTick = 0;

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    public DistilleryBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.DISTILLERY_BE.get(), pos, state);

        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
                if(slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT) {
                    ItemStack bottleStack = getStackInSlot(SLOT_BOTTLES);
                    boolean isOrb = bottleStack.getItem() == ItemRegistry.DEBUG_ORB.get();

                    int bottleLimit =
                            bottleStack.isEmpty() ? 0 :
                                    bottleStack.getItem() == ItemRegistry.DEBUG_ORB.get() ? Integer.MAX_VALUE : bottleStack.getCount();

                    if (bottleLimit == 0) return ItemStack.EMPTY.copy();

                    ItemStack item = super.extractItem(slot, Math.min(amount, bottleLimit), simulate);
                    item.removeTagKey("CustomModelData");

                    if (!simulate && !isOrb) bottleStack.shrink(Math.min(amount, bottleLimit));

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
                        return DistilleryBlockEntity.this.progress;
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
                    case DATA_BATCH_SIZE: {
                        return DistilleryBlockEntity.this.batchSize;
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
        linkPlugins();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("remainingHeat", this.remainingHeat);
        nbt.putInt("heatDuration", this.heatDuration);
        nbt.putInt("batchSize", this.batchSize);
        nbt.putLong("grime", GrimeProvider.getCapability(this).getGrime());
        if(!inputTank.isEmpty()) {
            CompoundTag inputTankTag = new CompoundTag();
            inputTankTag.putString("fluid", ForgeRegistries.FLUIDS.getKey(inputTank.getFluid()).toString());
            inputTankTag.putInt("amount",inputTank.getAmount());
            nbt.put("inputTank",inputTankTag);
        }
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        unpackDataFromNBT(nbt);
        progress = nbt.getInt("craftingProgress");
        remainingHeat = nbt.getInt("remainingHeat");
        heatDuration = nbt.getInt("heatDuration");
        batchSize = nbt.getInt("batchSize");
        if(nbt.contains("inputTank")) {
            CompoundTag inputTankTag = nbt.getCompound("inputTank");
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(inputTankTag.getString("fluid")));
            if(fluid != null)
                inputTank = new FluidStack(fluid, inputTankTag.getInt("amount"));
        } else {
            inputTank = FluidStack.EMPTY;
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("remainingHeat", this.remainingHeat);
        nbt.putInt("heatDuration", this.heatDuration);
        nbt.putInt("batchSize", this.batchSize);
        nbt.putLong("grime", GrimeProvider.getCapability(this).getGrime());
        if(!inputTank.isEmpty()) {
            CompoundTag inputTankTag = new CompoundTag();
            inputTankTag.putString("fluid",ForgeRegistries.FLUIDS.getKey(inputTank.getFluid()).toString());
            inputTankTag.putInt("amount",inputTank.getAmount());
            nbt.put("inputTank",inputTankTag);
        }
        return nbt;
    }

    @Override
    public void packDataToBlockItem() {
        ItemStack stack = new ItemStack(BlockRegistry.DISTILLERY.get());
        IGrimeCapability grimeCap = GrimeProvider.getCapability(DistilleryBlockEntity.this);

        CompoundTag nbt = new CompoundTag();
        nbt.putInt("grime", grimeCap.getGrime());
        nbt.put("inventory", itemHandler.serializeNBT());
        if(!inputTank.isEmpty()) {
            CompoundTag inputTankTag = new CompoundTag();
            inputTankTag.putString("fluid",ForgeRegistries.FLUIDS.getKey(inputTank.getFluid()).toString());
            inputTankTag.putInt("amount",inputTank.getAmount());
            nbt.put("inputTank",inputTankTag);
        }

        stack.setTag(nbt);

        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    @Override
    public void unpackDataFromNBT(CompoundTag pNBT) {
        if(pNBT.contains("inventory")) {
            CompoundTag inventoryTag = pNBT.getCompound("inventory");
            int size = inventoryTag.getInt("Size");
            if(size == SLOT_COUNT) {
                itemHandler.deserializeNBT(inventoryTag);
                if(inventoryTag.contains("inputTank")) {
                    CompoundTag inputTankTag = inventoryTag.getCompound("inputTank");
                    Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(inputTankTag.getString("fluid")));
                    if(fluid != null)
                        inputTank = new FluidStack(fluid, inputTankTag.getInt("amount"));
                } else {
                    inputTank = FluidStack.EMPTY;
                }
            } else if(getLevel() != null && getLevel().isClientSide()) {
                final LocalPlayer player = Minecraft.getInstance().player;
                if(player != null) {
                    MutableComponent msg = Component.translatable("feedback.warning.inventory_size_mismatch.part1")
                            .append(Component.translatable("block.magichem.distillery").withStyle(ChatFormatting.GOLD))
                            .append(Component.translatable("feedback.warning.inventory_size_mismatch.part2"));
                    player.displayClientMessage(msg, false);
                }
            }
        }
        if (pNBT.contains("grime")) {
            GrimeProvider.getCapability(this).setGrime(pNBT.getInt("grime"));
        }
        if(pNBT.contains("inputTank")) {
            CompoundTag inputTankTag = pNBT.getCompound("inputTank");
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(inputTankTag.getString("fluid")));
            if(fluid != null)
                inputTank = new FluidStack(fluid, inputTankTag.getInt("amount"));
        }
    }

    ////////////////////
    // FLUID HANDLING
    ////////////////////

    @Override
    public int getTankCapacity(int tank) {
        return ServerConfig.distilleryTankCapacity;
    }

    ////////////////////
    // DATA SLOT HANDLING
    ////////////////////

    @Override
    public int getMaximumGrime() {
        return ServerConfig.distilleryMaximumGrime;
    }

    public int getHeatFromData() {
        return data.get(DATA_REMAINING_HEAT);
    }

    @Override
    public int clean() {
        int grimeDetected = GrimeProvider.getCapability(this).getGrime();
        IGrimeCapability grimeCapability = GrimeProvider.getCapability(this);
        grimeCapability.setGrime(0);
        syncAndSave();
        return grimeDetected / ServerConfig.grimePerWaste;
    }

    public static int getScaledGrime(int grime) {
        return (GUI_GRIME_BAR_WIDTH * grime) / ServerConfig.distilleryMaximumGrime;
    }

    @Override
    protected void pushData() {
        this.data.set(DATA_PROGRESS, progress);
        this.data.set(DATA_REMAINING_HEAT, remainingHeat);
        this.data.set(DATA_HEAT_DURATION, heatDuration);
        //TODO: push op time mod
    }

    public boolean hasFuelInSlot() {
        return !itemHandler.getStackInSlot(SLOT_FUEL).isEmpty();
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
        if(getPlugEntity() instanceof AbstractDirectionalPluginBlockEntity dpbe) {
            if(dpbe.getTargetMachine() == this) pluginDevices.add(dpbe);
        }

        List<BlockEntity> query = new ArrayList<>();
        for(Triplet<BlockPos, DistilleryRouterType, DevicePlugDirection> posAndType : DistilleryBlock.getRouterOffsets(getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING))) {
            BlockEntity be = level.getBlockEntity(getBlockPos().offset(posAndType.getFirst()));
            if(be != null)
                query.add(be);
        }

        for(BlockEntity be : query) {
            if (be instanceof DistilleryRouterBlockEntity drbe) {
                BlockEntity pe = drbe.getPlugEntity();
                if(pe instanceof AbstractDirectionalPluginBlockEntity dpbe) {
                    final ICanTakePlugins targetMachine = dpbe.getTargetMachine();
                    if(targetMachine instanceof DistilleryRouterBlockEntity router) {
                        DistilleryBlockEntity master = router.getMaster();
                        if(master == this) pluginDevices.add(dpbe);
                    } else if (targetMachine == this) {
                        pluginDevices.add(dpbe);
                    }
                }
            }
        }
    }

    @Override
    public List<AbstractDirectionalPluginBlockEntity> getPlugins() {
        return pluginDevices;
    }

    ////////////////////
    // INTERACTION AND VFX
    ////////////////////

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-1, 0, -1), getBlockPos().offset(1,1,1));
    }

    ////////////////////
    // OVERRIDES
    ////////////////////

    public SimpleContainer getContentsOfOutputSlots() {
        return getContentsOfOutputSlots(DistilleryBlockEntity::getVar);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, DistilleryBlockEntity pEntity) {
        if(!pEntity.getLevel().isClientSide()) {
            for (AbstractDirectionalPluginBlockEntity dpbe : pEntity.pluginDevices) {
                if (dpbe instanceof ActuatorFireBlockEntity fire) {
                    if (!pEntity.itemHandler.getStackInSlot(SLOT_FUEL).isEmpty()) {
                        //try to push fuel to an open slot in an inferno engine if there is one
                        pEntity.itemHandler.setStackInSlot(SLOT_FUEL,fire.tryPushFuel(pEntity.itemHandler.getStackInSlot(SLOT_FUEL)));
                    }
                    if(pEntity.remainingHeat == 0 && dpbe.getIsSatisfied() && !dpbe.getPaused()) {
                        pEntity.remainingHeat = 1000;
                        pEntity.heatDuration = 1000;
                        pEntity.syncAndSave();
                    }
                }
            }
        }

        if(pEntity.remainingHeat <= 0) {
            ItemStack fuelStack = pEntity.itemHandler.getStackInSlot(SLOT_FUEL);
            if(!fuelStack.isEmpty()) {
                int burnTime = ForgeHooks.getBurnTime(new ItemStack(fuelStack.getItem()), RecipeType.SMELTING);

                if(burnTime > 0){
                    if (fuelStack.getItem() == ItemInit.FLUID_JUG.get()) {
                        LazyOptional<IFluidHandlerItem> cap = fuelStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
                        AtomicReference<Integer> mi = new AtomicReference<>(0);
                        cap.ifPresent(handler -> {
                            FluidStack fluidInTank = handler.getFluidInTank(0);
                            if (fluidInTank.getAmount() > 0) {
                                if (fluidInTank.getFluid() == Fluids.LAVA || fluidInTank.getFluid() == Fluids.FLOWING_LAVA) {
                                    FluidStack operation = handler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                                    float proportion = operation.getAmount() / 1000f;
                                    int lavaBurnTime = ForgeHooks.getBurnTime(new ItemStack(Items.LAVA_BUCKET), RecipeType.SMELTING);
                                    mi.set((int) (lavaBurnTime * proportion));
                                }
                            }
                        });
                        burnTime = mi.get();
                    } else if (fuelStack.getItem() == ItemInit.FLUID_JUG_INFINITE_LAVA.get()) {
                        burnTime = ForgeHooks.getBurnTime(new ItemStack(Items.LAVA_BUCKET), RecipeType.SMELTING);
                    } else {
                        ItemStack remainder = fuelStack.getCraftingRemainingItem();
                        if (!remainder.isEmpty()) {
                            fuelStack = remainder.copy();
                        } else
                            fuelStack.shrink(1);
                    }

                    pEntity.itemHandler.setStackInSlot(SLOT_FUEL, fuelStack);
                    pEntity.remainingHeat = burnTime;
                    pEntity.heatDuration = burnTime;
                    pEntity.pushData();

                    pEntity.syncAndSave();
                }
            }
        }

        if(!pLevel.isClientSide()) {
            int analogSignalThisTick = pState.getBlock().getAnalogOutputSignal(pState, pLevel, pPos);
            if(analogSignalThisTick != pEntity.analogSignalLastTick) {
                pEntity.setChanged();
                for (Triplet<BlockPos, DistilleryRouterType, DevicePlugDirection> offset : DistilleryBlock.getRouterOffsets(pState.getValue(MagiChemBlockStateProperties.FACING))) {
                    BlockEntity be = pLevel.getBlockEntity(pPos.offset(offset.getFirst()));
                    if(be != null) be.setChanged();
                }
            }
            pEntity.analogSignalLastTick = analogSignalThisTick;
        }

        AbstractDistillationBlockEntity.tick(pLevel, pPos, pState, pEntity, DistilleryBlockEntity::getVar, pEntity::getPoweredOperationTime);
    }

    @Override
    public boolean needsSorting() {
        boolean materiaInOutput = false;
        for(int i=SLOT_OUTPUT_START; i<SLOT_OUTPUT_START+SLOT_OUTPUT_COUNT; i++) {
            if(!itemHandler.getStackInSlot(i).isEmpty()) {
                materiaInOutput = true;
                break;
            }
        }
        return materiaInOutput;
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

            case MODE_USES_RF -> 0;

            case GUI_PROGRESS_BAR_WIDTH -> GUI_PROGRESS_BAR_WIDTH;
            case GUI_GRIME_BAR_WIDTH -> GUI_GRIME_BAR_WIDTH;
            case GUI_HEAT_GAUGE_HEIGHT -> GUI_HEAT_GAUGE_HEIGHT;

            case CONFIG_BASE_EFFICIENCY -> ServerConfig.distilleryEfficiency;
            case CONFIG_MAX_GRIME -> ServerConfig.distilleryMaximumGrime;
            case CONFIG_OPERATION_TIME -> ServerConfig.distilleryOperationTime;
            case CONFIG_GRIME_ON_SUCCESS -> ServerConfig.distilleryGrimeOnSuccess;
            case CONFIG_GRIME_ON_FAILURE -> ServerConfig.distilleryGrimeOnFailure;

            default -> -1;
        };
    }

    @Override
    public void destroyRouters() {
        DistilleryBlock.destroyRouters(getLevel(), getBlockPos(), getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
    }
}
