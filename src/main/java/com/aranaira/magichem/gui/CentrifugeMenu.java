package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.AlembicBlockEntity;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.container.*;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

public class CentrifugeMenu extends AbstractContainerMenu {

    public final CentrifugeBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public CentrifugeMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(CentrifugeBlockEntity.DATA_COUNT));
    }

    public CentrifugeMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.CENTRIFUGE_MENU.get(), id);
        checkContainerSize(inv, CentrifugeBlockEntity.SLOT_COUNT);
        blockEntity = (CentrifugeBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {

            //Bottle slots
            this.addSlot(new BottleStockSlot(handler, CentrifugeBlockEntity.SLOT_BOTTLES, 116, -3, false));
            this.addSlot(new BottleStockSlot(handler, CentrifugeBlockEntity.SLOT_BOTTLES_OUTPUT, 62, 11, true));

            //Input item slots
            for(int i=CentrifugeBlockEntity.SLOT_INPUT_START; i<CentrifugeBlockEntity.SLOT_INPUT_START + CentrifugeBlockEntity.SLOT_INPUT_COUNT; i++)
            {
                this.addSlot(new OnlyAdmixtureInputSlot(handler, i, 26, 30 + (i - CentrifugeBlockEntity.SLOT_INPUT_START) * 18));
            }

            //Output item slots
            for(int i=CentrifugeBlockEntity.SLOT_OUTPUT_START; i<CentrifugeBlockEntity.SLOT_OUTPUT_START + CentrifugeBlockEntity.SLOT_OUTPUT_COUNT; i++)
            {
                int x = (i - CentrifugeBlockEntity.SLOT_OUTPUT_START) % 3;
                int y = (i - CentrifugeBlockEntity.SLOT_OUTPUT_START) / 3;

                this.addSlot(new BottleConsumingResultSlot(handler, i, 98 + (x) * 18, 30 + (y) * 18, CentrifugeBlockEntity.SLOT_BOTTLES));
            }
        });

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.CENTRIFUGE.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for(int i=0; i<3; i++) {
            for(int l=0; l<9; l++) {
                this.addSlot((new Slot(playerInventory, l + i*9 + 9, 8 + l*18, 96 + i*18)));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i=0; i<9; i++) {
            this.addSlot((new Slot(playerInventory, i, 8 + i*18, 154 )));
        }
    }

    private static final int SLOT_INVENTORY_BEGIN = 0;
    private static final int SLOT_INVENTORY_COUNT = 36;
    private static final int SLOT_BOTTLES = 36;
    private static final int SLOT_INPUT_BEGIN = 37;
    private static final int SLOT_PROCESSING = 40;
    private static final int SLOT_OUTPUT_BEGIN = 41;

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack targetStack = slots.get(pIndex).getItem();
        ItemStack targetStackCopy = new ItemStack(slots.get(pIndex).getItem().getItem(), slots.get(pIndex).getItem().getCount());

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
            moveItemStackTo(targetStackCopy, SLOT_INPUT_BEGIN, SLOT_INPUT_BEGIN + CentrifugeBlockEntity.SLOT_INPUT_COUNT, false);
            slots.get(pIndex).set(targetStackCopy);
            return ItemStack.EMPTY;
        }
        //If processing slot
        if(pIndex == SLOT_PROCESSING) {
            //try to move to player inventory
            moveItemStackTo(targetStackCopy, SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_BEGIN + SLOT_INVENTORY_COUNT, false);
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
        if(pIndex >= SLOT_INPUT_BEGIN && pIndex < SLOT_INPUT_BEGIN + CentrifugeBlockEntity.SLOT_INPUT_COUNT) {
            moveItemStackTo(targetStackCopy, SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_BEGIN + SLOT_INVENTORY_COUNT, false);
            slots.get(pIndex).set(targetStackCopy);
            return ItemStack.EMPTY;
        }
        //If output slots
        if(pIndex >= SLOT_OUTPUT_BEGIN && pIndex < SLOT_OUTPUT_BEGIN + CentrifugeBlockEntity.SLOT_OUTPUT_COUNT) {
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
        return data.get(CentrifugeBlockEntity.DATA_PROGRESS);
    }

    public int getGrime() {
        return data.get(CentrifugeBlockEntity.DATA_GRIME);
    }
}
