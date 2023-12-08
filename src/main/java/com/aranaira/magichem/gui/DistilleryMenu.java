package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.ModBlocks;
import com.aranaira.magichem.block.entity.DistilleryBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class DistilleryMenu extends AbstractContainerMenu {
    public final DistilleryBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public DistilleryMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level.getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(21));
    }

    public DistilleryMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.DISTILLERY_MENU.get(), id);
        checkContainerSize(inv, 21);
        blockEntity = (DistilleryBlockEntity) entity;
        this.level = inv.player.level;
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            //Input item slots
            this.addSlot(new SlotItemHandler(handler, 0, 44, -5));
            this.addSlot(new SlotItemHandler(handler, 1, 44, 13));
            this.addSlot(new SlotItemHandler(handler, 2, 44, 31));
            this.addSlot(new SlotItemHandler(handler, 3, 44, 49));
            this.addSlot(new SlotItemHandler(handler, 4, 44, 67));

            //Processing slot
            this.addSlot(new SlotItemHandler(handler, 5, 80, 31));

            //Output item slots
            this.addSlot(new SlotItemHandler(handler, 6, 116, -5));
            this.addSlot(new SlotItemHandler(handler, 7, 134, -5));
            this.addSlot(new SlotItemHandler(handler, 8, 152, -5));
            this.addSlot(new SlotItemHandler(handler, 9, 116, 13));
            this.addSlot(new SlotItemHandler(handler, 10, 134, 13));
            this.addSlot(new SlotItemHandler(handler, 11, 152, 13));
            this.addSlot(new SlotItemHandler(handler, 12, 116, 31));
            this.addSlot(new SlotItemHandler(handler, 13, 134, 31));
            this.addSlot(new SlotItemHandler(handler, 14, 152, 31));
            this.addSlot(new SlotItemHandler(handler, 15, 116, 49));
            this.addSlot(new SlotItemHandler(handler, 16, 134, 49));
            this.addSlot(new SlotItemHandler(handler, 17, 152, 49));
            this.addSlot(new SlotItemHandler(handler, 18, 116, 67));
            this.addSlot(new SlotItemHandler(handler, 19, 134, 67));
            this.addSlot(new SlotItemHandler(handler, 20, 152, 67));
        });

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.DISTILLERY.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for(int i=0; i<3; i++) {
            for(int l=0; l<9; l++) {
                this.addSlot((new Slot(playerInventory, l + i*9 + 9, 8 + l*18, 97 + i*18)));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i=0; i<9; i++) {
            this.addSlot((new Slot(playerInventory, i, 8 + i*18, 155)));
        }
    }

    public int getScaledProgress(int tier) {
        return -1;
    }

    public boolean isCrafting(int tier) {
        return false;
    }

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 21;  // must be the number of slots you have!

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }
}
