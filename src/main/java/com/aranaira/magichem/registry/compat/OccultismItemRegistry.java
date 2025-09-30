package com.aranaira.magichem.registry.compat;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.compat.occultism.OccultRitualTalismanItem;
import com.aranaira.magichem.registry.FluidRegistry;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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

    public static final RegistryObject<Item> LIQUEFACTED_SILVER_BUCKET = ITEMS_COMPAT_OCCULTISM.register("liquefacted_silver_bucket",
            () -> new BucketItem(OccultismFluidRegistry.LIQUEFACTED_SILVER, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final RegistryObject<Item> LIQUEFACTED_IESNIUM_BUCKET = ITEMS_COMPAT_OCCULTISM.register("liquefacted_iesnium_bucket",
            () -> new BucketItem(OccultismFluidRegistry.LIQUEFACTED_IESNIUM, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static void register(IEventBus eventBus) {
        ITEMS_COMPAT_OCCULTISM.register(eventBus);
    }
}
