package com.aranaira.magichem.foundation;

import net.minecraft.world.item.ItemStack;

public interface IHasDeviceRecipeSlot {
    byte
        ERROR_CODE_SUCCESS = 0,
        ERROR_CODE_NO_BLOCK_ENTITY = 1,
        ERROR_CODE_MUST_BE_ADMIXTURE = 2,
        ERROR_CODE_CANNOT_BE_ADMIXTURE = 3,
        ERROR_CODE_NO_SUCH_RECIPE = 4;

    byte setRecipe(ItemStack pStack);

    ItemStack getRecipeItem();

    ItemStack getRecipeItem(boolean pMakeCopy);
}
