package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CircleToilBlockEntity extends BlockEntity {

    public static float
        MAXIMUM_THETA = 1f, THETA_ACCELERATION_RATE = 0.02f;
    public float
        theta;

    public CircleToilBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntitiesRegistry.CIRCLE_TOIL_BE.get(), pos, state);
    }
}
