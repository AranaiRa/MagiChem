package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.NourishingCenserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class NourishingCenserBlock extends BaseEntityBlock {
    public static final VoxelShape
        VOXEL_SHAPE_FOOT_1, VOXEL_SHAPE_FOOT_2, VOXEL_SHAPE_FOOT_3, VOXEL_SHAPE_FOOT_4,
        VOXEL_SHAPE_BODY, VOXEL_SHAPE_LIP, VOXEL_SHAPE_VENT,
        VOXEL_SHAPE_AGGREGATE;

    public NourishingCenserBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new NourishingCenserBlockEntity(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if(entity instanceof NourishingCenserBlockEntity censer) {
                NetworkHooks.openScreen((ServerPlayer) pPlayer, censer, pPos);
            } else {
                throw new IllegalStateException("NourishingCenser container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return VOXEL_SHAPE_AGGREGATE;
    }

    static {
        VOXEL_SHAPE_FOOT_1 = box(4, 0, 4, 7, 1, 7);
        VOXEL_SHAPE_FOOT_2 = box(9, 0, 4, 12, 1, 7);
        VOXEL_SHAPE_FOOT_3 = box(4, 0, 9, 7, 1, 12);
        VOXEL_SHAPE_FOOT_4 = box(9, 0, 9, 12, 1, 12);

        VOXEL_SHAPE_BODY = box(1, 1, 1, 15, 9, 15);
        VOXEL_SHAPE_LIP = box(3, 9, 3, 13, 11, 13);
        VOXEL_SHAPE_VENT = box(5, 11, 5, 11, 13, 11);

        VOXEL_SHAPE_AGGREGATE = Shapes.or(
                VOXEL_SHAPE_FOOT_1,
                VOXEL_SHAPE_FOOT_2,
                VOXEL_SHAPE_FOOT_3,
                VOXEL_SHAPE_FOOT_4,
                VOXEL_SHAPE_BODY,
                VOXEL_SHAPE_LIP,
                VOXEL_SHAPE_VENT
        );
    }
}
