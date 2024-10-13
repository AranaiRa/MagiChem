package com.aranaira.magichem.gui;

import com.aranaira.magichem.registry.MenuRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mna.items.ItemInit;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public class TravellersCompassMenu extends AbstractContainerMenu {
    public static final int
        SLOT_COUNT = 24,
        SLOT_RADIAL_START = 0, SLOT_RADIAL_COUNT = 12,
        SLOT_STORAGE_START = 12, SLOT_STORAGE_COUNT = 12;
    public int compassHoldingSlot;

    private static final int[]
            SLOT_X_POS = new int[]{
                97, 115, 150, 150, 150, 115, 97, 79, 44, 44, 44, 79,
                70, 88, 106, 124, 70, 88, 106, 124, 70, 88, 106, 124
            },
            SLOT_Y_POS = new int[]{
                8, 8, 34, 52, 70, 96, 96, 96, 70, 52, 34, 8,
                34, 34, 34, 34, 52, 52, 52, 52, 70, 70, 70, 70
            };

    public Inventory playerInventory;
    public ItemStackHandler itemHandler = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        protected void onContentsChanged(int slot) {
            if(!playerInventory.player.level().isClientSide()) {
                ItemStack itemCompass = playerInventory.getItem(compassHoldingSlot);
                ItemStack itemChanged = getStackInSlot(slot);
                CompoundTag nbtCompass = itemCompass.getOrCreateTag();
            }

            super.onContentsChanged(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() == ItemInit.THAUMATURGIC_COMPASS.get();
        }
    };

    public TravellersCompassMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, new SimpleContainerData(SLOT_COUNT));
    }

    public TravellersCompassMenu(int id, Inventory inv, ContainerData data) {
        super(MenuRegistry.TRAVELLERS_COMPASS_MENU.get(), id);

        playerInventory = inv;
        compassHoldingSlot = data.get(0);

        CompoundTag nbt = playerInventory.getItem(compassHoldingSlot).getOrCreateTag();
        if(nbt.contains("inventory")) {
            itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        }

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        for(int i = 0; i< SLOT_X_POS.length; i++) {
            this.addSlot(new SlotItemHandler(itemHandler, i, SLOT_X_POS[i], SLOT_Y_POS[i]));
        }
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public void clicked(int pSlotId, int pButton, ClickType pClickType, Player pPlayer) {
        int selectedSlot = pPlayer.getInventory().findSlotMatchingItem(pPlayer.getInventory().getSelected());

        if(selectedSlot != pSlotId - 27)
            super.clicked(pSlotId, pButton, pClickType, pPlayer);
    }

    @Override
    public void removed(Player pPlayer) {
        if(!pPlayer.level().isClientSide) {
            CompoundTag nbt = playerInventory.getItem(compassHoldingSlot).getOrCreateTag();
            nbt.put("inventory", itemHandler.serializeNBT());
        }

        super.removed(pPlayer);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for(int i=0; i<3; i++) {
            for(int l=0; l<9; l++) {
                this.addSlot((new Slot(playerInventory, l + i*9 + 9, 8 + l*18, 127 + i*18)));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i=0; i<9; i++) {
            this.addSlot((new Slot(playerInventory, i, 8 + i*18, 185)));
        }
    }

    private static final int SLOT_INVENTORY_BEGIN = 0;
    private static final int SLOT_INVENTORY_COUNT = 36;

    private static final Pair<Item, Integer>[] DIRSPEC = null;
    private static final Vector2i[] SPEC_FROM_INVENTORY = new Vector2i[] {
            new Vector2i( //Input slots
                    SLOT_INVENTORY_COUNT, SLOT_INVENTORY_COUNT + 1),
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    private static final Vector2i[] SPEC_TO_INVENTORY = new Vector2i[] {
            new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT)
    };
    private static final Pair<Integer, Vector2i> SPEC_CONTAINER = null;

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack result = InventoryHelper.quickMoveStackHandler(pIndex, slots, DIRSPEC, new Vector2i(SLOT_INVENTORY_BEGIN, SLOT_INVENTORY_COUNT), SPEC_FROM_INVENTORY, SPEC_TO_INVENTORY, SPEC_CONTAINER);

        slots.get(pIndex).set(result);

        return ItemStack.EMPTY;
    }
}
