package com.aranaira.magichem.block;

import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.blocks.WaterloggableBlock;
import com.mna.blocks.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EnsorcelledWetSoilBlock extends WaterloggableBlock {
    VoxelShape VOXEL_SHAPE_SLAB = box(0,0,0,16,8,16);

    public EnsorcelledWetSoilBlock(Properties pProperties) {
        super(pProperties, false);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if(pState.getBlock() == BlockRegistry.ENSORCELLED_SOIL_DEPTHS.get())
            return VOXEL_SHAPE_SLAB;

        return super.getShape(pState, pLevel, pPos, pContext);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(!pLevel.isClientSide()) {
            if(pPlayer.getItemInHand(pHand).getItem() == ItemRegistry.DEBUG_ORB.get()) {
                if(pLevel.getBlockState(pPos.above()).isAir()) {
                    boolean success = false;
                    if(pState.getBlock() == BlockRegistry.ENSORCELLED_SOIL_DEPTHS.get() && pState.getValue(BlockStateProperties.WATERLOGGED)) {
                        pLevel.setBlock(pPos.above(), BlockInit.WAKEBLOOM.get().defaultBlockState(), 3);
                        success = true;
                    }

                    if(success) {
                        pPlayer.swing(pHand);
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }

        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }
}
