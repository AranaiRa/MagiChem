package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.gui.CirclePowerMenu;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.util.IEnergyStoragePlus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CirclePowerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int
        SLOT_COUNT = 9,
        SLOT_REAGENT_1 = 0, SLOT_REAGENT_2 = 1, SLOT_REAGENT_3 = 2, SLOT_REAGENT_4 = 3, SLOT_RECHARGE = 4,
        WASTE_REAGENT_1 = 5, WASTE_REAGENT_2 = 6, WASTE_REAGENT_3 = 7, WASTE_REAGENT_4 = 8;

    private final ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch(slot) {
                case SLOT_REAGENT_1 ->
                        stack.getItem() == ItemRegistry.SILVER_DUST.get() ||
                        stack.getItem() == ItemRegistry.DEBUG_ORB.get();
                case SLOT_REAGENT_2 ->
                        stack.getItem() == ItemRegistry.FOCUSING_CATALYST.get() ||
                        stack.getItem() == ItemRegistry.DEBUG_ORB.get();
                case SLOT_REAGENT_3 ->
                        stack.getItem() == ItemRegistry.AMPLIFYING_PRISM.get() ||
                        stack.getItem() == ItemRegistry.DEBUG_ORB.get();
                case SLOT_REAGENT_4 ->
                        stack.getItem() == ItemRegistry.DEBUG_ORB.get();
                case SLOT_RECHARGE ->
                        stack.getCapability(ForgeCapabilities.ENERGY).isPresent();
                default -> false;
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
            maxProgressReagentTier3 = 3680,
            maxProgressReagentTier4 = 1280;

    public static final Item
            REAGENT_TIER1 =  ItemRegistry.SILVER_DUST.get(),
            REAGENT_TIER2 =  ItemRegistry.FOCUSING_CATALYST.get(),
            REAGENT_TIER3 =  ItemRegistry.AMPLIFYING_PRISM.get(),
            WASTE_TIER1 =  ItemRegistry.TARNISHED_SILVER_LUMP.get(),
            WASTE_TIER2 =  ItemRegistry.WARPED_FOCUSING_CATALYST.get(),
            WASTE_TIER3 =  ItemRegistry.MALFORMED_BRINDLE_GLASS.get();
    private NonNullList<ItemStack> itemsForRemoteCharging = NonNullList.create();

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.magichem.circle_power");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player) {
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
        nbt.putInt("progressReagentTier1", this.progressReagentTier1);
        nbt.putInt("progressReagentTier2", this.progressReagentTier2);
        nbt.putInt("progressReagentTier3", this.progressReagentTier3);
        nbt.putInt("progressReagentTier4", this.progressReagentTier4);
        nbt.putInt("storedEnergy", this.ENERGY_STORAGE.getEnergyStored());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        if(nbt.getCompound("inventory").getInt("Size") != itemHandler.getSlots()) {
            ItemStackHandler temp = new ItemStackHandler(nbt.getCompound("inventory").size());
            temp.deserializeNBT(nbt.getCompound("inventory"));
            for(int i=0; i<temp.getSlots(); i++) {
                itemHandler.setStackInSlot(i, temp.getStackInSlot(i));
            }
        } else {
            this.itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        }
        progressReagentTier1 = nbt.getInt("progressReagentTier1");
        progressReagentTier2 = nbt.getInt("progressReagentTier2");
        progressReagentTier3 = nbt.getInt("progressReagentTier3");
        progressReagentTier4 = nbt.getInt("progressReagentTier4");
        ENERGY_STORAGE.setEnergy(nbt.getInt("storedEnergy"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("progressReagentTier1", this.progressReagentTier1);
        nbt.putInt("progressReagentTier2", this.progressReagentTier2);
        nbt.putInt("progressReagentTier3", this.progressReagentTier3);
        nbt.putInt("progressReagentTier4", this.progressReagentTier4);
        nbt.putInt("storedEnergy", this.ENERGY_STORAGE.getEnergyStored());
        return nbt;
    }

    public void dropInventoryToWorld() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots()+4);
        for (int i=0; i<itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        //Make sure we don't void reagents entirely if the block is broken; always drop waste of a currently "burning" reagent
        if(progressReagentTier1 > 0) {
            if(itemHandler.getStackInSlot(SLOT_REAGENT_1).getItem() == ItemRegistry.DEBUG_ORB.get())
                inventory.addItem(new ItemStack(ItemRegistry.DEBUG_ORB.get(), 1));
            else
                inventory.addItem(new ItemStack(WASTE_TIER1, 1));
        }
        if(progressReagentTier2 > 0) {
            if (itemHandler.getStackInSlot(SLOT_REAGENT_2).getItem() == ItemRegistry.DEBUG_ORB.get())
                inventory.addItem(new ItemStack(ItemRegistry.DEBUG_ORB.get(), 1));
            else
                inventory.addItem(new ItemStack(WASTE_TIER2, 1));
        }
        if(progressReagentTier3 > 0) {
            if (itemHandler.getStackInSlot(SLOT_REAGENT_3).getItem() == ItemRegistry.DEBUG_ORB.get())
                inventory.addItem(new ItemStack(ItemRegistry.DEBUG_ORB.get(), 1));
            else
                inventory.addItem(new ItemStack(WASTE_TIER3, 1));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public void addRemoteChargeItem(ItemStack pStack) {
        itemsForRemoteCharging.add(pStack);
    }

    public boolean removeRemoteChargeItem(ItemStack pStack) {
        if (itemsForRemoteCharging.contains(pStack)) {
            itemsForRemoteCharging.remove(pStack);
            return true;
        }

        return false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CirclePowerBlockEntity entity) {
        if(level.isClientSide()) {
            return;
        }

        kickstart(entity);

        if(entity.ENERGY_STORAGE.getEnergyStored() < getEnergyLimit(entity)) {
            processReagent(level, pos, state, entity, 1);
            processReagent(level, pos, state, entity, 2);
            processReagent(level, pos, state, entity, 3);
            processReagent(level, pos, state, entity, 4);

            generatePower(entity);
        }

        //Charge anything in the inline charging slot
        {
            ItemStack toCharge = entity.itemHandler.getStackInSlot(SLOT_RECHARGE);
            if(!toCharge.isEmpty()) {
                LazyOptional<IEnergyStorage> energyCapability = toCharge.getCapability(ForgeCapabilities.ENERGY);

                energyCapability.ifPresent(itemCap -> {
                    int energyNeeded = itemCap.getMaxEnergyStored() - itemCap.getEnergyStored();
                    int energyExtracted = entity.ENERGY_STORAGE.extractEnergy(energyNeeded, false);
                    itemCap.receiveEnergy(energyExtracted, false);
                });
            }
        }

        //Charge anything routed to this Circle of Power from a Charging Talisman
        for(int i=entity.itemsForRemoteCharging.size()-1; i>=0; i--) {
            ItemStack toCharge = entity.itemsForRemoteCharging.get(i);
            LazyOptional<IEnergyStorage> energyCapability = toCharge.getCapability(ForgeCapabilities.ENERGY);

            energyCapability.ifPresent(itemCap -> {
                int energyNeeded = itemCap.getMaxEnergyStored() - itemCap.getEnergyStored();
                int energyExtracted = entity.ENERGY_STORAGE.extractEnergy(energyNeeded, false);
                itemCap.receiveEnergy(energyExtracted, false);
            });

            if(toCharge.isEmpty()) {
                entity.itemsForRemoteCharging.remove(i);
            }
        }
    }

    private static void kickstart(CirclePowerBlockEntity entity) {
        if(getProgressByTier(entity, 1) == 0 && entity.itemHandler.getStackInSlot(SLOT_REAGENT_1).getItem() == REAGENT_TIER1) {
            entity.itemHandler.setStackInSlot(SLOT_REAGENT_1, ItemStack.EMPTY);
            entity.incrementProgress(1);
        } else if(getProgressByTier(entity, 1) == 0 && entity.itemHandler.getStackInSlot(SLOT_REAGENT_1).getItem() == ItemRegistry.DEBUG_ORB.get()) {
            entity.incrementProgress(1);
        }

        if(getProgressByTier(entity, 2) == 0 && entity.itemHandler.getStackInSlot(SLOT_REAGENT_2).getItem() == REAGENT_TIER2) {
            entity.itemHandler.setStackInSlot(SLOT_REAGENT_2, ItemStack.EMPTY);
            entity.incrementProgress(2);
        } else if(getProgressByTier(entity, 2) == 0 && entity.itemHandler.getStackInSlot(SLOT_REAGENT_2).getItem() == ItemRegistry.DEBUG_ORB.get()) {
            entity.incrementProgress(2);
        }

        if(getProgressByTier(entity, 3) == 0 && entity.itemHandler.getStackInSlot(SLOT_REAGENT_3).getItem() == REAGENT_TIER3) {
            entity.itemHandler.setStackInSlot(SLOT_REAGENT_3, ItemStack.EMPTY);
            entity.incrementProgress(3);
        } else if(getProgressByTier(entity, 2) == 0 && entity.itemHandler.getStackInSlot(SLOT_REAGENT_3).getItem() == ItemRegistry.DEBUG_ORB.get()) {
            entity.incrementProgress(3);
        }

        if(getProgressByTier(entity, 4) == 0 && entity.itemHandler.getStackInSlot(SLOT_REAGENT_4).getItem() == ItemRegistry.DEBUG_ORB.get()) {
            entity.incrementProgress(4);
        }
    }

    private static void processReagent(Level level, BlockPos pos, BlockState state, CirclePowerBlockEntity entity, int tier) {
        if(hasReagent(tier, entity) || getProgressByTier(entity, tier) > 0) {
            entity.incrementProgress(tier);

            if(getProgressByTier(entity, tier) >= getMaxProgressByTier(tier)) {
                ejectWaste(tier, level, entity);
                entity.resetProgress(tier);
            }
            setChanged(level, pos, state);
        }
    }

    /* GENERATOR REAGENT USE LOGIC */

    private static void ejectWaste(int tier, Level level, CirclePowerBlockEntity entity) {
        if(entity.itemHandler.getStackInSlot(tier - 1).getItem() == ItemRegistry.DEBUG_ORB.get())
            return;

        ItemStack wasteInSlot = null;
        ItemStack wasteProduct = null;
        Item wasteItem = null;
        boolean eject = false;
        int slot = -1;
        int slotLimit = -1;

        if(tier == 1) {
            slot = WASTE_REAGENT_1;
            wasteItem = WASTE_TIER1;
        } else if(tier == 2) {
            slot = WASTE_REAGENT_2;
            wasteItem = WASTE_TIER2;
        } else if(tier == 3) {
            slot = WASTE_REAGENT_3;
            wasteItem = WASTE_TIER3;
        } else if(tier == 4) {
            slot = WASTE_REAGENT_4;
            wasteItem = Items.COD;
        } else {
            return;
        }
        wasteProduct = new ItemStack(wasteItem, 1);
        wasteInSlot = entity.itemHandler.getStackInSlot(slot);
        slotLimit = wasteProduct.getItem().getMaxStackSize(wasteProduct);

        if (wasteInSlot.isEmpty()) {
            entity.itemHandler.setStackInSlot(slot, wasteProduct);
            entity.syncAndSave();
        } else if (wasteInSlot.getCount() < slotLimit) {
            wasteInSlot.grow(1);
            entity.itemHandler.setStackInSlot(slot, wasteInSlot);
            entity.syncAndSave();
        } else {
            eject = true;
        }

        if(eject)
            Containers.dropItemStack(level, entity.worldPosition.getX(), entity.worldPosition.getY()+0.125, entity.worldPosition.getZ(), wasteProduct);
    }

    public void syncAndSave() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void resetProgress(int tier) {
        if(tier == 1) progressReagentTier1 =0;
        else if(tier == 2) progressReagentTier2 = 0;
        else if(tier == 3) progressReagentTier3 = 0;
        else if(tier == 4) progressReagentTier4 = 0;
    }

    private void incrementProgress(int tier) {
        if(tier == 1) progressReagentTier1++;
        else if(tier == 2) progressReagentTier2++;
        else if(tier == 3) progressReagentTier3++;
        else if(tier == 4) progressReagentTier4++;
    }

    public static int getMaxProgressByTier(int tier) {
        if(tier == 1) return  maxProgressReagentTier1;
        else if(tier == 2) return  maxProgressReagentTier2;
        else if(tier == 3) return  maxProgressReagentTier3;
        else if(tier == 4) return  maxProgressReagentTier4;
        return -1;
    }

    public static int getProgressByTier(CirclePowerBlockEntity entity, int tier) {
        if(tier == 1) return  entity.progressReagentTier1;
        else if(tier == 2) return  entity.progressReagentTier2;
        else if(tier == 3) return  entity.progressReagentTier3;
        else if(tier == 4) return  entity.progressReagentTier4;
        return -1;
    }

    public static int getEnergyLimit(CirclePowerBlockEntity entity) {
        int currentEnergy = entity.ENERGY_STORAGE.getEnergyStored();

        int reagentCount = 0;
        if(entity.progressReagentTier1 > 0) reagentCount++;
        if(entity.progressReagentTier2 > 0) reagentCount++;
        if(entity.progressReagentTier3 > 0) reagentCount++;
        if(entity.progressReagentTier4 > 0) reagentCount++;

        return getGenRate(reagentCount) * Config.circlePowerBuffer;
    }

    private static boolean hasReagent(int reagentTier, CirclePowerBlockEntity entity) {
        SimpleContainer inventory = new SimpleContainer(entity.itemHandler.getSlots());
        for (int i=0; i<entity.itemHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemHandler.getStackInSlot(i));
        }

        boolean query = false;

        //Again, switch statement fucks up here and I don't know why
        if(reagentTier == 1) {
            //Just do the thing if there's a debug orb in the slot
            if(entity.itemHandler.getStackInSlot(SLOT_REAGENT_1).getItem() == ItemRegistry.DEBUG_ORB.get())
                return true;

            query = entity.itemHandler.getStackInSlot(SLOT_REAGENT_1).getItem() == REAGENT_TIER1;
            //Otherwise, consume the reagent if we don't have an existing one "burning"
            if(query && getProgressByTier(entity, 1) == 0) {
                entity.itemHandler.setStackInSlot(SLOT_REAGENT_1, ItemStack.EMPTY);
                entity.incrementProgress(1);
            }
        }
        if(reagentTier == 2) {
            //Just do the thing if there's a debug orb in the slot
            if(entity.itemHandler.getStackInSlot(SLOT_REAGENT_2).getItem() == ItemRegistry.DEBUG_ORB.get())
                return true;

            query = entity.itemHandler.getStackInSlot(SLOT_REAGENT_2).getItem() == REAGENT_TIER2;
            //Otherwise, consume the reagent if we don't have an existing one "burning"
            if(query && getProgressByTier(entity, 2) == 0) {
                entity.itemHandler.setStackInSlot(SLOT_REAGENT_2, ItemStack.EMPTY);
                entity.incrementProgress(2);
            }
        }
        if(reagentTier == 3) {
            //Just do the thing if there's a debug orb in the slot
            if(entity.itemHandler.getStackInSlot(SLOT_REAGENT_3).getItem() == ItemRegistry.DEBUG_ORB.get())
                return true;

            query = entity.itemHandler.getStackInSlot(SLOT_REAGENT_3).getItem() == REAGENT_TIER2;
            //Otherwise, consume the reagent if we don't have an existing one "burning"
            if(query && getProgressByTier(entity, 3) == 0) {
                entity.itemHandler.setStackInSlot(SLOT_REAGENT_3, ItemStack.EMPTY);
                entity.incrementProgress(3);
            }
        }
        if(reagentTier == 4) {
            //Just do the thing if there's a debug orb in the slot
            if (entity.itemHandler.getStackInSlot(SLOT_REAGENT_4).getItem() == ItemRegistry.DEBUG_ORB.get())
                return true;
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
        int cap;
        int currentEnergy = entity.ENERGY_STORAGE.getEnergyStored();
        if(entity.progressReagentTier1 > 0) reagentCount++;
        if(entity.progressReagentTier2 > 0) reagentCount++;
        if(entity.progressReagentTier3 > 0) reagentCount++;
        if(entity.progressReagentTier4 > 0) reagentCount++;

        int genRate = getGenRate(reagentCount);

        cap = genRate * Config.circlePowerBuffer;
        entity.ENERGY_STORAGE.receiveEnergy(genRate, false);
        if (currentEnergy + genRate > cap) entity.ENERGY_STORAGE.setEnergy(cap);
    }

    private static int getGenRate(int reagentCount) {
        int genRate = 0;
        if(reagentCount == 1) genRate = Config.circlePowerGen1Reagent;
        else if(reagentCount == 2) genRate = Config.circlePowerGen2Reagent;
        else if(reagentCount == 3) genRate = Config.circlePowerGen3Reagent;
        else if(reagentCount == 4) genRate = Config.circlePowerGen4Reagent;
        return genRate;
    }
}
