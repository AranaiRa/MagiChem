package com.aranaira.magichem.block.entity;

import com.aranaira.magichem.block.entity.renderer.AstralObserverBlockEntityRenderer;
import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class AstralObserverBlockEntity extends BlockEntity {
    public AstralObserverBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ASTRAL_OBSERVER_BE.get(), pPos, pBlockState);
    }

    public void dropInventory() {

    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState blockState, T t) {
        if(t instanceof AstralObserverBlockEntity entity) {
            if(level.isClientSide()) {

            }
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().offset(-3, 0, -3), getBlockPos().offset(3,3,3));
    }
}
