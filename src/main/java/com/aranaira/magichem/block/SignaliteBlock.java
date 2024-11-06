package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.SignaliteBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SignaliteBlock extends BaseEntityBlock {
    public static final VoxelShape
        VOXEL_SHAPE_CORE,
        VOXEL_SHAPE_NORTH, VOXEL_SHAPE_EAST, VOXEL_SHAPE_SOUTH, VOXEL_SHAPE_WEST, VOXEL_SHAPE_UP, VOXEL_SHAPE_DOWN,
        VOXEL_SHAPE_AGGREGATE;

    public SignaliteBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SignaliteBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return VOXEL_SHAPE_AGGREGATE;
    }

    static {
        VOXEL_SHAPE_CORE = Block.box(6, 6, 6, 10, 10, 10);

        VOXEL_SHAPE_NORTH = Block.box(7, 7, 0, 9, 9, 8);
        VOXEL_SHAPE_SOUTH = Block.box(7, 7, 8, 9, 9, 16);
        VOXEL_SHAPE_EAST  = Block.box(0, 7, 7, 8, 9, 9);
        VOXEL_SHAPE_WEST  = Block.box(8, 7, 7, 16, 9, 9);
        VOXEL_SHAPE_UP    = Block.box(7, 0, 7, 9, 8, 9);
        VOXEL_SHAPE_DOWN  = Block.box(7, 8, 7, 9, 16, 9);

        VOXEL_SHAPE_AGGREGATE = Shapes.or(
                VOXEL_SHAPE_CORE,
                VOXEL_SHAPE_NORTH,
                VOXEL_SHAPE_EAST,
                VOXEL_SHAPE_SOUTH,
                VOXEL_SHAPE_WEST,
                VOXEL_SHAPE_UP,
                VOXEL_SHAPE_DOWN
        );
    }
}
