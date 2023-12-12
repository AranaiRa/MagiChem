package com.aranaira.magichem.item;

import net.minecraft.world.item.Item;

public class MateriaItem extends Item {
    private final String name;
    private int color;

    public MateriaItem(String name, String color, Item.Properties properties) {
        super(properties);
        this.name = name;
        this.color = Integer.parseInt(color, 16) | 0xFF000000;
    }

    public String getMateriaName() {
        return this.name;
    }

    public int getMateriaColor() {
        return this.color;
    }
}
