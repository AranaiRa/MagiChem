package com.aranaira.magichem.registry;

import com.aranaira.magichem.entities.renderers.ShlorpEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class EntitiesClientRegistry {

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerEntityRenderers(FMLClientSetupEvent event) {
        EntityRenderers.register((EntityType)EntitiesRegistry.SHLORP_ENTITY.get(), ShlorpEntityRenderer::new);
    }
}
