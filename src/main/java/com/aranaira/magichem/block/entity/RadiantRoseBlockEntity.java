package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RadiantRoseBlockEntity extends BlockEntity {
    public RadiantRoseBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.RADIANT_ROSE_BE.get(), pPos, pBlockState);
    }


}
