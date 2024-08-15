package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.AlembicBlockEntity;
import com.aranaira.magichem.block.entity.VariegatorBlockEntity;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class VariegatorMenu extends AbstractContainerMenu {

    private final VariegatorBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public VariegatorMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(VariegatorBlockEntity.DATA_COUNT));
    }

    public VariegatorMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.VARIEGATOR_MENU.get(), id);
        checkContainerSize(inv, VariegatorBlockEntity.SLOT_COUNT);
        blockEntity = (VariegatorBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, BlockRegistry.VARIEGATOR.get());
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }
}
