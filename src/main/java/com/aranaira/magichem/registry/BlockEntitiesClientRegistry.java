package com.aranaira.magichem.registry;

import com.aranaira.magichem.block.entity.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class BlockEntitiesClientRegistry {

    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(BlockEntitiesRegistry.MATERIA_VESSEL_BE.get(), MateriaVesselBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.CENTRIFUGE_BE.get(), CentrifugeBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.ACTUATOR_FIRE_BE.get(), ActuatorFireBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.ACTUATOR_WATER_BE.get(), ActuatorWaterBlockEntityRenderer::new);
    }
}
