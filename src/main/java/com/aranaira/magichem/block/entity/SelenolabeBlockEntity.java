package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SelenolabeBlockEntity extends BlockEntity {
    public SelenolabeBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.SELENOLABE_BE.get(), pPos, pBlockState);
    }

    public static <E extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pState, E e) {
        if(e instanceof SelenolabeBlockEntity sbe) {
            if(pLevel.getGameTime() % 1200 == 0) {
                pLevel.updateNeighborsAt(pPos, sbe.getBlockState().getBlock());
            }
        }
    }
}
