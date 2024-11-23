package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class StandingRetortBlockEntity extends BlockEntity {
    public StandingRetortBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.STANDING_RETORT_BE.get(), pPos, pBlockState);
    }
}
