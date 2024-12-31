package com.aranaira.magichem.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EssentiaDropletsItem extends EssentiaItem {
    public EssentiaDropletsItem(String essentiaName, String essentiaAbbreviation, String essentiaHouse, int essentiaWheel, String essentiaColor) {
        super(essentiaName, essentiaAbbreviation, essentiaHouse, essentiaWheel, essentiaColor);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        pStack.shrink(1);
    }
}
