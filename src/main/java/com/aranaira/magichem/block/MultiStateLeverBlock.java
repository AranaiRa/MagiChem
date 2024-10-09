package com.aranaira.magichem.block;

import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.LEVER_SIGNAL;

public class MultiStateLeverBlock extends FaceAttachedHorizontalDirectionalBlock {
    private final int maxSignal;

    public MultiStateLeverBlock(int maxSignal, BlockBehaviour.Properties pProperties) {
        super(pProperties);
        this.maxSignal = maxSignal;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(BlockStateProperties.ATTACH_FACE);
        pBuilder.add(FACING);
        pBuilder.add(LEVER_SIGNAL);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        int oldSignal = pState.getValue(LEVER_SIGNAL);
        int value = oldSignal >= maxSignal ? 0 : oldSignal + 1;

        if(pLevel.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            float f = value * 0.1f + 0.5f;
            pLevel.playSound((Player)null, pPos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);
            BlockState newState = pState.setValue(LEVER_SIGNAL, value);
            pLevel.setBlock(pPos, newState, 3);
            pLevel.sendBlockUpdated(pPos, pState, newState, 3);
            return InteractionResult.CONSUME;
        }
    }
}
