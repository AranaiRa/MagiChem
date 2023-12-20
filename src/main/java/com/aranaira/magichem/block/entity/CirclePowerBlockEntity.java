package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.gui.CirclePowerMenu;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.util.IEnergyStoragePlus;
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
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CirclePowerBlockEntity extends BlockEntity implements MenuProvider {
    public static final String REGISTRY_NAME = "circle_power";
    public static final int
        SLOT_REAGENT_1 = 0, SLOT_REAGENT_2 = 1, SLOT_REAGENT_3 = 2, SLOT_REAGENT_4 = 3;

    private final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch(slot) {
                case SLOT_REAGENT_1 -> stack.getItem() == ItemRegistry.SILVER_DUST.get();
                case SLOT_REAGENT_2 -> stack.getItem() == ItemRegistry.FOCUSING_CATALYST.get();
                case SLOT_REAGENT_3, SLOT_REAGENT_4 -> false;
                default -> super.isItemValid(slot, stack);
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public CirclePowerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CIRCLE_POWER_BE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> CirclePowerBlockEntity.this.progressReagentTier1;
                    case 1 -> CirclePowerBlockEntity.this.progressReagentTier2;
                    case 2 -> CirclePowerBlockEntity.this.progressReagentTier3;
                    case 3 -> CirclePowerBlockEntity.this.progressReagentTier4;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> CirclePowerBlockEntity.this.progressReagentTier1 = value;
                    case 1 -> CirclePowerBlockEntity.this.progressReagentTier2 = value;
                    case 2 -> CirclePowerBlockEntity.this.progressReagentTier3 = value;
                    case 3 -> CirclePowerBlockEntity.this.progressReagentTier4 = value;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();

    protected final ContainerData data;
    private int
            progressReagentTier1 = 0,
            progressReagentTier2 = 0,
            progressReagentTier3 = 0,
            progressReagentTier4 = 0;
    private static final int
            maxProgressReagentTier1 = 640,
            maxProgressReagentTier2 = 2160,
            maxProgressReagentTier3 = 1280,
            maxProgressReagentTier4 = 1280;

    public static final Item
            REAGENT_TIER1 =  ItemRegistry.SILVER_DUST.get(),
            REAGENT_TIER2 =  ItemRegistry.FOCUSING_CATALYST.get(),
            WASTE_TIER1 =  ItemRegistry.TARNISHED_SILVER_LUMP.get(),
            WASTE_TIER2 =  ItemRegistry.WARPED_FOCUSING_CATALYST.get();

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.magichem.circle_power");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CirclePowerMenu(id, inventory, this, this.data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyHandler.cast();
        }

        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyEnergyHandler = LazyOptional.of(() -> ENERGY_STORAGE);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyEnergyHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt(REGISTRY_NAME+".progressReagentTier1", this.progressReagentTier1);
        nbt.putInt(REGISTRY_NAME+".progressReagentTier2", this.progressReagentTier2);
        nbt.putInt(REGISTRY_NAME+".progressReagentTier3", this.progressReagentTier3);
        nbt.putInt(REGISTRY_NAME+".progressReagentTier4", this.progressReagentTier4);
        nbt.putInt(REGISTRY_NAME+".energy", this.ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progressReagentTier1 = nbt.getInt(REGISTRY_NAME+".progressReagentTier1");
        progressReagentTier2 = nbt.getInt(REGISTRY_NAME+".progressReagentTier2");
        progressReagentTier3 = nbt.getInt(REGISTRY_NAME+".progressReagentTier3");
        progressReagentTier4 = nbt.getInt(REGISTRY_NAME+".progressReagentTier4");
        ENERGY_STORAGE.setEnergy(nbt.getInt(REGISTRY_NAME+".energy"));
    }

    public void dropInventoryToWorld() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()+4);
        for (int i=0; i<itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        //Make sure we don't void reagents entirely if the block is broken; always drop waste of a currently "burning" reagent
        if(progressReagentTier1 > 0) inventory.addItem(new ItemStack(WASTE_TIER1, 1));
        if(progressReagentTier2 > 0) inventory.addItem(new ItemStack(WASTE_TIER2, 1));

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CirclePowerBlockEntity entity) {
        if(level.isClientSide()) {
            return;
        }

        for(int tier=1; tier<=2; tier++) {
            processReagent(level, pos, state, entity, tier);
        }

        generatePower(entity);
    }

    private static void processReagent(Level level, BlockPos pos, BlockState state, CirclePowerBlockEntity entity, int tier) {
        if(hasReagent(tier, entity) || getProgressByTier(entity, tier) > 0) {
            entity.incrementProgress(1);
            setChanged(level, pos, state);

            if(getProgressByTier(entity, tier) >= getMaxProgressByTier(tier)) {
                ejectWaste(1, level, entity);
                entity.resetProgress(1);
            }
            setChanged(level, pos, state);
        }
    }

    /* GENERATOR REAGENT USE LOGIC */

    private static void ejectWaste(int tier, Level level, CirclePowerBlockEntity entity) {
        ItemStack wasteProduct = null;

        switch (tier) {
            case 1: {
                wasteProduct = new ItemStack(WASTE_TIER1, 1);
            }
            case 2: {
                wasteProduct = new ItemStack(WASTE_TIER2, 1);
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
        if(tier == 1) progressReagentTier1++;
        if(tier == 2) progressReagentTier2++;
        if(tier == 3) progressReagentTier3++;
        if(tier == 4) progressReagentTier4++;
    }

    public static int getMaxProgressByTier(int tier) {
        switch (tier) {
            case 1: return maxProgressReagentTier1;
            case 2: return maxProgressReagentTier2;
            case 3: return maxProgressReagentTier3;
            case 4: return maxProgressReagentTier4;
            default: return -1;
        }
    }

    public static int getProgressByTier(CirclePowerBlockEntity entity, int tier) {
        switch (tier) {
            case 1: return entity.progressReagentTier1;
            case 2: return entity.progressReagentTier2;
            case 3: return entity.progressReagentTier3;
            case 4: return entity.progressReagentTier4;
            default: return -1;
        }
    }

    private static boolean hasReagent(int reagentTier, CirclePowerBlockEntity entity) {
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i=0; i<entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        boolean query = false;

        switch(reagentTier) {
            case 1: {
                query = entity.itemHandler.getStackInSlot(SLOT_REAGENT_1).getItem() == REAGENT_TIER1;
                //Consume the reagent if we don't have an existing one "burning"
                if(query && getProgressByTier(entity, 1) == 0) {
                    entity.itemHandler.getStackInSlot(SLOT_REAGENT_1).setCount(0);
                    entity.incrementProgress(1);
                }
            }
            case 2: {
                query = entity.itemHandler.getStackInSlot(SLOT_REAGENT_2).getItem() == REAGENT_TIER2;
                //Consume the reagent if we don't have an existing one "burning"
                if(query && getProgressByTier(entity, 2) == 0) {
                    entity.itemHandler.getStackInSlot(SLOT_REAGENT_2).setCount(0);
                    entity.incrementProgress(2);
                }
            }
        }

        return query;
    }

    /* FE STUFF */

    private final IEnergyStoragePlus ENERGY_STORAGE = new IEnergyStoragePlus(Integer.MAX_VALUE, Integer.MAX_VALUE) {
        @Override
        public void onEnergyChanged() {
            setChanged();
        }
    };

    private static void generatePower(CirclePowerBlockEntity entity) {
        int reagentCount = 0;
        int currentEnergy = entity.ENERGY_STORAGE.getEnergyStored();
        if(entity.progressReagentTier1 > 0) reagentCount++;
        if(entity.progressReagentTier2 > 0) reagentCount++;
        if(entity.progressReagentTier3 > 0) reagentCount++;
        if(entity.progressReagentTier4 > 0) reagentCount++;

        switch(reagentCount) {
            case 1: {
                int cap = Config.circlePowerGen1Reagent * Config.circlePowerBuffer;
                if (currentEnergy < cap) {
                    int mod = currentEnergy + Config.circlePowerGen1Reagent;
                    if (currentEnergy > cap) mod = cap - currentEnergy;
                    int insert = entity.ENERGY_STORAGE.receiveEnergy(mod, false);
                }
            }
            case 2: {
                int cap = Config.circlePowerGen2Reagent * Config.circlePowerBuffer;
                if(currentEnergy < cap) {
                    int mod = currentEnergy + Config.circlePowerGen2Reagent;
                    if(currentEnergy > cap) mod = cap - currentEnergy;
                    int insert = entity.ENERGY_STORAGE.receiveEnergy(mod,false);
                }
            }
        }
    }
}
