package com.aranaira.magichem.foundation;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IHasDeviceRecipeSlot {
    byte
        ERROR_CODE_SUCCESS = 0,
        ERROR_CODE_NO_BLOCK_ENTITY = 1,
        ERROR_CODE_MUST_BE_ADMIXTURE = 2,
        ERROR_CODE_CANNOT_BE_ADMIXTURE = 3,
        ERROR_CODE_NO_SUCH_RECIPE = 4,
        ERROR_CODE_INSUFFICIENT_WISDOM = 5,
        ERROR_CODE_REQUIRED_ADVANCEMENT_MISSING = 6,
        ERROR_CODE_FORBIDDEN_ADVANCEMENT_PRESENT = 7;

    byte setRecipe(ItemStack pStack, Player player);

    ItemStack getRecipeItem();

    ItemStack getRecipeItem(boolean pMakeCopy);
}
