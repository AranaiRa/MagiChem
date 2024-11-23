package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.StandingRetortBlockEntity;
import com.mna.blocks.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class StandingRetortBlock extends BaseEntityBlock {
    private static final VoxelShape VOXEL_SHAPE = Block.box(5.5, 1, 5.5, 10.5, 10, 10.5);

    public StandingRetortBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new StandingRetortBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        final BlockPos clickedPos = pContext.getClickedPos();
        final BlockState clickedState = pContext.getLevel().getBlockState(clickedPos);
        final BlockState fumeStateQuery = pContext.getLevel().getBlockState(clickedPos.offset(0, -2, 0));

        if(clickedState.isAir() && fumeStateQuery.getBlock() == BlockInit.ELDRIN_FUME.get()) {
            Direction dir = fumeStateQuery.getValue(HORIZONTAL_FACING);

            return defaultBlockState().setValue(HORIZONTAL_FACING, dir);
        }

        return super.getStateForPlacement(pContext);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return VOXEL_SHAPE;
    }
}
