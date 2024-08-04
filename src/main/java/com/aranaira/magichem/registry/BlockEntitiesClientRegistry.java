package com.aranaira.magichem.registry;

import com.aranaira.magichem.block.entity.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class BlockEntitiesClientRegistry {

    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(BlockEntitiesRegistry.MATERIA_JAR_BE.get(), MateriaJarBlockEntityRenderer::new);
        BlockEntityRenderers.register(BlockEntitiesRegistry.MATERIA_VESSEL_BE.get(), MateriaVesselBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.ALEMBIC_BE.get(), AlembicBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.CENTRIFUGE_BE.get(), CentrifugeBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.DISTILLERY_BE.get(), DistilleryBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.FUSERY_BE.get(), FuseryBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.GRAND_DISTILLERY_BE.get(), GrandDistilleryBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.ALCHEMICAL_NEXUS_BE.get(), AlchemicalNexusBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.ACTUATOR_FIRE_BE.get(), ActuatorFireBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.ACTUATOR_WATER_BE.get(), ActuatorWaterBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.ACTUATOR_EARTH_BE.get(), ActuatorEarthBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.ACTUATOR_AIR_BE.get(), ActuatorAirBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(BlockEntitiesRegistry.EXPERIENCE_EXCHANGER_BE.get(), ExperienceExchangerBlockEntityRenderer::new);
    }
}
