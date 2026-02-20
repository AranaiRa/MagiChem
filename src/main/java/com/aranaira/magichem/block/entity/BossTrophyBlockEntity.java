package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BossTrophyBlockEntity extends BlockEntity {
    public BossTrophyBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.BOSS_TROPHY_BE.get(), pPos, pBlockState);
    }
}
