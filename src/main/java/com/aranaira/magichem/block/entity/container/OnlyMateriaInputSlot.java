package com.aranaira.magichem.block.entity.container;

import com.aranaira.magichem.gui.CircleFabricationMenu;
import com.aranaira.magichem.item.MateriaItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class OnlyMateriaInputSlot extends SlotItemHandler {

    private MateriaItem slotFilter;

    public OnlyMateriaInputSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    public MateriaItem getSlotFilter() {
        return slotFilter;
    }

    public void setSlotFilter(MateriaItem pSlotFilter) {
        slotFilter = pSlotFilter;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {

        if(stack.getItem() instanceof MateriaItem) {
            if(slotFilter != null) {
                if(stack.getItem() != slotFilter)
                    return false;
            }

            return true;
        }
        return false;
    }
}
