package com.aranaira.magichem.block;

import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;

public class PrimeAggregatorBlock extends Block {
    public PrimeAggregatorBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }
}
