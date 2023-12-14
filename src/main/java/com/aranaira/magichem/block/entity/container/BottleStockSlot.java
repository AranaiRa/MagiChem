package com.aranaira.magichem.block.entity.container;

import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class BottleStockSlot extends SlotItemHandler {

    private boolean extractOnly = false;

    public BottleStockSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, boolean extractOnly) {
        super(itemHandler, index, xPosition, yPosition);
        this.extractOnly = extractOnly;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        if(stack.getItem() instanceof BottleItem) {
            if(extractOnly) return false;
            return true;
        }
        return false;
    }
}
