package com.aranaira.magichem.block;

import com.aranaira.magichem.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class BuddingVinteumCrystalBlock extends AmethystBlock {
    public static final int GROWTH_CHANCE = 5;
    private static final Direction[] DIRECTIONS = Direction.values();

    public BuddingVinteumCrystalBlock(Properties pProperties) {
        super(pProperties);
    }

    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pRandom.nextInt(5) == 0) {
            Direction direction = DIRECTIONS[pRandom.nextInt(DIRECTIONS.length)];
            BlockPos blockpos = pPos.relative(direction);
            BlockState blockstate = pLevel.getBlockState(blockpos);
            Block block = null;
            if (canClusterGrowAtState(blockstate)) {
                block = BlockRegistry.SMALL_VINTEUM_CLUSTER.get();
            } else if (blockstate.is(BlockRegistry.SMALL_VINTEUM_CLUSTER.get()) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
                block = BlockRegistry.MEDIUM_VINTEUM_CLUSTER.get();
            } else if (blockstate.is(BlockRegistry.MEDIUM_VINTEUM_CLUSTER.get()) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
                block = BlockRegistry.LARGE_VINTEUM_CLUSTER.get();
            } else if (blockstate.is(BlockRegistry.LARGE_VINTEUM_CLUSTER.get()) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
                block = BlockRegistry.VINTEUM_CLUSTER.get();
            }

            if (block != null) {
                BlockState blockstate1 = block.defaultBlockState().setValue(AmethystClusterBlock.FACING, direction).setValue(AmethystClusterBlock.WATERLOGGED, blockstate.getFluidState().getType() == Fluids.WATER);
                pLevel.setBlockAndUpdate(blockpos, blockstate1);
            }
        }
    }

    public static boolean canClusterGrowAtState(BlockState pState) {
        return pState.isAir() || pState.is(Blocks.WATER) && pState.getFluidState().getAmount() == 8;
    }
}
