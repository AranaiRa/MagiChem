package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.registry.MenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class ChargingTalismanMenu extends AbstractContainerMenu {
    public static final int
        SLOT_COUNT = 2,
        SLOT_SPIKE = 0, SLOT_CHARGEABLE_ITEM = 1;

    public ChargingTalismanMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(SLOT_COUNT));
    }

    public ChargingTalismanMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.CHARGING_TALISMAN_MENU.get(), id);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
}
