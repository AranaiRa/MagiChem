package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.SignaliteBlockEntity;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SignaliteBlockEntityRenderer implements BlockEntityRenderer<SignaliteBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_BUTT = new ResourceLocation(MagiChemMod.MODID, "obj/special/signalite_butt");
    public static final ResourceLocation RENDERER_MODEL_SPIKE = new ResourceLocation(MagiChemMod.MODID, "obj/special/signalite_spike");
    public static final ResourceLocation RENDERER_MODEL_SPIKE_CHAOTIC = new ResourceLocation(MagiChemMod.MODID, "obj/special/signalite_spike_chaotic");
    public static final ResourceLocation RENDERER_MODEL_SPIKE_DEVOURING = new ResourceLocation(MagiChemMod.MODID, "obj/special/signalite_spike_devouring");
    public static final ResourceLocation RENDERER_MODEL_SPIKE_GATEKEEPING = new ResourceLocation(MagiChemMod.MODID, "obj/special/signalite_spike_gatekeeping");
    public static final ResourceLocation RENDERER_MODEL_SPIKE_NEGATING = new ResourceLocation(MagiChemMod.MODID, "obj/special/signalite_spike_negating");

    public SignaliteBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(SignaliteBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();
        int signalStrength = state.getValue(BlockStateProperties.POWER);

        float color = (signalStrength / 15f) * 0.4f + 0.3f + (signalStrength > 0 ? 0.3f : 0f);

        float posIndex = Math.abs(pos.getX() % 4) + Math.abs(pos.getY() % 4) + Math.abs(pos.getZ() % 4);
        int bobPeriod = 182;
        double bob = (float) Math.sin((((world.getGameTime() + pPartialTick + (posIndex / 12f) * 360f) % bobPeriod) / (float)bobPeriod) * Math.PI * 2);

        int xPeriod = 216;
        float xTime = (float) Math.sin((((world.getGameTime() + pPartialTick + (posIndex / 12f) * 240f) % xPeriod) / (float)xPeriod) * Math.PI * 2);

        int yPeriod = 432;
        float yTime = (float) Math.sin((((world.getGameTime() + pPartialTick + (posIndex / 12f) * 240f) % yPeriod) / (float)yPeriod) * Math.PI * 2);

        int zPeriod = 288;
        float zTime = (float) Math.sin((((world.getGameTime() + pPartialTick + (posIndex / 12f) * 240f) % zPeriod) / (float)zPeriod) * Math.PI * 2);

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0.5 + bob * 0.015625, 0.5);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(2 * xTime));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(2 * yTime));
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(2 * zTime));

        pPoseStack.pushPose();
        if(pBlockEntity.connectedNorth) {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SPIKE, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
        } else {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(270));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_BUTT, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
        }
        pPoseStack.popPose();

        pPoseStack.pushPose();
        if(pBlockEntity.connectedEast) {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(0));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SPIKE, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
        } else {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_BUTT, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
        }
        pPoseStack.popPose();

        pPoseStack.pushPose();
        if(pBlockEntity.connectedSouth) {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(270));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SPIKE, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
        } else {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_BUTT, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
        }
        pPoseStack.popPose();

        pPoseStack.pushPose();
        if(pBlockEntity.connectedWest) {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SPIKE, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
        } else {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(0));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_BUTT, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
        }
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(90));
        pPoseStack.mulPose(Axis.XP.rotationDegrees(45));
        if(pBlockEntity.connectedUp) {
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SPIKE, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
        } else {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_BUTT, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
        }
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(270));
        pPoseStack.mulPose(Axis.XN.rotationDegrees(45));
        if(pBlockEntity.connectedDown) {
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SPIKE, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
        } else {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_BUTT, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
        }
        pPoseStack.popPose();

        pPoseStack.popPose();
    }
}
