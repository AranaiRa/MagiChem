package com.aranaira.magichem.registry.compat;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.compat.occultism.OccultRitualTalismanItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class OccultismItemRegistry {

    public static final DeferredRegister<Item> ITEMS_COMPAT_OCCULTISM = DeferredRegister.create(ForgeRegistries.ITEMS, MagiChemMod.MODID);

    ///////////////
    // OCCULTISM COMPAT ITEMS
    ///////////////

    public static final RegistryObject<Item> OCCULT_RITUAL_TALISMAN = ITEMS_COMPAT_OCCULTISM.register("occult_ritual_talisman",
            () -> new OccultRitualTalismanItem(new Item.Properties().stacksTo(16))
    );

    public static void register(IEventBus eventBus) {
        ITEMS_COMPAT_OCCULTISM.register(eventBus);
    }
}
