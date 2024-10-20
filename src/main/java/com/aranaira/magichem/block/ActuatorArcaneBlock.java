package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.ActuatorArcaneBlockEntity;
import com.aranaira.magichem.block.entity.routers.BaseActuatorRouterBlockEntity;
import com.aranaira.magichem.foundation.ICanTakePlugins;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
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

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ACTUATOR_ELEMENT;

public class ActuatorArcaneBlock extends BaseEntityBlock {
    public ActuatorArcaneBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    private static final VoxelShape
            VOXEL_SHAPE_ERROR           = Block.box(0,0,0,16,16,16),
            VOXEL_SHAPE_PLUG      = Block.box(1,0,0,15,15,2),
            VOXEL_SHAPE_BASE      = Block.box(2,0,2,14,2,14),
            VOXEL_SHAPE_BODY_LONG   = Block.box(3,2,4,13,11,13),
            VOXEL_SHAPE_BODY_TALL   = Block.box(3,2,2,13,13,4),
            VOXEL_SHAPE_PLATFORM   = Block.box(5,10,5,11,15,11),
            VOXEL_SHAPE_AGGREGATE_NORTH, VOXEL_SHAPE_AGGREGATE_SOUTH, VOXEL_SHAPE_AGGREGATE_EAST, VOXEL_SHAPE_AGGREGATE_WEST;
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if(pPlacer instanceof Player player) {
            ActuatorArcaneBlockEntity awbe = (ActuatorArcaneBlockEntity) pLevel.getBlockEntity(pPos);
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
            if(be instanceof ActuatorArcaneBlockEntity aabe) {
                aabe.setPaused(pLevel.hasNeighborSignal(pPos));
            }
        }
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);
        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockState state = BlockRegistry.ACTUATOR_ARCANE_ROUTER.get().defaultBlockState();
        state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        state = state.setValue(ACTUATOR_ELEMENT, BaseActuatorRouterBlock.ELEMENT_ARCANE);

        BlockPos targetPos = pPos.offset(0,1,0);
        pLevel.setBlock(targetPos, state, 3);
        ((BaseActuatorRouterBlockEntity)pLevel.getBlockEntity(targetPos)).configure(pPos, facing);

        ActuatorArcaneBlockEntity aebe = (ActuatorArcaneBlockEntity) pLevel.getBlockEntity(pPos);
        ICanTakePlugins ictp = aebe.getTargetMachine();
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
        ActuatorArcaneBlockEntity aebe = (ActuatorArcaneBlockEntity) level.getBlockEntity(pos);
        ICanTakePlugins ictp = aebe.getTargetMachine();
        if(ictp != null)
            ictp.removePlugin(aebe);

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof ActuatorArcaneBlockEntity aebe) {
                NetworkHooks.openScreen((ServerPlayer)player, (ActuatorArcaneBlockEntity)entity, pos);
            } else {
                throw new IllegalStateException("ActuatorArcaneBlockEntity container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ActuatorArcaneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pBlockEntityType == BlockEntitiesRegistry.ACTUATOR_ARCANE_BE.get()) {
            return ActuatorArcaneBlockEntity::tick;
        }

        return null;
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    static {
        VOXEL_SHAPE_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_PLUG,
                VOXEL_SHAPE_BASE,
                VOXEL_SHAPE_BODY_LONG,
                VOXEL_SHAPE_BODY_TALL,
                VOXEL_SHAPE_PLATFORM);

        VOXEL_SHAPE_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_LONG, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_TALL, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLATFORM, 1));

        VOXEL_SHAPE_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_LONG, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_TALL, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLATFORM, 2));

        VOXEL_SHAPE_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_LONG, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_TALL, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLATFORM, 3));
    }
}
