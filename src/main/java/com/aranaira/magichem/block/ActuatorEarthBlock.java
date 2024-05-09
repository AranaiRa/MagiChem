package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.ActuatorAirBlockEntity;
import com.aranaira.magichem.block.entity.ActuatorEarthBlockEntity;
import com.aranaira.magichem.block.entity.routers.BaseActuatorRouterBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class ActuatorEarthBlock extends BaseEntityBlock {
    public ActuatorEarthBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    private static final VoxelShape
            VOXEL_SHAPE_ERROR           = Block.box(0,0,0,16,16,16),
            VOXEL_SHAPE_PLUG_NORTH      = Block.box(1,0,0,15,15,4),
            VOXEL_SHAPE_BASE_NORTH      = Block.box(1,0,2,15,2,15),
            VOXEL_SHAPE_POST_NW_NORTH   = Block.box(2,2,2,4,16,4),
            VOXEL_SHAPE_POST_NE_NORTH   = Block.box(12,2,2,14,16,4),
            VOXEL_SHAPE_POST_SW_NORTH   = Block.box(2,2,12,4,16,14),
            VOXEL_SHAPE_POST_SE_NORTH   = Block.box(12,2,12,14,16,14),
            VOXEL_SHAPE_BASIN_NORTH     = Block.box(3,2,3,13,11,13),
            VOXEL_SHAPE_AGGREGATE_NORTH, VOXEL_SHAPE_AGGREGATE_SOUTH, VOXEL_SHAPE_AGGREGATE_EAST, VOXEL_SHAPE_AGGREGATE_WEST;
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if(pPlacer instanceof Player player) {
            ActuatorEarthBlockEntity awbe = (ActuatorEarthBlockEntity) pLevel.getBlockEntity(pPos);
            awbe.setOwner(player);
        }
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos().above();

        if(!pContext.getLevel().isEmptyBlock(pos)) {
            return null;
        }

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, BlockPos pNeighborPos, boolean pMovedByPiston) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be != null) {
            if(be instanceof ActuatorEarthBlockEntity aebe) {
                ActuatorEarthBlockEntity.setPaused(aebe, pLevel.hasNeighborSignal(pPos));
            }
        }
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);
        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockState state = BlockRegistry.ACTUATOR_EARTH_ROUTER.get().defaultBlockState();
        state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        state = state.setValue(BaseActuatorRouterBlock.ELEMENT, BaseActuatorRouterBlock.ELEMENT_EARTH);

        BlockPos targetPos = pPos.offset(0,1,0);
        pLevel.setBlock(targetPos, state, 3);
        ((BaseActuatorRouterBlockEntity)pLevel.getBlockEntity(targetPos)).configure(pPos, facing);

        ActuatorEarthBlockEntity awbe = (ActuatorEarthBlockEntity) pLevel.getBlockEntity(pPos);
        ICanTakePlugins ictp = awbe.getTargetMachine();
        if(ictp != null)
            ictp.linkPluginsDeferred();
    }

    @Override
    public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        pLevel.destroyBlock(pPos.above(), true);

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
        ActuatorEarthBlockEntity aebe = (ActuatorEarthBlockEntity) level.getBlockEntity(pos);
        ICanTakePlugins ictp = aebe.getTargetMachine();
        if(ictp != null)
            ictp.removePlugin(aebe);

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof ActuatorEarthBlockEntity aebe) {
                NetworkHooks.openScreen((ServerPlayer)player, (ActuatorEarthBlockEntity)entity, pos);
            } else {
                throw new IllegalStateException("ActuatorEarthBlockEntity container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ActuatorEarthBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pBlockEntityType == BlockEntitiesRegistry.ACTUATOR_EARTH_BE.get()) {
            return ActuatorEarthBlockEntity::tick;
        }

        return null;
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    static {
        VOXEL_SHAPE_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_PLUG_NORTH,
                VOXEL_SHAPE_BASE_NORTH,
                VOXEL_SHAPE_POST_NW_NORTH,
                VOXEL_SHAPE_POST_NE_NORTH,
                VOXEL_SHAPE_POST_SW_NORTH,
                VOXEL_SHAPE_POST_SE_NORTH,
                VOXEL_SHAPE_BASIN_NORTH);

        VOXEL_SHAPE_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_POST_NW_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_POST_NE_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_POST_SW_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_POST_SE_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASIN_NORTH, 1));

        VOXEL_SHAPE_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_POST_NW_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_POST_NE_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_POST_SW_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_POST_SE_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASIN_NORTH, 2));

        VOXEL_SHAPE_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_POST_NW_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_POST_NE_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_POST_SW_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_POST_SE_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASIN_NORTH, 3));
    }
}
