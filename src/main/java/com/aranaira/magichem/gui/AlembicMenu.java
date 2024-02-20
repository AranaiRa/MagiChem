package com.aranaira.magichem.gui;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.block.entity.container.BottleStockSlot;
import com.aranaira.magichem.block.entity.container.BottleConsumingResultSlot;
import com.aranaira.magichem.block.entity.container.NoMateriaInputSlot;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.block.entity.AlembicBlockEntity;
import com.aranaira.magichem.registry.MenuRegistry;
import net.minecraft.client.Minecraft;
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
import org.jetbrains.annotations.NotNull;

public class AlembicMenu extends AbstractContainerMenu {

    public final AlembicBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public AlembicMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(AlembicBlockEntity.DATA_COUNT));
    }

    public AlembicMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.ALEMBIC_MENU.get(), id);
        checkContainerSize(inv, 14);
        blockEntity = (AlembicBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {

            //Bottle slot
            this.addSlot(new BottleStockSlot(handler, AlembicBlockEntity.SLOT_BOTTLES, 134, -5, false));

            //Input item slots
            for(int i=AlembicBlockEntity.SLOT_INPUT_START; i<AlembicBlockEntity.SLOT_INPUT_START + AlembicBlockEntity.SLOT_INPUT_COUNT; i++)
            {
                this.addSlot(new NoMateriaInputSlot(handler, i, 44, 28 + (i - AlembicBlockEntity.SLOT_INPUT_START) * 18));
            }

            //Processing slot
            this.addSlot(new NoMateriaInputSlot(handler, AlembicBlockEntity.SLOT_PROCESSING, 80, 46) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });

            //Output item slots
            for(int i=AlembicBlockEntity.SLOT_OUTPUT_START; i<AlembicBlockEntity.SLOT_OUTPUT_START + AlembicBlockEntity.SLOT_OUTPUT_COUNT; i++)
            {
                int x = (i - AlembicBlockEntity.SLOT_OUTPUT_START) % 3;
                int y = (i - AlembicBlockEntity.SLOT_OUTPUT_START) / 3;

                this.addSlot(new BottleConsumingResultSlot(handler, i, 116 + (x) * 18, 28 + (y) * 18, AlembicBlockEntity.SLOT_BOTTLES));
            }
        });

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.ALEMBIC.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for(int i=0; i<3; i++) {
            for(int l=0; l<9; l++) {
                this.addSlot((new Slot(playerInventory, l + i*9 + 9, 8 + l*18, 94 + i*18)));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i=0; i<9; i++) {
            this.addSlot((new Slot(playerInventory, i, 8 + i*18, 152)));
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
            moveItemStackTo(targetStackCopy, SLOT_INPUT_BEGIN, SLOT_INPUT_BEGIN + AlembicBlockEntity.SLOT_INPUT_COUNT, false);
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
        if(pIndex >= SLOT_INPUT_BEGIN && pIndex < SLOT_INPUT_BEGIN + AlembicBlockEntity.SLOT_INPUT_COUNT) {
            moveItemStackTo(targetStackCopy, SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_BEGIN + SLOT_INVENTORY_COUNT, false);
            slots.get(pIndex).set(targetStackCopy);
            return ItemStack.EMPTY;
        }
        //If output slots
        if(pIndex >= SLOT_OUTPUT_BEGIN && pIndex < SLOT_OUTPUT_BEGIN + AlembicBlockEntity.SLOT_OUTPUT_COUNT) {
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
        return data.get(AlembicBlockEntity.DATA_PROGRESS);
    }

    public int getGrime() {
        return data.get(AlembicBlockEntity.DATA_GRIME);
    }
}
