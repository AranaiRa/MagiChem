package com.aranaira.magichem.block.entity.routers;

import com.aranaira.magichem.registry.BlockEntitiesRegistry;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class AcidBasinRouterBlockEntity extends BlockEntity {
    private BlockPos masterPos = null;

    public AcidBasinRouterBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BlockEntitiesRegistry.ACID_BASIN_ROUTER_BE.get(), pPos, pBlockState);
    }

    public void configure(BlockPos pPos) {
        masterPos = pPos;
    }
}
