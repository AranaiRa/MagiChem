package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.foundation.DirectionalPluginBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.gui.AlchemicalNexusMenu;
import com.aranaira.magichem.recipe.AlchemicalInfusionRecipe;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.FluidRegistry;
import com.mna.items.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlchemicalNexusBlockEntity extends BlockEntity implements MenuProvider, ICanTakePlugins, IFluidHandler {

    protected ItemStackHandler itemHandler;
    protected LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    protected LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.of(() -> this);

    protected FluidStack containedSlurry = FluidStack.EMPTY;
    protected AlchemicalInfusionRecipe currentRecipe;
    protected ContainerData data;
    protected int
        progress = 0, pluginLinkageCountdown = 3;
    protected boolean isStalled = false;

    public static final int
            SLOT_COUNT = 17,
            SLOT_MARKS = 0, SLOT_PROCESSING = 1, SLOT_RECIPE = 2,
            SLOT_INPUT_START = 3, SLOT_INPUT_COUNT = 5, SLOT_OUTPUT_START = 8, SLOT_OUTPUT_COUNT = 9,
            DATA_COUNT = 1,
            DATA_PROGRESS = 0;

    ////////////////////
    // CONSTRUCTOR
    ////////////////////

    public AlchemicalNexusBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ALCHEMICAL_NEXUS_BE.get(), pPos, pBlockState);

        this.itemHandler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                if(slot == SLOT_RECIPE)
                    currentRecipe = AlchemicalInfusionRecipe.getInfusionRecipe(level, getStackInSlot(SLOT_RECIPE));
                setChanged();
                if((slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) || (slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT)) {
                    isStalled = false;
                }
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if(slot == SLOT_MARKS)
                    return stack.getItem() == ItemInit.RUNE_MARKING.get() || stack.getItem() == ItemInit.BOOK_MARKS.get();
                else if(slot == SLOT_RECIPE || slot == SLOT_PROCESSING || (slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT))
                    return false;

                return super.isItemValid(slot, stack);
            }


        };

        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch(pIndex) {
                    case DATA_PROGRESS -> progress;
                    default -> -1;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case DATA_PROGRESS -> {
                        AlchemicalNexusBlockEntity.this.progress = pValue;
                        return;
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
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        } else if(cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyFluidHandler.invalidate();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyFluidHandler = LazyOptional.of(() -> this);
        currentRecipe = AlchemicalInfusionRecipe.getInfusionRecipe(level, itemHandler.getStackInSlot(SLOT_RECIPE));
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        nbt.putInt("fluidContents", 0);
        lazyFluidHandler.ifPresent(cap -> {
            nbt.putInt("fluidContents", cap.getFluidInTank(0).getAmount());
        });
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("craftingProgress");
        int fluidContents = nbt.getInt("fluidContents");
        if(fluidContents > 0)
            containedSlurry = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), fluidContents);
        else
            containedSlurry = FluidStack.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("craftingProgress", this.progress);
        if(containedSlurry.isEmpty())
            nbt.putInt("fluidContents", 0);
        else
            nbt.putInt("fluidContents", containedSlurry.getAmount());
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.alchemical_nexus");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new AlchemicalNexusMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    ////////////////////
    // CRAFTING HANDLERS
    ////////////////////

    public static <E extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, E e) {
        if(e instanceof AlchemicalNexusBlockEntity anbe) {
            int a = 1;
            int b = 2;

            int c = a + b;
        }
    }

    public SimpleContainer getContentsOfOutputSlots() {
        SimpleContainer output = new SimpleContainer(SLOT_OUTPUT_COUNT);

        for(int i = SLOT_OUTPUT_START; i<SLOT_OUTPUT_START+SLOT_OUTPUT_COUNT; i++) {
            output.setItem(i-SLOT_OUTPUT_START, itemHandler.getStackInSlot(i).copy());
        }

        return output;
    }

    public SimpleContainer getContentsOfInputSlots() {
        SimpleContainer input = new SimpleContainer(SLOT_INPUT_COUNT);

        for(int i = SLOT_INPUT_START; i<SLOT_INPUT_START+SLOT_INPUT_COUNT; i++) {
            input.setItem(i-SLOT_INPUT_START, itemHandler.getStackInSlot(i).copy());
        }

        return input;
    }

    protected void resetProgress() {
        progress = 0;
    }

    protected void incrementProgress() {
        progress++;
    }

    ////////////////////
    // RECIPE HANDLING
    ////////////////////

    ////////////////////
    // FLUID HANDLING
    ////////////////////

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return containedSlurry;
    }

    @Override
    public int getTankCapacity(int tank) {
        return Config.alchemicalNexusTankCapacity;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack fluidAction) {
        return fluidAction.getFluid() == FluidRegistry.ACADEMIC_SLURRY.get();
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction action) {
        if(action.execute()) {
            setChanged();
            level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }

        Fluid fluid = fluidStack.getFluid();
        int incomingAmount = fluidStack.getAmount();
        if(fluid == FluidRegistry.ACADEMIC_SLURRY.get()) {
            int extantAmount = containedSlurry.getAmount();

            //Hit capacity
            if(incomingAmount + extantAmount > getTankCapacity(0)) {
                int actualTransfer = getTankCapacity(0) - extantAmount;
                if(action == FluidAction.EXECUTE)
                    this.containedSlurry = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), getTankCapacity(0));
                return actualTransfer;
            } else {
                if(action == FluidAction.EXECUTE)
                    this.containedSlurry = new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), extantAmount + incomingAmount);
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

        Fluid fluid = fluidStack.getFluid();
        int incomingAmount = fluidStack.getAmount();
        if(fluid == FluidRegistry.ACADEMIC_SLURRY.get()) {
            int extantAmount = containedSlurry.getAmount();
            if(extantAmount >= incomingAmount) {
                if(fluidAction == FluidAction.EXECUTE)
                    containedSlurry.shrink(incomingAmount);
                return new FluidStack(fluid, incomingAmount);
            } else {
                if(fluidAction == FluidAction.EXECUTE)
                    containedSlurry = FluidStack.EMPTY;
                return new FluidStack(fluid, extantAmount);
            }
        }
        return fluidStack;
    }

    @Override
    public @NotNull FluidStack drain(int i, FluidAction fluidAction) {
        return drain(new FluidStack(FluidRegistry.ACADEMIC_SLURRY.get(), i), fluidAction);
    }

    ////////////////////
    // DATA SLOT HANDLING
    ////////////////////

    ////////////////////
    // ACTUATOR HANDLING
    ////////////////////

    @Override
    public void linkPlugins() {

    }

    @Override
    public void removePlugin(DirectionalPluginBlockEntity pPlugin) {

    }

    @Override
    public void linkPluginsDeferred() {

    }
}
