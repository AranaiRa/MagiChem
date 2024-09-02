package com.aranaira.magichem.foundation;

import net.minecraft.world.item.ItemStack;

public interface IShlorpReceiver {
    /**
     * Returns the number of items in the target stack that can be inserted into the target inventory.
     * @param pStack The item stack to check.
     * @return The number of items in the stack that could be inserted.
     */
    int canAcceptStackFromShlorp(ItemStack pStack);

    /**
     * Tries to insert an ItemStack into the target inventory.
     * @param pStack The item stack to insert.
     * @return Any leftover items.
     */
    int insertStackFromShlorp(ItemStack pStack);
}
