package com.aranaira.magichem.registry;

import com.aranaira.magichem.entities.renderers.DestructiveHarmonicsEntityRenderer;
import com.aranaira.magichem.entities.renderers.GnosticOrbExecutorEntityRenderer;
import com.aranaira.magichem.entities.renderers.SublimationRitualVFXEntityRenderer;
import com.aranaira.magichem.entities.renderers.ShlorpEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class EntitiesClientRegistry {

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerEntityRenderers(FMLClientSetupEvent event) {
        EntityRenderers.register(EntitiesRegistry.SHLORP_ENTITY.get(), ShlorpEntityRenderer::new);
        EntityRenderers.register(EntitiesRegistry.SUBLIMATION_RITUAL_VFX_ENTITY.get(), SublimationRitualVFXEntityRenderer::new);
        EntityRenderers.register(EntitiesRegistry.THROWN_THUNDERSTONE_ENTITY.get(), ThrownItemRenderer::new);
        EntityRenderers.register(EntitiesRegistry.DESTRUCTIVE_HARMONICS_ENTITY.get(), DestructiveHarmonicsEntityRenderer::new);
        EntityRenderers.register(EntitiesRegistry.GNOSTIC_ORB_EXECUTOR_ENTITY.get(), GnosticOrbExecutorEntityRenderer::new);
    }
}
