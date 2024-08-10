package com.aranaira.magichem.block.entity.container;

import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class BottleConsumingResultSlot extends SlotItemHandler {
    private final int bottleIndex;

    public BottleConsumingResultSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, int bottleSlotIndex) {
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
        if(bottles.getItem() == ItemRegistry.DEBUG_ORB.get()) {
            return super.remove(amount);
        } else {
            int amountToRemove = Math.min(amount, bottles.getCount());
            bottles.shrink(amountToRemove);
            return super.remove(amountToRemove);
        }
    }
}
