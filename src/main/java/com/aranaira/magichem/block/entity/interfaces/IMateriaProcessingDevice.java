package com.aranaira.magichem.block.entity.interfaces;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public interface IMateriaProcessingDevice {

    public SimpleContainer getContentsOfOutputSlots();

    public void setContentsOfOutputSlots(SimpleContainer replacementInventory);
}
