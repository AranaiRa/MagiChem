package com.aranaira.magichem.block;

import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.VERTICAL_CRYSTAL_SHAPE_TYPE;

public class VinteumCrystalBlock extends Block {
    public static final int
        CRYSTAL_TYPE_SINGLE = 0, CRYSTAL_TYPE_CENTER = 1,
        CRYSTAL_TYPE_TOP_FLAT = 2, CRYSTAL_TYPE_TOP_DECO = 3,
        CRYSTAL_TYPE_BOTTOM_FLAT = 4, CRYSTAL_TYPE_BOTTOM_DECO = 5;

    public VinteumCrystalBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, VERTICAL_CRYSTAL_SHAPE_TYPE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction dir = pContext.getHorizontalDirection();

        int type = getCrystalType(pContext.getLevel(), pContext.getClickedPos());

        return this.defaultBlockState()
                .setValue(FACING, dir)
                .setValue(VERTICAL_CRYSTAL_SHAPE_TYPE, type);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);

        BlockPos above = pPos.above();
        if(pLevel.getBlockState(above).getBlock() == BlockRegistry.VINTEUM_CRYSTAL_BLOCK.get()) {
            pLevel.setBlock(above, pLevel.getBlockState(above).setValue(VERTICAL_CRYSTAL_SHAPE_TYPE, getCrystalType(pLevel, above)), 3);
            pLevel.sendBlockUpdated(above, pLevel.getBlockState(above), pLevel.getBlockState(above).setValue(VERTICAL_CRYSTAL_SHAPE_TYPE, getCrystalType(pLevel, above)), 2);
        }

        BlockPos below = pPos.below();
        if(pLevel.getBlockState(below).getBlock() == BlockRegistry.VINTEUM_CRYSTAL_BLOCK.get()) {
            pLevel.setBlock(below, pLevel.getBlockState(below).setValue(VERTICAL_CRYSTAL_SHAPE_TYPE, getCrystalType(pLevel, below)), 3);
            pLevel.sendBlockUpdated(below, pLevel.getBlockState(below), pLevel.getBlockState(below).setValue(VERTICAL_CRYSTAL_SHAPE_TYPE, getCrystalType(pLevel, below)), 2);
        }

        pLevel.setBlock(pPos, pState.setValue(VERTICAL_CRYSTAL_SHAPE_TYPE, getCrystalType(pLevel, pPos)), 3);
        pLevel.sendBlockUpdated(pPos, pState, pState.setValue(VERTICAL_CRYSTAL_SHAPE_TYPE, getCrystalType(pLevel, pPos)), 2);
    }

    private int getCrystalType(Level pLevel, BlockPos pPos) {
        BlockState blockAbove = pLevel.getBlockState(pPos.above());
        BlockState blockBelow = pLevel.getBlockState(pPos.below());

        boolean belowIsCrystal = blockBelow.getBlock() == BlockRegistry.VINTEUM_CRYSTAL_BLOCK.get() || blockBelow.getBlock() == BlockRegistry.BUDDING_VINTEUM_CRYSTAL_BLOCK.get();
        boolean belowIsSolid = blockBelow.isCollisionShapeFullBlock(pLevel, pPos.below());
        boolean aboveIsCrystal = blockAbove.getBlock() == BlockRegistry.VINTEUM_CRYSTAL_BLOCK.get() || blockAbove.getBlock() == BlockRegistry.BUDDING_VINTEUM_CRYSTAL_BLOCK.get();
        boolean aboveIsSolid = blockAbove.isCollisionShapeFullBlock(pLevel, pPos.below());

        int type;
        if(!aboveIsCrystal && belowIsCrystal) {
            if(aboveIsSolid)
                type = CRYSTAL_TYPE_TOP_FLAT;
            else
                type = CRYSTAL_TYPE_TOP_DECO;
        }
        else if(!belowIsCrystal && aboveIsCrystal) {
            if(belowIsSolid)
                type = CRYSTAL_TYPE_BOTTOM_FLAT;
            else
                type = CRYSTAL_TYPE_BOTTOM_DECO;
        }
        else if(aboveIsCrystal && belowIsCrystal)
            type = CRYSTAL_TYPE_CENTER;
        else
            type = CRYSTAL_TYPE_SINGLE;

        return type;
    }
}
