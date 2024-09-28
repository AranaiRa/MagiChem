package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.CircleToilBlockEntity;
import com.aranaira.magichem.util.render.ConstructRenderHelper;
import com.mna.api.entities.construct.ConstructMaterial;
import com.mna.entities.models.constructs.modular.ConstructModelRegistry;
import com.mna.tools.math.Vector3;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class CircleToilBlockEntityRenderer implements BlockEntityRenderer<CircleToilBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_HANDLE = new ResourceLocation(MagiChemMod.MODID, "obj/special/circle_toil_handle");

    private static final CompoundTag DEFAULT_CONSTRUCT = new CompoundTag();
    private Map<ConstructRenderHelper.ConstructPartType, Pair<ResourceLocation, Vector3>> renderData = new HashMap<>();

    public CircleToilBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        if(DEFAULT_CONSTRUCT.size() == 0) {
            DEFAULT_CONSTRUCT.putString("HEAD", "mna:constructs/construct_basic_head_wood");
            DEFAULT_CONSTRUCT.putString("TORSO", "mna:constructs/construct_basic_torso_wood");
            DEFAULT_CONSTRUCT.putString("LEFT_ARM", "mna:constructs/construct_grabber_arm_left_wood");
            DEFAULT_CONSTRUCT.putString("RIGHT_ARM", "mna:constructs/construct_grabber_arm_right_wood");
            DEFAULT_CONSTRUCT.putString("LEGS", "mna:constructs/construct_basic_legs_wood");
        }

        renderData = ConstructRenderHelper.getRenderDataFromTag(DEFAULT_CONSTRUCT);
    }

    @Override
    public void render(CircleToilBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        int period = 750;
        float temp = -((((world.getGameTime()) + pPartialTick) % period) / (float)period) * 360;

        pPoseStack.pushPose();
        {
            pPoseStack.translate(0.5, 0, 0.5);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(temp));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_HANDLE, pPoseStack, pPackedLight, pPackedOverlay);

            Pair<ResourceLocation, Vector3> currentPiece;

            double zGripCorrection = ConstructRenderHelper.mappedSinusoidalAngle(world.getGameTime(), pPartialTick, 50, 0, -0.05, -0.13);
            double yGripCorrection = ConstructRenderHelper.mappedSinusoidalAngle(world.getGameTime(), pPartialTick, 50, 0, 0, 0.04);

            pPoseStack.translate(-1.30529, yGripCorrection, 0.8375 + zGripCorrection);

            currentPiece = renderData.get(ConstructRenderHelper.ConstructPartType.TORSO);
            double torsoRot = ConstructRenderHelper.mappedSinusoidalAngle(world.getGameTime(), pPartialTick, 50, 0, 342.5, 347.5);

            pPoseStack.pushPose();
            {
                pPoseStack.translate(currentPiece.getSecond().x, currentPiece.getSecond().y, currentPiece.getSecond().z);
                pPoseStack.rotateAround(Axis.XP.rotationDegrees((float)torsoRot), 0, -currentPiece.getSecond().y*2 - 0.25f, 0);
                pPoseStack.translate(-currentPiece.getSecond().x, -currentPiece.getSecond().y, -currentPiece.getSecond().z);
                ModelUtils.renderModel(pBuffer, world, pos, state, currentPiece.getFirst(), pPoseStack, pPackedLight, pPackedOverlay);

                currentPiece = renderData.get(ConstructRenderHelper.ConstructPartType.HEAD);
                double headRot = ConstructRenderHelper.mappedSinusoidalAngle(world.getGameTime(), pPartialTick, 50, 25, 350, 360);

                pPoseStack.pushPose();
                {
                    pPoseStack.translate(currentPiece.getSecond().x, currentPiece.getSecond().y, currentPiece.getSecond().z);
                    pPoseStack.rotateAround(Axis.XP.rotationDegrees((float)headRot), 0, -currentPiece.getSecond().y*2 - 0.25f, 0);

                    pPoseStack.translate(-currentPiece.getSecond().x, -currentPiece.getSecond().y, -currentPiece.getSecond().z);
                    ModelUtils.renderModel(pBuffer, world, pos, state, currentPiece.getFirst(), pPoseStack, pPackedLight, pPackedOverlay);
                    ModelUtils.renderModel(pBuffer, world, pos, state, renderData.get(ConstructRenderHelper.ConstructPartType.EYES).getFirst(), pPoseStack, pPackedLight, pPackedOverlay);
                    pPoseStack.popPose();
                }

                currentPiece = renderData.get(ConstructRenderHelper.ConstructPartType.ARM_LEFT);
                double armRot = ConstructRenderHelper.mappedSinusoidalAngle(world.getGameTime(), pPartialTick, 50, 0, 80, 70);;

                pPoseStack.pushPose();
                {
                    pPoseStack.translate(currentPiece.getSecond().x, currentPiece.getSecond().y, currentPiece.getSecond().z);
                    pPoseStack.rotateAround(Axis.XP.rotationDegrees((float)armRot), -currentPiece.getSecond().x, -currentPiece.getSecond().y*2, -currentPiece.getSecond().z*2);

                    pPoseStack.translate(-currentPiece.getSecond().x, -currentPiece.getSecond().y, -currentPiece.getSecond().z);
                    ModelUtils.renderModel(pBuffer, world, pos, state, currentPiece.getFirst(), pPoseStack, pPackedLight, pPackedOverlay);
                    pPoseStack.popPose();
                }

                currentPiece = renderData.get(ConstructRenderHelper.ConstructPartType.ARM_RIGHT);;

                pPoseStack.pushPose();
                {
                    pPoseStack.translate(currentPiece.getSecond().x, currentPiece.getSecond().y, currentPiece.getSecond().z);
                    pPoseStack.rotateAround(Axis.XP.rotationDegrees((float)armRot), -currentPiece.getSecond().x*2, -currentPiece.getSecond().y*2, -currentPiece.getSecond().z*2);

                    pPoseStack.translate(-currentPiece.getSecond().x, -currentPiece.getSecond().y, -currentPiece.getSecond().z);
                    ModelUtils.renderModel(pBuffer, world, pos, state, currentPiece.getFirst(), pPoseStack, pPackedLight, pPackedOverlay);
                    pPoseStack.popPose();
                }

                pPoseStack.popPose();
            }

            currentPiece = renderData.get(ConstructRenderHelper.ConstructPartType.LEG_LEFT);
            double legRot = ConstructRenderHelper.mappedSinusoidalAngle(world.getGameTime(), pPartialTick, 100, 50, 380, 320);

            pPoseStack.pushPose();
            {
                pPoseStack.translate(currentPiece.getSecond().x, currentPiece.getSecond().y, currentPiece.getSecond().z);
                pPoseStack.rotateAround(Axis.XP.rotationDegrees((float)legRot), -currentPiece.getSecond().x, -currentPiece.getSecond().y*2, -currentPiece.getSecond().z*2);

                pPoseStack.translate(-currentPiece.getSecond().x, -currentPiece.getSecond().y, -currentPiece.getSecond().z);
                ModelUtils.renderModel(pBuffer, world, pos, state, currentPiece.getFirst(), pPoseStack, pPackedLight, pPackedOverlay);
                pPoseStack.popPose();
            }

            currentPiece = renderData.get(ConstructRenderHelper.ConstructPartType.LEG_RIGHT);
            legRot = ConstructRenderHelper.mappedSinusoidalAngle(world.getGameTime(), pPartialTick, 100, 50, 400, 340);

            pPoseStack.pushPose();
            {
                pPoseStack.translate(currentPiece.getSecond().x, currentPiece.getSecond().y, currentPiece.getSecond().z);
                pPoseStack.rotateAround(Axis.XN.rotationDegrees((float)legRot), -currentPiece.getSecond().x, -currentPiece.getSecond().y*2, -currentPiece.getSecond().z*2);

                pPoseStack.translate(-currentPiece.getSecond().x, -currentPiece.getSecond().y, -currentPiece.getSecond().z);
                ModelUtils.renderModel(pBuffer, world, pos, state, currentPiece.getFirst(), pPoseStack, pPackedLight, pPackedOverlay);
                pPoseStack.popPose();
            }

            pPoseStack.popPose();
        }
    }
}
