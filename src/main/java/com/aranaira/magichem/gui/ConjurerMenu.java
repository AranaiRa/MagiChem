package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.ConjurerBlockEntity;
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

import static com.aranaira.magichem.block.entity.ConjurerBlockEntity.*;

public class ConjurerMenu extends AbstractContainerMenu {
    public final ConjurerBlockEntity blockEntity;
    private final Level level;

    public ConjurerMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(0));
    }

    public ConjurerMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.CONJURER_MENU.get(), id);
        blockEntity = (ConjurerBlockEntity) entity;
        level = inv.player.level();
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, BlockRegistry.CONJURER.get());
    }
}
