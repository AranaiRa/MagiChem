package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.CirclePowerBlockEntity;
import com.aranaira.magichem.capabilities.wisdom.IWisdomCapability;
import com.aranaira.magichem.capabilities.wisdom.WisdomProvider;
import com.aranaira.magichem.item.PhilosophersStoneItem;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import com.aranaira.magichem.util.InventoryHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.Optional;

public class WisdomMenu extends AbstractContainerMenu {
    public Inventory playerInventory;
    public IWisdomCapability capability;
    private final ContainerData data;

    public WisdomMenu(int pID, Inventory pInv, FriendlyByteBuf pExtraData) {
        this(pID, pInv, new SimpleContainerData(1));
    }

    public WisdomMenu(int pID, Inventory inv, ContainerData data) {
        super(MenuRegistry.WISDOM_MENU.get(), pID);

        this.data = data;
        playerInventory = inv;
        Optional<IWisdomCapability> capQuery = WisdomProvider.getCapability(playerInventory.player);
        capability = capQuery.orElse(null);
        addDataSlots(data);
    }

    public int getWisdom() {
        return data.get(0);
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return capability != null;
    }

    @Override
    public void clicked(int pSlotId, int pButton, ClickType pClickType, Player pPlayer) {
        int selectedSlot = pPlayer.getInventory().findSlotMatchingItem(pPlayer.getInventory().getSelected());

        if(selectedSlot != pSlotId - 27)
            super.clicked(pSlotId, pButton, pClickType, pPlayer);
    }

    @Override
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }
}
