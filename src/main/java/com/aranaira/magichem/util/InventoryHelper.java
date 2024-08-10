package com.aranaira.magichem.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;

public class InventoryHelper {

    /**
     * Generic handler for the goddamned quickMoveStack method. The Y values passed in for any ranges should be exclusive.
     * @param pTargetSlot What slot was clicked.
     * @param pSlots The menu's item slots. Pass this in directly.
     * @param pDirectSpec A paired item and slot ID. If the item in the clicked slot matches a direct spec, it'll try to enter that slot first.
     * @param pInventoryRange A range of slot IDs that composes the player's inventory.
     *                        X is the first index, Y is the final index.
     * @param pInventoryOutgoingSpec If the clicked slot falls within the inventory range, this determines a prioritized order of slots to try to insert the item into.
     *                               For each entry, X is the first index and Y is the final index.
     *                               This should always include the bag and the hotbar as separate ranges tacked onto the end.
     * @param pInventoryIncomingSpec If the clicked slot falls outside the inventory range, this determines a prioritized order of slots to try to insert into.
     *                               For each entry, X is the first index and Y is the final index.
     *                               NONE of these should overlap, and they should ONLY be within the inventory ID range.
     * @param pContainerSpec The first parameter defines a slot to use as containers for extraction.
     *                       The second parameter defines a range of slots that can only be extracted if sufficient container items are available.
     * @return Ideally ItemStack.EMPTY if everything worked correctly.
     */
    public static ItemStack quickMoveStackHandler(int pTargetSlot, NonNullList<Slot> pSlots, Pair<Item, Integer>[] pDirectSpec, Vector2i pInventoryRange, Vector2i[] pInventoryOutgoingSpec, Vector2i[] pInventoryIncomingSpec, Pair<Integer, Vector2i> pContainerSpec) {
        ItemStack modStack = pSlots.get(pTargetSlot).getItem();
        boolean doContainerLimiting = pContainerSpec != null;

        //First we check to see if the slot was within the provided inventory range.
        if(pTargetSlot >= pInventoryRange.x && pTargetSlot < pInventoryRange.y) {

            //First we check to see if the targeted item is part of the direct specs
            if(pDirectSpec != null) {
                for (Pair<Item, Integer> pair : pDirectSpec) {
                    if (pair.getFirst().equals(modStack.getItem())) {
                        moveItemStackTo(pSlots, modStack, pair.getSecond(), pair.getSecond() + 1, false);
                        if (modStack.isEmpty())
                            break;
                    }
                }
            }

            for(Vector2i spec : pInventoryOutgoingSpec) {
                if(isInRange(spec.x, spec.y, pTargetSlot)) {
                    Pair<Vector2i, Vector2i> safeRange = getSafeRange(spec.x, spec.y, pTargetSlot);
                    if(safeRange.getFirst() != null)
                        moveItemStackTo(pSlots, modStack, safeRange.getFirst().x, safeRange.getFirst().y, false);
                    if(safeRange.getSecond() != null)
                        moveItemStackTo(pSlots, modStack, safeRange.getSecond().x, safeRange.getSecond().y, false);
                } else {
                    moveItemStackTo(pSlots, modStack, spec.x, spec.y, false);
                }

                if(modStack.isEmpty())
                    break;
            }
        }
        //Next we check to see if the slot was outside the provided inventory range.
        else {
            if(doContainerLimiting && isInRange(pContainerSpec.getSecond().x, pContainerSpec.getSecond().y, pTargetSlot)) {
                ItemStack containerStack = pSlots.get(pContainerSpec.getFirst()).getItem();
                ItemStack limitedStack = modStack.copy();
                limitedStack.setCount(Math.min(containerStack.getCount(), limitedStack.getCount()));
                int pre = limitedStack.getCount();

                for(Vector2i spec : pInventoryIncomingSpec) {
                    if(isInRange(spec.x, spec.y, pTargetSlot)) {
                        Pair<Vector2i, Vector2i> safeRange = getSafeRange(spec.x, spec.y, pTargetSlot);
                        if(safeRange.getFirst() != null)
                            moveItemStackTo(pSlots, limitedStack, safeRange.getFirst().x, safeRange.getFirst().y, false);
                        if(safeRange.getSecond() != null)
                            moveItemStackTo(pSlots, limitedStack, safeRange.getSecond().x, safeRange.getSecond().y, false);
                    } else {
                        moveItemStackTo(pSlots, limitedStack, spec.x, spec.y, false);
                    }

                    if(limitedStack.isEmpty()) {
                        break;
                    }
                }

                int post = limitedStack.getCount();
                containerStack.shrink(pre - post);
                modStack.shrink(pre - post);
                return modStack;
            } else {
                for (Vector2i spec : pInventoryIncomingSpec) {
                    if (isInRange(spec.x, spec.y, pTargetSlot)) {
                        Pair<Vector2i, Vector2i> safeRange = getSafeRange(spec.x, spec.y, pTargetSlot);
                        if (safeRange.getFirst() != null)
                            moveItemStackTo(pSlots, modStack, safeRange.getFirst().x, safeRange.getFirst().y, false);
                        if (safeRange.getSecond() != null)
                            moveItemStackTo(pSlots, modStack, safeRange.getSecond().x, safeRange.getSecond().y, false);
                    } else {
                        moveItemStackTo(pSlots, modStack, spec.x, spec.y, false);
                    }

                    if (modStack.isEmpty())
                        break;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * This is a copy of vanilla code found in net.minecraft.world.inventoryAbstractContainerMenu.
     * It is included here with an additional parameter so that quickMoveStackPriorityHandler can function.
     */
    private static boolean moveItemStackTo(NonNullList<Slot> pSlots, ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection) {
        boolean flag = false;
        int i = pStartIndex;
        if (pReverseDirection) {
            i = pEndIndex - 1;
        }

        if (pStack.isStackable()) {
            while(!pStack.isEmpty()) {
                if (pReverseDirection) {
                    if (i < pStartIndex) {
                        break;
                    }
                } else if (i >= pEndIndex) {
                    break;
                }

                Slot slot = pSlots.get(i);
                ItemStack itemstack = slot.getItem();
                if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(pStack, itemstack)) {
                    int j = itemstack.getCount() + pStack.getCount();
                    int maxSize = Math.min(slot.getMaxStackSize(), pStack.getMaxStackSize());
                    if (j <= maxSize) {
                        pStack.setCount(0);
                        itemstack.setCount(j);
                        slot.setChanged();
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        pStack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.setChanged();
                        flag = true;
                    }
                }

                if (pReverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!pStack.isEmpty()) {
            if (pReverseDirection) {
                i = pEndIndex - 1;
            } else {
                i = pStartIndex;
            }

            while(true) {
                if (pReverseDirection) {
                    if (i < pStartIndex) {
                        break;
                    }
                } else if (i >= pEndIndex) {
                    break;
                }

                Slot slot1 = pSlots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (itemstack1.isEmpty() && slot1.mayPlace(pStack)) {
                    if (pStack.getCount() > slot1.getMaxStackSize()) {
                        slot1.setByPlayer(pStack.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.setByPlayer(pStack.split(pStack.getCount()));
                    }

                    slot1.setChanged();
                    flag = true;
                    break;
                }

                if (pReverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }

    private static boolean isInRange(int pMin, int pMaxExclusive, int pQuery) {
        return pQuery >= pMin && pQuery < pMaxExclusive;
    }

    private static Pair<Vector2i, Vector2i> getSafeRange(int pMin, int pMaxExclusive, int pExclude) {
        Vector2i a;
        Vector2i b;

        if(pExclude == pMin) {
            a = null;
            b = new Vector2i(pMin+1, pMaxExclusive);

        } else if (pExclude == pMaxExclusive - 1) {
            a = new Vector2i(pMin, pMaxExclusive-1);
            b = null;
        } else {
            a = new Vector2i(pMin, pExclude);
            b = new Vector2i(pExclude+1, pMaxExclusive);
        }
        return new Pair<>(a, b);
    }
}
