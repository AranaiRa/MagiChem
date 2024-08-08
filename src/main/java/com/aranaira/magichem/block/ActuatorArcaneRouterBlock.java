package com.aranaira.magichem.block;

import com.aranaira.magichem.block.entity.routers.ActuatorEarthRouterBlockEntity;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.Nullable;

public class ActuatorArcaneRouterBlock extends BaseActuatorRouterBlock implements INoCreativeTab {

    public ActuatorArcaneRouterBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ActuatorEarthRouterBlockEntity(pPos, pState);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected void spawnDestroyParticles(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState) {
        BlockEntity be = pLevel.getBlockEntity(pPos);
        if(be instanceof ActuatorEarthRouterBlockEntity router) {
            BlockState masterState = router.getMaster().getBlockState();
            pLevel.levelEvent(pPlayer, 2001, pPos, getId(masterState));
        }
    }

    @Override
    public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
        return false;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(BlockRegistry.ACTUATOR_ARCANE.get());
    }
}