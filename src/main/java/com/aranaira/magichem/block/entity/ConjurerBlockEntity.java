package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.gui.ConjurerMenu;
import com.aranaira.magichem.gui.GrandDistilleryMenu;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ConjurerBlockEntity extends BlockEntity implements MenuProvider {

    public ConjurerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CONJURER_BE.get(), pos, state);
    }

    public ConjurerBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.empty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ConjurerMenu(pContainerId, pPlayerInventory, this, null);
    }
}
