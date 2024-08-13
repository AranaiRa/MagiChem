package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.ActuatorAirBlockEntity;
import com.aranaira.magichem.block.entity.VariegatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.*;

public class VariegatorBlock extends BaseEntityBlock {
    public VariegatorBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(GROUNDED, true));
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(GROUNDED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction lookDir = pContext.getNearestLookingVerticalDirection();

        if(lookDir == Direction.UP)
            return this.defaultBlockState().setValue(GROUNDED, false);

        return super.getStateForPlacement(pContext);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new VariegatorBlockEntity(pPos, pState);
    }
}
