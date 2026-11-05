package com.aranaira.magichem.block;

import com.aranaira.magichem.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class PileOfRichesBlock extends Block {
    public static final VoxelShape
        VOXEL_SHAPE_LAYER_1, VOXEL_SHAPE_LAYER_2, VOXEL_SHAPE_LAYER_3, VOXEL_SHAPE_LAYER_4, VOXEL_SHAPE_ERROR,
        VOXEL_SHAPE_AGGREGATE_NS, VOXEL_SHAPE_AGGREGATE_EW;

    public PileOfRichesBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 15;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return switch(state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            default -> VOXEL_SHAPE_ERROR;
            case NORTH, SOUTH -> VOXEL_SHAPE_AGGREGATE_NS;
            case WEST, EAST -> VOXEL_SHAPE_AGGREGATE_EW;
        };
    }

    static {
        VOXEL_SHAPE_LAYER_1 = Block.box(0, 0, 0.5, 16, 3, 15.5);
        VOXEL_SHAPE_LAYER_2 = Block.box(2, 3, 1.5, 14, 6, 14.5);
        VOXEL_SHAPE_LAYER_3 = Block.box(3, 6, 2.5, 13, 7, 13.5);
        VOXEL_SHAPE_LAYER_4 = Block.box(5, 7, 5, 11, 8, 11);
        VOXEL_SHAPE_ERROR = Block.box(4,4,4,12,12,12);

        VOXEL_SHAPE_AGGREGATE_NS = Shapes.or(
                VOXEL_SHAPE_LAYER_1,
                VOXEL_SHAPE_LAYER_2,
                VOXEL_SHAPE_LAYER_3,
                VOXEL_SHAPE_LAYER_4
        );

        VOXEL_SHAPE_AGGREGATE_EW = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LAYER_1, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LAYER_2, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LAYER_3, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LAYER_4, 1)
        );
    }
}
