package com.aranaira.magichem.item;

import net.minecraft.world.item.Item;

public class PhilosophersStoneItem extends Item {
    int wisdom = 0;

    public PhilosophersStoneItem(Properties pProperties, int pWisdom) {
        super(pProperties);
        this.wisdom = pWisdom;
    }

    public int getWisdom() {
        return wisdom;
    }
}
