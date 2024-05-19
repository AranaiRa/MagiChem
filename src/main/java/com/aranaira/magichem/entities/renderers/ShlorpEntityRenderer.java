package com.aranaira.magichem.entities.renderers;

import com.aranaira.magichem.entities.ShlorpEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class ShlorpEntityRenderer extends EntityRenderer<ShlorpEntity> {
    public ShlorpEntityRenderer(Context pContext) {
        super(pContext);
    }

    @Override
    public ResourceLocation getTextureLocation(ShlorpEntity pEntity) {
        return null;
    }

    @Override
    public void render(ShlorpEntity pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {

    }
}
