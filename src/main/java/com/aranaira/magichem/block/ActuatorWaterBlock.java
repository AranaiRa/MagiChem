package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.ActuatorWaterBlockEntity;
import com.aranaira.magichem.block.entity.routers.ActuatorWaterRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.BaseActuatorRouterBlockEntity;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ActuatorWaterBlock extends BaseEntityBlock {
    public ActuatorWaterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    private static final VoxelShape
            VOXEL_SHAPE_ERROR           = Block.box(0,0,0,16,16,16),
            VOXEL_SHAPE_NORTH_WIDE      = Block.box(1,0,0,15,15,4),
            VOXEL_SHAPE_NORTH_LONG      = Block.box(4,0,4,12,12,8),
            VOXEL_SHAPE_NORTH_TUBEBASE  = Block.box(4,0,8,12,2,15),
            VOXEL_SHAPE_NORTH_TUBEBODY  = Block.box(5,0,8,11,16,14),
            VOXEL_SHAPE_NORTH_PIPES     = Block.box(5.5,9,5,10.5,16,8),
            VOXEL_SHAPE_AGGREGATE_NORTH, VOXEL_SHAPE_AGGREGATE_SOUTH, VOXEL_SHAPE_AGGREGATE_EAST, VOXEL_SHAPE_AGGREGATE_WEST;
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);
        BlockState state = BlockRegistry.ACTUATOR_WATER_ROUTER.get().defaultBlockState();
        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        BlockPos targetPos = pPos.offset(0,1,0);
        pLevel.setBlock(targetPos, state, 3);
        ((BaseActuatorRouterBlockEntity)pLevel.getBlockEntity(targetPos)).configure(pPos, facing);
    }

    @Override
    public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        pLevel.destroyBlock(pPos.offset(0, 1, 0), true);

        super.destroy(pLevel, pPos, pState);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return switch(state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            default -> VOXEL_SHAPE_ERROR;
            case NORTH -> VOXEL_SHAPE_AGGREGATE_NORTH;
            case SOUTH -> VOXEL_SHAPE_AGGREGATE_SOUTH;
            case WEST -> VOXEL_SHAPE_AGGREGATE_WEST;
            case EAST -> VOXEL_SHAPE_AGGREGATE_EAST;
        };
    }

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
        //super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING);
    }

    /* BLOCK ENTITY STUFF BELOW THIS POINT*/

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        /*if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof CentrifugeBlockEntity) {
                ((CentrifugeBlockEntity) blockEntity).dropInventoryToWorld();
            }
        }*/
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        /*if(!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof ActuatorFireBlockEntity afbe) {
                NetworkHooks.openScreen((ServerPlayer)player, (ActuatorFireBlockEntity)entity, pos);
            } else {
                throw new IllegalStateException("ActuatorFireBlockEntity container provider is missing!");
            }
        }*/

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ActuatorWaterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.ACTUATOR_WATER_BE.get(),
                ActuatorWaterBlockEntity::tick);
    }

    static {
        VOXEL_SHAPE_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_NORTH_WIDE, VOXEL_SHAPE_NORTH_LONG,
                VOXEL_SHAPE_NORTH_TUBEBASE, VOXEL_SHAPE_NORTH_TUBEBODY,
                VOXEL_SHAPE_NORTH_PIPES);

        VOXEL_SHAPE_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_WIDE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_LONG, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_TUBEBASE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_TUBEBODY, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPES, 1));

        VOXEL_SHAPE_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_WIDE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_LONG, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_TUBEBASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_TUBEBODY, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPES, 2));

        VOXEL_SHAPE_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_WIDE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_LONG, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_TUBEBASE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_TUBEBODY, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPES, 3));
    }
}
