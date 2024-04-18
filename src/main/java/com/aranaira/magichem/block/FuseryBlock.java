package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.FuseryBlockEntity;
import com.aranaira.magichem.block.entity.routers.CentrifugeRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.FuseryRouterBlockEntity;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.foundation.enums.CentrifugeRouterType;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.FuseryRouterType;
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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
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

import java.util.ArrayList;
import java.util.List;

public class FuseryBlock extends BaseEntityBlock {
    public FuseryBlock(Properties properties) {
        super(properties);
    }

    private static final VoxelShape VOXEL_SHAPE;
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return switch(state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> VOXEL_SHAPE;
            case EAST -> MathHelper.rotateVoxelShape(VOXEL_SHAPE, 1);
            case SOUTH -> MathHelper.rotateVoxelShape(VOXEL_SHAPE, 2);
            case WEST -> MathHelper.rotateVoxelShape(VOXEL_SHAPE, 3);
            default -> Shapes.box(4, 4, 4, 12, 12, 12);
        };
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
            if(blockEntity instanceof FuseryBlockEntity) {
                ((FuseryBlockEntity) blockEntity).dropInventoryToWorld();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof FuseryBlockEntity) {
                NetworkHooks.openScreen((ServerPlayer)player, (FuseryBlockEntity)entity, pos);
            } else {
                throw new IllegalStateException("AdmixerBlockEntity container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FuseryBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.FUSERY_BE.get(),
                FuseryBlockEntity::tick);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @Override
    public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        Direction facing = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        destroyRouters(pLevel, pPos, facing);

        super.destroy(pLevel, pPos, pState);
    }

    public static void destroyRouters(LevelAccessor pLevel, BlockPos pPos, Direction pFacing) {
        if(pFacing != null) {
            for (Triplet<BlockPos, FuseryRouterType, DevicePlugDirection> posAndType : getRouterOffsets(pFacing)) {
                pLevel.destroyBlock(pPos.offset(posAndType.getFirst()), true);
            }
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos();

        for(Triplet<BlockPos, FuseryRouterType, DevicePlugDirection> posAndType : getRouterOffsets(pContext.getHorizontalDirection())) {
            if(!pContext.getLevel().isEmptyBlock(pos.offset(posAndType.getFirst()))) {
                return null;
            }
        }

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        BlockState state = BlockRegistry.FUSERY_ROUTER.get().defaultBlockState();
        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);

        for (Triplet<BlockPos, FuseryRouterType, DevicePlugDirection> posAndType : getRouterOffsets(facing)) {
            BlockPos targetPos = pPos.offset(posAndType.getFirst());
            if(pLevel.getBlockState(targetPos).isAir()) {
                pLevel.setBlock(targetPos, state, 3);
                ((FuseryRouterBlockEntity) pLevel.getBlockEntity(targetPos)).configure(pPos, posAndType.getSecond(), facing, posAndType.getThird());
            }
        }
    }

    public static List<Triplet<BlockPos, FuseryRouterType, DevicePlugDirection>> getRouterOffsets(Direction pFacing) {
        List<Triplet<BlockPos, FuseryRouterType, DevicePlugDirection>> offsets = new ArrayList<>();
        BlockPos origin = new BlockPos(0,0,0);
        if(pFacing == Direction.NORTH) {
            offsets.add(new Triplet<>(origin.west(), FuseryRouterType.PLUG_LEFT, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.north(), FuseryRouterType.PLUG_RIGHT, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.west().north(), FuseryRouterType.COG, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.north().above(), FuseryRouterType.TANK_RIGHT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.west().north().above(), FuseryRouterType.TANK_ACROSS, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.SOUTH) {
            offsets.add(new Triplet<>(origin.east(), FuseryRouterType.PLUG_LEFT, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.south(), FuseryRouterType.PLUG_RIGHT, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.east().south(), FuseryRouterType.COG, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.south().above(), FuseryRouterType.TANK_RIGHT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.east().south().above(), FuseryRouterType.TANK_ACROSS, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.EAST) {
            offsets.add(new Triplet<>(origin.north(), FuseryRouterType.PLUG_LEFT, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.east(), FuseryRouterType.PLUG_RIGHT, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.north().east(), FuseryRouterType.COG, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.east().above(), FuseryRouterType.TANK_RIGHT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.north().east().above(), FuseryRouterType.TANK_ACROSS, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.WEST) {
            offsets.add(new Triplet<>(origin.south(), FuseryRouterType.PLUG_LEFT, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.west(), FuseryRouterType.PLUG_RIGHT, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.south().west(), FuseryRouterType.COG, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.west().above(), FuseryRouterType.TANK_RIGHT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.south().west().above(), FuseryRouterType.TANK_ACROSS, DevicePlugDirection.NONE));
        }
        return offsets;
    }

    static {
        VOXEL_SHAPE = Block.box(0.0D, 0.0D,  0.0D, 14.0D, 8.0D, 14.0D);
    }
}
