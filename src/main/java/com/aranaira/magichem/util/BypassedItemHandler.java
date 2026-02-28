package com.aranaira.magichem.util;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BypassedItemHandler implements IItemHandlerModifiable {
    public static boolean IsSignal(int slot) {
        return slot < 0;
    }
    public static int ConvertSlot(int original) {
        return ~original;
    }

    public static class Extract extends BypassedItemHandler {
        public Extract(IItemHandler original, Integer... targetSlots) {
            super(original, targetSlots);
        }
        @Override
        public @NotNull ItemStack extractItem(int slot, int count, boolean simulate) {
            if (targets.contains(slot)) slot = ConvertSlot(slot); // signal for bypassed
            return super.extractItem(slot, count, simulate);
        }
    }

    final IItemHandlerModifiable master;
    final Set<Integer> targets;

    protected BypassedItemHandler(IItemHandler original, Integer... targetSlots) {
        master = (IItemHandlerModifiable) original;
        targets = new HashSet<>(List.of(targetSlots));
    }

    // all operations to master
    @Override
    public @NotNull ItemStack extractItem(int slot, int count, boolean simulate) {
        return master.extractItem(slot, count, simulate);
    }
    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        master.setStackInSlot(slot, stack);
    }
    @Override
    public int getSlots() {
        return master.getSlots();
    }
    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return master.getStackInSlot(slot);
    }
    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return master.insertItem(slot, stack, simulate);
    }
    @Override
    public int getSlotLimit(int slot) {
        return master.getSlotLimit(slot);
    }
    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return master.isItemValid(slot, stack);
    }
}
