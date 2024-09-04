package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.CirclePowerBlockEntity;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public class ChargingTalismanMenu extends AbstractContainerMenu {
    public static final int
        SLOT_COUNT = 2,
        SLOT_SPIKE = 0, SLOT_CHARGEABLE_ITEM = 1;
    public int talismanHoldingSlot;
    public BlockPos circlePos;

    public Inventory playerInventory;
    public ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            if(!playerInventory.player.level().isClientSide()) {
                ItemStack itemTalisman = playerInventory.getItem(talismanHoldingSlot);
                ItemStack itemChanged = getStackInSlot(slot);
                CompoundTag nbtTalisman = itemTalisman.getOrCreateTag();

                if(slot == SLOT_SPIKE) {
                    if(itemChanged.getItem() == BlockRegistry.POWER_SPIKE.get().asItem()) {
                        CompoundTag nbtSpike = itemChanged.getOrCreateTag();

                        if(nbtSpike.contains("magichem.powerspike.targetpos")) {
                            nbtTalisman.putLong("magichem.powerspike.targetpos", nbtSpike.getLong("magichem.powerspike.targetpos"));
                            circlePos = BlockPos.of(nbtSpike.getLong("magichem.powerspike.targetpos"));
                        }
                    }
                    else {
                        if(nbtTalisman.contains("magichem.powerspike.targetpos")) {
                            nbtTalisman.remove("magichem.powerspike.targetpos");
                            itemTalisman.setTag(nbtTalisman);
                        }
                    }
                }
                else if(slot == SLOT_CHARGEABLE_ITEM) {
                    if(!playerInventory.player.level().isClientSide()) {
                        if (nbtTalisman.contains("magichem.powerspike.targetpos")) {
                            circlePos = BlockPos.of(nbtTalisman.getLong("magichem.powerspike.targetpos"));
                        }
                        BlockEntity be = playerInventory.player.level().getBlockEntity(circlePos);
                        if(be != null) {
                            if(be instanceof CirclePowerBlockEntity cpbe) {

                                if (itemChanged != ItemStack.EMPTY) {
                                    cpbe.addRemoteChargeItem(itemChanged);
                                }
                            }
                        }
                    }
                }
            }

            super.onContentsChanged(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if(slot == SLOT_SPIKE) {
                if(stack.getItem() == BlockRegistry.POWER_SPIKE.get().asItem()) {
                    if(stack.hasTag()) {
                        if (stack.getTag().contains("magichem.powerspike.targetpos")) {
                            return true;
                        }
                    }
                }

                return false;
            }
            else if(slot == SLOT_CHARGEABLE_ITEM)
                return stack.getCapability(ForgeCapabilities.ENERGY).isPresent();
            return false;
        }
    };

    public ChargingTalismanMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, new SimpleContainerData(SLOT_COUNT));
    }

    public ChargingTalismanMenu(int id, Inventory inv, ContainerData data) {
        super(MenuRegistry.CHARGING_TALISMAN_MENU.get(), id);

        playerInventory = inv;
        talismanHoldingSlot = data.get(0);

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.addSlot(new SlotItemHandler(itemHandler, SLOT_SPIKE, 94, 22));
        this.addSlot(new SlotItemHandler(itemHandler, SLOT_CHARGEABLE_ITEM, 66, 50));

        ItemStack itemTalisman = playerInventory.getItem(talismanHoldingSlot);
        CompoundTag nbtTalisman = itemTalisman.getOrCreateTag();
        if(nbtTalisman.contains("magichem.powerspike.targetpos")) {
            long circlePos = nbtTalisman.getLong("magichem.powerspike.targetpos");
            ItemStack powerSpike = new ItemStack(BlockRegistry.POWER_SPIKE.get());
            CompoundTag nbt = new CompoundTag();
            nbt.putLong("magichem.powerspike.targetpos", circlePos);
            powerSpike.setTag(nbt);
            itemHandler.setStackInSlot(SLOT_SPIKE, powerSpike);
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
            if(itemHandler.getStackInSlot(SLOT_CHARGEABLE_ITEM) != ItemStack.EMPTY) {

                BlockEntity be = playerInventory.player.level().getBlockEntity(circlePos);
                if(be != null) {
                    if (be instanceof CirclePowerBlockEntity cpbe) {
                        cpbe.removeRemoteChargeItem(itemHandler.getStackInSlot(SLOT_CHARGEABLE_ITEM));
                    }
                }

                ItemEntity ie = new ItemEntity(pPlayer.level(), pPlayer.position().x, pPlayer.position().y, pPlayer.position().z, itemHandler.getStackInSlot(SLOT_CHARGEABLE_ITEM));
                pPlayer.level().addFreshEntity(ie);
            }
        }

        super.removed(pPlayer);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for(int i=0; i<3; i++) {
            for(int l=0; l<9; l++) {
                this.addSlot((new Slot(playerInventory, l + i*9 + 9, 8 + l*18, 85 + i*18)));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for(int i=0; i<9; i++) {
            this.addSlot((new Slot(playerInventory, i, 8 + i*18, 143 )));
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
