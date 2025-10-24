package com.aranaira.magichem.foundation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IKeepsInventoryOnBreak {
    void packDataToBlockItem();

    void unpackDataFromNBT(CompoundTag pNBT);
}
