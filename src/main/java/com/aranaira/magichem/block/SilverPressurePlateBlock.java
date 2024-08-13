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

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.USER_TIER_TYPE;

public class SilverPressurePlateBlock extends PressurePlateBlock {

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
        int tier = 0;

        if(pEntity.getCapability(PlayerProgressionProvider.PROGRESSION).isPresent()) {
            tier = pEntity.getCapability(PlayerProgressionProvider.PROGRESSION).resolve().get().getTier();
        }

        pState = pState.setValue(USER_TIER_TYPE, tier);
        pLevel.setBlock(pPos, pState, 3);
        super.entityInside(pState, pLevel, pPos, pEntity);
    }
}
