package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.CircleToilBlockEntity;
import com.aranaira.magichem.block.entity.VariegatorBlockEntity;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CircleToilBlock extends BaseEntityBlock {

    public static final VoxelShape
        VOXEL_SHAPE_BASE, VOXEL_SHAPE_BODY, VOXEL_SHAPE_COG, VOXEL_SHAPE_SHAFT, VOXEL_SHAPE_AGGREGATE;

    public CircleToilBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new CircleToilBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if(!pLevel.isClientSide()) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof CircleToilBlockEntity ctbe) {
                ctbe.ejectConstruct();
            }
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.CIRCLE_TOIL_BE.get(),
                CircleToilBlockEntity::tick);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return VOXEL_SHAPE_AGGREGATE;
    }

    static {
        VOXEL_SHAPE_BASE = Block.box(1, 0, 1, 15, 4, 15);
        VOXEL_SHAPE_BODY = Block.box(3, 4, 3, 13, 11, 13);
        VOXEL_SHAPE_SHAFT = Block.box(7, 8, 7, 9, 19, 9);
        VOXEL_SHAPE_COG = Block.box(1, 12.5, 1, 15, 13.5, 15);

        VOXEL_SHAPE_AGGREGATE = Shapes.or(
                VOXEL_SHAPE_BASE, VOXEL_SHAPE_BODY, VOXEL_SHAPE_SHAFT, VOXEL_SHAPE_COG
        );
    }
}
