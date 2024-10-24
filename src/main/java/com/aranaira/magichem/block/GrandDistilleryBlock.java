package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.GrandDistilleryBlockEntity;
import com.aranaira.magichem.block.entity.routers.GrandDistilleryRouterBlockEntity;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.GrandDistilleryRouterType;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_GRAND_DISTILLERY;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.HAS_LABORATORY_UPGRADE;
import static com.aranaira.magichem.foundation.enums.GrandDistilleryRouterType.*;

public class GrandDistilleryBlock extends BaseEntityBlock {

    public GrandDistilleryBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(HAS_LABORATORY_UPGRADE, false)
        );
    }

    private static final VoxelShape
        VOXEL_SHAPE_ERROR,

        VOXEL_SHAPE_CORE_BASE, VOXEL_SHAPE_CORE_BRIDGE, VOXEL_SHAPE_CORE_BACKBOARD,

        VOXEL_SHAPE_AGGREGATE_NORTH, VOXEL_SHAPE_AGGREGATE_EAST, VOXEL_SHAPE_AGGREGATE_SOUTH, VOXEL_SHAPE_AGGREGATE_WEST;
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos();

        for(Triplet<BlockPos, GrandDistilleryRouterType, DevicePlugDirection> posAndType : getRouterOffsets(pContext.getHorizontalDirection())) {
            if(!pContext.getLevel().isEmptyBlock(pos.offset(posAndType.getFirst()))) {
                return null;
            }
        }

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        BlockState state = BlockRegistry.GRAND_DISTILLERY_ROUTER.get().defaultBlockState();
        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);

        for (Triplet<BlockPos, GrandDistilleryRouterType, DevicePlugDirection> posAndType : getRouterOffsets(facing)) {
            BlockPos targetPos = pPos.offset(posAndType.getFirst());
            if(pLevel.getBlockState(targetPos).isAir()) {
                int routerType = GrandDistilleryRouterBlock.mapRouterTypeToInt(posAndType.getSecond());

                pLevel.setBlock(
                        targetPos,
                        state
                                .setValue(FACING, facing)
                                .setValue(ROUTER_TYPE_GRAND_DISTILLERY, routerType),
                        3);
                ((GrandDistilleryRouterBlockEntity) pLevel.getBlockEntity(targetPos)).configure(pPos, posAndType.getThird());
            }
        }
    }

    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        Direction facing = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        for(Triplet<BlockPos, GrandDistilleryRouterType, DevicePlugDirection> posAndType : getRouterOffsets(facing)) {
            pLevel.destroyBlock(pPos.offset(posAndType.getFirst()), true);
        }

        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    @Override
    public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        Direction facing = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        destroyRouters(pLevel, pPos, facing);

        super.destroy(pLevel, pPos, pState);
    }

    public static void destroyRouters(LevelAccessor pLevel, BlockPos pPos, Direction pFacing) {
        for(Triplet<BlockPos, GrandDistilleryRouterType, DevicePlugDirection> posAndType : getRouterOffsets(pFacing)) {
            pLevel.destroyBlock(pPos.offset(posAndType.getFirst()), true);
        }
    }

    public static List<Triplet<BlockPos, GrandDistilleryRouterType, DevicePlugDirection>> getRouterOffsets(Direction pFacing) {
        List<Triplet<BlockPos, GrandDistilleryRouterType, DevicePlugDirection>> offsets = new ArrayList<>();
        BlockPos origin = new BlockPos(0,0,0);
        if(pFacing == Direction.NORTH) {
            offsets.add(new Triplet<>(origin.south(), DAIS, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.north(), BACK, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.south().west(), PLUG_FRONT_LEFT, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.south().east(), PLUG_FRONT_RIGHT, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.west(), PLUG_MID_LEFT, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.east(), PLUG_MID_RIGHT, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.north().west(), PLUG_BACK_LEFT, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.north().east(), PLUG_BACK_RIGHT, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.above(), TANK_MID_FRONT_CENTER, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().west(), TANK_MID_FRONT_LEFT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().east(), TANK_MID_FRONT_RIGHT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().north(), TANK_MID_REAR_CENTER, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().north().west(), TANK_MID_REAR_LEFT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().north().east(), TANK_MID_REAR_RIGHT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().above(), TANK_TOP_FRONT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().above().north(), TANK_TOP_REAR_CENTER, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().above().north().west(), TANK_TOP_REAR_LEFT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().above().north().east(), TANK_TOP_REAR_RIGHT, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.SOUTH) {
            offsets.add(new Triplet<>(origin.north(), DAIS, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.south(), BACK, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.north().east(), PLUG_FRONT_LEFT, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.north().west(), PLUG_FRONT_RIGHT, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.east(), PLUG_MID_LEFT, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.west(), PLUG_MID_RIGHT, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.south().east(), PLUG_BACK_LEFT, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.south().west(), PLUG_BACK_RIGHT, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.above(), TANK_MID_FRONT_CENTER, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().east(), TANK_MID_FRONT_LEFT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().west(), TANK_MID_FRONT_RIGHT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().south(), TANK_MID_REAR_CENTER, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().south().east(), TANK_MID_REAR_LEFT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().south().west(), TANK_MID_REAR_RIGHT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().above(), TANK_TOP_FRONT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().above().south(), TANK_TOP_REAR_CENTER, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().above().south().east(), TANK_TOP_REAR_LEFT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().above().south().west(), TANK_TOP_REAR_RIGHT, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.EAST) {
            offsets.add(new Triplet<>(origin.west(), DAIS, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.east(), BACK, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.west().north(), PLUG_FRONT_LEFT, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.west().south(), PLUG_FRONT_RIGHT, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.north(), PLUG_MID_LEFT, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.south(), PLUG_MID_RIGHT, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.east().north(), PLUG_BACK_LEFT, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.east().south(), PLUG_BACK_RIGHT, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.above(), TANK_MID_FRONT_CENTER, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().north(), TANK_MID_FRONT_LEFT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().south(), TANK_MID_FRONT_RIGHT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().east(), TANK_MID_REAR_CENTER, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().east().north(), TANK_MID_REAR_LEFT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().east().south(), TANK_MID_REAR_RIGHT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().above(), TANK_TOP_FRONT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().above().east(), TANK_TOP_REAR_CENTER, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().above().east().north(), TANK_TOP_REAR_LEFT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().above().east().south(), TANK_TOP_REAR_RIGHT, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.WEST) {
            offsets.add(new Triplet<>(origin.east(), DAIS, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.west(), BACK, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.east().south(), PLUG_FRONT_LEFT, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.east().north(), PLUG_FRONT_RIGHT, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.south(), PLUG_MID_LEFT, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.north(), PLUG_MID_RIGHT, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.west().south(), PLUG_BACK_LEFT, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.west().north(), PLUG_BACK_RIGHT, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.above(), TANK_MID_FRONT_CENTER, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().south(), TANK_MID_FRONT_LEFT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().north(), TANK_MID_FRONT_RIGHT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().west(), TANK_MID_REAR_CENTER, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().west().south(), TANK_MID_REAR_LEFT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().west().north(), TANK_MID_REAR_RIGHT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().above(), TANK_TOP_FRONT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().above().west(), TANK_TOP_REAR_CENTER, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().above().west().south(), TANK_TOP_REAR_LEFT, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.above().above().west().north(), TANK_TOP_REAR_RIGHT, DevicePlugDirection.NONE));
        }
        return offsets;
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
        pBuilder.add(FACING, HAS_LABORATORY_UPGRADE);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof GrandDistilleryBlockEntity) {
                ((GrandDistilleryBlockEntity) blockEntity).packInventoryToBlockItem();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return switch(state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            default -> VOXEL_SHAPE_ERROR;
            case NORTH -> VOXEL_SHAPE_AGGREGATE_NORTH;
            case EAST -> VOXEL_SHAPE_AGGREGATE_EAST;
            case SOUTH -> VOXEL_SHAPE_AGGREGATE_SOUTH;
            case WEST -> VOXEL_SHAPE_AGGREGATE_WEST;
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);

            boolean holdingLabCharm = player.getInventory().getSelected().getItem() == ItemRegistry.LABORATORY_CHARM.get();
            boolean holdingPowerSpike = player.getInventory().getSelected().getItem() == BlockRegistry.POWER_SPIKE.get().asItem();
            boolean holdingCleaningBrush = player.getInventory().getSelected().getItem() == ItemRegistry.CLEANING_BRUSH.get();

            if(holdingLabCharm)
                return InteractionResult.PASS;
            if(!holdingCleaningBrush) {
                if (holdingPowerSpike) {
                    return InteractionResult.PASS;
                } else {
                    if (entity instanceof GrandDistilleryBlockEntity) {
                        NetworkHooks.openScreen((ServerPlayer) player, (GrandDistilleryBlockEntity) entity, pos);
                    } else {
                        throw new IllegalStateException("GrandDistilleryBlockEntity container provider is missing!");
                    }
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new GrandDistilleryBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.GRAND_DISTILLERY_BE.get(),
                GrandDistilleryBlockEntity::tick);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    static {
        VOXEL_SHAPE_ERROR = Block.box(4, 4, 4, 12, 12, 12);

        VOXEL_SHAPE_CORE_BASE = Block.box(0, 0, 0, 16, 8, 16);
        VOXEL_SHAPE_CORE_BRIDGE = Block.box(5, 0, 0, 11, 13, 16);
        VOXEL_SHAPE_CORE_BACKBOARD = Block.box(0, 0, 0, 16, 16, 3);

        VOXEL_SHAPE_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_CORE_BASE,
                VOXEL_SHAPE_CORE_BRIDGE,
                VOXEL_SHAPE_CORE_BACKBOARD
        );

        VOXEL_SHAPE_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CORE_BASE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CORE_BRIDGE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CORE_BACKBOARD, 1)
        );

        VOXEL_SHAPE_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CORE_BASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CORE_BRIDGE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CORE_BACKBOARD, 2)
        );

        VOXEL_SHAPE_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CORE_BASE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CORE_BRIDGE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_CORE_BACKBOARD, 3)
        );
    }
}