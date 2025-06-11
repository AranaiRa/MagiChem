package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.routers.AcidBasinRouterBlockEntity;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AcidBasinRouterBlock extends BaseEntityBlock implements INoCreativeTab {
    public AcidBasinRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AcidBasinRouterBlockEntity(pPos, pState);
    }
}
