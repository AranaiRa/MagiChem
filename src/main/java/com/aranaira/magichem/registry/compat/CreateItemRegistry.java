package com.aranaira.magichem.registry.compat;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.compat.occultism.OccultRitualTalismanItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CreateItemRegistry {

    public static final DeferredRegister<Item> ITEMS_COMPAT_CREATE = DeferredRegister.create(ForgeRegistries.ITEMS, MagiChemMod.MODID);

    ///////////////
    // CREATE COMPAT ITEMS
    ///////////////

    public static final RegistryObject<Item> LIQUEFACTED_ZINC_BUCKET = ITEMS_COMPAT_CREATE.register("liquefacted_zinc_bucket",
            () -> new BucketItem(CreateFluidRegistry.LIQUEFACTED_ZINC, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static void register(IEventBus eventBus) {
        ITEMS_COMPAT_CREATE.register(eventBus);
    }
}
