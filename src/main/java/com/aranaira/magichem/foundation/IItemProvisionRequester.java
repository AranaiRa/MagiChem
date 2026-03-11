package com.aranaira.magichem.foundation;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public interface IItemProvisionRequester {
    boolean needsItemProvisioning();

    boolean needsAllItemsPresentForProvisioning();

    NonNullList<ItemStack> getItemProvisioningNeeds();

    void setItemProvisioningInProgress();

    void cancelItemProvisioningInProgress();

    void provideItems(NonNullList<ItemStack> pStacks);
}
