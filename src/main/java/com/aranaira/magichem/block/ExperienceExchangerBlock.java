package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.ExperienceExchangerBlockEntity;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ExperienceExchangerBlock extends BaseEntityBlock {

    public static final VoxelShape
        VOXEL_SHAPE_BODY, VOXEL_SHAPE_EW, VOXEL_SHAPE_NS, VOXEL_SHAPE_GEMZONE,
        VOXEL_SHAPE_AGGREGATE;

    public ExperienceExchangerBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ExperienceExchangerBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return VOXEL_SHAPE_AGGREGATE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pBlockEntityType == BlockEntitiesRegistry.EXPERIENCE_EXCHANGER_BE.get()) {
            return ExperienceExchangerBlockEntity::tick;
        }

        return null;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof ExperienceExchangerBlockEntity eebe) {
            eebe.ejectStack(pPos);
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    static {
        VOXEL_SHAPE_BODY = Block.box(4,5,4,12,13,12);
        VOXEL_SHAPE_NS = Block.box(5,0,7.5,11,2,8.5);
        VOXEL_SHAPE_EW = Block.box(7.5,0,5,8.5,2,11);
        VOXEL_SHAPE_GEMZONE = Block.box(7,0,7,9,3.5,9);

        VOXEL_SHAPE_AGGREGATE = Shapes.or(
                VOXEL_SHAPE_BODY,
                VOXEL_SHAPE_NS,
                VOXEL_SHAPE_EW,
                VOXEL_SHAPE_GEMZONE
        );
    }
}
