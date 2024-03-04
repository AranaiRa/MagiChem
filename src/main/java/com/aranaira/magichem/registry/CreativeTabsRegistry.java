package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.mna.items.base.INoCreativeTab;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CreativeTabsRegistry {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MagiChemMod.MODID);

    public static RegistryObject<CreativeModeTab> MAGICHEM_TAB = TABS.register("magichem_main",
            () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(BlockRegistry.CIRCLE_POWER.get()))
            .title(Component.translatable("tab.magichem"))
            .displayItems((parameters, output) -> {
                ItemRegistry.ITEMS.getEntries().stream().forEach((item) -> {
                    if(item.get() instanceof BlockItem bi) {
                        boolean skip = bi.getBlock() instanceof INoCreativeTab;
                        if(!skip) output.accept(item.get());
                    } else if(!ItemRegistry.ITEMS_EXCLUDED_FROM_TABS.contains(item))
                        output.accept(item.get());
                });
            })
            .build());

    public static final RegistryObject<CreativeModeTab> MAGICHEM_MATERIA_TAB = TABS.register("magichem_materia",
            () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(ItemRegistry.ADMIXTURES.getEntries().stream().findAny().get().get()))
            .title(Component.translatable("tab.magichem.materia"))
            .displayItems((parameters, output) -> {
                ItemRegistry.ESSENTIA.getEntries().stream().forEach((item) -> {
                    output.accept(item.get());
                });
                ItemRegistry.ADMIXTURES.getEntries().stream().forEach((item) -> {
                    output.accept(item.get());
                });
            })
            .build());

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
