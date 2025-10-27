package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.DisintegrationPyreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class DisintegrationPyreBlock extends BaseEntityBlock {
    public static final VoxelShape VOXEL_SHAPE_BASE, VOXEL_SHAPE_BODY, VOXEL_SHAPE_AGGREGATE;

    public DisintegrationPyreBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DisintegrationPyreBlockEntity(pPos, pState);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return VOXEL_SHAPE_AGGREGATE;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 15;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if(entity instanceof DisintegrationPyreBlockEntity observer) {
                NetworkHooks.openScreen((ServerPlayer) pPlayer, observer, pPos);
            } else {
                throw new IllegalStateException("DisintegrationPyre container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    static {
        VOXEL_SHAPE_BASE = Block.box(0, 0, 0, 16, 3, 16);
        VOXEL_SHAPE_BODY = Block.box(1, 3, 1, 15, 7, 15);

        VOXEL_SHAPE_AGGREGATE = Shapes.or(VOXEL_SHAPE_BASE, VOXEL_SHAPE_BODY);
    }
}
