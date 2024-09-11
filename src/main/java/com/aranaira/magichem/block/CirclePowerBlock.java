package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.CirclePowerBlockEntity;
import com.aranaira.magichem.block.entity.routers.CirclePowerRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.ROUTER_TYPE_CIRCLE_POWER;

public class CirclePowerBlock extends BaseEntityBlock {

    public CirclePowerBlock(Properties properties) {
        super(properties);
    }

    private static final VoxelShape VOXEL_SHAPE = Block.box(0,0,0,16,8,16);

    @Override
    public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        destroyRouters(pLevel, pPos, null);

        super.destroy(pLevel, pPos, pState);
    }

    public static void destroyRouters(LevelAccessor pLevel, BlockPos pPos, Direction facing) {
        for(Pair<BlockPos, Integer> posAndType : getRouterOffsets()) {
            pLevel.destroyBlock(pPos.offset(posAndType.getFirst()), true);
        }
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

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos pos = pContext.getClickedPos();

        for (Pair<BlockPos, Integer> posAndType : getRouterOffsets()) {
            if(!pContext.getLevel().isEmptyBlock(pos.offset(posAndType.getFirst()))) {
                return null;
            }
        }

        return this.defaultBlockState();
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pState, pLevel, pPos, pOldState, pMovedByPiston);

        BlockState state = BlockRegistry.CIRCLE_POWER_ROUTER.get().defaultBlockState();

        for (Pair<BlockPos, Integer> posAndType : getRouterOffsets()) {
            BlockPos targetPos = pPos.offset(posAndType.getFirst());
            if(pLevel.getBlockState(targetPos).isAir()) {
                int routerType = posAndType.getSecond();

                pLevel.setBlock(targetPos, state.setValue(ROUTER_TYPE_CIRCLE_POWER, routerType), 3);
                ((CirclePowerRouterBlockEntity) pLevel.getBlockEntity(targetPos)).configure(pPos);
            }
        }

    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof CirclePowerBlockEntity) {
                ((CirclePowerBlockEntity) blockEntity).dropInventoryToWorld();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof CirclePowerBlockEntity) {
                boolean holdingPowerSpike = player.getInventory().getSelected().getItem() == BlockRegistry.POWER_SPIKE.get().asItem();

                if(holdingPowerSpike) {
                    return InteractionResult.PASS;
                } else {
                    NetworkHooks.openScreen((ServerPlayer) player, (CirclePowerBlockEntity) entity, pos);
                }
            } else {
                throw new IllegalStateException("MagicCircleBlockEntity container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CirclePowerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.CIRCLE_POWER_BE.get(),
                CirclePowerBlockEntity::tick);
    }

    public static List<Pair<BlockPos, Integer>> getRouterOffsets() {
        List<Pair<BlockPos, Integer>> offsets = new ArrayList<>();
        BlockPos origin = new BlockPos(0,0,0);

        offsets.add(new Pair<>(origin.north(), 0));
        offsets.add(new Pair<>(origin.north().east(), 1));
        offsets.add(new Pair<>(origin.east(), 2));
        offsets.add(new Pair<>(origin.south().east(), 3));
        offsets.add(new Pair<>(origin.south(), 4));
        offsets.add(new Pair<>(origin.south().west(), 5));
        offsets.add(new Pair<>(origin.west(), 6));
        offsets.add(new Pair<>(origin.west().north(), 7));

        return offsets;
    }
}
