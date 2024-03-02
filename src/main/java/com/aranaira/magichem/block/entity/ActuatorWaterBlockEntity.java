package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ActuatorWaterBlockEntity extends BlockEntity {
    public ActuatorWaterBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public ActuatorWaterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ACTUATOR_WATER_BE.get(), pPos, pBlockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ActuatorWaterBlockEntity entity) {

    }
}
