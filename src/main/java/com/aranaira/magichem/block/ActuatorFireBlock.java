package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.ActuatorFireBlockEntity;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.routers.CentrifugeRouterBlockEntity;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.foundation.enums.CentrifugeRouterType;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ActuatorFireBlock extends BaseEntityBlock {
    public ActuatorFireBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    private static final VoxelShape
            VOXEL_SHAPE_ERROR                = Block.box(0,0,0,16,16,16),
            VOXEL_SHAPE_NORTH_WIDE           = Block.box(1,0,0,15,15,6),
            VOXEL_SHAPE_NORTH_LONG           = Block.box(4,0,0,12,15,12),
            VOXEL_SHAPE_NORTH_PIPELEFTOUTER  = Block.box(2,3,6,4,16,8),
            VOXEL_SHAPE_NORTH_PIPELEFTINNER  = Block.box(4,3,0,6,16,8),
            VOXEL_SHAPE_NORTH_PIPERIGHTOUTER = Block.box(12,3,6,14,16,8),
            VOXEL_SHAPE_NORTH_PIPERIGHTINNER = Block.box(10,3,0,12,16,8),
            VOXEL_SHAPE_NORTH_PIPECENTER     = Block.box(6,15,1,10,16,5),
            VOXEL_SHAPE_AGGREGATE_NORTH, VOXEL_SHAPE_AGGREGATE_SOUTH, VOXEL_SHAPE_AGGREGATE_EAST, VOXEL_SHAPE_AGGREGATE_WEST;
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);
        /*BlockState state = BlockRegistry.CENTRIFUGE_ROUTER.get().defaultBlockState();
        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        for(Triplet<BlockPos, CentrifugeRouterType, DevicePlugDirection> posAndType : getRouterOffsets(facing)) {
            BlockPos targetPos = pPos.offset(posAndType.getFirst());
            pLevel.setBlock(targetPos, state, 3);
            ((CentrifugeRouterBlockEntity)pLevel.getBlockEntity(targetPos)).configure(pPos, posAndType.getSecond(), facing, posAndType.getThird());
        }*/
    }

    @Override
    public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        /*Direction facing = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        for(Triplet<BlockPos, CentrifugeRouterType, DevicePlugDirection> posAndType : getRouterOffsets(facing)) {
            pLevel.destroyBlock(pPos.offset(posAndType.getFirst()), true);
        }
        */
        super.destroy(pLevel, pPos, pState);
    }

    public static List<Triplet<BlockPos, CentrifugeRouterType, DevicePlugDirection>> getRouterOffsets(Direction pFacing) {
        List<Triplet<BlockPos, CentrifugeRouterType, DevicePlugDirection>> offsets = new ArrayList<>();
        BlockPos origin = new BlockPos(0,0,0);
        if(pFacing == Direction.NORTH) {
            offsets.add(new Triplet<>(origin.west(), CentrifugeRouterType.PLUG_LEFT, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.north(), CentrifugeRouterType.PLUG_RIGHT, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.west().north(), CentrifugeRouterType.COG, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.SOUTH) {
            offsets.add(new Triplet<>(origin.east(), CentrifugeRouterType.PLUG_LEFT, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.south(), CentrifugeRouterType.PLUG_RIGHT, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.east().south(), CentrifugeRouterType.COG, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.EAST) {
            offsets.add(new Triplet<>(origin.north(), CentrifugeRouterType.PLUG_LEFT, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.east(), CentrifugeRouterType.PLUG_RIGHT, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.north().east(), CentrifugeRouterType.COG, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.WEST) {
            offsets.add(new Triplet<>(origin.south(), CentrifugeRouterType.PLUG_LEFT, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.west(), CentrifugeRouterType.PLUG_RIGHT, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.south().west(), CentrifugeRouterType.COG, DevicePlugDirection.NONE));
        }
        return offsets;
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
        return new ActuatorFireBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.CENTRIFUGE_BE.get(),
                CentrifugeBlockEntity::tick);
    }

    static {
        VOXEL_SHAPE_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_NORTH_WIDE, VOXEL_SHAPE_NORTH_LONG, VOXEL_SHAPE_NORTH_PIPELEFTINNER, VOXEL_SHAPE_NORTH_PIPELEFTOUTER,
                VOXEL_SHAPE_NORTH_PIPERIGHTINNER, VOXEL_SHAPE_NORTH_PIPERIGHTOUTER, VOXEL_SHAPE_NORTH_PIPECENTER);

        VOXEL_SHAPE_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_WIDE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_LONG, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPELEFTINNER, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPELEFTOUTER, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPERIGHTINNER, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPERIGHTOUTER, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPECENTER, 1));

        VOXEL_SHAPE_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_WIDE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_LONG, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPELEFTINNER, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPELEFTOUTER, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPERIGHTINNER, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPERIGHTOUTER, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPECENTER, 2));

        VOXEL_SHAPE_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_WIDE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_LONG, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPELEFTINNER, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPELEFTOUTER, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPERIGHTINNER, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPERIGHTOUTER, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_NORTH_PIPECENTER, 3));
    }
}
