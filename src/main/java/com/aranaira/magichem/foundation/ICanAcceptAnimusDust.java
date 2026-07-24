package com.aranaira.magichem.foundation;

import net.minecraft.world.level.block.state.BlockState;

public interface ICanAcceptAnimusDust {
    boolean canAcceptDust(BlockState be);

    void applyAnimusDust();
}
