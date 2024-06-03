package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AlchemicalNexusBlockEntity extends BlockEntity {
    public AlchemicalNexusBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ALCHEMICAL_NEXUS_BE.get(), pPos, pBlockState);
    }

    public static <E extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, E e) {

    }
}
