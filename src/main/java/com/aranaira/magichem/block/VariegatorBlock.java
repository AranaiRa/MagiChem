package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.ActuatorAirBlockEntity;
import com.aranaira.magichem.block.entity.VariegatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class VariegatorBlock extends BaseEntityBlock {
    public VariegatorBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new VariegatorBlockEntity(pPos, pState);
    }
}
