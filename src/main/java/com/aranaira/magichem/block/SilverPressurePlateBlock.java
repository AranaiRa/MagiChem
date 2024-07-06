package com.aranaira.magichem.block;

import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.antlr.v4.misc.MutableInt;

public class SilverPressurePlateBlock extends PressurePlateBlock {
    public static final IntegerProperty USER_TIER_TYPE = IntegerProperty.create("user_tier_type", 0, 5);

    public SilverPressurePlateBlock(Properties pProperties) {
        super(Sensitivity.EVERYTHING, pProperties, BlockSetType.GOLD);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(USER_TIER_TYPE);
    }

    @Override
    public int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        int tier = pBlockState.getValue(USER_TIER_TYPE);

        return pBlockState.getValue(POWERED) && pSide == Direction.UP ? tier + 1 : 0;
    }

    @Override
    public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        int tier = pBlockState.getValue(USER_TIER_TYPE);

        return pBlockState.getValue(POWERED) ? tier + 1 : 0;
    }

    @Override
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        MutableInt tier = new MutableInt(0);
        pEntity.getCapability(PlayerProgressionProvider.PROGRESSION).ifPresent(cap -> {
            tier.v = cap.getTier();
        });

        pState = pState.setValue(USER_TIER_TYPE, tier.v);
        pLevel.setBlock(pPos, pState, 3);
        super.entityInside(pState, pLevel, pPos, pEntity);
    }
}