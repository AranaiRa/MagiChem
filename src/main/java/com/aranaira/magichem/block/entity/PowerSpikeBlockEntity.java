package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.gui.MagicCircleMenu;
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

public class PowerSpikeBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public PowerSpikeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAGIC_CIRCLE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> PowerSpikeBlockEntity.this.progressReagentTier1;
                    case 1 -> PowerSpikeBlockEntity.this.progressReagentTier2;
                    case 2 -> PowerSpikeBlockEntity.this.progressReagentTier3;
                    case 3 -> PowerSpikeBlockEntity.this.progressReagentTier4;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> PowerSpikeBlockEntity.this.progressReagentTier1 = value;
                    case 1 -> PowerSpikeBlockEntity.this.progressReagentTier2 = value;
                    case 2 -> PowerSpikeBlockEntity.this.progressReagentTier3 = value;
                    case 3 -> PowerSpikeBlockEntity.this.progressReagentTier4 = value;
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
            progressReagentTier1 = 0,
            progressReagentTier2 = 0,
            progressReagentTier3 = 0,
            progressReagentTier4 = 0;
    private static int
            maxProgressReagentTier1 = 1280,
            maxProgressReagentTier2 = 1280,
            maxProgressReagentTier3 = 1280,
            maxProgressReagentTier4 = 1280;

    public static final RegistryObject<Item>
            REAGENT_TIER1 =  ModItems.SILVER_DUST,
            WASTE_TIER1 =  ModItems.TARNISHED_SILVER_LUMP;

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
        nbt.putInt("magic_circle.progressReagentTier1", this.progressReagentTier1);
        nbt.putInt("magic_circle.progressReagentTier2", this.progressReagentTier2);
        nbt.putInt("magic_circle.progressReagentTier3", this.progressReagentTier3);
        nbt.putInt("magic_circle.progressReagentTier4", this.progressReagentTier4);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progressReagentTier1 = nbt.getInt("magic_circle.progressReagentTier1");
        progressReagentTier2 = nbt.getInt("magic_circle.progressReagentTier2");
        progressReagentTier3 = nbt.getInt("magic_circle.progressReagentTier3");
        progressReagentTier4 = nbt.getInt("magic_circle.progressReagentTier4");
    }

    public void dropInventoryToWorld() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()+4);
        for (int i=0; i<itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        //Make sure we don't void reagents entirely if the block is broken; always drop waste of a currently "burning" reagent
        if(progressReagentTier1 > 0) inventory.addItem(new ItemStack(WASTE_TIER1.get(), 1));

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PowerSpikeBlockEntity entity) {
        if(level.isClientSide()) {
            return;
        }

        if(hasReagent(1, entity) || entity.progressReagentTier1 > 0) {
            entity.incrementProgress(1);
            setChanged(level, pos, state);

            if(entity.progressReagentTier1 >= entity.maxProgressReagentTier1) {
                ejectWaste(1, level, entity);
                entity.resetProgress(1);
            }
            setChanged(level, pos, state);
        }

        //System.out.println("hasReagent1="+hasReagent(1, entity)+";   prog="+entity.progressReagentTier1);
    }

    /* GENERATOR REAGENT USE LOGIC */

    private static void ejectWaste(int tier, Level level, PowerSpikeBlockEntity entity) {
        ItemStack wasteProduct = null;

        switch (tier) {
            case 1: {
                wasteProduct = new ItemStack(WASTE_TIER1.get(), 1);
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

    private void incrementProgress(int tier) {
        switch (tier) {
            case 1: progressReagentTier1++;
            case 2: progressReagentTier2++;
            case 3: progressReagentTier3++;
            case 4: progressReagentTier4++;
        }
    }

    public static int getMaxProgressByTier(int tier) {
        int query = -1;

        switch (tier) {
            case 1: query = maxProgressReagentTier1;
            case 2: query = maxProgressReagentTier2;
            case 3: query = maxProgressReagentTier3;
            case 4: query = maxProgressReagentTier4;
        }

        return query;
    }

    private static boolean hasReagent(int reagentTier, PowerSpikeBlockEntity entity) {
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i=0; i<entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        boolean query = false;

        switch(reagentTier) {
            case 1: {
                query = entity.itemHandler.getStackInSlot(0).getItem() == REAGENT_TIER1.get();
                //Consume the reagent if we don't have an existing one "burning"
                if(query && entity.progressReagentTier1 == 0) {
                    entity.itemHandler.getStackInSlot(0).setCount(0);
                    entity.incrementProgress(1);
                }
            }
        }

        return query;
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
