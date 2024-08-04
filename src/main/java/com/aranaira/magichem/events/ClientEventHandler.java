package com.aranaira.magichem.events;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.KeybindInit;
import com.mna.gui.radial.*;
import com.mna.items.sorcery.ItemSpellBook;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = MagiChemMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientEventHandler {
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register( (stack, layer) -> (layer == 0 && stack.getItem() instanceof MateriaItem mItem) ? mItem.getMateriaColor() : -1, ItemRegistry.getEssentia().toArray(new EssentiaItem[0]));
        event.register( (stack, layer) -> (layer == 0 && stack.getItem() instanceof MateriaItem mItem) ? mItem.getMateriaColor() : -1, ItemRegistry.getAdmixtures().toArray(new AdmixtureItem[0]));
    }
}
