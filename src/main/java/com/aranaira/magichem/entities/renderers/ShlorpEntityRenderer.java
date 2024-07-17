package com.aranaira.magichem.entities.renderers;

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

public class ShlorpEntityRenderer extends EntityRenderer<ShlorpEntity> {
    public ShlorpEntityRenderer(Context pContext) {
        super(pContext);
    }

    public static final float
            VERT_CLUSTER_THICKNESS = 0.0625f, FLUID_DISTORTION_AMPLITUDE = 0.625f, FLUID_DISTORTION_PERIOD = 0.825f, TICKS_FOR_FULL_MARCH = 16;
    public static final Vector3
            VECTOR_POS_CORRECTION = new Vector3(0.5, 0, 0.5);
    private final List<VertexDataHolder> vertData = new ArrayList<>();

    @Override
    public ResourceLocation getTextureLocation(ShlorpEntity pEntity) {
        return null;
    }

    @Override
    public void render(ShlorpEntity pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        //Fill up our arraylist of vert data
        generateVertDataThisFrame(pEntity, Minecraft.getInstance().level.getGameTime(), pPartialTick);

        //Set up some references
        VertexConsumer vertexBuilder = pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(new ResourceLocation("minecraft", "block/snow"));

        float minU = 0.0f;// * (float)texture.contents().width();
        float maxU = 3.0f;// * (float)texture.contents().width();
        float minV = 0.0f;// * (float)texture.contents().height();
        float maxV = 2.0f;// * (float)texture.contents().height();

        Matrix4f renderMatrix = pPoseStack.last().pose();
        Matrix3f normalMatrix = pPoseStack.last().normal();
        int[] color = pEntity.color;

        //If we don't have at least 2 entries in the vert data list, shit's going to break. So skip rendering if it's not compliant.
        if(vertData.size() < 2)
            return;

        //Otherwise, IT BEGINS

        for(int i=1; i<vertData.size(); i++) {
            VertexDataHolder current = vertData.get(i);
            VertexDataHolder previous = vertData.get(i-1);

            if(previous instanceof SingleVertex psv) {
                if(current instanceof VertexRing cvr) {

                    pPoseStack.pushPose();
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            psv.x, psv.y, psv.z, 0, 0, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            cvr.top.x, cvr.top.y, cvr.top.z, 1, 0, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            cvr.right.x, cvr.right.y, cvr.right.z, 1, 1, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            psv.x, psv.y, psv.z, 0, 0, 0, 0, color);
                    pPoseStack.popPose();

                    pPoseStack.pushPose();
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            psv.x, psv.y, psv.z, 0, 0, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            cvr.right.x, cvr.right.y, cvr.right.z, 1, 0, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            cvr.bottom.x, cvr.bottom.y, cvr.bottom.z, 1, 1, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            psv.x, psv.y, psv.z, 0, 0, 0, 0, color);
                    pPoseStack.popPose();

                    pPoseStack.pushPose();
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            psv.x, psv.y, psv.z, 0, 0, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            cvr.bottom.x, cvr.bottom.y, cvr.bottom.z, 1, 1, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            cvr.left.x, cvr.left.y, cvr.left.z, 1, 1, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            psv.x, psv.y, psv.z, 0, 0, 0, 0, color);
                    pPoseStack.popPose();

                    pPoseStack.pushPose();
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            psv.x, psv.y, psv.z, 0, 0, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            cvr.left.x, cvr.left.y, cvr.left.z, 1, 1, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            cvr.top.x, cvr.top.y, cvr.top.z, 1, 1, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            psv.x, psv.y, psv.z, 0, 0, 0, 0, color);
                    pPoseStack.popPose();
                }
            } else if(previous instanceof VertexRing pvr) {
                if(current instanceof SingleVertex csv) {

                    pPoseStack.pushPose();
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            csv.x, csv.y, csv.z, 0, 0, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            pvr.top.x, pvr.top.y, pvr.top.z, 1, 0, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            pvr.right.x, pvr.right.y, pvr.right.z, 1, 1, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            csv.x, csv.y, csv.z, 0, 0, 0, 0, color);
                    pPoseStack.popPose();


                    pPoseStack.pushPose();
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            csv.x, csv.y, csv.z, 0, 0, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            pvr.right.x, pvr.right.y, pvr.right.z, 1, 0, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            pvr.bottom.x, pvr.bottom.y, pvr.bottom.z, 1, 1, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            csv.x, csv.y, csv.z, 0, 0, 0, 0, color);
                    pPoseStack.popPose();


                    pPoseStack.pushPose();
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            csv.x, csv.y, csv.z, 0, 0, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            pvr.bottom.x, pvr.bottom.y, pvr.bottom.z, 1, 0, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            pvr.left.x, pvr.left.y, pvr.left.z, 1, 1, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            csv.x, csv.y, csv.z, 0, 0, 0, 0, color);
                    pPoseStack.popPose();


                    pPoseStack.pushPose();
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            csv.x, csv.y, csv.z, 0, 0, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            pvr.left.x, pvr.left.y, pvr.left.z, 1, 0, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            pvr.top.x, pvr.top.y, pvr.top.z, 1, 1, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            csv.x, csv.y, csv.z, 0, 0, 0, 0, color);
                    pPoseStack.popPose();
                }
                else if(current instanceof VertexRing cvr) {
                    pPoseStack.pushPose();
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            cvr.top.x, cvr.top.y, cvr.top.z, maxU, maxV, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            pvr.top.x, pvr.top.y, pvr.top.z, minU, maxV, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            pvr.right.x, pvr.right.y, pvr.right.z, minU, minV, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            cvr.right.x, cvr.right.y, cvr.right.z, maxU, minV, 0, 0, color);
                    pPoseStack.popPose();

                    pPoseStack.pushPose();
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            cvr.right.x, cvr.right.y, cvr.right.z, maxU, maxV, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            pvr.right.x, pvr.right.y, pvr.right.z, minU, maxV, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            pvr.bottom.x, pvr.bottom.y, pvr.bottom.z, minU, minV, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            cvr.bottom.x, cvr.bottom.y, cvr.bottom.z, maxU, minV, 0, 0, color);
                    pPoseStack.popPose();

                    pPoseStack.pushPose();
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            cvr.bottom.x, cvr.bottom.y, cvr.bottom.z, maxU, maxV, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            pvr.bottom.x, pvr.bottom.y, pvr.bottom.z, minU, maxV, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            pvr.left.x, pvr.left.y, pvr.left.z, minU, minV, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            cvr.left.x, cvr.left.y, cvr.left.z, maxU, minV, 0, 0, color);
                    pPoseStack.popPose();

                    pPoseStack.pushPose();
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            cvr.left.x, cvr.left.y, cvr.left.z, maxU, maxV, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            pvr.left.x, pvr.left.y, pvr.left.z, minU, maxV, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            pvr.top.x, pvr.top.y, pvr.top.z, minU, minV, 0, 0, color);
                    addVertex(vertexBuilder, renderMatrix, normalMatrix, texture, pPackedLight,
                            cvr.top.x, cvr.top.y, cvr.top.z, maxU, minV, 0, 0, color);
                    pPoseStack.popPose();
                }
            }
        }

    }

    public void generateVertDataThisFrame(ShlorpEntity pEntity, long pTick, float pPartialTick){
        vertData.clear();

        //quick references
        float dbc = pEntity.distanceBetweenClusters;
        int cc = pEntity.vertClusterCount;
        float curveP = pEntity.currentPosOnTrack + pEntity.speed * pPartialTick;
        float curveL = pEntity.length;

        //generate start point
        {
            Vector3 curveCoord = pEntity.generatePointOnBezierCurve(curveP, curveL).sub(VECTOR_POS_CORRECTION);
            SingleVertex startVert = new SingleVertex(curveCoord.x, curveCoord.y, curveCoord.z);

            vertData.add(startVert);
        }

        for(int i=1; i<cc; i++) {
            //How far along the curve this vert/vert ring is
            float curveDist = curveP - (i * dbc);

            //If this cluster is before the track starts, generate a single point at the start instead
            if(curveDist < 0) {
                Vector3 curveCoord = pEntity.generatePointOnBezierCurve(0, curveL).sub(VECTOR_POS_CORRECTION);
                SingleVertex newVert = new SingleVertex(curveCoord.x, curveCoord.y, curveCoord.z);

                vertData.add(newVert);
            }
            //If this cluster is after the track ends, generate a single point at the end instead
            else if(curveDist > curveL) {
                Vector3 curveCoord = pEntity.generatePointOnBezierCurve(curveL, curveL).sub(VECTOR_POS_CORRECTION);
                SingleVertex newVert = new SingleVertex(curveCoord.x, curveCoord.y, curveCoord.z);

                vertData.add(newVert);
            }
            //Otherwise we're making a vert ring
            else {
                if(curveDist > 0 && curveDist < 0.05)
                    curveDist += 0;

                float periodicTick = ((pTick + pPartialTick) % TICKS_FOR_FULL_MARCH) / (TICKS_FOR_FULL_MARCH + 1);
                float periodicDist = ((curveDist + periodicTick) % FLUID_DISTORTION_PERIOD) / FLUID_DISTORTION_PERIOD * (float)Math.PI;

                BezierVectors bv = getAxisVectors(pEntity, curveDist);
                Vector3 point = pEntity.generatePointOnBezierCurve(curveDist, curveL).sub(VECTOR_POS_CORRECTION);
                VertexRing newVertRing = new VertexRing();

                //Scale down the vert ring if we're transitioning in and out
                float scale = VERT_CLUSTER_THICKNESS * (1 + (FLUID_DISTORTION_AMPLITUDE * (float)Math.sin(periodicDist)));
                if(curveDist + dbc > curveL) {
                    scale *= (curveL - curveDist) / dbc;
                } else if (curveDist - dbc < 0) {
                    scale *= curveDist / dbc;
                }
                //Further scale down if we're adjacent to an endpoint
                if(i == cc-1 || i == 1) {
                    scale *= 0.75;
                }

                Vector3 top = point.add(bv.up.scale(-scale));
                Vector3 right = point.add(bv.right.scale(scale));
                Vector3 bottom = point.add(bv.up.scale(scale));
                Vector3 left = point.add(bv.right.scale(-scale));

                newVertRing.configureRing(0,
                        top.x, top.y, top.z,
                        0,0,0,0);
                newVertRing.configureRing(1,
                        right.x, right.y, right.z,
                        0,0,0,0);
                newVertRing.configureRing(2,
                        bottom.x, bottom.y, bottom.z,
                        0,0,0,0);
                newVertRing.configureRing(3,
                        left.x, left.y, left.z,
                        0,0,0,0);

                vertData.add(newVertRing);
            }
        }

        //generate end point
        {
            Vector3 curveCoord = pEntity.generatePointOnBezierCurve(curveP - (cc)*dbc, curveL).sub(VECTOR_POS_CORRECTION);
            SingleVertex endVert = new SingleVertex(curveCoord.x, curveCoord.y, curveCoord.z);

            vertData.add(endVert);
        }
    }

    public BezierVectors getAxisVectors(ShlorpEntity pEntity, float pCurvePosition) {
        Vector3 atPos = pEntity.generatePointOnBezierCurve(pCurvePosition, pEntity.length);
        Vector3 future = pEntity.generatePointOnBezierCurve((float)Math.min(pCurvePosition+0.001, pEntity.length), pEntity.length);

        //generate a forward vector
        Vector3 forward = future.sub(atPos).normalize();
        Vector3 right;

        //Check to see if the forward vector IS absolute up or down
        if(Vector3.dotProduct(forward, Vector3.up()) == 1) {
            //Since we can't use absolute up to generate a right vector, we have to use reverse forward and hope for the best
            right = Vector3.crossProduct(forward, forward.scale(-1)).normalize();
        }
        else {
            //generate a right vector from the forward vector and absolute up
            right = Vector3.crossProduct(forward, Vector3.up()).normalize();
        }

        //generate a real up vector from forward and right
        //since forward and right are already normalized this will be normalized automatically
        Vector3 up = Vector3.crossProduct(forward, right);

        return new BezierVectors(forward, right, up);
    }

    private static void addVertex(VertexConsumer pVertexBuilder, Matrix4f pRenderMatrix, Matrix3f pNormalMatrix, TextureAtlasSprite texture, int pPackedLight, float x, float y, float z, float u, float v, float nrmH, float nrmV, int[] rgba) {
        pVertexBuilder.vertex(pRenderMatrix, x, y, z).color(rgba[0], rgba[1], rgba[2], rgba[3]).uv(texture.getU(u), texture.getV(v)).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(pNormalMatrix, nrmH, nrmV, nrmH).endVertex();
    }

    //Helper classes below here
    private abstract class VertexDataHolder {

    }

    private class SingleVertex extends VertexDataHolder {
        public SingleVertex(float pX, float pY, float pZ) {
            configure(pX, pY, pZ);
        }

        public SingleVertex() {

        }

        public float x;
        public float y;
        public float z;
        public static final int[] COLOR_WHITE = {255, 255, 255, 255};

        public void configure(float pX, float pY, float pZ) {
            this.x = pX;
            this.y = pY;
            this.z = pZ;
        }
    }

    private class VertexRing extends VertexDataHolder {
        public VertexRing() {
            top = new SingleVertex();
            right = new SingleVertex();
            bottom = new SingleVertex();
            left = new SingleVertex();
        }

        public SingleVertex
                top, right, bottom, left;

        public void configureRing(int pID, float pX, float pY, float pZ, float pU, float pV, float pNH, float pNV) {
            if(pID == 0)
                top.configure(pX, pY, pZ);
            else if(pID == 1)
                right.configure(pX, pY, pZ);
            else if(pID == 2)
                bottom.configure(pX, pY, pZ);
            else if(pID == 3)
                left.configure(pX, pY, pZ);
        }
    }

    private class BezierVectors {
        public BezierVectors(Vector3 pForward, Vector3 pRight, Vector3 pUp) {
            this.forward = pForward;
            this.right = pRight;
            this.up = pUp;
        }

        public Vector3
            forward, right, up;
    }
}
