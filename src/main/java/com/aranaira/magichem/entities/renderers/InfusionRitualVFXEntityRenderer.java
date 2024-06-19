package com.aranaira.magichem.entities.renderers;

import com.aranaira.magichem.entities.InfusionRitualVFXEntity;
import com.aranaira.magichem.entities.ShlorpEntity;
import com.mna.tools.math.Vector3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

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
