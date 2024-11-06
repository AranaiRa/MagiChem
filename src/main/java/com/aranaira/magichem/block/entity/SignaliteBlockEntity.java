package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SignaliteBlockEntity extends BlockEntity {
    public SignaliteBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.SIGNALITE_BE.get(), pPos, pBlockState);
    }
}
