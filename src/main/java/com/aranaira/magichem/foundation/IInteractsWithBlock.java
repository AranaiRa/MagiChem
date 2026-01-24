package com.aranaira.magichem.foundation;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public interface IInteractsWithBlock {
    /**
     * @param pEvent
     * @return Boolean parameter determines whether the event should be intercepted. If false, the event will be cancelled and InteractionResult will be set.
     */
    Pair<Boolean, InteractionResult> shouldInterceptEvent(PlayerInteractEvent.RightClickBlock pEvent);

    boolean interceptEvent(PlayerInteractEvent.RightClickBlock pEvent);
}
