package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.gui.MagicCircleMenu;
import com.aranaira.magichem.item.ModItems;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MagicCircleBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public MagicCircleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAGIC_CIRCLE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> MagicCircleBlockEntity.this.progressReagentTier1;
                    case 1 -> MagicCircleBlockEntity.this.maxProgressReagentTier1;
                    case 2 -> MagicCircleBlockEntity.this.progressReagentTier2;
                    case 3 -> MagicCircleBlockEntity.this.maxProgressReagentTier2;
                    case 4 -> MagicCircleBlockEntity.this.progressReagentTier3;
                    case 5 -> MagicCircleBlockEntity.this.maxProgressReagentTier3;
                    case 6 -> MagicCircleBlockEntity.this.progressReagentTier4;
                    case 7 -> MagicCircleBlockEntity.this.maxProgressReagentTier4;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> MagicCircleBlockEntity.this.progressReagentTier1 = value;
                    case 1 -> MagicCircleBlockEntity.this.maxProgressReagentTier1 = value;
                    case 2 -> MagicCircleBlockEntity.this.progressReagentTier2 = value;
                    case 3 -> MagicCircleBlockEntity.this.maxProgressReagentTier2 = value;
                    case 4 -> MagicCircleBlockEntity.this.progressReagentTier3 = value;
                    case 5 -> MagicCircleBlockEntity.this.maxProgressReagentTier3 = value;
                    case 6 -> MagicCircleBlockEntity.this.progressReagentTier4 = value;
                    case 7 -> MagicCircleBlockEntity.this.maxProgressReagentTier4 = value;
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
    private int progressReagentTier1 = 0;
    private int maxProgressReagentTier1 = 1280;
    private int progressReagentTier2 = 0;
    private int maxProgressReagentTier2 = 1280;
    private int progressReagentTier3 = 0;
    private int maxProgressReagentTier3 = 1280;
    private int progressReagentTier4 = 0;
    private int maxProgressReagentTier4 = 1280;

    @Override
    public Component getDisplayName() {
        //return Component.literal("Magic Circle");
        return Component.translatable("block.magichem.magic_circle");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MagicCircleMenu(id, inventory, this, this.data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
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

        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
    }

    public void dropInventoryToWorld() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i=0; i<itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MagicCircleBlockEntity entity) {
        if(level.isClientSide()) {
            return;
        }

        if(hasReagent(1, entity)) {
            entity.progressReagentTier1++;
            setChanged(level, pos, state);

            if(entity.progressReagentTier1 >= entity.maxProgressReagentTier1) {
                ejectWaste(1, level, entity);
            } else {
                entity.resetProgress(1);
                setChanged(level, pos, state);
            }
        }
    }

    /* GENERATOR REAGENT USE LOGIC */

    private static void ejectWaste(int tier, Level level, MagicCircleBlockEntity entity) {
        ItemStack wasteProduct = null;

        switch (tier) {
            case 1: {
                wasteProduct = new ItemStack(ModItems.TARNISHED_SILVER_LUMP.get(), 1);
            }
        }

        if(wasteProduct != null)
            Containers.dropItemStack(level, entity.worldPosition.getX(), entity.worldPosition.getY()+0.125, entity.worldPosition.getZ(), wasteProduct);
    }

    private void resetProgress(int tier) {
        switch (tier) {
            case 1: progressReagentTier1 = 0;
            case 2: progressReagentTier2 = 0;
            case 3: progressReagentTier3 = 0;
            case 4: progressReagentTier4 = 0;
        }
    }

    private static boolean hasReagent(int reagentTier, MagicCircleBlockEntity entity) {
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i=0; i<entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        boolean query = false;

        switch(reagentTier) {
            case 1: {
                query = entity.itemHandler.getStackInSlot(1).getItem() == ModItems.SILVER_DUST.get();
            }
        }

        return query;
    }
}
