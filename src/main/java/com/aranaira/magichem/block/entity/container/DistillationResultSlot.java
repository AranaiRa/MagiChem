package com.aranaira.magichem.block.entity.container;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class DistillationResultSlot extends SlotItemHandler {
    private final int bottleIndex;

    public DistillationResultSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, int bottleSlotIndex) {
        super(itemHandler, index, xPosition, yPosition);
        bottleIndex = bottleSlotIndex;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        ItemStack bottles = getItemHandler().getStackInSlot(bottleIndex);
        int amountToRemove = Math.min(amount, bottles.getCount());
        bottles.shrink(amountToRemove);
        return super.remove(amountToRemove);
    }
}
