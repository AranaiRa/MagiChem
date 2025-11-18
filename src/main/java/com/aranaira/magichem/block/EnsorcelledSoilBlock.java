package com.aranaira.magichem.block;

import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.blocks.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class EnsorcelledSoilBlock extends Block {
    public EnsorcelledSoilBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if(!pLevel.isClientSide()) {
            if(pPlayer.getItemInHand(pHand).getItem() == ItemRegistry.DEBUG_ORB.get()) {
                if(pLevel.getBlockState(pPos.above()).isAir()) {
                    boolean success = false;
                    if(pState.getBlock() == BlockRegistry.ENSORCELLED_SOIL_PLAINS.get()) {
                        pLevel.setBlock(pPos.above(), BlockInit.CERUBLOSSOM.get().defaultBlockState(), 3);
                        success = true;
                    } else if(pState.getBlock() == BlockRegistry.ENSORCELLED_SOIL_FOREST.get()) {
                        pLevel.setBlock(pPos.above(), BlockInit.AUM.get().defaultBlockState(), 3);
                        success = true;
                    } else if(pState.getBlock() == BlockRegistry.ENSORCELLED_SOIL_WASTES.get()) {
                        pLevel.setBlock(pPos.above(), BlockInit.DESERT_NOVA.get().defaultBlockState(), 3);
                        success = true;
                    } else if(pState.getBlock() == BlockRegistry.ENSORCELLED_SOIL_SWAMPS.get()) {
                        pLevel.setBlock(pPos.above(), BlockInit.TARMA_ROOT.get().defaultBlockState(), 3);
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
