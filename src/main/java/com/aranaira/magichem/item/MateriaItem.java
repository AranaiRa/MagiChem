package com.aranaira.magichem.item;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

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

    public String getDisplayFormula() { return "?"; }
}
