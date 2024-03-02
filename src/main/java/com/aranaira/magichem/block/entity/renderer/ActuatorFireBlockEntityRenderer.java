package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ActuatorFireBlockEntity;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
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

public class ActuatorFireBlockEntityRenderer implements BlockEntityRenderer<ActuatorFireBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_PIPE_LEFT = new ResourceLocation(MagiChemMod.MODID, "obj/special/actuator_fire_pipeleft");
    public static final ResourceLocation RENDERER_MODEL_PIPE_RIGHT = new ResourceLocation(MagiChemMod.MODID, "obj/special/actuator_fire_piperight");
    public static final ResourceLocation RENDERER_MODEL_PIPE_CENTER = new ResourceLocation(MagiChemMod.MODID, "obj/special/actuator_fire_pipecenter");

    public ActuatorFireBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(ActuatorFireBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderPipes(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    private void renderPipes(ActuatorFireBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        pPoseStack.pushPose();
        switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> {
                //pPoseStack.mulPose(Axis.YP.rotationDegrees(pBlockEntity.wheelAngle + pPartialTick * (pBlockEntity.wheelSpeed)));
            }
            case EAST -> {
                pPoseStack.translate(1.0f, 0.0f, 0.0f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(270));
            }
            case SOUTH -> {
                pPoseStack.translate(1.0f, 0.0f, 1.0f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
            }
            case WEST -> {
                pPoseStack.translate(0.0f, 0.0f, 1.0f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
            }
        }

        pPoseStack.pushPose();

        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_PIPE_LEFT, pPoseStack, pPackedLight, pPackedOverlay);
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_PIPE_CENTER, pPoseStack, pPackedLight, pPackedOverlay);
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_PIPE_RIGHT, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();

        pPoseStack.popPose();
    }
}
