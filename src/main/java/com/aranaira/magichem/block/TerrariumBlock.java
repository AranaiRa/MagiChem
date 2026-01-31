package com.aranaira.magichem.block;

import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;

public class TerrariumBlock extends Block {
    public static final VoxelShape
        VOXEL_SHAPE_BASE, VOXEL_SHAPE_LIP, VOXEL_SHAPE_GLASS,
        VOXEL_SHAPE_AGGREGATE;

    public TerrariumBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(MagiChemBlockStateProperties.FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return VOXEL_SHAPE_AGGREGATE;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return true;
    }

    static {
        VOXEL_SHAPE_BASE  = Block.box(1,0,1,15,3,15);
        VOXEL_SHAPE_LIP   = Block.box(2,3,2,14,4,14);
        VOXEL_SHAPE_GLASS = Block.box(2.5,4,2.5,13.5,15,13.5);

        VOXEL_SHAPE_AGGREGATE = Shapes.or(VOXEL_SHAPE_BASE, VOXEL_SHAPE_LIP, VOXEL_SHAPE_GLASS);
    }
}
