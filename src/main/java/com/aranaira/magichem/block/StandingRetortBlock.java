package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.StandingRetortBlockEntity;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.blocks.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class StandingRetortBlock extends BaseEntityBlock {
    private static final VoxelShape VOXEL_SHAPE = Block.box(5.5, 1, 5.5, 10.5, 10, 10.5);

    public StandingRetortBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new StandingRetortBlockEntity(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof StandingRetortBlockEntity) {
                NetworkHooks.openScreen((ServerPlayer) player, (StandingRetortBlockEntity) entity, pos);
            } else {
                throw new IllegalStateException("StandingRetortBlockEntity container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        final BlockPos clickedPos = pContext.getClickedPos();
        final BlockState clickedState = pContext.getLevel().getBlockState(clickedPos);
        final BlockState fumeStateQuery = pContext.getLevel().getBlockState(clickedPos.offset(0, -2, 0));

        if(clickedState.isAir() && fumeStateQuery.getBlock() == BlockInit.ELDRIN_FUME.get()) {
            Direction dir = fumeStateQuery.getValue(HORIZONTAL_FACING);

            return defaultBlockState().setValue(HORIZONTAL_FACING, dir);
        }

        return super.getStateForPlacement(pContext);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return VOXEL_SHAPE;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.STANDING_RETORT_BE.get(),
                StandingRetortBlockEntity::tick);
    }
}
