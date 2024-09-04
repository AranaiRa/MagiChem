package com.aranaira.magichem.foundation;

import com.aranaira.magichem.item.MateriaItem;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public interface IMateriaProvisionRequester {
    boolean allowIncreasedDeliverySize();

    boolean needsProvisioning();

    Map<MateriaItem, Integer> getProvisioningNeeds();

    void setProvisioningInProgress(MateriaItem pMateriaItem);

    void cancelProvisioningInProgress(MateriaItem pMateriaItem);

    void provide(ItemStack pStack);
}
