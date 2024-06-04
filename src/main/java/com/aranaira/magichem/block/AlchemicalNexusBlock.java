package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.routers.CentrifugeRouterBlockEntity;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.foundation.enums.CentrifugeRouterType;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AlchemicalNexusBlock extends BaseEntityBlock {
    public AlchemicalNexusBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    private static final VoxelShape
            DO_THIS_LATER = Block.box(0,0,0,14,8,14);
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos();

//        for(Triplet<BlockPos, CentrifugeRouterType, DevicePlugDirection> posAndType : getRouterOffsets(pContext.getHorizontalDirection())) {
//            if(!pContext.getLevel().isEmptyBlock(pos.offset(posAndType.getFirst()))) {
//                return null;
//            }
//        }

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

//    @Override
//    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
//        BlockState state = BlockRegistry.CENTRIFUGE_ROUTER.get().defaultBlockState();
//        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);
//
//        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);
//
//        for (Triplet<BlockPos, CentrifugeRouterType, DevicePlugDirection> posAndType : getRouterOffsets(facing)) {
//            BlockPos targetPos = pPos.offset(posAndType.getFirst());
//            if(pLevel.getBlockState(targetPos).isAir()) {
//                pLevel.setBlock(targetPos, state, 3);
//                ((CentrifugeRouterBlockEntity) pLevel.getBlockEntity(targetPos)).configure(pPos, posAndType.getSecond(), facing, posAndType.getThird());
//            }
//        }
//    }

//    @Override
//    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
//        Direction facing = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);
//
//        for(Triplet<BlockPos, CentrifugeRouterType, DevicePlugDirection> posAndType : getRouterOffsets(facing)) {
//            pLevel.destroyBlock(pPos.offset(posAndType.getFirst()), true);
//        }
//
//        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
//    }

//    @Override
//    public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
//        Direction facing = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);
//
//        destroyRouters(pLevel, pPos, facing);
//
//        super.destroy(pLevel, pPos, pState);
//    }

//    public static void destroyRouters(LevelAccessor pLevel, BlockPos pPos, Direction pFacing) {
//        for(Triplet<BlockPos, CentrifugeRouterType, DevicePlugDirection> posAndType : getRouterOffsets(pFacing)) {
//            pLevel.destroyBlock(pPos.offset(posAndType.getFirst()), true);
//        }
//    }

//    public static List<Triplet<BlockPos, CentrifugeRouterType, DevicePlugDirection>> getRouterOffsets(Direction pFacing) {
//        List<Triplet<BlockPos, CentrifugeRouterType, DevicePlugDirection>> offsets = new ArrayList<>();
//        BlockPos origin = new BlockPos(0,0,0);
//        if(pFacing == Direction.NORTH) {
//            offsets.add(new Triplet<>(origin.west(), CentrifugeRouterType.PLUG_LEFT, DevicePlugDirection.SOUTH));
//            offsets.add(new Triplet<>(origin.north(), CentrifugeRouterType.PLUG_RIGHT, DevicePlugDirection.EAST));
//            offsets.add(new Triplet<>(origin.west().north(), CentrifugeRouterType.COG, DevicePlugDirection.NONE));
//        } else if(pFacing == Direction.SOUTH) {
//            offsets.add(new Triplet<>(origin.east(), CentrifugeRouterType.PLUG_LEFT, DevicePlugDirection.NORTH));
//            offsets.add(new Triplet<>(origin.south(), CentrifugeRouterType.PLUG_RIGHT, DevicePlugDirection.WEST));
//            offsets.add(new Triplet<>(origin.east().south(), CentrifugeRouterType.COG, DevicePlugDirection.NONE));
//        } else if(pFacing == Direction.EAST) {
//            offsets.add(new Triplet<>(origin.north(), CentrifugeRouterType.PLUG_LEFT, DevicePlugDirection.WEST));
//            offsets.add(new Triplet<>(origin.east(), CentrifugeRouterType.PLUG_RIGHT, DevicePlugDirection.SOUTH));
//            offsets.add(new Triplet<>(origin.north().east(), CentrifugeRouterType.COG, DevicePlugDirection.NONE));
//        } else if(pFacing == Direction.WEST) {
//            offsets.add(new Triplet<>(origin.south(), CentrifugeRouterType.PLUG_LEFT, DevicePlugDirection.EAST));
//            offsets.add(new Triplet<>(origin.west(), CentrifugeRouterType.PLUG_RIGHT, DevicePlugDirection.NORTH));
//            offsets.add(new Triplet<>(origin.south().west(), CentrifugeRouterType.COG, DevicePlugDirection.NONE));
//        }
//        return offsets;
//    }

//    @Override
//    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
//        return switch(state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
//            default -> VOXEL_SHAPE_ERROR;
//            case NORTH -> VOXEL_SHAPE_NORTH;
//            case SOUTH -> VOXEL_SHAPE_SOUTH;
//            case WEST -> VOXEL_SHAPE_WEST;
//            case EAST -> VOXEL_SHAPE_EAST;
//        };
//    }

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
        pBuilder.add(FACING);
    }

    /* BLOCK ENTITY STUFF BELOW THIS POINT*/

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof AlchemicalNexusBlockEntity anbe) {
                //anbe.dropInventoryToWorld();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof AlchemicalNexusBlockEntity anbe) {
                NetworkHooks.openScreen((ServerPlayer)player, anbe, pos);
            } else {
                throw new IllegalStateException("AlchemicalNexusBlockEntity container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlchemicalNexusBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.ALCHEMICAL_NEXUS_BE.get(),
                AlchemicalNexusBlockEntity::tick);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }
}
