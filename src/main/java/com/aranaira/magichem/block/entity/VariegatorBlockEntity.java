package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.gui.AlembicMenu;
import com.aranaira.magichem.gui.VariegatorMenu;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class VariegatorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int
        SLOT_COUNT = 8,
        DATA_COUNT = 1;
    protected ContainerData data;

    public VariegatorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.VARIEGATOR_BE.get(), pPos, pBlockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new VariegatorMenu(pContainerId, pPlayerInventory, this, this.data);
    }
}
