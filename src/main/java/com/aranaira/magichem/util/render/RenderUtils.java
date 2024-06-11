/**
 * This class largely adapted from a class from AlchemyLib. See the original here:
 * https://github.com/SmashingMods/AlchemyLib/blob/1.19.x/src/main/java/com/smashingmods/alchemylib/api/blockentity/container/FakeItemRenderer.java
 */

package com.aranaira.magichem.util.render;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.entities.renderers.ShlorpEntityRenderer;
import com.aranaira.magichem.item.MateriaItem;
import com.mna.tools.math.MathUtils;
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

    public static void generateMagicCircleRing(Vector3 pCenter, int pPointCount, float pRadius, float pThickness, float pRotation, TextureAtlasSprite pTexture, Vec2 pUV1, Vec2 pUV2, float pTextureTileDistance, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        float thetaD = ((pPointCount - 2) * 180f) / pPointCount;
        float thetaR = thetaD * (float)Math.PI / 180f;
        float insetLeg = pThickness / (float)Math.tan(thetaR / 2);
        float insetHypotenuse = pThickness / (float)Math.sin(thetaR / 2);
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
                    pCenter.add(right.scale(pRadius-insetHypotenuse))
            );
            SingleVertex d = new SingleVertex(
                    pCenter.add(left.scale(pRadius-insetHypotenuse))
            );

            vertData.add(new QuadVertData(a, b, c, d));
        }

        //Some calculations and references necessary for rendering
        VertexConsumer vertexBuilder = pBuffer.getBuffer(RenderType.cutout());
        Matrix4f renderMatrix = pPoseStack.last().pose();
        Matrix3f normalMatrix = pPoseStack.last().normal();
        int[] color = new int[]{255, 255, 255, 255};

        //cancel rendering the circle if there's too few circle points to prevent OOB exceptions
        if(vertData.size() <= 1)
            return;

        List<Float> divisionPoints = new ArrayList<>();
        float distanceBetweenExteriorPoints =
                (float)new Vector3(vertData.get(0).a.x, vertData.get(0).a.y, vertData.get(0).a.z).distanceTo(
                        new Vector3(vertData.get(0).b.x, vertData.get(0).b.y, vertData.get(0).b.z)
                );
        float distanceBetweenInteriorPoints =
                (float)new Vector3(vertData.get(0).c.x, vertData.get(0).c.y, vertData.get(0).c.z).distanceTo(
                        new Vector3(vertData.get(0).d.x, vertData.get(0).d.y, vertData.get(0).d.z)
                );

        if(distanceBetweenExteriorPoints > pTextureTileDistance * 4) {
            float remainingDistance = distanceBetweenExteriorPoints;

            divisionPoints.add(pTextureTileDistance);
            remainingDistance -= pTextureTileDistance;

            while(remainingDistance > pTextureTileDistance * 4) {
                remainingDistance -= pTextureTileDistance;
                divisionPoints.add(distanceBetweenExteriorPoints - remainingDistance);
            }

            divisionPoints.add(distanceBetweenExteriorPoints - pTextureTileDistance);

        } else if(distanceBetweenExteriorPoints > pTextureTileDistance) {
            divisionPoints.add(distanceBetweenExteriorPoints / 2);
        }

        //Rendering if we have to carve up the circle for texture tiling
        if(divisionPoints.size() > 0) {
            int[] red = new int[]{255, 0, 0, 255};

            for(QuadVertData qvd : vertData) {

                //simple solution, we only have to split it down the middle
                if(divisionPoints.size() == 1) {
                    float textureSpanPercent = distanceBetweenExteriorPoints / pTextureTileDistance;
                    float pxWidth = textureSpanPercent * 4 * pTextureTileDistance;

                    Vector2f ull = new Vector2f(pUV1.x + (insetLeg / distanceBetweenExteriorPoints) * pxWidth * 2, pUV1.y);
                    Vector2f url = new Vector2f(pUV1.x + pxWidth, pUV1.y);
                    Vector2f lll = new Vector2f(pUV1.x, pUV2.y);
                    Vector2f lrl = new Vector2f(pUV1.x + pxWidth, pUV2.y);

                    Vector2f ulr = new Vector2f(pUV2.x - pxWidth, pUV1.y);
                    Vector2f urr = new Vector2f(pUV2.x - (insetLeg / distanceBetweenExteriorPoints) * pxWidth * 2, pUV1.y);
                    Vector2f llr = new Vector2f(pUV2.x - pxWidth, pUV2.y);
                    Vector2f lrr = new Vector2f(pUV2.x, pUV2.y);

                    //Top faces
                    pPoseStack.pushPose();
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                            qvd.a.x, qvd.a.y, qvd.a.z,
                            lrr.x, lrr.y, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                            qvd.d.x, qvd.d.y, qvd.d.z,
                            urr.x, urr.y, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                            MathUtils.lerpf(qvd.c.x, qvd.d.x, 0.5f), qvd.c.y, MathUtils.lerpf(qvd.c.z, qvd.d.z, 0.5f),
                            ulr.x, ulr.y, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                            MathUtils.lerpf(qvd.b.x, qvd.a.x, 0.5f), qvd.b.y, MathUtils.lerpf(qvd.b.z, qvd.a.z, 0.5f),
                            llr.x, llr.y, 0, 0, color);
                    pPoseStack.popPose();

                    pPoseStack.pushPose();
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                            MathUtils.lerpf(qvd.a.x, qvd.b.x, 0.5f), qvd.a.y, MathUtils.lerpf(qvd.a.z, qvd.b.z, 0.5f),
                            lrl.x, lrl.y, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                            MathUtils.lerpf(qvd.d.x, qvd.c.x, 0.5f), qvd.d.y, MathUtils.lerpf(qvd.d.z, qvd.c.z, 0.5f),
                            url.x, url.y, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                            qvd.c.x, qvd.c.y, qvd.c.z,
                            ull.x, ull.y, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                            qvd.b.x, qvd.b.y, qvd.b.z,
                            lll.x, lll.y, 0, 0, color);
                    pPoseStack.popPose();

                    //Bottom faces
                    pPoseStack.pushPose();
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                            MathUtils.lerpf(qvd.b.x, qvd.a.x, 0.5f), qvd.b.y, MathUtils.lerpf(qvd.b.z, qvd.a.z, 0.5f),
                            llr.x, llr.y, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                            MathUtils.lerpf(qvd.c.x, qvd.d.x, 0.5f), qvd.c.y, MathUtils.lerpf(qvd.c.z, qvd.d.z, 0.5f),
                            ulr.x, ulr.y, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                            qvd.d.x, qvd.d.y, qvd.d.z,
                            urr.x, urr.y, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                            qvd.a.x, qvd.a.y, qvd.a.z,
                            lrr.x, lrr.y, 0, 0, color);
                    pPoseStack.popPose();

                    pPoseStack.pushPose();
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                            qvd.b.x, qvd.b.y, qvd.b.z,
                            lll.x, lll.y, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                            qvd.c.x, qvd.c.y, qvd.c.z,
                            ull.x, ull.y, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                            MathUtils.lerpf(qvd.d.x, qvd.c.x, 0.5f), qvd.d.y, MathUtils.lerpf(qvd.d.z, qvd.c.z, 0.5f),
                            url.x, url.y, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                            MathUtils.lerpf(qvd.a.x, qvd.b.x, 0.5f), qvd.a.y, MathUtils.lerpf(qvd.a.z, qvd.b.z, 0.5f),
                            lrl.x, lrl.y, 0, 0, color);
                    pPoseStack.popPose();
                }
                //set up a full length quad on both end caps and then tile the middle
                else {
                    float remainingDistance = distanceBetweenExteriorPoints - pTextureTileDistance * 4;
                    float newFaceInset = 0f;

                    Vector2f ull = new Vector2f(pUV1.x + (insetLeg / pTextureTileDistance) * 6, pUV1.y);
                    Vector2f url = new Vector2f(pUV2.x, pUV1.y);
                    Vector2f lll = new Vector2f(pUV1.x, pUV2.y);
                    Vector2f lrl = new Vector2f(pUV2.x, pUV2.y);

                    Vector2f ulr = new Vector2f(pUV1.x, pUV1.y);
                    Vector2f urr = new Vector2f(pUV2.x - (insetLeg / pTextureTileDistance) * 6, pUV1.y);
                    Vector2f llr = new Vector2f(pUV1.x, pUV2.y);
                    Vector2f lrr = new Vector2f(pUV2.x, pUV2.y);

                    float capLerpPercentInner = 1 - (((pTextureTileDistance - insetLeg * .5f) / distanceBetweenInteriorPoints) * 2);
                    float capLerpPercentOuter = 1 - ((pTextureTileDistance / distanceBetweenExteriorPoints) * 2);

                    //Left cap
                    {
                        //Top face
                        pPoseStack.pushPose();
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(qvd.a.x, qvd.b.x, capLerpPercentOuter), qvd.a.y, MathUtils.lerpf(qvd.a.z, qvd.b.z, capLerpPercentOuter),
                                lrl.x, lrl.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(qvd.d.x, qvd.c.x, capLerpPercentInner), qvd.d.y, MathUtils.lerpf(qvd.d.z, qvd.c.z, capLerpPercentInner),
                                url.x, url.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                qvd.c.x, qvd.c.y, qvd.c.z,
                                ull.x, ull.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                qvd.b.x, qvd.b.y, qvd.b.z,
                                lll.x, lll.y, 0, 0, color);
                        pPoseStack.popPose();

                        //Bottom face
                        pPoseStack.pushPose();
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                qvd.b.x, qvd.b.y, qvd.b.z,
                                lll.x, lll.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                qvd.c.x, qvd.c.y, qvd.c.z,
                                ull.x, ull.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(qvd.d.x, qvd.c.x, capLerpPercentInner), qvd.d.y, MathUtils.lerpf(qvd.d.z, qvd.c.z, capLerpPercentInner),
                                url.x, url.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(qvd.a.x, qvd.b.x, capLerpPercentOuter), qvd.a.y, MathUtils.lerpf(qvd.a.z, qvd.b.z, capLerpPercentOuter),
                                lrl.x, lrl.y, 0, 0, color);
                        pPoseStack.popPose();
                    }
                    //Right cap
                    {
                        //Top face
                        pPoseStack.pushPose();
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                qvd.a.x, qvd.a.y, qvd.a.z,
                                lrr.x, lrr.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                qvd.d.x, qvd.d.y, qvd.d.z,
                                urr.x, urr.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(qvd.c.x, qvd.d.x, capLerpPercentInner), qvd.c.y, MathUtils.lerpf(qvd.c.z, qvd.d.z, capLerpPercentInner),
                                ulr.x, ulr.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(qvd.b.x, qvd.a.x, capLerpPercentOuter), qvd.b.y, MathUtils.lerpf(qvd.b.z, qvd.a.z, capLerpPercentOuter),
                                llr.x, llr.y, 0, 0, color);
                        pPoseStack.popPose();

                        //Bottom face
                        pPoseStack.pushPose();
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(qvd.b.x, qvd.a.x, capLerpPercentOuter), qvd.b.y, MathUtils.lerpf(qvd.b.z, qvd.a.z, capLerpPercentOuter),
                                llr.x, llr.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(qvd.c.x, qvd.d.x, capLerpPercentInner), qvd.c.y, MathUtils.lerpf(qvd.c.z, qvd.d.z, capLerpPercentInner),
                                ulr.x, ulr.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                qvd.d.x, qvd.d.y, qvd.d.z,
                                urr.x, urr.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                qvd.a.x, qvd.a.y, qvd.a.z,
                                lrr.x, lrr.y, 0, 0, color);
                        pPoseStack.popPose();
                    }

                    Vector3 bUL = new Vector3(MathUtils.lerpf(qvd.d.x, qvd.c.x, capLerpPercentInner), qvd.d.y, MathUtils.lerpf(qvd.d.z, qvd.c.z, capLerpPercentInner));
                    Vector3 bLL = new Vector3(MathUtils.lerpf(qvd.a.x, qvd.b.x, capLerpPercentOuter), qvd.a.y, MathUtils.lerpf(qvd.a.z, qvd.b.z, capLerpPercentOuter));
                    Vector3 bUR = new Vector3(MathUtils.lerpf(qvd.c.x, qvd.d.x, capLerpPercentInner), qvd.c.y, MathUtils.lerpf(qvd.c.z, qvd.d.z, capLerpPercentInner));
                    Vector3 bLR = new Vector3(MathUtils.lerpf(qvd.b.x, qvd.a.x, capLerpPercentOuter), qvd.b.y, MathUtils.lerpf(qvd.b.z, qvd.a.z, capLerpPercentOuter));

                    float interiorWidth = (float)bLR.distanceTo(bLL);

                    while(remainingDistance > pTextureTileDistance * 4) {
                        float percentL = (newFaceInset) / (interiorWidth);
                        float percentR = (newFaceInset + pTextureTileDistance * 2) / (interiorWidth);

                        //Left side
                        {
                            //Top face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, percentR), bLR.y, MathUtils.lerpf(bLL.z, bLR.z, percentR),
                                        pUV2.x, pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, percentR), bUR.y, MathUtils.lerpf(bUL.z, bUR.z, percentR),
                                        pUV2.x, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, percentL), bUL.y, MathUtils.lerpf(bUL.z, bUR.z, percentL),
                                        pUV1.x, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, percentL), bLL.y, MathUtils.lerpf(bLL.z, bLR.z, percentL),
                                        pUV1.x, pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }

                            //Bottom face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, percentL), bLL.y, MathUtils.lerpf(bLL.z, bLR.z, percentL),
                                        pUV1.x, pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, percentL), bUL.y, MathUtils.lerpf(bUL.z, bUR.z, percentL),
                                        pUV1.x, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, percentR), bUR.y, MathUtils.lerpf(bUL.z, bUR.z, percentR),
                                        pUV2.x, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, percentR), bLR.y, MathUtils.lerpf(bLL.z, bLR.z, percentR),
                                        pUV2.x, pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }
                        }

                        //Right side
                        {
                            //Top face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, 1-percentL), bLR.y, MathUtils.lerpf(bLL.z, bLR.z, 1-percentL),
                                        pUV2.x, pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, 1-percentL), bUR.y, MathUtils.lerpf(bUL.z, bUR.z, 1-percentL),
                                        pUV2.x, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, 1-percentR), bUL.y, MathUtils.lerpf(bUL.z, bUR.z, 1-percentR),
                                        pUV1.x, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, 1-percentR), bLL.y, MathUtils.lerpf(bLL.z, bLR.z, 1-percentR),
                                        pUV1.x, pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }

                            //Bottom face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, 1-percentR), bLL.y, MathUtils.lerpf(bLL.z, bLR.z, 1-percentR),
                                        pUV1.x, pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, 1-percentR), bUL.y, MathUtils.lerpf(bUL.z, bUR.z, 1-percentR),
                                        pUV1.x, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, 1-percentL), bUR.y, MathUtils.lerpf(bUL.z, bUR.z, 1-percentL),
                                        pUV2.x, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, 1-percentL), bLR.y, MathUtils.lerpf(bLL.z, bLR.z, 1-percentL),
                                        pUV2.x, pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }
                        }

                        remainingDistance -= pTextureTileDistance * 4;
                        newFaceInset += pTextureTileDistance * 2;
                    }

                    //Twin Quad Center Fill
                    if(remainingDistance > pTextureTileDistance * 2){
                        float uvWidth = remainingDistance / (pTextureTileDistance * 4);
                        float uvLL = pUV1.x;
                        float uvLR = ((pUV2.x - pUV1.x) * uvWidth) + pUV1.x;
                        float uvRL = pUV2.x - ((pUV2.x - pUV1.x) * uvWidth);
                        float uvRR = pUV2.x;

                        //Left side
                        {
                            //Top face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, 0.5f), bLR.y, MathUtils.lerpf(bLL.z, bLR.z, 0.5f),
                                        uvLR, pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, 0.5f), bUR.y, MathUtils.lerpf(bUL.z, bUR.z, 0.5f),
                                        uvLR, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, 0.0f), bUL.y, MathUtils.lerpf(bUL.z, bUR.z, 0.0f),
                                        uvLL, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, 0.0f), bLL.y, MathUtils.lerpf(bLL.z, bLR.z, 0.0f),
                                        uvLL, pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }

                            //Bottom face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, 0.0f), bLL.y, MathUtils.lerpf(bLL.z, bLR.z, 0.0f),
                                        uvLL, pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, 0.0f), bUL.y, MathUtils.lerpf(bUL.z, bUR.z, 0.0f),
                                        uvLL, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, 0.5f), bUR.y, MathUtils.lerpf(bUL.z, bUR.z, 0.5f),
                                        uvLR, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, 0.5f), bLR.y, MathUtils.lerpf(bLL.z, bLR.z, 0.5f),
                                        uvLR, pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }
                        }

                        //Right side
                        {
                            //Top face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, 1.0f), bLR.y, MathUtils.lerpf(bLL.z, bLR.z, 1.0f),
                                        uvRR, pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, 1.0f), bUR.y, MathUtils.lerpf(bUL.z, bUR.z, 1.0f),
                                        uvRR, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, 0.5f), bUL.y, MathUtils.lerpf(bUL.z, bUR.z, 0.5f),
                                        uvRL, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, 0.5f), bLL.y, MathUtils.lerpf(bLL.z, bLR.z, 0.5f),
                                        uvRL, pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }

                            //Bottom face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, 0.5f), bLL.y, MathUtils.lerpf(bLL.z, bLR.z, 0.5f),
                                        uvRL, pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, 0.5f), bUL.y, MathUtils.lerpf(bUL.z, bUR.z, 0.5f),
                                        uvRL, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, 1.0f), bUR.y, MathUtils.lerpf(bUL.z, bUR.z, 1.0f),
                                        uvRR, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, 1.0f), bLR.y, MathUtils.lerpf(bLL.z, bLR.z, 1.0f),
                                        uvRR, pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }
                        }
                    }
                    //Single Quad Center Fill
                    else {
                        float percentL = (newFaceInset) / (interiorWidth);
                        float percentR = 1-percentL;
                        float uvWidth = remainingDistance / (pTextureTileDistance * 2);
                        float uvRight = ((pUV2.x - pUV1.x) * uvWidth) + pUV1.x;

                        //Center
                        {
                            //Top face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, percentR), bLR.y, MathUtils.lerpf(bLL.z, bLR.z, percentR),
                                        uvRight, pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, percentR), bUR.y, MathUtils.lerpf(bUL.z, bUR.z, percentR),
                                        uvRight, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, percentL), bUL.y, MathUtils.lerpf(bUL.z, bUR.z, percentL),
                                        pUV1.x, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, percentL), bLL.y, MathUtils.lerpf(bLL.z, bLR.z, percentL),
                                        pUV1.x, pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }

                            //Bottom face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, percentL), bLL.y, MathUtils.lerpf(bLL.z, bLR.z, percentL),
                                        pUV1.x, pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, percentL), bUL.y, MathUtils.lerpf(bUL.z, bUR.z, percentL),
                                        pUV1.x, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bUL.x, bUR.x, percentR), bUR.y, MathUtils.lerpf(bUL.z, bUR.z, percentR),
                                        uvRight, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(bLL.x, bLR.x, percentR), bLR.y, MathUtils.lerpf(bLL.z, bLR.z, percentR),
                                        uvRight, pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }
                        }
                    }
                }
            }
        }
        //Rendering if we don't have to do any carving
        else {
            float textureSpanPercent = distanceBetweenExteriorPoints / pTextureTileDistance;
            float startX = pUV1.x;
            float startY = pUV1.y;
            float endX = startX + (textureSpanPercent * (pUV2.x - pUV1.x) * 0.5f);
            float endY = pUV2.y;
            float uvInset = (insetLeg / distanceBetweenExteriorPoints) * (endX - startX);

            Vector2f ul = new Vector2f(startX + uvInset, startY);
            Vector2f ur = new Vector2f(endX - uvInset, startY);
            Vector2f ll = new Vector2f(startX, endY);
            Vector2f lr = new Vector2f(endX, endY);

            for(QuadVertData qvd : vertData) {

                pPoseStack.pushPose();
                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                        qvd.a.x, qvd.a.y, qvd.a.z, lr.x, lr.y, 0, 0, color);
                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                        qvd.d.x, qvd.d.y, qvd.d.z, ur.x, ur.y, 0, 0, color);
                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                        qvd.c.x, qvd.c.y, qvd.c.z, ul.x, ul.y, 0, 0, color);
                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                        qvd.b.x, qvd.b.y, qvd.b.z, ll.x, ll.y, 0, 0, color);
                pPoseStack.popPose();

                pPoseStack.pushPose();
                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                        qvd.b.x, qvd.b.y, qvd.b.z, lr.x, lr.y, 0, 0, color);
                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                        qvd.c.x, qvd.c.y, qvd.c.z, ur.x, ur.y, 0, 0, color);
                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                        qvd.d.x, qvd.d.y, qvd.d.z, ul.x, ul.y, 0, 0, color);
                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                        qvd.a.x, qvd.a.y, qvd.a.z, ll.x, ll.y, 0, 0, color);

                pPoseStack.popPose();

            }
        }


        //render that shit!

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