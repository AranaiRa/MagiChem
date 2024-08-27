package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CrystalCandleBlockEntity extends BlockEntity {

    public CrystalCandleBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public CrystalCandleBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.CRYSTAL_CANDLE_BE.get(), pPos, pBlockState);
    }
}
