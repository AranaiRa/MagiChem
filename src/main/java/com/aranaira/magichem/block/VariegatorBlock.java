package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.ActuatorAirBlockEntity;
import com.aranaira.magichem.block.entity.AlembicBlockEntity;
import com.aranaira.magichem.block.entity.VariegatorBlockEntity;
import com.aranaira.magichem.block.entity.routers.GrandDistilleryRouterBlockEntity;
import com.aranaira.magichem.block.entity.routers.VariegatorRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.foundation.enums.DevicePlugDirection;
import com.aranaira.magichem.foundation.enums.GrandDistilleryRouterType;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
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

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.*;

public class VariegatorBlock extends BaseEntityBlock {
    public VariegatorBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState().setValue(GROUNDED, true));
    }

    public static final VoxelShape
        VOXEL_SHAPE_LAYER_1_UPRIGHT, VOXEL_SHAPE_LAYER_2_UPRIGHT, VOXEL_SHAPE_LAYER_3_UPRIGHT, VOXEL_SHAPE_LAYER_4_UPRIGHT, VOXEL_SHAPE_AGGREGATE_UPRIGHT,
        VOXEL_SHAPE_LAYER_1_REVERSED, VOXEL_SHAPE_LAYER_2_REVERSED, VOXEL_SHAPE_LAYER_3_REVERSED, VOXEL_SHAPE_LAYER_4_REVERSED, VOXEL_SHAPE_AGGREGATE_REVERSED;

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(GROUNDED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction lookDir = pContext.getNearestLookingVerticalDirection();

        if(lookDir == Direction.UP) {
            if(pContext.getLevel().isEmptyBlock(pContext.getClickedPos().below())) {
                return this.defaultBlockState().setValue(GROUNDED, false);
            }
        } else {
            if(pContext.getLevel().isEmptyBlock(pContext.getClickedPos().above())) {
                return super.getStateForPlacement(pContext);
            }
        }

        return null;
    }

    @Override
    public void onPlace(BlockState pNewState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        BlockState state = BlockRegistry.VARIEGATOR_ROUTER.get().defaultBlockState();
        boolean grounded = pNewState.getValue(GROUNDED);

        super.onPlace(pNewState, pLevel, pPos, pOldState, pMovedByPiston);

        BlockPos targetPos = grounded ? pPos.above() : pPos.below();

        if(pLevel.getBlockState(targetPos).isAir()) {
            pLevel.setBlock(
                    targetPos,
                    state.setValue(GROUNDED, grounded),
                    3);
            ((VariegatorRouterBlockEntity) pLevel.getBlockEntity(targetPos)).configure(pPos);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new VariegatorBlockEntity(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof VariegatorBlockEntity) {
                NetworkHooks.openScreen((ServerPlayer) player, (VariegatorBlockEntity) entity, pos);
            } else {
                throw new IllegalStateException("VariegatorBlockEntity container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static void destroyRouters(LevelAccessor pLevel, BlockPos pPos) {
        BlockState state = pLevel.getBlockState(pPos);

        if(state.getValue(MagiChemBlockStateProperties.GROUNDED)) {
            pLevel.destroyBlock(pPos.above(), true);
        } else {
            pLevel.destroyBlock(pPos.below(), true);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.VARIEGATOR_BE.get(),
                VariegatorBlockEntity::tick);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if(pState.getValue(MagiChemBlockStateProperties.GROUNDED)) {
            return VOXEL_SHAPE_AGGREGATE_UPRIGHT;
        } else {
            return VOXEL_SHAPE_AGGREGATE_REVERSED;
        }
    }

    static {
        VOXEL_SHAPE_LAYER_1_UPRIGHT = Block.box(0, 0, 0, 16, 1, 16);
        VOXEL_SHAPE_LAYER_2_UPRIGHT = Block.box(1, 1, 1, 15, 3, 15);
        VOXEL_SHAPE_LAYER_3_UPRIGHT = Block.box(2, 3, 2, 14, 14, 14);
        VOXEL_SHAPE_LAYER_4_UPRIGHT = Block.box(3, 14, 3, 13, 16, 13);

        VOXEL_SHAPE_AGGREGATE_UPRIGHT = Shapes.or(VOXEL_SHAPE_LAYER_1_UPRIGHT, VOXEL_SHAPE_LAYER_2_UPRIGHT, VOXEL_SHAPE_LAYER_3_UPRIGHT, VOXEL_SHAPE_LAYER_4_UPRIGHT);

        VOXEL_SHAPE_LAYER_1_REVERSED = Block.box(0, 15, 0, 16, 16, 16);
        VOXEL_SHAPE_LAYER_2_REVERSED = Block.box(1, 13, 1, 15, 15, 15);
        VOXEL_SHAPE_LAYER_3_REVERSED = Block.box(2, 2, 2, 14, 13, 14);
        VOXEL_SHAPE_LAYER_4_REVERSED = Block.box(3, 0, 3, 13, 2, 13);

        VOXEL_SHAPE_AGGREGATE_REVERSED = Shapes.or(VOXEL_SHAPE_LAYER_1_REVERSED, VOXEL_SHAPE_LAYER_2_REVERSED, VOXEL_SHAPE_LAYER_3_REVERSED, VOXEL_SHAPE_LAYER_4_REVERSED);
    }
}
