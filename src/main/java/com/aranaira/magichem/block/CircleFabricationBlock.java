package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.CircleFabricationBlockEntity;
import com.aranaira.magichem.block.entity.routers.CircleFabricationRouterBlockEntity;
import com.aranaira.magichem.gui.CircleFabricationMenu;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.*;

public class CircleFabricationBlock extends BaseEntityBlock {

    public CircleFabricationBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    private static final VoxelShape VOXEL_SHAPE = Block.box(0,0,0,16,2,16);

    @Override
    public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        destroyRouters(pLevel, pPos, pState.getValue(FACING));

        super.destroy(pLevel, pPos, pState);
    }

    public static void destroyRouters(LevelAccessor pLevel, BlockPos pPos, Direction facing) {
        for(Pair<BlockPos, Integer> posAndType : getRouterOffsets(facing)) {
            pLevel.destroyBlock(pPos.offset(posAndType.getFirst()), true);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos();

        for (Pair<BlockPos, Integer> posAndType : getRouterOffsets(pContext.getHorizontalDirection())) {
            if(!pContext.getLevel().isEmptyBlock(pos.offset(posAndType.getFirst()))) {
                return null;
            }
        }

        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        BlockState state = BlockRegistry.CIRCLE_FABRICATION_ROUTER.get().defaultBlockState();
        Direction facing = pNewState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);

        for (Pair<BlockPos, Integer> posAndType : getRouterOffsets(facing)) {
            BlockPos targetPos = pPos.offset(posAndType.getFirst());
            if(pLevel.getBlockState(targetPos).isAir()) {
                int routerType = posAndType.getSecond();

                pLevel.setBlock(targetPos, state
                        .setValue(ROUTER_TYPE_CIRCLE_FABRICATION, routerType)
                        .setValue(FACING, facing),
                        3);
                ((CircleFabricationRouterBlockEntity) pLevel.getBlockEntity(targetPos)).configure(pPos);
            }
        }

    }

    public static List<Pair<BlockPos, Integer>> getRouterOffsets(Direction pFacing) {
        List<Pair<BlockPos, Integer>> offsets = new ArrayList<>();
        BlockPos origin = new BlockPos(0,0,0);

        if(pFacing == Direction.NORTH) {
            offsets.add(new Pair<>(origin.north(), 0));
            offsets.add(new Pair<>(origin.north().east(), 1));
            offsets.add(new Pair<>(origin.east(), 2));
            offsets.add(new Pair<>(origin.south().east(), 3));
            offsets.add(new Pair<>(origin.south(), 4));
            offsets.add(new Pair<>(origin.south().west(), 5));
            offsets.add(new Pair<>(origin.west(), 6));
            offsets.add(new Pair<>(origin.west().north(), 7));
        } else if(pFacing == Direction.EAST) {
            offsets.add(new Pair<>(origin.east(), 0));
            offsets.add(new Pair<>(origin.east().south(), 1));
            offsets.add(new Pair<>(origin.south(), 2));
            offsets.add(new Pair<>(origin.south().west(), 3));
            offsets.add(new Pair<>(origin.west(), 4));
            offsets.add(new Pair<>(origin.west().north(), 5));
            offsets.add(new Pair<>(origin.north(), 6));
            offsets.add(new Pair<>(origin.north().east(), 7));
        } else if(pFacing == Direction.SOUTH) {
            offsets.add(new Pair<>(origin.south(), 0));
            offsets.add(new Pair<>(origin.south().west(), 1));
            offsets.add(new Pair<>(origin.west(), 2));
            offsets.add(new Pair<>(origin.west().north(), 3));
            offsets.add(new Pair<>(origin.north(), 4));
            offsets.add(new Pair<>(origin.north().east(), 5));
            offsets.add(new Pair<>(origin.east(), 6));
            offsets.add(new Pair<>(origin.east().south(), 7));
        } else if(pFacing == Direction.WEST) {
            offsets.add(new Pair<>(origin.west(), 0));
            offsets.add(new Pair<>(origin.west().north(), 1));
            offsets.add(new Pair<>(origin.north(), 2));
            offsets.add(new Pair<>(origin.north().east(), 3));
            offsets.add(new Pair<>(origin.east(), 4));
            offsets.add(new Pair<>(origin.east().south(), 5));
            offsets.add(new Pair<>(origin.south(), 6));
            offsets.add(new Pair<>(origin.south().west(), 7));
        }

        return offsets;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return VOXEL_SHAPE;
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
            if(blockEntity instanceof CircleFabricationBlockEntity) {
                ((CircleFabricationBlockEntity) blockEntity).dropInventoryToWorld();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof CircleFabricationBlockEntity cfbe) {
                NetworkHooks.openScreen((ServerPlayer)player, new SimpleMenuProvider((id, playerInventory, user) -> {
                    return new CircleFabricationMenu(id, playerInventory, cfbe);
                }, Component.empty()), cfbe);
            } else {
                throw new IllegalStateException("CircleFabricationBlockEntity container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CircleFabricationBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.CIRCLE_FABRICATION_BE.get(),
                CircleFabricationBlockEntity::tick);
    }
}
