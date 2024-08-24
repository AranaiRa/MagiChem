package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.ColoringCauldronBlockEntity;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
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
import org.jetbrains.annotations.Nullable;

public class ColoringCauldronBlock extends BaseEntityBlock {

    private static final VoxelShape
        VOXEL_SHAPE_FOOT_NS, VOXEL_SHAPE_FOOT_EW, VOXEL_SHAPE_BODY, VOXEL_SHAPE_RIM, VOXEL_SHAPE_AGGREGATE;

    public ColoringCauldronBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ColoringCauldronBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntitiesRegistry.COLORING_CAULDRON_BE.get(),
                ColoringCauldronBlockEntity::tick);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return VOXEL_SHAPE_AGGREGATE;
    }

    @Override
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if(pEntity instanceof ItemEntity ie && !pLevel.isClientSide()) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof ColoringCauldronBlockEntity ccbe) {
                if(ccbe.insertItemStack(ie.getItem())) {
                    ie.getItem().shrink(1);
                    if(ie.getItem().getCount() == 0)
                        ie.kill();
                }
            }
        }
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if (be instanceof ColoringCauldronBlockEntity ccbe) {
            ccbe.collectItem(pLevel, pPlayer);
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    static {
        VOXEL_SHAPE_FOOT_NS = Block.box(6, 0, 0, 10, 2, 16);
        VOXEL_SHAPE_FOOT_EW = Block.box(0, 0, 6, 16, 2, 10);
        VOXEL_SHAPE_BODY = Block.box(2, 2, 2, 14, 13, 14);
        VOXEL_SHAPE_RIM = Block.box(1, 13, 1, 15, 14.875, 15);

        VOXEL_SHAPE_AGGREGATE = Shapes.or(VOXEL_SHAPE_FOOT_NS, VOXEL_SHAPE_FOOT_EW, VOXEL_SHAPE_BODY, VOXEL_SHAPE_RIM);
    }
}
