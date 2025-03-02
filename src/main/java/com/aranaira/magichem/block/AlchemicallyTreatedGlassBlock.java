package com.aranaira.magichem.block;

import com.mna.api.blocks.interfaces.ITranslucentBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class AlchemicallyTreatedGlassBlock extends AbstractGlassBlock implements ITranslucentBlock {
    public AlchemicallyTreatedGlassBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return true;
    }
}
