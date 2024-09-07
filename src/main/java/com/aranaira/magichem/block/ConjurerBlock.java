package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.ActuatorArcaneBlockEntity;
import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
import com.aranaira.magichem.block.entity.AlembicBlockEntity;
import com.aranaira.magichem.block.entity.ConjurerBlockEntity;
import com.aranaira.magichem.block.entity.routers.ConjurerRouterBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.aranaira.magichem.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class ConjurerBlock extends BaseEntityBlock {

    private static final VoxelShape
        VOXEL_SHAPE_BASE, VOXEL_SHAPE_PILLAR_1, VOXEL_SHAPE_PILLAR_2, VOXEL_SHAPE_PILLAR_3, VOXEL_SHAPE_PILLAR_4, VOXEL_SHAPE_BODY, VOXEL_SHAPE_SETTING, VOXEL_SHAPE_GEM, VOXEL_SHAPE_AGGREGATE;

    public ConjurerBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ConjurerBlockEntity(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof ConjurerBlockEntity cbe) {
                NetworkHooks.openScreen((ServerPlayer)player, (ConjurerBlockEntity)entity, pos);
            } else {
                throw new IllegalStateException("ConjurerBlockEntity container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        boolean safeToPlace = pContext.getLevel().getBlockState(pContext.getClickedPos().above()).isAir();
        safeToPlace &= pContext.getLevel().getBlockState(pContext.getClickedPos().above().above()).isAir();

        if(!safeToPlace)
            return null;

        return super.getStateForPlacement(pContext);
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        super.onPlace(pState, pLevel, pPos, pOldState, pMovedByPiston);
        BlockState state = BlockRegistry.CONJURER_ROUTER.get().defaultBlockState();

        BlockPos targetPos = pPos.above();
        pLevel.setBlock(targetPos, state.setValue(MagiChemBlockStateProperties.ROUTER_TYPE_CONJURER, ConjurerRouterBlock.ROUTER_TYPE_MIDDLE), 3);
        ((ConjurerRouterBlockEntity)pLevel.getBlockEntity(targetPos)).configure(pPos);

        targetPos = pPos.above().above();
        pLevel.setBlock(targetPos, state.setValue(MagiChemBlockStateProperties.ROUTER_TYPE_CONJURER, ConjurerRouterBlock.ROUTER_TYPE_TOP), 3);
        ((ConjurerRouterBlockEntity)pLevel.getBlockEntity(targetPos)).configure(pPos);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.CONJURER_BE.get(),
                ConjurerBlockEntity::tick);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return VOXEL_SHAPE_AGGREGATE;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof ConjurerBlockEntity) {
                ((ConjurerBlockEntity) blockEntity).packInventoryToBlockItem();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    static {
        VOXEL_SHAPE_BASE = Block.box(1, 0, 1, 15, 3, 15);
        VOXEL_SHAPE_PILLAR_1 = Block.box(2, 3, 2, 4, 16, 4);
        VOXEL_SHAPE_PILLAR_2 = Block.box(12, 3, 2, 14, 16, 4);
        VOXEL_SHAPE_PILLAR_3 = Block.box(12, 3, 12, 14, 16, 14);
        VOXEL_SHAPE_PILLAR_4 = Block.box(2, 3, 12, 4, 16, 14);
        VOXEL_SHAPE_BODY = Block.box(3, 3, 3, 13, 12, 13);
        VOXEL_SHAPE_SETTING = Block.box(5, 12, 5, 11, 13, 11);
        VOXEL_SHAPE_GEM = Block.box(6, 13, 6, 10, 16, 10);

        VOXEL_SHAPE_AGGREGATE = Shapes.or(
                VOXEL_SHAPE_BASE,
                VOXEL_SHAPE_PILLAR_1,
                VOXEL_SHAPE_PILLAR_2,
                VOXEL_SHAPE_PILLAR_3,
                VOXEL_SHAPE_PILLAR_4,
                VOXEL_SHAPE_BODY,
                VOXEL_SHAPE_SETTING,
                VOXEL_SHAPE_GEM
        );
    }
}
