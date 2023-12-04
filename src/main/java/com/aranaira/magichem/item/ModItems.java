package com.aranaira.magichem.item;

import com.aranaira.magichem.MagiChemMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MagiChemMod.MODID);

    public static final RegistryObject<Item> SILVER_DUST = ITEMS.register("silver_dust",
            () -> new Item(new Item.Properties().durability(64).defaultDurability(64).setNoRepair().tab(ModCreativeModeTab.MAGICHEM_TAB))
    );

    public static final RegistryObject<Item> TARNISHED_SILVER_LUMP = ITEMS.register("tarnished_silver_lump",
            () -> new Item(new Item.Properties().tab(ModCreativeModeTab.MAGICHEM_TAB))
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
