package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.routers.ActuatorWaterRouterBlockEntity;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ActuatorWaterRouterBlock extends BaseActuatorRouterBlock implements INoCreativeTab {

    public ActuatorWaterRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ActuatorWaterRouterBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void spawnDestroyParticles(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof ActuatorWaterRouterBlockEntity router) {
            BlockState masterState = router.getMaster().getBlockState();
            pLevel.levelEvent(pPlayer, 2001, pPos, getId(masterState));
        }
    }
}