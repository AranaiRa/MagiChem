/**
 * This class largely adapted from a class from AlchemyLib. See the original here:
 * https://github.com/SmashingMods/AlchemyLib/blob/1.19.x/src/main/java/com/smashingmods/alchemylib/api/blockentity/container/FakeItemRenderer.java
 */

package com.aranaira.magichem.util.render;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.entities.renderers.ShlorpEntityRenderer;
import com.aranaira.magichem.item.MateriaItem;
import com.mna.tools.math.Vector3;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2d;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class RenderUtils {

    public static final Minecraft MINECRAFT_REF = Minecraft.getInstance();
    public static final ItemRenderer ITEM_RENDERER = MINECRAFT_REF.getItemRenderer();

    private static ModelResourceLocation getResourceLocation(ItemStack stack) {
        ModelResourceLocation output = null;

        if (stack.getItem() instanceof MateriaItem) {
            output = new ModelResourceLocation(new ResourceLocation(MagiChemMod.MODID, stack.getItem().toString()), "inventory");
        }

        return output;
    }

    //This function taken from EnderIO. See the original here: https://github.com/Team-EnderIO/EnderIO/blob/dev/1.19.x/src/core/java/com/enderio/core/client/RenderUtil.java
    public static void renderFace(Direction face, Matrix4f pose, Matrix3f normal, VertexConsumer consumer, TextureAtlasSprite texture,
                                  float x, float y, float z, float w, float h, int tint, int packedLight) {
        switch (face) {
            case DOWN -> renderFace(pose, normal, consumer, texture, tint, packedLight, x, x + w, 1.0f - z, 1.0f - z, y, y, y + h, y + h, x, x + w, y, y + h, 0, -1, 0);
            case UP -> renderFace(pose, normal, consumer, texture, tint, packedLight, x, x + w, z, z, y + h, y + h, y, y, x, x + w, y, y + h, 0, 1, 0);
            case NORTH -> renderFace(pose, normal, consumer, texture, tint, packedLight, x, x + w, y + h, y, z, z, z, z, x, x + w, y, y + h, 0, 0, -1);
            case SOUTH -> renderFace(pose, normal, consumer, texture, tint, packedLight, x, x + w, y, y + h, 1.0f - z, 1.0f - z, 1.0f - z, 1.0f - z, x + w, x, y + h, y, 0, 0, 1);
            case WEST -> renderFace(pose, normal, consumer, texture, tint, packedLight, 1.0f - z, 1.0f - z, y + h, y, x, x + w, x + w, x, x, x + w, y, y + h, 1, 0, 0);
            case EAST -> renderFace(pose, normal, consumer, texture, tint, packedLight, z, z, y, y + h, x, x + w, x + w, x, x + w, x, y + h, y, -1, 0, 0);
        }
    }

    public static void renderFaceWithUV(Direction face, Matrix4f pose, Matrix3f normal, VertexConsumer consumer, TextureAtlasSprite texture,
                                        float x, float y, float z, float w, float h,
                                        float u, float uw, float v, float vh,
                                        int tint, int packedLight) {
        switch (face) {
            case DOWN -> renderFace(pose, normal, consumer, texture, tint, packedLight, x, x + w, 1.0f - z, 1.0f - z, y, y, y + h, y + h, u, uw, v, vh, 0, -1, 0);
            case UP -> renderFace(pose, normal, consumer, texture, tint, packedLight, x, x + w, z, z, y + h, y + h, y, y, u, uw, v, vh, 0, 1, 0);
            case NORTH -> renderFace(pose, normal, consumer, texture, tint, packedLight, x, x + w, y + h, y, z, z, z, z, u, uw, v, vh, 0, 0, -1);
            case SOUTH -> renderFace(pose, normal, consumer, texture, tint, packedLight, x, x + w, y, y + h, 1.0f - z, 1.0f - z, 1.0f - z, 1.0f - z, u, uw, v, vh, 0, 0, 1);
            case WEST -> renderFace(pose, normal, consumer, texture, tint, packedLight, 1.0f - z, 1.0f - z, y + h, y, x, x + w, x + w, x, u, uw, v, vh, 1, 0, 0);
            case EAST -> renderFace(pose, normal, consumer, texture, tint, packedLight, z, z, y, y + h, x, x + w, x + w, x, u, uw, v, vh, -1, 0, 0);
        }
    }

    //This function taken from EnderIO. See the original here: https://github.com/Team-EnderIO/EnderIO/blob/dev/1.20.1/src/core/java/com/enderio/core/client/RenderUtil.java
    private static void renderFace(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, TextureAtlasSprite texture, int tint, int packedLight,
                                   float x0, float x1, float y0, float y1, float z0, float z1, float z2,
                                   float z3, float u0, float u1, float v0, float v1,
                                   float normalX, float normalY, float normalZ) {
        float minU = u0 * (float)texture.contents().width();
        float maxU = u1 * (float)texture.contents().width();
        float minV = v0 * (float)texture.contents().height();
        float maxV = v1 * (float)texture.contents().height();

        consumer.vertex(pose, x0, y0, z0).color(tint).uv(texture.getU(minU), texture.getV(minV)).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, normalX, normalY, normalZ).endVertex();
        consumer.vertex(pose, x1, y0, z1).color(tint).uv(texture.getU(maxU), texture.getV(minV)).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, normalX, normalY, normalZ).endVertex();
        consumer.vertex(pose, x1, y1, z2).color(tint).uv(texture.getU(maxU), texture.getV(maxV)).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, normalX, normalY, normalZ).endVertex();
        consumer.vertex(pose, x0, y1, z3).color(tint).uv(texture.getU(minU), texture.getV(maxV)).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, normalX, normalY, normalZ).endVertex();
    }

    public static void generateMagicCircleRing(Vector3 pCenter, int pPointCount, float pRadius, float pThickness, float pRotation, TextureAtlasSprite pTexture, Vec2 pUV1, Vec2 pUV2, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        float thetaD = ((pPointCount - 2) * 180f) / pPointCount;
        float thetaR = thetaD * (float)Math.PI / 180f;
        float inset = pThickness / (float)Math.sin(thetaR / 2);
        float swivel = (float)(Math.PI * 2) / pPointCount;

        //figure out the unit vectors needed to generate our geometry
        List<Vector3> vectors = new ArrayList<>();
        for(int i=0; i<pPointCount; i++) {
            float x = (float) Math.cos(swivel * i + pRotation);
            float y = (float) Math.sin(swivel * i + pRotation);

            vectors.add(new Vector3(x, 0, y));
        }

        //generate our vertices
        List<QuadVertData> vertData = new ArrayList<>();
        for(int i=0; i<vectors.size(); i++) {
            Vector3 left = vectors.get(i);
            Vector3 right;

            if(i == vectors.size() - 1) {
                right = vectors.get(0);
            } else {
                right = vectors.get(i + 1);
            }

            SingleVertex a = new SingleVertex(
                    pCenter.add(left.scale(pRadius))
            );
            SingleVertex b = new SingleVertex(
                    pCenter.add(right.scale(pRadius))
            );
            SingleVertex c = new SingleVertex(
                    pCenter.add(right.scale(pRadius-inset))
            );
            SingleVertex d = new SingleVertex(
                    pCenter.add(left.scale(pRadius-inset))
            );

            vertData.add(new QuadVertData(a, b, c, d));
        }

        //render that shit!
//        VertexConsumer vertexBuilder = pBuffer.getBuffer(RenderType.solid());
        VertexConsumer vertexBuilder = pBuffer.getBuffer(RenderType.cutout());
        Matrix4f renderMatrix = pPoseStack.last().pose();
        Matrix3f normalMatrix = pPoseStack.last().normal();
        int[] color = new int[]{255, 255, 255, 255};

        for(QuadVertData qvd : vertData) {

            pPoseStack.pushPose();
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    qvd.a.x, qvd.a.y, qvd.a.z, pUV1.x, pUV1.y, 0, 0, color);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    qvd.d.x, qvd.d.y, qvd.d.z, pUV1.x, pUV2.y, 0, 0, color);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    qvd.c.x, qvd.c.y, qvd.c.z, pUV2.x, pUV2.y, 0, 0, color);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    qvd.b.x, qvd.b.y, qvd.b.z, pUV2.x, pUV1.y, 0, 0, color);
            pPoseStack.popPose();

            pPoseStack.pushPose();
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    qvd.b.x, qvd.b.y, qvd.b.z, pUV2.x, pUV1.y, 0, 0, color);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    qvd.c.x, qvd.c.y, qvd.c.z, pUV2.x, pUV2.y, 0, 0, color);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    qvd.d.x, qvd.d.y, qvd.d.z, pUV1.x, pUV2.y, 0, 0, color);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    qvd.a.x, qvd.a.y, qvd.a.z, pUV1.x, pUV1.y, 0, 0, color);

            pPoseStack.popPose();

        }
    }

    private static void addVertex(VertexConsumer pVertexBuilder, Matrix4f pRenderMatrix, Matrix3f pNormalMatrix, TextureAtlasSprite texture, int pPackedLight, float x, float y, float z, float u, float v, float nrmH, float nrmV, int[] rgba) {
        pVertexBuilder.vertex(pRenderMatrix, x, y, z).color(rgba[0], rgba[1], rgba[2], rgba[3]).uv(texture.getU(u), texture.getV(v)).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(pNormalMatrix, nrmH, nrmV, nrmH).endVertex();
    }

    private static class SingleVertex {

        public float x;
        public float y;
        public float z;
        public static final int[] COLOR_WHITE = {255, 255, 255, 255};

        public SingleVertex(Vector3 coords) {
            this.x = coords.x;
            this.y = coords.y;
            this.z = coords.z;
        }
    }

    private static class QuadVertData {
        public SingleVertex a, b, c, d;

        public QuadVertData(SingleVertex pA, SingleVertex pB, SingleVertex pC, SingleVertex pD) {
            this.a = pA;
            this.b = pB;
            this.c = pC;
            this.d = pD;
        }
    }
}