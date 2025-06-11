package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AcidBasinRouterBlockEntity extends BlockEntity {
    public AcidBasinRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ACID_BASIN_ROUTER_BE.get(), pPos, pBlockState);
    }
}
