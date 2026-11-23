package com.aranaira.magichem.foundation;

import net.minecraft.world.item.ItemStack;

public interface ICanHaveUnbottledMateriaInInputTray {
    ItemStack tryExtractUnbottled(ItemStack pBottlesInHand);

    boolean isClogged();
}
