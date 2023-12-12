package com.aranaira.magichem.block.entity.ext;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityWithEfficiency extends BlockEntity {
    public static int baseEfficiency;
    protected int efficiencyMod, modTimeRemaining, modTimeUntilBoostable;

    public BlockEntityWithEfficiency(BlockEntityType<?> blockEntityType, BlockPos blockPos, int efficiency, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        baseEfficiency = efficiency;
    }
}
