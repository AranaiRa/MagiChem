package com.aranaira.magichem.block.entity.container;

import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        if(stack.getItem() == ItemRegistry.DEBUG_ORB.get() || stack.getItem() == Items.GLASS_BOTTLE) {
            return !extractOnly;
        }
        return false;
    }
}
