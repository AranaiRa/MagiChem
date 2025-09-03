package com.aranaira.magichem.block;

import com.aranaira.magichem.registry.BlockRegistry;
import com.mna.api.blocks.WaterloggableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EnchantedWetSoilBlock extends WaterloggableBlock {
    VoxelShape VOXEL_SHAPE_SLAB = box(0,0,0,16,8,16);

    public EnchantedWetSoilBlock(Properties pProperties) {
        super(pProperties, false);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if(pState.getBlock() == BlockRegistry.ENCHANTED_SOIL_DEPTHS.get())
            return VOXEL_SHAPE_SLAB;

        return super.getShape(pState, pLevel, pPos, pContext);
    }
}
