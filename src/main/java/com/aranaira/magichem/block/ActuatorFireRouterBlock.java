package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.routers.ActuatorFireRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.ActuatorWaterRouterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ActuatorFireRouterBlock extends BaseActuatorRouterBlock {

    public ActuatorFireRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ActuatorFireRouterBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }
}