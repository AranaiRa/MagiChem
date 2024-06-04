package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
import com.aranaira.magichem.block.entity.AlembicBlockEntity;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.MenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class AlchemicalNexusMenu extends AbstractContainerMenu {
    public final AlchemicalNexusBlockEntity blockEntity;
    private final Level level;
    //private final ContainerData data;

    public AlchemicalNexusMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(AlchemicalNexusBlockEntity.DATA_COUNT));
    }

    public AlchemicalNexusMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.ALCHEMICAL_NEXUS_MENU.get(), id);
        checkContainerSize(inv, 14);
        blockEntity = (AlchemicalNexusBlockEntity) entity;
        this.level = inv.player.level();
        //this.data = data;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, BlockRegistry.ALCHEMICAL_NEXUS.get());
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }
}
