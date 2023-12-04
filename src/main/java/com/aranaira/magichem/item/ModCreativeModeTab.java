package com.aranaira.magichem.item;

import com.aranaira.magichem.block.ModBlocks;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTab {
    public static final CreativeModeTab MAGICHEM_TAB = new CreativeModeTab("magichemtab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModBlocks.MAGIC_CIRCLE.get());
        }
    };
}
