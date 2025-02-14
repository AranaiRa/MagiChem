package com.aranaira.magichem.entities.renderers;

import com.aranaira.magichem.entities.DestructiveHarmonicsEntity;
import com.aranaira.magichem.entities.SublimationRitualVFXEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class DestructiveHarmonicsEntityRenderer extends EntityRenderer<DestructiveHarmonicsEntity> {
    public DestructiveHarmonicsEntityRenderer(Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(DestructiveHarmonicsEntity pEntity) {
        return null;
    }

    @Override
    public void render(DestructiveHarmonicsEntity pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {

    }
}
