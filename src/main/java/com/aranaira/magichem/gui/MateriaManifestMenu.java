package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.MateriaManifestBlock;
import com.aranaira.magichem.block.entity.MateriaManifestBlockEntity;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class MateriaManifestMenu extends AbstractContainerMenu {

    public final MateriaManifestBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public MateriaManifestMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(0));
    }

    public MateriaManifestMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(MenuRegistry.MATERIA_MANIFEST_MENU.get(), id);
        checkContainerSize(inv, 1);
        blockEntity = (MateriaManifestBlockEntity) entity;
        this.level = inv.player.level();
        this.data = data;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, BlockRegistry.MATERIA_MANIFEST.get());
    }
}
