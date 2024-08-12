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
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.world.entity.player.Inventory;
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

    public static void generateMagicCircleRing(Vector3 pCenter, int pPointCount, float pRadius, float pThickness, float pRotation, TextureAtlasSprite pTexture, Vec2 pUV1, Vec2 pUV2, float pTextureTileDistance, float pFillPercent, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
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
        int[] red = new int[]{255, 0, 0, 255};

        //cancel rendering the circle if there's too few circle points to prevent OOB exceptions
        if(vertData.size() <= 1)
            return;

        //TODO: get rid of the array and just use an int
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

            int i=0;
            for(QuadVertData qvd : vertData) {
                float remainingDistanceToDraw = pPointCount * distanceBetweenExteriorPoints * pFillPercent;
                float drawLerp = 1f;
                if(remainingDistanceToDraw - (distanceBetweenExteriorPoints * i) < 0)
                    break;
                else if(remainingDistanceToDraw - (distanceBetweenExteriorPoints * i) < distanceBetweenExteriorPoints)
                    drawLerp = (remainingDistanceToDraw - (distanceBetweenExteriorPoints * i)) / distanceBetweenExteriorPoints;

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

                    float drawLerpFirst = Math.min(drawLerp*2, 1);
                    float drawLerpSecond = Math.min(1,Math.max((drawLerp*2)-1, 0));

                    //Top faces
                    {
                        pPoseStack.pushPose();
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                qvd.a.x, qvd.a.y, qvd.a.z,
                                lrr.x, lrr.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                qvd.d.x, qvd.d.y, qvd.d.z,
                                urr.x, urr.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(qvd.d.x, MathUtils.lerpf(qvd.c.x, qvd.d.x, 0.5f), drawLerpFirst), qvd.c.y, MathUtils.lerpf(qvd.d.z, MathUtils.lerpf(qvd.c.z, qvd.d.z, 0.5f), drawLerpFirst),
                                MathUtils.lerpf(urr.x, ulr.x, drawLerpFirst), ulr.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(qvd.a.x, MathUtils.lerpf(qvd.b.x, qvd.a.x, 0.5f), drawLerpFirst), qvd.b.y, MathUtils.lerpf(qvd.a.z, MathUtils.lerpf(qvd.b.z, qvd.a.z, 0.5f), drawLerpFirst),
                                MathUtils.lerpf(lrr.x, llr.x, drawLerpFirst), llr.y, 0, 0, color);
                        pPoseStack.popPose();

                        pPoseStack.pushPose();
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(qvd.a.x, qvd.b.x, 0.5f), qvd.a.y, MathUtils.lerpf(qvd.a.z, qvd.b.z, 0.5f),
                                lrl.x, lrl.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(qvd.d.x, qvd.c.x, 0.5f), qvd.d.y, MathUtils.lerpf(qvd.d.z, qvd.c.z, 0.5f),
                                url.x, url.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(MathUtils.lerpf(qvd.d.x, qvd.c.x, 0.5f), qvd.c.x, drawLerpSecond), qvd.c.y, MathUtils.lerpf(MathUtils.lerpf(qvd.d.z, qvd.c.z, 0.5f), qvd.c.z, drawLerpSecond),
                                MathUtils.lerpf(url.x, ull.x, drawLerpSecond), ull.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(MathUtils.lerpf(qvd.b.x, qvd.a.x, 0.5f), qvd.b.x, drawLerpSecond), qvd.b.y, MathUtils.lerpf(MathUtils.lerpf(qvd.b.z, qvd.a.z, 0.5f), qvd.b.z, drawLerpSecond),
                                MathUtils.lerpf(lrl.x, lll.x, drawLerpSecond), lll.y, 0, 0, color);
                        pPoseStack.popPose();
                    }

                    //Bottom faces
                    {
                        pPoseStack.pushPose();
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(qvd.a.x, MathUtils.lerpf(qvd.b.x, qvd.a.x, 0.5f), drawLerpFirst), qvd.b.y, MathUtils.lerpf(qvd.a.z, MathUtils.lerpf(qvd.b.z, qvd.a.z, 0.5f), drawLerpFirst),
                                MathUtils.lerpf(lrr.x, llr.x, drawLerpFirst), llr.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(qvd.d.x, MathUtils.lerpf(qvd.c.x, qvd.d.x, 0.5f), drawLerpFirst), qvd.c.y, MathUtils.lerpf(qvd.d.z, MathUtils.lerpf(qvd.c.z, qvd.d.z, 0.5f), drawLerpFirst),
                                MathUtils.lerpf(urr.x, ulr.x, drawLerpFirst), ulr.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                qvd.d.x, qvd.d.y, qvd.d.z,
                                urr.x, urr.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                qvd.a.x, qvd.a.y, qvd.a.z,
                                lrr.x, lrr.y, 0, 0, color);
                        pPoseStack.popPose();

                        pPoseStack.pushPose();
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(MathUtils.lerpf(qvd.b.x, qvd.a.x, 0.5f), qvd.b.x, drawLerpSecond), qvd.b.y, MathUtils.lerpf(MathUtils.lerpf(qvd.b.z, qvd.a.z, 0.5f), qvd.b.z, drawLerpSecond),
                                MathUtils.lerpf(lrl.x, lll.x, drawLerpSecond), lll.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(MathUtils.lerpf(qvd.d.x, qvd.c.x, 0.5f), qvd.c.x, drawLerpSecond), qvd.c.y, MathUtils.lerpf(MathUtils.lerpf(qvd.d.z, qvd.c.z, 0.5f), qvd.c.z, drawLerpSecond),
                                MathUtils.lerpf(url.x, ull.x, drawLerpSecond), ull.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(qvd.d.x, qvd.c.x, 0.5f), qvd.d.y, MathUtils.lerpf(qvd.d.z, qvd.c.z, 0.5f),
                                url.x, url.y, 0, 0, color);
                        addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                MathUtils.lerpf(qvd.a.x, qvd.b.x, 0.5f), qvd.a.y, MathUtils.lerpf(qvd.a.z, qvd.b.z, 0.5f),
                                lrl.x, lrl.y, 0, 0, color);
                        pPoseStack.popPose();
                    }
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
                        float start = 1 - ((pTextureTileDistance * 2) / distanceBetweenExteriorPoints);
                        float end = 1f;
                        float modifiedDrawLerp = mapRangeToModifiedDrawLerp(drawLerp, start, end);

                        float x1 = MathUtils.lerpf(qvd.a.x, qvd.b.x, capLerpPercentOuter);
                        float z1 = MathUtils.lerpf(qvd.a.z, qvd.b.z, capLerpPercentOuter);
                        float x2 = MathUtils.lerpf(qvd.d.x, qvd.c.x, capLerpPercentInner);
                        float z2 = MathUtils.lerpf(qvd.d.z, qvd.c.z, capLerpPercentInner);
                        float x3 = qvd.c.x;
                        float z3 = qvd.c.z;
                        float x4 = qvd.b.x;
                        float z4 = qvd.b.z;

                        //Top face
                        {
                            pPoseStack.pushPose();
                            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                    x1, qvd.a.y, z1,
                                    lrl.x, lrl.y, 0, 0, color);
                            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                    x2, qvd.d.y, z2,
                                    url.x, url.y, 0, 0, color);
                            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                    MathUtils.lerpf(x2, x3, modifiedDrawLerp), qvd.c.y, MathUtils.lerpf(z2, z3, modifiedDrawLerp),
                                    MathUtils.lerpf(url.x, ull.x, modifiedDrawLerp), ull.y, 0, 0, color);
                            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                    MathUtils.lerpf(x1, x4, modifiedDrawLerp), qvd.b.y, MathUtils.lerpf(z1, z4, modifiedDrawLerp),
                                    MathUtils.lerpf(lrl.x, lll.x, modifiedDrawLerp), lll.y, 0, 0, color);
                            pPoseStack.popPose();
                        }

                        //Bottom face
                        {
                            pPoseStack.pushPose();
                            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                    MathUtils.lerpf(x1, x4, modifiedDrawLerp), qvd.b.y, MathUtils.lerpf(z1, z4, modifiedDrawLerp),
                                    MathUtils.lerpf(lrl.x, lll.x, modifiedDrawLerp), lll.y, 0, 0, color);
                            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                    MathUtils.lerpf(x2, x3, modifiedDrawLerp), qvd.c.y, MathUtils.lerpf(z2, z3, modifiedDrawLerp),
                                    MathUtils.lerpf(url.x, ull.x, modifiedDrawLerp), ull.y, 0, 0, color);
                            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                    x2, qvd.d.y, z2,
                                    url.x, url.y, 0, 0, color);
                            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                    x1, qvd.a.y, z1,
                                    lrl.x, lrl.y, 0, 0, color);
                            pPoseStack.popPose();
                        }
                    }
                    //Right cap
                    {
                        float start = 0f;
                        float end = ((pTextureTileDistance * 2) / distanceBetweenExteriorPoints);
                        float modifiedDrawLerp = mapRangeToModifiedDrawLerp(drawLerp, start, end);

                        float x1 = qvd.a.x;
                        float z1 = qvd.a.z;
                        float x2 = qvd.d.x;
                        float z2 = qvd.d.z;
                        float x3 = MathUtils.lerpf(qvd.c.x, qvd.d.x, capLerpPercentInner);
                        float z3 = MathUtils.lerpf(qvd.c.z, qvd.d.z, capLerpPercentInner);
                        float x4 = MathUtils.lerpf(qvd.b.x, qvd.a.x, capLerpPercentOuter);
                        float z4 = MathUtils.lerpf(qvd.b.z, qvd.a.z, capLerpPercentOuter);

                        //Top face
                        {
                            pPoseStack.pushPose();
                            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                    x1, qvd.a.y, z1,
                                    lrr.x, lrr.y, 0, 0, color);
                            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                    x2, qvd.d.y, z2,
                                    urr.x, urr.y, 0, 0, color);
                            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                    MathUtils.lerpf(x2, x3, modifiedDrawLerp), qvd.c.y, MathUtils.lerpf(z2, z3, modifiedDrawLerp),
                                    MathUtils.lerpf(urr.x, ulr.x, modifiedDrawLerp), ulr.y, 0, 0, color);
                            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                    MathUtils.lerpf(x1, x4, modifiedDrawLerp), qvd.b.y, MathUtils.lerpf(z1, z4, modifiedDrawLerp),
                                    MathUtils.lerpf(lrr.x, llr.x, modifiedDrawLerp), llr.y, 0, 0, color);
                            pPoseStack.popPose();
                        }

                        //Bottom face
                        {
                            pPoseStack.pushPose();
                            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                    MathUtils.lerpf(x1, x4, modifiedDrawLerp), qvd.b.y, MathUtils.lerpf(z1, z4, modifiedDrawLerp),
                                    MathUtils.lerpf(lrr.x, llr.x, modifiedDrawLerp), llr.y, 0, 0, color);
                            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                    MathUtils.lerpf(x2, x3, modifiedDrawLerp), qvd.c.y, MathUtils.lerpf(z2, z3, modifiedDrawLerp),
                                    MathUtils.lerpf(urr.x, ulr.x, modifiedDrawLerp), ulr.y, 0, 0, color);
                            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                    x2, qvd.d.y, z2,
                                    urr.x, urr.y, 0, 0, color);
                            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                    x1, qvd.a.y, z1,
                                    lrr.x, lrr.y, 0, 0, color);
                            pPoseStack.popPose();
                        }
                    }

                    Vector3 bUL = new Vector3(MathUtils.lerpf(qvd.d.x, qvd.c.x, capLerpPercentInner), qvd.d.y, MathUtils.lerpf(qvd.d.z, qvd.c.z, capLerpPercentInner));
                    Vector3 bLL = new Vector3(MathUtils.lerpf(qvd.a.x, qvd.b.x, capLerpPercentOuter), qvd.a.y, MathUtils.lerpf(qvd.a.z, qvd.b.z, capLerpPercentOuter));
                    Vector3 bUR = new Vector3(MathUtils.lerpf(qvd.c.x, qvd.d.x, capLerpPercentInner), qvd.c.y, MathUtils.lerpf(qvd.c.z, qvd.d.z, capLerpPercentInner));
                    Vector3 bLR = new Vector3(MathUtils.lerpf(qvd.b.x, qvd.a.x, capLerpPercentOuter), qvd.b.y, MathUtils.lerpf(qvd.b.z, qvd.a.z, capLerpPercentOuter));

                    float interiorWidth = (float)bLR.distanceTo(bLL);

                    int w = 0;
                    while(remainingDistance > pTextureTileDistance * 4) {
                        float percentL = (newFaceInset) / (interiorWidth);
                        float percentR = (newFaceInset + pTextureTileDistance * 2) / (interiorWidth);

                        //Left side
                        {
                            float start = 1 - ((pTextureTileDistance * 2) * (w + 1) / distanceBetweenExteriorPoints);
                            float end = 1 - ((pTextureTileDistance * 2) * (w + 2) / distanceBetweenExteriorPoints);
                            float modifiedDrawLerp = mapRangeToModifiedDrawLerp(drawLerp, start, end);

                            float x1 = MathUtils.lerpf(bLL.x, bLR.x, percentR);
                            float z1 = MathUtils.lerpf(bLL.z, bLR.z, percentR);
                            float x2 = MathUtils.lerpf(bUL.x, bUR.x, percentR);
                            float z2 = MathUtils.lerpf(bUL.z, bUR.z, percentR);
                            float x3 = MathUtils.lerpf(bUL.x, bUR.x, percentL);
                            float z3 = MathUtils.lerpf(bUL.z, bUR.z, percentL);
                            float x4 = MathUtils.lerpf(bLL.x, bLR.x, percentL);
                            float z4 = MathUtils.lerpf(bLL.z, bLR.z, percentL);

                            //Top face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x1, bLR.y, z1,
                                        pUV2.x, pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x2, bUR.y, z2,
                                        pUV2.x, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x2, x3, modifiedDrawLerp), bUL.y, MathUtils.lerpf(z2, z3, modifiedDrawLerp),
                                        MathUtils.lerpf(pUV2.x, pUV1.x, modifiedDrawLerp), pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x1, x4, modifiedDrawLerp), bLL.y, MathUtils.lerpf(z1, z4, modifiedDrawLerp),
                                        MathUtils.lerpf(pUV2.x, pUV1.x, modifiedDrawLerp), pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }

                            //Bottom face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x1, x4, modifiedDrawLerp), bLL.y, MathUtils.lerpf(z1, z4, modifiedDrawLerp),
                                        MathUtils.lerpf(pUV2.x, pUV1.x, modifiedDrawLerp), pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x2, x3, modifiedDrawLerp), bUL.y, MathUtils.lerpf(z2, z3, modifiedDrawLerp),
                                        MathUtils.lerpf(pUV2.x, pUV1.x, modifiedDrawLerp), pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x2, bUR.y, z2,
                                        pUV2.x, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x1, bLR.y, z1,
                                        pUV2.x, pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }
                        }

                        //Right side
                        {
                            float start = ((pTextureTileDistance * 2) * (w + 1) / distanceBetweenExteriorPoints);
                            float end = ((pTextureTileDistance * 2) * (w + 2) / distanceBetweenExteriorPoints);
                            float modifiedDrawLerp = mapRangeToModifiedDrawLerp(drawLerp, start, end);

                            float x1 = MathUtils.lerpf(bLL.x, bLR.x, 1-percentL);
                            float z1 = MathUtils.lerpf(bLL.z, bLR.z, 1-percentL);
                            float x2 = MathUtils.lerpf(bUL.x, bUR.x, 1-percentL);
                            float z2 = MathUtils.lerpf(bUL.z, bUR.z, 1-percentL);
                            float x3 = MathUtils.lerpf(bUL.x, bUR.x, 1-percentR);
                            float z3 = MathUtils.lerpf(bUL.z, bUR.z, 1-percentR);
                            float x4 = MathUtils.lerpf(bLL.x, bLR.x, 1-percentR);
                            float z4 = MathUtils.lerpf(bLL.z, bLR.z, 1-percentR);

                            //Top face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x1, bLR.y, z1,
                                        pUV2.x, pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x2, bUR.y, z2,
                                        pUV2.x, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x2, x3, modifiedDrawLerp), bUL.y, MathUtils.lerpf(z2, z3, modifiedDrawLerp),
                                        MathUtils.lerpf(pUV2.x, pUV1.x, modifiedDrawLerp), pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x1, x4, modifiedDrawLerp), bLL.y, MathUtils.lerpf(z1, z4, modifiedDrawLerp),
                                        MathUtils.lerpf(pUV2.x, pUV1.x, modifiedDrawLerp), pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }

                            //Bottom face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x1, x4, modifiedDrawLerp), bLL.y, MathUtils.lerpf(z1, z4, modifiedDrawLerp),
                                        MathUtils.lerpf(pUV2.x, pUV1.x, modifiedDrawLerp), pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x2, x3, modifiedDrawLerp), bUL.y, MathUtils.lerpf(z2, z3, modifiedDrawLerp),
                                        MathUtils.lerpf(pUV2.x, pUV1.x, modifiedDrawLerp), pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x2, bUR.y, z2,
                                        pUV2.x, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x1, bLR.y, z1,
                                        pUV2.x, pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }
                        }

                        remainingDistance -= pTextureTileDistance * 4;
                        newFaceInset += pTextureTileDistance * 2;
                        w++;
                    }

                    float leftExtent = ((pTextureTileDistance * 2) * (w + 1) / distanceBetweenExteriorPoints);
                    float rightExtent = 1 - ((pTextureTileDistance * 2) * (w + 1) / distanceBetweenExteriorPoints);

                    //Twin Quad Center Fill
                    if(remainingDistance > pTextureTileDistance * 2){
                        float stepI = (pTextureTileDistance * 2) / distanceBetweenInteriorPoints;
                        float stepO = (pTextureTileDistance * 2) / distanceBetweenExteriorPoints;

                        Vector3 bTUL = new Vector3(MathUtils.lerpf(qvd.d.x, qvd.c.x, capLerpPercentInner - (stepI * w)), qvd.d.y, MathUtils.lerpf(qvd.d.z, qvd.c.z, capLerpPercentInner - (stepI * w)));
                        Vector3 bTLL = new Vector3(MathUtils.lerpf(qvd.a.x, qvd.b.x, capLerpPercentOuter - (stepO * w)), qvd.a.y, MathUtils.lerpf(qvd.a.z, qvd.b.z, capLerpPercentOuter - (stepO * w)));
                        Vector3 bTUR = new Vector3(MathUtils.lerpf(qvd.c.x, qvd.d.x, capLerpPercentInner - (stepI * w)), qvd.c.y, MathUtils.lerpf(qvd.c.z, qvd.d.z, capLerpPercentInner - (stepI * w)));
                        Vector3 bTLR = new Vector3(MathUtils.lerpf(qvd.b.x, qvd.a.x, capLerpPercentOuter - (stepO * w)), qvd.b.y, MathUtils.lerpf(qvd.b.z, qvd.a.z, capLerpPercentOuter - (stepO * w)));

                        float uvWidth = remainingDistance / (pTextureTileDistance * 4);
                        float uvLL = pUV1.x;
                        float uvLR = ((pUV2.x - pUV1.x) * uvWidth) + pUV1.x;
                        float uvRL = pUV2.x - ((pUV2.x - pUV1.x) * uvWidth);
                        float uvRR = pUV2.x;

                        //Left side
                        {
                            float modifiedDrawLerp = mapRangeToModifiedDrawLerp(drawLerp, 0.5f, rightExtent);

                            float x1 = MathUtils.lerpf(bTLL.x, bTLR.x, 0.5f);
                            float z1 = MathUtils.lerpf(bTLL.z, bTLR.z, 0.5f);
                            float x2 = MathUtils.lerpf(bTUL.x, bTUR.x, 0.5f);
                            float z2 = MathUtils.lerpf(bTUL.z, bTUR.z, 0.5f);
                            float x3 = MathUtils.lerpf(bTUL.x, bTUR.x, 0.0f);
                            float z3 = MathUtils.lerpf(bTUL.z, bTUR.z, 0.0f);
                            float x4 = MathUtils.lerpf(bTLL.x, bTLR.x, 0.0f);
                            float z4 = MathUtils.lerpf(bTLL.z, bTLR.z, 0.0f);

                            //Top face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x1, bLR.y, z1,
                                        uvLR, pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x2, bUR.y, z2,
                                        uvLR, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x2, x3, modifiedDrawLerp), bUL.y, MathUtils.lerpf(z2, z3, modifiedDrawLerp),
                                        MathUtils.lerpf(uvLR, uvLL, modifiedDrawLerp), pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x1, x4, modifiedDrawLerp), bLL.y, MathUtils.lerpf(z1, z4, modifiedDrawLerp),
                                        MathUtils.lerpf(uvLR, uvLL, modifiedDrawLerp), pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }

                            //Bottom face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x1, x4, modifiedDrawLerp), bLL.y, MathUtils.lerpf(z1, z4, modifiedDrawLerp),
                                        MathUtils.lerpf(uvLR, uvLL, modifiedDrawLerp), pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x2, x3, modifiedDrawLerp), bUL.y, MathUtils.lerpf(z2, z3, modifiedDrawLerp),
                                        MathUtils.lerpf(uvLR, uvLL, modifiedDrawLerp), pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x2, bUR.y, z2,
                                        uvLR, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x1, bLR.y, z1,
                                        uvLR, pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }
                        }

                        //Right side
                        {
                            float modifiedDrawLerp = mapRangeToModifiedDrawLerp(drawLerp, leftExtent, 0.5f);

                            float x1 = MathUtils.lerpf(bTLL.x, bTLR.x, 1.0f);
                            float z1 = MathUtils.lerpf(bTLL.z, bTLR.z, 1.0f);
                            float x2 = MathUtils.lerpf(bTUL.x, bTUR.x, 1.0f);
                            float z2 = MathUtils.lerpf(bTUL.z, bTUR.z, 1.0f);
                            float x3 = MathUtils.lerpf(bTUL.x, bTUR.x, 0.5f);
                            float z3 = MathUtils.lerpf(bTUL.z, bTUR.z, 0.5f);
                            float x4 = MathUtils.lerpf(bTLL.x, bTLR.x, 0.5f);
                            float z4 = MathUtils.lerpf(bTLL.z, bTLR.z, 0.5f);

                            //Top face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x1, bLR.y, z1,
                                        uvRR, pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x2, bUR.y, z2,
                                        uvRR, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x2, x3, modifiedDrawLerp), bUL.y, MathUtils.lerpf(z2, z3, modifiedDrawLerp),
                                        MathUtils.lerpf(uvRR, uvRL, modifiedDrawLerp), pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x1, x4, modifiedDrawLerp), bLL.y, MathUtils.lerpf(z1, z4, modifiedDrawLerp),
                                        MathUtils.lerpf(uvRR, uvRL, modifiedDrawLerp), pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }

                            //Bottom face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x1, x4, modifiedDrawLerp), bLL.y, MathUtils.lerpf(z1, z4, modifiedDrawLerp),
                                        MathUtils.lerpf(uvRR, uvRL, modifiedDrawLerp), pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x2, x3, modifiedDrawLerp), bUL.y, MathUtils.lerpf(z2, z3, modifiedDrawLerp),
                                        MathUtils.lerpf(uvRR, uvRL, modifiedDrawLerp), pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x2, bUR.y, z2,
                                        uvRR, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x1, bLR.y, z1,
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
                            float modifiedDrawLerp = mapRangeToModifiedDrawLerp(drawLerp, leftExtent, rightExtent);

                            float x1 = MathUtils.lerpf(bLL.x, bLR.x, percentR);
                            float z1 = MathUtils.lerpf(bLL.z, bLR.z, percentR);
                            float x2 = MathUtils.lerpf(bUL.x, bUR.x, percentR);
                            float z2 = MathUtils.lerpf(bUL.z, bUR.z, percentR);
                            float x3 = MathUtils.lerpf(bUL.x, bUR.x, percentL);
                            float z3 = MathUtils.lerpf(bUL.z, bUR.z, percentL);
                            float x4 = MathUtils.lerpf(bLL.x, bLR.x, percentL);
                            float z4 = MathUtils.lerpf(bLL.z, bLR.z, percentL);

                            //Top face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x1, bLR.y, z1,
                                        uvRight, pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x2, bUR.y, z2,
                                        uvRight, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x2, x3, modifiedDrawLerp), bUL.y, MathUtils.lerpf(z2, z3, modifiedDrawLerp),
                                        MathUtils.lerpf(uvRight, pUV1.x, modifiedDrawLerp), pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x1, x4, modifiedDrawLerp), bLL.y, MathUtils.lerpf(z1, z4, modifiedDrawLerp),
                                        MathUtils.lerpf(uvRight, pUV1.x, modifiedDrawLerp), pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }

                            //Bottom face
                            {
                                pPoseStack.pushPose();
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x1, x4, modifiedDrawLerp), bLL.y, MathUtils.lerpf(z1, z4, modifiedDrawLerp),
                                        MathUtils.lerpf(uvRight, pUV1.x, modifiedDrawLerp), pUV2.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        MathUtils.lerpf(x2, x3, modifiedDrawLerp), bUL.y, MathUtils.lerpf(z2, z3, modifiedDrawLerp),
                                        MathUtils.lerpf(uvRight, pUV1.x, modifiedDrawLerp), pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x2, bUR.y, z2,
                                        uvRight, pUV1.y, 0, 0, color);
                                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                                        x1, bLR.y, z1,
                                        uvRight, pUV2.y, 0, 0, color);
                                pPoseStack.popPose();
                            }
                        }
                    }
                }
                i++;
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

            float segmentsToDraw = pFillPercent * pPointCount;
            for(QuadVertData qvd : vertData) {
                float drawLerp = 1f;
                if(segmentsToDraw < 1)
                    drawLerp = segmentsToDraw;

                //Top face
                pPoseStack.pushPose();
                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                        qvd.a.x, qvd.a.y, qvd.a.z,
                        lr.x, lr.y, 0, 0, color);
                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                        qvd.d.x, qvd.d.y, qvd.d.z,
                        ur.x, ur.y, 0, 0, color);
                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                        MathUtils.lerpf(qvd.d.x, qvd.c.x, drawLerp), qvd.c.y, MathUtils.lerpf(qvd.d.z, qvd.c.z, drawLerp),
                        MathUtils.lerpf(ur.x, ul.x, drawLerp), ul.y, 0, 0, color);
                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                        MathUtils.lerpf(qvd.a.x, qvd.b.x, drawLerp), qvd.b.y, MathUtils.lerpf(qvd.a.z, qvd.b.z, drawLerp),
                        MathUtils.lerpf(lr.x, ll.x, drawLerp), ll.y, 0, 0, color);
                pPoseStack.popPose();

                //Bottom face
                pPoseStack.pushPose();
                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                        MathUtils.lerpf(qvd.a.x, qvd.b.x, drawLerp), qvd.b.y, MathUtils.lerpf(qvd.a.z, qvd.b.z, drawLerp),
                        MathUtils.lerpf(lr.x, ll.x, drawLerp), ll.y, 0, 0, color);
                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                        MathUtils.lerpf(qvd.d.x, qvd.c.x, drawLerp), qvd.c.y, MathUtils.lerpf(qvd.d.z, qvd.c.z, drawLerp),
                        MathUtils.lerpf(ur.x, ul.x, drawLerp), ul.y, 0, 0, color);
                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                        qvd.d.x, qvd.d.y, qvd.d.z, ul.x, ul.y, 0, 0, color);
                addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                        qvd.a.x, qvd.a.y, qvd.a.z, ll.x, ll.y, 0, 0, color);

                pPoseStack.popPose();

                if(segmentsToDraw < 1)
                    break;
                segmentsToDraw--;
            }
        }


        //render that shit!

    }

    public static void generateLinearVolumetricBeam(Vector3 pStartPos, Vector3 pEndPos, float pRadius, TextureAtlasSprite pTexture, int[] pRGBA, float pFillPercent, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, float u1, float v1, float u2, float v2) {
        //Generate important vectors and vertex data
        Vector3 fwd = pEndPos.sub(pStartPos).normalize();
        Vector3 left = Vector3.crossProduct(fwd, Vector3.up()).normalize();
        Vector3 up = Vector3.crossProduct(fwd, left).normalize();

        QuadVertData startRing = new QuadVertData(
                new SingleVertex(pStartPos.add(new Vector3(up.x, up.y, up.z).scale(pRadius))),
                new SingleVertex(pStartPos.add(new Vector3(up.x, up.y, up.z).scale(-pRadius))),
                new SingleVertex(pStartPos.add(new Vector3(left.x, left.y, left.z).scale(pRadius))),
                new SingleVertex(pStartPos.add(new Vector3(left.x, left.y, left.z).scale(-pRadius)))
        );
        QuadVertData endRing = new QuadVertData(
                new SingleVertex(Vector3.lerp(pStartPos, pEndPos, pFillPercent)
                        .add(new Vector3(up.x, up.y, up.z).scale(pRadius))),
                new SingleVertex(Vector3.lerp(pStartPos, pEndPos, pFillPercent)
                        .add(new Vector3(up.x, up.y, up.z).scale(-pRadius))),
                new SingleVertex(Vector3.lerp(pStartPos, pEndPos, pFillPercent)
                        .add(new Vector3(left.x, left.y, left.z).scale(pRadius))),
                new SingleVertex(Vector3.lerp(pStartPos, pEndPos, pFillPercent)
                        .add(new Vector3(left.x, left.y, left.z).scale(-pRadius)))
        );

        VertexConsumer vertexBuilder = pBuffer.getBuffer(RenderType.translucentNoCrumbling());
        Matrix4f renderMatrix = pPoseStack.last().pose();
        Matrix3f normalMatrix = pPoseStack.last().normal();

        //Positive Horizontal
        {
            pPoseStack.pushPose();
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    startRing.a.x, startRing.a.y, startRing.a.z, u1*16, v2*16, 0, 0, pRGBA);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    endRing.a.x, endRing.a.y, endRing.a.z, u1*16, v1*16, 0, 0, pRGBA);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    endRing.b.x, endRing.b.y, endRing.b.z, u2*16, v1*16, 0, 0, pRGBA);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    startRing.b.x, startRing.b.y, startRing.b.z, u2*16, v2*16, 0, 0, pRGBA);
            pPoseStack.popPose();
        }
        //Negative Horizontal
        {
            pPoseStack.pushPose();
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    startRing.b.x, startRing.b.y, startRing.b.z, u1*16, v2*16, 0, 0, pRGBA);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    endRing.b.x, endRing.b.y, endRing.b.z, u1*16, v1*16, 0, 0, pRGBA);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    endRing.a.x, endRing.a.y, endRing.a.z, u2*16, v1*16, 0, 0, pRGBA);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    startRing.a.x, startRing.a.y, startRing.a.z, u2*16, v2*16, 0, 0, pRGBA);
            pPoseStack.popPose();
        }
        //Positive Vertical
        {
            pPoseStack.pushPose();
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    startRing.c.x, startRing.c.y, startRing.c.z, u1*16, v2*16, 0, 0, pRGBA);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    endRing.c.x, endRing.c.y, endRing.c.z, u1*16, v1*16, 0, 0, pRGBA);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    endRing.d.x, endRing.d.y, endRing.d.z, u2*16, v1*16, 0, 0, pRGBA);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    startRing.d.x, startRing.d.y, startRing.d.z, u2*16, v2*16, 0, 0, pRGBA);
            pPoseStack.popPose();
        }
        //Negative Vertical
        {
            pPoseStack.pushPose();
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    startRing.d.x, startRing.d.y, startRing.d.z, u1*16, v2*16, 0, 0, pRGBA);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    endRing.d.x, endRing.d.y, endRing.d.z, u1*16, v1*16, 0, 0, pRGBA);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    endRing.c.x, endRing.c.y, endRing.c.z, u2*16, v1*16, 0, 0, pRGBA);
            addVertex(vertexBuilder, renderMatrix, normalMatrix, pTexture, pPackedLight,
                    startRing.c.x, startRing.c.y, startRing.c.z, u2*16, v2*16, 0, 0, pRGBA);
            pPoseStack.popPose();
        }
    }

    private static float mapRangeToModifiedDrawLerp(float pDrawLerp, float pStart, float pEnd) {
        if(pStart < pEnd){
            if (pDrawLerp < pStart)
                return 0.0f;
            else if (pDrawLerp > pEnd)
                return 1.0f;
            else {
                return (pDrawLerp - pStart) / (pEnd - pStart);
            }
        } else {
            if (pDrawLerp < pEnd)
                return 0.0f;
            else if (pDrawLerp > pStart)
                return 1.0f;
            else {
                return (pDrawLerp - pEnd) / (pStart - pEnd);
            }
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