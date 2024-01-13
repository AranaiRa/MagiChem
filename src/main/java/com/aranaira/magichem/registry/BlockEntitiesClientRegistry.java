package com.aranaira.magichem.registry;

import com.aranaira.magichem.block.entity.renderer.MateriaVesselBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class BlockEntitiesClientRegistry {

    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(BlockEntitiesRegistry.MATERIA_VESSEL_BE.get(), MateriaVesselBlockEntityRenderer::new);
    }
}
