package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.HeftyHopperBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING_OMNI;

public class HeftyHopperBlock extends BaseEntityBlock {
    private static final VoxelShape
            VOXEL_SHAPE_LIP_TOP, VOXEL_SHAPE_LIP_BOTTOM, VOXEL_SHAPE_SHAFT, VOXEL_SHAPE_BODY,
            VOXEL_SHAPE_LIP_TOP_VERTICAL, VOXEL_SHAPE_LIP_BOTTOM_VERTICAL, VOXEL_SHAPE_SHAFT_VERTICAL, VOXEL_SHAPE_BODY_VERTICAL,
            VOXEL_SHAPE_AGGREGATE_UP, VOXEL_SHAPE_AGGREGATE_DOWN, VOXEL_SHAPE_AGGREGATE_NORTH, VOXEL_SHAPE_AGGREGATE_EAST, VOXEL_SHAPE_AGGREGATE_SOUTH, VOXEL_SHAPE_AGGREGATE_WEST;

    public HeftyHopperBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new HeftyHopperBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction dir = pContext.getClickedFace();
        Vec3i targetCoords = new Vec3i(dir.getOpposite().getStepX(),dir.getOpposite().getStepY(),dir.getOpposite().getStepZ());
        BlockState state = pContext.getLevel().getBlockState(pContext.getClickedPos().offset(targetCoords));

        return state.is(this) && state.getValue(FACING_OMNI) == dir
                ? this.defaultBlockState().setValue(FACING_OMNI, dir.getOpposite())
                : this.defaultBlockState().setValue(FACING_OMNI, dir);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING_OMNI);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pBlockEntityType == BlockEntitiesRegistry.HEFTY_HOPPER_BE.get()) {
            return HeftyHopperBlockEntity::tick;
        }

        return null;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        final Direction facing = pState.getValue(FACING_OMNI);

        if(facing == Direction.UP) return VOXEL_SHAPE_AGGREGATE_UP;
        else if(facing == Direction.DOWN) return VOXEL_SHAPE_AGGREGATE_DOWN;
        else if(facing == Direction.NORTH) return VOXEL_SHAPE_AGGREGATE_NORTH;
        else if(facing == Direction.EAST) return VOXEL_SHAPE_AGGREGATE_EAST;
        else if(facing == Direction.SOUTH) return VOXEL_SHAPE_AGGREGATE_SOUTH;
        else if(facing == Direction.WEST) return VOXEL_SHAPE_AGGREGATE_WEST;

        return super.getShape(pState, pLevel, pPos, pContext);
    }

    static {
        VOXEL_SHAPE_LIP_TOP_VERTICAL = Block.box(0,14,0,16,16,16);
        VOXEL_SHAPE_BODY_VERTICAL = Block.box(2,4,2,14,14,14);
        VOXEL_SHAPE_SHAFT_VERTICAL = Block.box(6,2,6,10,8,10);
        VOXEL_SHAPE_LIP_BOTTOM_VERTICAL = Block.box(5,0,5,11,2,11);

        VOXEL_SHAPE_AGGREGATE_UP = Shapes.or(
                VOXEL_SHAPE_LIP_TOP_VERTICAL,
                VOXEL_SHAPE_BODY_VERTICAL,
                VOXEL_SHAPE_SHAFT_VERTICAL,
                VOXEL_SHAPE_LIP_BOTTOM_VERTICAL
        );
        VOXEL_SHAPE_AGGREGATE_DOWN = Shapes.or(
                MathHelper.flipVoxelShapeY(VOXEL_SHAPE_LIP_TOP_VERTICAL),
                MathHelper.flipVoxelShapeY(VOXEL_SHAPE_BODY_VERTICAL),
                MathHelper.flipVoxelShapeY(VOXEL_SHAPE_SHAFT_VERTICAL),
                MathHelper.flipVoxelShapeY(VOXEL_SHAPE_LIP_BOTTOM_VERTICAL)
        );

        VOXEL_SHAPE_LIP_TOP = Block.box(0,0,0,16,16,2);
        VOXEL_SHAPE_BODY = Block.box(2,2,2,14,14,12);
        VOXEL_SHAPE_SHAFT = Block.box(6,6,12,10,10,14);
        VOXEL_SHAPE_LIP_BOTTOM = Block.box(5,5,14,11,11,16);

        VOXEL_SHAPE_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_LIP_TOP,
                VOXEL_SHAPE_BODY,
                VOXEL_SHAPE_SHAFT,
                VOXEL_SHAPE_LIP_BOTTOM
        );
        VOXEL_SHAPE_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LIP_TOP, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_SHAFT, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LIP_BOTTOM, 1)
        );
        VOXEL_SHAPE_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LIP_TOP, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_SHAFT, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LIP_BOTTOM, 2)
        );
        VOXEL_SHAPE_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LIP_TOP, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_SHAFT, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_LIP_BOTTOM, 3)
        );
    }
}
