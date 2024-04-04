package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.AlembicBlockEntity;
import com.aranaira.magichem.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DistilleryBlock extends BaseEntityBlock {
    public DistilleryBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    private static final VoxelShape
            VOXEL_SHAPE_ERROR,

            VOXEL_SHAPE_PLUG_NORTH, VOXEL_SHAPE_BODY_NORTH, VOXEL_SHAPE_MOUNT_NORTH, VOXEL_SHAPE_FURNACE_NORTH,
            VOXEL_SHAPE_TANK_NORTH, VOXEL_SHAPE_PIPE_LEFT_NORTH, VOXEL_SHAPE_PIPE_RIGHT_NORTH,

            VOXEL_SHAPE_AGGREGATE_NORTH, VOXEL_SHAPE_AGGREGATE_EAST, VOXEL_SHAPE_AGGREGATE_SOUTH, VOXEL_SHAPE_AGGREGATE_WEST;
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return super.mirror(pState, pMirror);
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return super.rotate(pState, pRotation);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return switch(state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            default -> VOXEL_SHAPE_ERROR;
            case NORTH -> VOXEL_SHAPE_AGGREGATE_NORTH;
            case EAST -> VOXEL_SHAPE_AGGREGATE_EAST;
            case SOUTH -> VOXEL_SHAPE_AGGREGATE_SOUTH;
            case WEST -> VOXEL_SHAPE_AGGREGATE_WEST;
        };
    }

    static {
        VOXEL_SHAPE_ERROR = Block.box(4, 4, 4, 12, 12, 12);

        VOXEL_SHAPE_BODY_NORTH = Block.box(0, 0, 2, 16, 8, 14);
        VOXEL_SHAPE_PLUG_NORTH = Block.box(12, 0, 0, 16, 16, 16);
        VOXEL_SHAPE_TANK_NORTH = Block.box(3, 13, 6, 7, 16, 10);
        VOXEL_SHAPE_MOUNT_NORTH = Block.box(2, 8, 5, 12, 12, 11);
        VOXEL_SHAPE_FURNACE_NORTH = Block.box(0, 0, 1, 6, 10, 15);
        VOXEL_SHAPE_PIPE_LEFT_NORTH = Block.box(0, 10, 5, 2, 16, 7);
        VOXEL_SHAPE_PIPE_RIGHT_NORTH = Block.box(7, 12, 7, 10, 16, 9);

        VOXEL_SHAPE_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_BODY_NORTH,
                VOXEL_SHAPE_PLUG_NORTH,
                VOXEL_SHAPE_TANK_NORTH,
                VOXEL_SHAPE_MOUNT_NORTH,
                VOXEL_SHAPE_FURNACE_NORTH,
                VOXEL_SHAPE_PIPE_LEFT_NORTH,
                VOXEL_SHAPE_PIPE_RIGHT_NORTH
                );

        VOXEL_SHAPE_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_MOUNT_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FURNACE_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_LEFT_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_RIGHT_NORTH, 1)
                );

        VOXEL_SHAPE_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_MOUNT_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FURNACE_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_LEFT_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_RIGHT_NORTH, 2)
                );

        VOXEL_SHAPE_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_MOUNT_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FURNACE_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_LEFT_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_RIGHT_NORTH, 3)
                );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AlembicBlockEntity(pPos, pState);
    }
}
