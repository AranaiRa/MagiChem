package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.ActuatorEarthBlockEntity;
import com.aranaira.magichem.block.entity.PrismaticConduitBlockEntity;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.api.affinity.Affinity;
import com.mna.blocks.artifice.EldrinConduitBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PrismaticConduitBlock extends EldrinConduitBlock {
    public PrismaticConduitBlock(boolean pIsLesser) {
        super(Affinity.UNKNOWN, pIsLesser);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PrismaticConduitBlockEntity(pPos, pState);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pBlockEntityType == BlockEntitiesRegistry.PRISMATIC_CONDUIT_BE.get()) {
            return PrismaticConduitBlockEntity::tick;
        }

        return null;
    }

    public boolean isLesser() {
        return isLesser();
    }
}
