package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.DistilleryBlockEntity;
import com.aranaira.magichem.block.entity.container.BottleConsumingResultSlot;
import com.aranaira.magichem.block.entity.container.BottleStockSlot;
import com.aranaira.magichem.block.entity.container.NoMateriaInputSlot;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class DistilleryMenu extends AbstractContainerMenu {

    public final DistilleryBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public DistilleryMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(DistilleryBlockEntity.DATA_COUNT));
    }

    public DistilleryMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.DISTILLERY_MENU.get(), id);
        checkContainerSize(inv, 14);
        blockEntity = (DistilleryBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {

            //Bottle slot
            this.addSlot(new BottleStockSlot(handler, DistilleryBlockEntity.SLOT_BOTTLES, 80, -11, false));

            //Fuel slot
            this.addSlot(new SlotItemHandler(handler, DistilleryBlockEntity.SLOT_FUEL, 80, 79));

            //Input item slots
            for(int i = DistilleryBlockEntity.SLOT_INPUT_START; i< DistilleryBlockEntity.SLOT_INPUT_START + DistilleryBlockEntity.SLOT_INPUT_COUNT; i++)
            {
                this.addSlot(new NoMateriaInputSlot(handler, i, 44, -11 + (i - DistilleryBlockEntity.SLOT_INPUT_START) * 18));
            }

            //Output item slots
            for(int i = DistilleryBlockEntity.SLOT_OUTPUT_START; i< DistilleryBlockEntity.SLOT_OUTPUT_START + DistilleryBlockEntity.SLOT_OUTPUT_COUNT; i++)
            {
                int x = (i - DistilleryBlockEntity.SLOT_OUTPUT_START) % 3;
                int y = (i - DistilleryBlockEntity.SLOT_OUTPUT_START) / 3;

                this.addSlot(new BottleConsumingResultSlot(handler, i, 116 + (x) * 18, -11 + (y) * 18, DistilleryBlockEntity.SLOT_BOTTLES));
            }
        });

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.DISTILLERY.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for(int i=0; i<3; i++) {
            for(int l=0; l<9; l++) {
                this.addSlot((new Slot(playerInventory, l + i*9 + 9, 8 + l*18, 103 + i*18)));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i=0; i<9; i++) {
            this.addSlot((new Slot(playerInventory, i, 8 + i*18, 161)));
        }
    }

    private static final int SLOT_INVENTORY_BEGIN = 0;
    private static final int SLOT_INVENTORY_COUNT = 36;
    private static final int SLOT_BOTTLES = SLOT_INVENTORY_COUNT + DistilleryBlockEntity.SLOT_BOTTLES;
    private static final int SLOT_INPUT_BEGIN = SLOT_INVENTORY_COUNT + DistilleryBlockEntity.SLOT_INPUT_START;
    private static final int SLOT_OUTPUT_BEGIN = SLOT_INVENTORY_COUNT + DistilleryBlockEntity.SLOT_OUTPUT_START;

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack targetStack = slots.get(pIndex).getItem();
        ItemStack targetStackCopy = new ItemStack(slots.get(pIndex).getItem().getItem(), slots.get(pIndex).getItem().getCount());
        CompoundTag nbt = slots.get(pIndex).getItem().getTag();
        if(nbt != null) {
            targetStack.setTag(nbt);
            targetStackCopy.setTag(nbt);
        }

        //If player inventory
        if(pIndex >= SLOT_INVENTORY_BEGIN && pIndex < SLOT_INVENTORY_BEGIN + SLOT_INVENTORY_COUNT) {
            //try to move to bottle slot first
            if(targetStack.getItem() == Items.GLASS_BOTTLE) {
                ItemStack bottleStack = slots.get(SLOT_BOTTLES).getItem();
                if(bottleStack.getCount() + targetStack.getCount() > 64) {
                    int amount = 64 - bottleStack.getCount();
                    bottleStack.grow(amount);
                    targetStack.shrink(amount);
                    slots.get(pIndex).set(targetStack);
                    return ItemStack.EMPTY;
                }
                else if(bottleStack.getCount() == 0) {
                    slots.get(SLOT_BOTTLES).set(targetStackCopy);
                    slots.get(pIndex).set(ItemStack.EMPTY);
                    return ItemStack.EMPTY;
                }
                else {
                    bottleStack.grow(targetStack.getCount());
                    targetStack = ItemStack.EMPTY;
                    slots.get(pIndex).set(targetStack);
                    return targetStack;
                }
            }
            //try to move to input slots
            moveItemStackTo(targetStackCopy, SLOT_INPUT_BEGIN, SLOT_INPUT_BEGIN + DistilleryBlockEntity.SLOT_INPUT_COUNT, false);
            slots.get(pIndex).set(targetStackCopy);
            return ItemStack.EMPTY;
        }
        //If bottle slot
        if(pIndex == SLOT_BOTTLES) {
            //try to move to player inventory
            moveItemStackTo(targetStackCopy, SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_BEGIN + SLOT_INVENTORY_COUNT, false);
            slots.get(pIndex).set(targetStackCopy);
            return ItemStack.EMPTY;
        }
        //If input slots
        if(pIndex >= SLOT_INPUT_BEGIN && pIndex < SLOT_INPUT_BEGIN + DistilleryBlockEntity.SLOT_INPUT_COUNT) {
            moveItemStackTo(targetStackCopy, SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_BEGIN + SLOT_INVENTORY_COUNT, false);
            slots.get(pIndex).set(targetStackCopy);
            return ItemStack.EMPTY;
        }
        //If output slots
        if(pIndex >= SLOT_OUTPUT_BEGIN && pIndex < SLOT_OUTPUT_BEGIN + DistilleryBlockEntity.SLOT_OUTPUT_COUNT) {
            //make sure there's enough bottles first, then try to move to player inventory
            int itemsToRemove = targetStackCopy.getCount();
            int bottles = slots.get(SLOT_BOTTLES).getItem().getCount();
            if(itemsToRemove >= bottles) {
                targetStack.setCount(bottles);
                targetStackCopy.shrink(bottles);
                if(moveItemStackTo(targetStack, SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_BEGIN + SLOT_INVENTORY_COUNT, false))
                    slots.get(SLOT_BOTTLES).set(ItemStack.EMPTY);
            } else {
                if(moveItemStackTo(targetStackCopy, SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_BEGIN + SLOT_INVENTORY_COUNT, false))
                    slots.get(SLOT_BOTTLES).getItem().shrink(itemsToRemove);
            }
            slots.get(pIndex).set(targetStackCopy);
            return ItemStack.EMPTY;
        }

        return getSlot(pIndex).getItem();
    }

    public int getProgress() {
        return data.get(DistilleryBlockEntity.DATA_PROGRESS);
    }

    public int getGrime() {
        return data.get(DistilleryBlockEntity.DATA_GRIME);
    }

    public int getHeat() {
        return data.get(DistilleryBlockEntity.DATA_REMAINING_HEAT);
    }

    public int getHeatDuration() {
        return data.get(DistilleryBlockEntity.DATA_HEAT_DURATION);
    }

    public int getEfficiencyMod() {
        return data.get(DistilleryBlockEntity.DATA_EFFICIENCY_MOD);
    }

    public int getOperationTimeMod() {
        return data.get(DistilleryBlockEntity.DATA_OPERATION_TIME_MOD);
    }
}
