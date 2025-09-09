package com.aranaira.magichem.gui;

import com.aranaira.magichem.capabilities.wisdom.IWisdomCapability;
import com.aranaira.magichem.capabilities.wisdom.WisdomProvider;
import com.aranaira.magichem.registry.MenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class CodexMateriaMenu extends AbstractContainerMenu {
    public Inventory playerInventory;
    public IWisdomCapability capability;

    public CodexMateriaMenu(int pID, Inventory pInv, FriendlyByteBuf pExtraData) {
        this(pID, pInv, new SimpleContainerData(1));
    }

    public CodexMateriaMenu(int pID, Inventory inv, ContainerData data) {
        super(MenuRegistry.CODEX_MATERIA_MENU.get(), pID);

        playerInventory = inv;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public void clicked(int pSlotId, int pButton, ClickType pClickType, Player pPlayer) {
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
