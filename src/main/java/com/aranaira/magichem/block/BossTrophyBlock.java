package com.aranaira.magichem.block;

import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;

public class BossTrophyBlock extends Block {
    private static final VoxelShape
        VOXEL_SHAPE_NORTH, VOXEL_SHAPE_EAST, VOXEL_SHAPE_SOUTH, VOXEL_SHAPE_WEST;

    public BossTrophyBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
        super.createBlockStateDefinition(pBuilder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if(state.getBlock() == BlockRegistry.BOSS_TROPHY_DEMONS.get()) return 15;

        return super.getLightEmission(state, level, pos);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        final Direction dir = pState.getValue(FACING);

        if(dir == Direction.NORTH) return VOXEL_SHAPE_NORTH;
        else if(dir == Direction.EAST) return VOXEL_SHAPE_EAST;
        else if(dir == Direction.SOUTH) return VOXEL_SHAPE_SOUTH;
        else if(dir == Direction.WEST) return VOXEL_SHAPE_WEST;

        return super.getShape(pState, pLevel, pPos, pContext);
    }

    static {
        VOXEL_SHAPE_NORTH = Block.box(2,0, 3, 14, 15, 13);
        VOXEL_SHAPE_EAST = MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH, 1);
        VOXEL_SHAPE_SOUTH = MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH, 2);
        VOXEL_SHAPE_WEST = MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH, 3);
    }
}
