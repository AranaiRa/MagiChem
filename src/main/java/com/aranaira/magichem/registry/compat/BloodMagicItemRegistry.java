package com.aranaira.magichem.registry.compat;

import com.aranaira.magichem.MagiChemMod;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BloodMagicItemRegistry {

    public static final DeferredRegister<Item> ITEMS_COMPAT_BLOOD_MAGIC = DeferredRegister.create(ForgeRegistries.ITEMS, MagiChemMod.MODID);

    ///////////////
    // BLOOD MAGIC COMPAT ITEMS
    ///////////////

    public static final RegistryObject<Item> LIQUEFACTED_DEMONITE_BUCKET = ITEMS_COMPAT_BLOOD_MAGIC.register("liquefacted_demonite_bucket",
            () -> new BucketItem(BloodMagicFluidRegistry.LIQUEFACTED_DEMONITE, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static void register(IEventBus eventBus) {
        ITEMS_COMPAT_BLOOD_MAGIC.register(eventBus);
    }
}
