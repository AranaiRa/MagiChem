package com.aranaira.magichem.block;

import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.MACapabilityForgeEventHandlers;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.antlr.v4.misc.MutableInt;

public class SilverButtonBlock extends ButtonBlock {
    private static final int ticksToStayPressed = 20;
    public static final IntegerProperty USER_TIER_TYPE = IntegerProperty.create("user_tier_type", 0, 5);

    public SilverButtonBlock(Properties pProperties) {
        super(pProperties, BlockSetType.GOLD, ticksToStayPressed, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(USER_TIER_TYPE);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pState.getValue(POWERED)) {
            return InteractionResult.CONSUME;
        } else {
            MutableInt tier = new MutableInt(0);
            pPlayer.getCapability(PlayerProgressionProvider.PROGRESSION).ifPresent(cap -> {
                tier.v = cap.getTier();
            });

            this.press(pState, pLevel, pPos, tier.v);
            this.playSound(pPlayer, pLevel, pPos, true);
            pLevel.gameEvent(pPlayer, GameEvent.BLOCK_ACTIVATE, pPos);
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }
    }

    public void press(BlockState pState, Level pLevel, BlockPos pPos, int pTier) {
        pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.TRUE).setValue(USER_TIER_TYPE, pTier), 3);
        this.updateNeighbours(pState, pLevel, pPos);
        pLevel.scheduleTick(pPos, this, ticksToStayPressed);
    }

    private void updateNeighbours(BlockState pState, Level pLevel, BlockPos pPos) {
        pLevel.updateNeighborsAt(pPos, this);
        pLevel.updateNeighborsAt(pPos.relative(getConnectedDirection(pState).getOpposite()), this);
    }

    @Override
    public int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        int tier = pBlockState.getValue(USER_TIER_TYPE);

        return pBlockState.getValue(POWERED) && getConnectedDirection(pBlockState) == pSide ? tier + 1 : 0;
    }

    @Override
    public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        int tier = pBlockState.getValue(USER_TIER_TYPE);

        return pBlockState.getValue(POWERED) ? tier + 1 : 0;
    }
}
