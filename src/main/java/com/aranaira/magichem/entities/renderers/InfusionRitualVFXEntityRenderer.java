package com.aranaira.magichem.entities.renderers;

import com.aranaira.magichem.entities.InfusionRitualVFXEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class InfusionRitualVFXEntityRenderer extends EntityRenderer<InfusionRitualVFXEntity> {
    public InfusionRitualVFXEntityRenderer(Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(InfusionRitualVFXEntity pEntity) {
        return null;
    }

    @Override
    public void render(InfusionRitualVFXEntity pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {

    }
}
