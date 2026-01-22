package com.aranaira.magichem.foundation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public interface IInteractsWithBlock {
    boolean interceptEvent(BlockPos pPos, BlockState pState, BlockEntity pEntity, PlayerInteractEvent.RightClickBlock pEvent);
}
