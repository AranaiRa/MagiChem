package com.aranaira.magichem.events;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.renderer.CentrifugeBlockEntityRenderer;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
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
