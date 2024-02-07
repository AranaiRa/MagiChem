package com.aranaira.magichem.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeModeTabs {
    public static CreativeModeTab MAGICHEM_TAB = CreativeModeTab.builder()
            .icon(() -> new ItemStack(BlockRegistry.CIRCLE_POWER.get()))
            .title(Component.translatable("tab.magichem"))
            .build();

    public static final CreativeModeTab MAGICHEM_MATERIA_TAB = CreativeModeTab.builder()
            .icon(() -> new ItemStack(ItemRegistry.ADMIXTURES.getEntries().stream().findAny().get().get()))
            .title(Component.translatable("tab.magichem.materia"))
            .build();
}
