package com.aranaira.magichem.foundation;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class InfusionStage {
    public final int experience;
    public final NonNullList<ItemStack> componentItems;
    public final NonNullList<ItemStack> componentMateria;

    public InfusionStage(int pExperience, NonNullList<ItemStack> pComponentItems, NonNullList<ItemStack> pComponentMateria) {
        this.experience = pExperience;
        this.componentItems = pComponentItems;
        this.componentMateria = pComponentMateria;
    }

    public InfusionStage copy() {
        NonNullList<ItemStack> ci = NonNullList.create();
        for(ItemStack is : this.componentItems) {
            ci.add(is.copy());
        }

        NonNullList<ItemStack> cm = NonNullList.create();
        for(ItemStack is : this.componentMateria) {
            cm.add(is.copy());
        }

        InfusionStage is = new InfusionStage(this.experience, ci, cm);

        return is;
    }
}
