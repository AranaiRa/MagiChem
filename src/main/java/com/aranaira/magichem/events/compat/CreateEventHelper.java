package com.aranaira.magichem.events.compat;

import com.aranaira.magichem.registry.compat.CreateItemRegistry;
import net.minecraft.world.item.BucketItem;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import java.util.Random;

public class CreateEventHelper {
    private static final Random r = new Random();

    public static void registerItemLayers(RegisterColorHandlersEvent.Item event) {
        event.register( (stack, layer) -> (layer == 1 && stack.getItem() instanceof BucketItem mItem) ? IClientFluidTypeExtensions.of(mItem.getFluid()).getTintColor() : -1, CreateItemRegistry.LIQUEFACTED_ZINC_BUCKET.get());
    }
}
