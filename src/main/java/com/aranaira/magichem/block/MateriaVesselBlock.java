package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MateriaVesselBlock extends BaseEntityBlock {
    public MateriaVesselBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MateriaVesselBlockEntity(pos, state);
    }
}
