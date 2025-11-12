package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.CovetousCofferBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;

public class CovetousCofferBlock extends BaseEntityBlock {
    public static final VoxelShape
            RENDER_SHAPE_BODY, RENDER_SHAPE_LID, RENDER_SHAPE_AGGREGATE;

    public CovetousCofferBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new CovetousCofferBlockEntity(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {

            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof CovetousCofferBlockEntity coffer) {
                NetworkHooks.openScreen((ServerPlayer) player, coffer, pos);
            } else {
                throw new IllegalStateException("CovetousCofferBlockEntity container provider is missing!");
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return RENDER_SHAPE_AGGREGATE;
    }

    static {
        RENDER_SHAPE_BODY = box(1, 0, 1, 15, 6, 15);
        RENDER_SHAPE_LID = box(2, 6, 2, 14, 8, 14);

        RENDER_SHAPE_AGGREGATE = Shapes.or(RENDER_SHAPE_BODY, RENDER_SHAPE_LID);
    }
}
