package com.aranaira.magichem.entities.renderers;

import com.aranaira.magichem.entities.ItemShlorpEntity;
import com.aranaira.magichem.util.render.ColorUtils;
import com.mna.tools.math.Vector3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY;

public class ItemShlorpEntityRenderer extends EntityRenderer<ItemShlorpEntity> {
    public ItemShlorpEntityRenderer(Context pContext) {
        super(pContext);
    }

    public static final float
            VERT_CLUSTER_THICKNESS = 0.0625f, FLUID_DISTORTION_AMPLITUDE = 0.625f, FLUID_DISTORTION_PERIOD = 0.825f, TICKS_FOR_FULL_MARCH = 16;
    public static final Vector3
            VECTOR_POS_CORRECTION = new Vector3(0.5, 0, 0.5);
    private final List<Vector3> vertData = new ArrayList<>();

    @Override
    public ResourceLocation getTextureLocation(ItemShlorpEntity pEntity) {
        return null;
    }

    @Override
    public void render(ItemShlorpEntity pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        //Fill up our arraylist of vert data
        generateVertDataThisFrame(pEntity, Minecraft.getInstance().level.getGameTime(), pPartialTick);

        Matrix4f renderMatrix = pPoseStack.last().pose();
        Matrix3f normalMatrix = pPoseStack.last().normal();

        //If we don't have at least 2 entries in the vert data list, shit's going to break. So skip rendering if it's not compliant.
        if(vertData.size() < 2)
            return;

        //Otherwise, IT BEGINS
        int period = 90;
        int gt = (int)(pEntity.level().getGameTime() % (period * 2));
        float rot = ((float)((gt + pPartialTick) % period) / (float)period) * 360f;

        final NonNullList<ItemStack> stacksInTransit = pEntity.getNonEmptyStacksInTransit();
        for(int i=0; i< stacksInTransit.size(); i++) {
            Vector3 current = vertData.get(i);
            float scale = 1f;
            float itemTrackPos = pEntity.currentPosOnTrack - pEntity.distanceBetweenClusters*i;
            if(itemTrackPos < pEntity.distanceBetweenClusters) {
                scale *= itemTrackPos / pEntity.distanceBetweenClusters;
            } else if(itemTrackPos > pEntity.length - pEntity.distanceBetweenClusters) {
                scale *= (pEntity.length - itemTrackPos) / pEntity.distanceBetweenClusters;
            }
            scale = Math.min(1,Math.max(0,scale)) * 0.375f;

            pPoseStack.pushPose();
            pPoseStack.translate(current.x, current.y, current.z);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(rot));
            pPoseStack.scale(scale, scale, scale);
            Minecraft.getInstance().getItemRenderer().renderStatic(stacksInTransit.get(i), ItemDisplayContext.FIXED, pPackedLight, NO_OVERLAY, pPoseStack, pBuffer, pEntity.level(), 0);
            pPoseStack.popPose();
        }
    }

    public void generateVertDataThisFrame(ItemShlorpEntity pEntity, long pTick, float pPartialTick){
        vertData.clear();

        //quick references
        float dbc = pEntity.distanceBetweenClusters;
        int cc = pEntity.getNonEmptyStacksInTransit().size();
        float curveP = pEntity.currentPosOnTrack + pEntity.speed * pPartialTick;
        float curveL = pEntity.length;

        //generate start point
        {
            Vector3 curveCoord = pEntity.generatePointOnBezierCurve(curveP, curveL).sub(VECTOR_POS_CORRECTION);
            vertData.add(curveCoord);
        }

        for(int i=1; i<cc; i++) {
            //How far along the curve this vert/vert ring is
            float curveDist = curveP - (i * dbc);

            //If this cluster is before the track starts, generate a single point at the start instead
            if(curveDist < 0) {
                Vector3 curveCoord = pEntity.generatePointOnBezierCurve(0, curveL).sub(VECTOR_POS_CORRECTION);

                vertData.add(curveCoord);
            }
            //If this cluster is after the track ends, generate a single point at the end instead
            else if(curveDist > curveL) {
                Vector3 curveCoord = pEntity.generatePointOnBezierCurve(curveL, curveL).sub(VECTOR_POS_CORRECTION);

                vertData.add(curveCoord);
            }
            //Otherwise generate as expected
            else {
                if(curveDist > 0 && curveDist < 0.05)
                    curveDist += 0;

                int gt = (int)(pTick % (long)(TICKS_FOR_FULL_MARCH * 2));
                float periodicTick = (float)((gt + pPartialTick) % (float)TICKS_FOR_FULL_MARCH) / (float)(TICKS_FOR_FULL_MARCH + 1);
                float periodicDist = ((curveDist + periodicTick) % FLUID_DISTORTION_PERIOD) / FLUID_DISTORTION_PERIOD * (float)Math.PI;

                Vector3 point = pEntity.generatePointOnBezierCurve(curveDist, curveL).sub(VECTOR_POS_CORRECTION);

                vertData.add(point);
            }
        }

        //generate end point
        {
            Vector3 curveCoord = pEntity.generatePointOnBezierCurve(curveP - (cc)*dbc, curveL).sub(VECTOR_POS_CORRECTION);

            vertData.add(curveCoord);
        }
    }
}
