package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.gui.DistilleryMenu;
import com.aranaira.magichem.item.ModItems;
import com.aranaira.magichem.util.ModEnergyStorage;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DistilleryBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(21) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public DistilleryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DISTILLERY.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> DistilleryBlockEntity.this.progress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> DistilleryBlockEntity.this.progress = value;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;
    private int
            progress = 0;
    private static int
            maxProgress = 60;

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
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("distillery.progress", this.progress);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("distillery.progress");
    }

    public void dropInventoryToWorld() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()+4);
        for (int i=0; i<itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DistilleryBlockEntity entity) {
        if(level.isClientSide()) {
            return;
        }

        //System.out.println("hasReagent1="+hasReagent(1, entity)+";   prog="+entity.progressReagentTier1);
    }

    private void resetProgress() {
        progress = 0;
    }

    private void incrementProgress() {
        progress++;
    }

    /* FE STUFF */
    private static final int
        ENERGY_GEN_1_REAGENT = 3,
        ENERGY_GEN_2_REAGENT = 12,
        ENERGY_GEN_3_REAGENT = 48,
        ENERGY_GEN_4_REAGENT = 200,
        ENERGY_MAX_MULTIPLIER = 3;

    private final ModEnergyStorage ENERGY_STORAGE = new ModEnergyStorage(Integer.MAX_VALUE, Integer.MAX_VALUE) {
        @Override
        public void onEnergyChanged() {
            setChanged();
        }
    };
}
