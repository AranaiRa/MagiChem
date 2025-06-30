package com.aranaira.magichem.entities.renderers;

import com.aranaira.magichem.entities.GnosticOrbExecutorEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class GnosticOrbExecutorEntityRenderer extends EntityRenderer<GnosticOrbExecutorEntity> {
    public GnosticOrbExecutorEntityRenderer(Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(GnosticOrbExecutorEntity pEntity) {
        return null;
    }

    @Override
    public void render(GnosticOrbExecutorEntity pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {

    }
}
