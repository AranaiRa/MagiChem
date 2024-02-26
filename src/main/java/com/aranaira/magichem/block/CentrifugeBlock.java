package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.routers.CentrifugeRouterBlockEntity;
import com.aranaira.magichem.foundation.enums.CentrifugeRouterType;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mojang.datafixers.util.Pair;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CentrifugeBlock extends BaseEntityBlock {
    public CentrifugeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    private static final VoxelShape
            VOXEL_SHAPE_NORTH = Block.box(0,0,0,14,8,14),
            VOXEL_SHAPE_SOUTH = Block.box(2,0,2,16,8,16),
            VOXEL_SHAPE_EAST = Block.box(2,0,0,16,8,14),
            VOXEL_SHAPE_WEST = Block.box(0,0,2,14,8,16),
            VOXEL_SHAPE_ERROR = Block.box(4,4,4,12,12,12);;
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);
        BlockState state = BlockRegistry.CENTRIFUGE_ROUTER.get().defaultBlockState();
        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        for(Pair<BlockPos, CentrifugeRouterType> posAndType : getRouterOffsets(facing)) {
            BlockPos targetPos = pPos.offset(posAndType.getFirst());
            pLevel.setBlock(targetPos, state, 3);
            ((CentrifugeRouterBlockEntity)pLevel.getBlockEntity(targetPos)).configure(pPos, posAndType.getSecond(), facing);
        }
    }

    @Override
    public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        Direction facing = pState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        for(Pair<BlockPos, CentrifugeRouterType> posAndType : getRouterOffsets(facing)) {
            pLevel.destroyBlock(pPos.offset(posAndType.getFirst()), true);
        }

        super.destroy(pLevel, pPos, pState);
    }

    public static List<Pair<BlockPos, CentrifugeRouterType>> getRouterOffsets(Direction pFacing) {
        List<Pair<BlockPos, CentrifugeRouterType>> offsets = new ArrayList<>();
        BlockPos origin = new BlockPos(0,0,0);
        if(pFacing == Direction.NORTH) {
            offsets.add(new Pair<>(origin.west(), CentrifugeRouterType.PLUG_LEFT));
            offsets.add(new Pair<>(origin.north(), CentrifugeRouterType.PLUG_RIGHT));
            offsets.add(new Pair<>(origin.west().north(), CentrifugeRouterType.COG));
        } else if(pFacing == Direction.SOUTH) {
            offsets.add(new Pair<>(origin.east(), CentrifugeRouterType.PLUG_LEFT));
            offsets.add(new Pair<>(origin.south(), CentrifugeRouterType.PLUG_RIGHT));
            offsets.add(new Pair<>(origin.east().south(), CentrifugeRouterType.COG));
        } else if(pFacing == Direction.EAST) {
            offsets.add(new Pair<>(origin.north(), CentrifugeRouterType.PLUG_LEFT));
            offsets.add(new Pair<>(origin.east(), CentrifugeRouterType.PLUG_RIGHT));
            offsets.add(new Pair<>(origin.north().east(), CentrifugeRouterType.COG));
        } else if(pFacing == Direction.WEST) {
            offsets.add(new Pair<>(origin.south(), CentrifugeRouterType.PLUG_LEFT));
            offsets.add(new Pair<>(origin.west(), CentrifugeRouterType.PLUG_RIGHT));
            offsets.add(new Pair<>(origin.south().west(), CentrifugeRouterType.COG));
        }
        return offsets;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return switch(state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            default -> VOXEL_SHAPE_ERROR;
            case NORTH -> VOXEL_SHAPE_NORTH;
            case SOUTH -> VOXEL_SHAPE_SOUTH;
            case WEST -> VOXEL_SHAPE_WEST;
            case EAST -> VOXEL_SHAPE_EAST;
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
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof CentrifugeBlockEntity) {
                ((CentrifugeBlockEntity) blockEntity).dropInventoryToWorld();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof CentrifugeBlockEntity) {
                NetworkHooks.openScreen((ServerPlayer)player, (CentrifugeBlockEntity)entity, pos);
            } else {
                throw new IllegalStateException("CentrifugeBlockEntity container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CentrifugeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.CENTRIFUGE_BE.get(),
                CentrifugeBlockEntity::tick);
    }
}
