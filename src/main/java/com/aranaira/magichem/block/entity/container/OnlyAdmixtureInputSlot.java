package com.aranaira.magichem.block.entity.container;

import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.MateriaItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class OnlyAdmixtureInputSlot extends SlotItemHandler {

    public OnlyAdmixtureInputSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {

        if(stack.getItem() instanceof AdmixtureItem) {
            return true;
        }
        return false;
    }
}
