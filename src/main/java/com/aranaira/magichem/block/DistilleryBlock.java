package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.block.entity.routers.CentrifugeRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.DistilleryRouterBlockEntity;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.foundation.enums.CentrifugeRouterType;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.DistilleryRouterType;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.MathHelper;
import com.mna.api.affinity.Affinity;
import com.mna.api.blocks.ISpellInteractibleBlock;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.collections.Components;
import com.mna.spells.SpellsInit;
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

public class DistilleryBlock extends BaseEntityBlock implements ISpellInteractibleBlock<DistilleryBlock> {
    public DistilleryBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    private static final VoxelShape
            VOXEL_SHAPE_ERROR,

            VOXEL_SHAPE_PLUG_NORTH, VOXEL_SHAPE_BODY_NORTH, VOXEL_SHAPE_MOUNT_NORTH, VOXEL_SHAPE_FURNACE_NORTH,
            VOXEL_SHAPE_TANK_NORTH, VOXEL_SHAPE_PIPE_LEFT_NORTH, VOXEL_SHAPE_PIPE_RIGHT_NORTH,

            VOXEL_SHAPE_AGGREGATE_NORTH, VOXEL_SHAPE_AGGREGATE_EAST, VOXEL_SHAPE_AGGREGATE_SOUTH, VOXEL_SHAPE_AGGREGATE_WEST;
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos();

        for(Triplet<BlockPos, DistilleryRouterType, DevicePlugDirection> posAndType : getRouterOffsets(pContext.getHorizontalDirection())) {
            if(!pContext.getLevel().isEmptyBlock(pos.offset(posAndType.getFirst()))) {
                return null;
            }
        }

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        BlockState state = BlockRegistry.DISTILLERY_ROUTER.get().defaultBlockState();
        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);

        for (Triplet<BlockPos, DistilleryRouterType, DevicePlugDirection> posAndType : getRouterOffsets(facing)) {
            BlockPos targetPos = pPos.offset(posAndType.getFirst());
            if(pLevel.getBlockState(targetPos).isAir()) {
                pLevel.setBlock(targetPos, state, 3);
                ((DistilleryRouterBlockEntity) pLevel.getBlockEntity(targetPos)).configure(pPos, posAndType.getSecond(), facing, posAndType.getThird());
            }
        }
    }

    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        Direction facing = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        for(Triplet<BlockPos, DistilleryRouterType, DevicePlugDirection> posAndType : getRouterOffsets(facing)) {
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
        for(Triplet<BlockPos, DistilleryRouterType, DevicePlugDirection> posAndType : getRouterOffsets(pFacing)) {
            pLevel.destroyBlock(pPos.offset(posAndType.getFirst()), true);
        }
    }

    public static List<Triplet<BlockPos, DistilleryRouterType, DevicePlugDirection>> getRouterOffsets(Direction pFacing) {
        List<Triplet<BlockPos, DistilleryRouterType, DevicePlugDirection>> offsets = new ArrayList<>();
        BlockPos origin = new BlockPos(0,0,0);
        if(pFacing == Direction.NORTH) {
            offsets.add(new Triplet<>(origin.west(), DistilleryRouterType.PLUG_LEFT, DevicePlugDirection.WEST));
            offsets.add(new Triplet<>(origin.above(), DistilleryRouterType.ABOVE, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.west().above(), DistilleryRouterType.ABOVE_LEFT, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.SOUTH) {
            offsets.add(new Triplet<>(origin.east(), DistilleryRouterType.PLUG_LEFT, DevicePlugDirection.EAST));
            offsets.add(new Triplet<>(origin.above(), DistilleryRouterType.ABOVE, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.east().above(), DistilleryRouterType.ABOVE_LEFT, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.EAST) {
            offsets.add(new Triplet<>(origin.north(), DistilleryRouterType.PLUG_LEFT, DevicePlugDirection.NORTH));
            offsets.add(new Triplet<>(origin.above(), DistilleryRouterType.ABOVE, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.north().above(), DistilleryRouterType.ABOVE_LEFT, DevicePlugDirection.NONE));
        } else if(pFacing == Direction.WEST) {
            offsets.add(new Triplet<>(origin.south(), DistilleryRouterType.PLUG_LEFT, DevicePlugDirection.SOUTH));
            offsets.add(new Triplet<>(origin.above(), DistilleryRouterType.ABOVE, DevicePlugDirection.NONE));
            offsets.add(new Triplet<>(origin.south().above(), DistilleryRouterType.ABOVE_LEFT, DevicePlugDirection.NONE));
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
        pBuilder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof DistilleryBlockEntity) {
                ((DistilleryBlockEntity) blockEntity).packInventoryToBlockItem();
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
            if(entity instanceof DistilleryBlockEntity) {
                NetworkHooks.openScreen((ServerPlayer)player, (DistilleryBlockEntity)entity, pos);
            } else {
                throw new IllegalStateException("DistilleryBlockEntity container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DistilleryBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.DISTILLERY_BE.get(),
                DistilleryBlockEntity::tick);
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    static {
        VOXEL_SHAPE_ERROR = Block.box(4, 4, 4, 12, 12, 12);

        VOXEL_SHAPE_BODY_NORTH = Block.box(0, 0, 2, 16, 8, 14);
        VOXEL_SHAPE_PLUG_NORTH = Block.box(12, 0, 0, 16, 16, 16);
        VOXEL_SHAPE_TANK_NORTH = Block.box(3, 13, 6, 7, 16, 10);
        VOXEL_SHAPE_MOUNT_NORTH = Block.box(2, 8, 5, 12, 12, 11);
        VOXEL_SHAPE_FURNACE_NORTH = Block.box(0, 0, 1, 6, 10, 15);
        VOXEL_SHAPE_PIPE_LEFT_NORTH = Block.box(0, 10, 5, 2, 16, 7);
        VOXEL_SHAPE_PIPE_RIGHT_NORTH = Block.box(7, 12, 7, 10, 16, 9);

        VOXEL_SHAPE_AGGREGATE_NORTH = Shapes.or(
                VOXEL_SHAPE_BODY_NORTH,
                VOXEL_SHAPE_PLUG_NORTH,
                VOXEL_SHAPE_TANK_NORTH,
                VOXEL_SHAPE_MOUNT_NORTH,
                VOXEL_SHAPE_FURNACE_NORTH,
                VOXEL_SHAPE_PIPE_LEFT_NORTH,
                VOXEL_SHAPE_PIPE_RIGHT_NORTH
                );

        VOXEL_SHAPE_AGGREGATE_EAST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_MOUNT_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FURNACE_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_LEFT_NORTH, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_RIGHT_NORTH, 1)
                );

        VOXEL_SHAPE_AGGREGATE_SOUTH = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_MOUNT_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FURNACE_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_LEFT_NORTH, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_RIGHT_NORTH, 2)
                );

        VOXEL_SHAPE_AGGREGATE_WEST = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PLUG_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_TANK_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_MOUNT_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_FURNACE_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_LEFT_NORTH, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_PIPE_RIGHT_NORTH, 3)
                );
    }

    @Override
    public boolean onHitBySpell(Level level, BlockPos blockPos, ISpellDefinition iSpellDefinition) {
        for(IModifiedSpellPart isp : iSpellDefinition.getComponents()){
            if(isp.getPart().equals(Components.FIRE_DAMAGE)) {
                float damage = isp.getValue(Attribute.DAMAGE);
                float duration = isp.getValue(Attribute.DURATION);
                BlockEntity be = level.getBlockEntity(blockPos);
                if(be instanceof DistilleryBlockEntity dbe) {
                    dbe.setHeat(Math.round(damage * duration * 20));
                    return true;
                }
            }
        }
        return false;
    }
}
