package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.MirrorLabyrinthBlockEntity;
import com.aranaira.magichem.block.entity.routers.MirrorLabyrinthRouterBlockEntity;
import com.aranaira.magichem.foundation.enums.MirrorLabyrinthRouterType;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.MathHelper;
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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;
import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_MIRROR_LABYRINTH;
import static com.aranaira.magichem.foundation.enums.MirrorLabyrinthRouterType.*;

public class MirrorLabyrinthBlock extends BaseEntityBlock {
    public static final VoxelShape
            VOXEL_SHAPE_BASE, VOXEL_SHAPE_BODY, VOXEL_SHAPE_DAIS_CONNECTOR,
            VOXEL_SHAPE_AGGREGATE_N, VOXEL_SHAPE_AGGREGATE_S, VOXEL_SHAPE_AGGREGATE_E, VOXEL_SHAPE_AGGREGATE_W;

    public MirrorLabyrinthBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new MirrorLabyrinthBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos();

        for(Pair<BlockPos, MirrorLabyrinthRouterType> posAndType : getRouterOffsets(pContext.getHorizontalDirection())) {
            if(!pContext.getLevel().isEmptyBlock(pos.offset(posAndType.getFirst()))) {
                return null;
            }
        }

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        BlockState state = BlockRegistry.MIRROR_LABYRINTH_ROUTER.get().defaultBlockState();
        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);

        for (Pair<BlockPos, MirrorLabyrinthRouterType> posAndType : getRouterOffsets(facing)) {
            BlockPos targetPos = pPos.offset(posAndType.getFirst());
            if(pLevel.getBlockState(targetPos).isAir()) {
                int routerType = MirrorLabyrinthRouterBlock.mapRouterTypeToInt(posAndType.getSecond());

                pLevel.setBlock(
                        targetPos,
                        state
                                .setValue(FACING, facing)
                                .setValue(ROUTER_TYPE_MIRROR_LABYRINTH, routerType),
                        3);
                ((MirrorLabyrinthRouterBlockEntity) pLevel.getBlockEntity(targetPos)).configure(pPos);
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof MirrorLabyrinthBlockEntity) {
                ((MirrorLabyrinthBlockEntity) blockEntity).ejectConstruct();
                ((MirrorLabyrinthBlockEntity) blockEntity).packInventoryToBlockItem();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public static List<Pair<BlockPos, MirrorLabyrinthRouterType>> getRouterOffsets(Direction pFacing) {
        List<Pair<BlockPos, MirrorLabyrinthRouterType>> offsets = new ArrayList<>();
        BlockPos origin = new BlockPos(0,0,0);
        if(pFacing == Direction.NORTH) {
            offsets.add(new Pair<>(origin.south(), DAIS));
            offsets.add(new Pair<>(origin.north(), CENTER));
            offsets.add(new Pair<>(origin.west(), LEFT_FRONT));
            offsets.add(new Pair<>(origin.east(), RIGHT_FRONT));
            offsets.add(new Pair<>(origin.north().west(), LEFT));
            offsets.add(new Pair<>(origin.north().east(), RIGHT));
            offsets.add(new Pair<>(origin.north().north(), CENTER_BACK));
            offsets.add(new Pair<>(origin.north().north().west(), LEFT_BACK));
            offsets.add(new Pair<>(origin.north().north().east(), RIGHT_BACK));
            offsets.add(new Pair<>(origin.north().above(), CONSTRUCT_LOWER));
            offsets.add(new Pair<>(origin.north().above().above(), CONSTRUCT_UPPER));
            offsets.add(new Pair<>(origin.north().offset(0,3,0), MATRIX_LOWER));
            offsets.add(new Pair<>(origin.north().offset(0,4,0), MATRIX_UPPER));
        } else if(pFacing == Direction.SOUTH) {
            offsets.add(new Pair<>(origin.north(), DAIS));
            offsets.add(new Pair<>(origin.south(), CENTER));
            offsets.add(new Pair<>(origin.east(), LEFT_FRONT));
            offsets.add(new Pair<>(origin.west(), RIGHT_FRONT));
            offsets.add(new Pair<>(origin.south().east(), LEFT));
            offsets.add(new Pair<>(origin.south().west(), RIGHT));
            offsets.add(new Pair<>(origin.south().south(), CENTER_BACK));
            offsets.add(new Pair<>(origin.south().south().east(), LEFT_BACK));
            offsets.add(new Pair<>(origin.south().south().west(), RIGHT_BACK));
            offsets.add(new Pair<>(origin.south().above(), CONSTRUCT_LOWER));
            offsets.add(new Pair<>(origin.south().above().above(), CONSTRUCT_UPPER));
            offsets.add(new Pair<>(origin.south().offset(0,3,0), MATRIX_LOWER));
            offsets.add(new Pair<>(origin.south().offset(0,4,0), MATRIX_UPPER));
        } else if(pFacing == Direction.EAST) {
            offsets.add(new Pair<>(origin.west(), DAIS));
            offsets.add(new Pair<>(origin.east(), CENTER));
            offsets.add(new Pair<>(origin.north(), LEFT_FRONT));
            offsets.add(new Pair<>(origin.south(), RIGHT_FRONT));
            offsets.add(new Pair<>(origin.east().north(), LEFT));
            offsets.add(new Pair<>(origin.east().south(), RIGHT));
            offsets.add(new Pair<>(origin.east().east(), CENTER_BACK));
            offsets.add(new Pair<>(origin.east().east().north(), LEFT_BACK));
            offsets.add(new Pair<>(origin.east().east().south(), RIGHT_BACK));
            offsets.add(new Pair<>(origin.east().above(), CONSTRUCT_LOWER));
            offsets.add(new Pair<>(origin.east().above().above(), CONSTRUCT_UPPER));
            offsets.add(new Pair<>(origin.east().offset(0,3,0), MATRIX_LOWER));
            offsets.add(new Pair<>(origin.east().offset(0,4,0), MATRIX_UPPER));
        } else if(pFacing == Direction.WEST) {
            offsets.add(new Pair<>(origin.east(), DAIS));
            offsets.add(new Pair<>(origin.west(), CENTER));
            offsets.add(new Pair<>(origin.south(), LEFT_FRONT));
            offsets.add(new Pair<>(origin.north(), RIGHT_FRONT));
            offsets.add(new Pair<>(origin.west().south(), LEFT));
            offsets.add(new Pair<>(origin.west().north(), RIGHT));
            offsets.add(new Pair<>(origin.west().west(), CENTER_BACK));
            offsets.add(new Pair<>(origin.west().west().south(), LEFT_BACK));
            offsets.add(new Pair<>(origin.west().west().north(), RIGHT_BACK));
            offsets.add(new Pair<>(origin.west().above(), CONSTRUCT_LOWER));
            offsets.add(new Pair<>(origin.west().above().above(), CONSTRUCT_UPPER));
            offsets.add(new Pair<>(origin.west().offset(0,3,0), MATRIX_LOWER));
            offsets.add(new Pair<>(origin.west().offset(0,4,0), MATRIX_UPPER));
        }
        return offsets;
    }

    public static void destroyRouters(LevelAccessor pLevel, BlockPos pPos, Direction pFacing) {
        for(Pair<BlockPos, MirrorLabyrinthRouterType> posAndType : getRouterOffsets(pFacing)) {
            pLevel.destroyBlock(pPos.offset(posAndType.getFirst()), true);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, BlockEntitiesRegistry.MIRROR_LABYRINTH_BE.get(),
                MirrorLabyrinthBlockEntity::tick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof MirrorLabyrinthBlockEntity) {
                boolean holdingPowerSpike = player.getInventory().getSelected().getItem() == BlockRegistry.POWER_SPIKE.get().asItem();

                if(holdingPowerSpike) {
                    return InteractionResult.PASS;
                } else {
                    NetworkHooks.openScreen((ServerPlayer) player, (MirrorLabyrinthBlockEntity) entity, pos);
                }
            } else {
                throw new IllegalStateException("MirrorLabyrinthBlockEntity container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        BlockState state = pLevel.getBlockState(pPos);

        if (state.getBlock() == BlockRegistry.MIRROR_LABYRINTH.get()) {
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

            //Again, switch statements always default here and I have no idea why
            if (facing == Direction.NORTH) {
                return VOXEL_SHAPE_AGGREGATE_N;
            }
            else if (facing == Direction.EAST) {
                return VOXEL_SHAPE_AGGREGATE_E;
            }
            else if (facing == Direction.SOUTH) {
                return VOXEL_SHAPE_AGGREGATE_S;
            }
            else if (facing == Direction.WEST) {
                return VOXEL_SHAPE_AGGREGATE_W;
            }
        }

        return MirrorLabyrinthRouterBlock.VOXEL_SHAPE_DEFAULT;
    }

    static {
        VOXEL_SHAPE_BASE = Block.box(0, 0, 0, 16, 7, 16);
        VOXEL_SHAPE_BODY = Block.box(0, 7, 0, 16, 16, 8.9);
        VOXEL_SHAPE_DAIS_CONNECTOR = Block.box(5, 7, 0, 11, 14, 16);

        VOXEL_SHAPE_AGGREGATE_N = Shapes.or(
                VOXEL_SHAPE_BASE,
                VOXEL_SHAPE_BODY,
                VOXEL_SHAPE_DAIS_CONNECTOR
        );

        VOXEL_SHAPE_AGGREGATE_E = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY, 1),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_DAIS_CONNECTOR, 1)
        );

        VOXEL_SHAPE_AGGREGATE_S = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY, 2),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_DAIS_CONNECTOR, 2)
        );

        VOXEL_SHAPE_AGGREGATE_W = Shapes.or(
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BASE, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_BODY, 3),
                MathHelper.rotateVoxelShape(VOXEL_SHAPE_DAIS_CONNECTOR, 3)
        );
    }
}