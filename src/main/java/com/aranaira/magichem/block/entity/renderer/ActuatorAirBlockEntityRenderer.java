package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ActuatorAirBlockEntity;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Random;

public class ActuatorAirBlockEntityRenderer implements BlockEntityRenderer<ActuatorAirBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_FANS = new ResourceLocation(MagiChemMod.MODID, "obj/special/actuator_air_fan");

    public ActuatorAirBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(ActuatorAirBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderFans(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    private void renderFans(ActuatorAirBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        pPoseStack.pushPose();
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if(dir == Direction.NORTH) {
            pPoseStack.translate(0.5f, 0.59375f, 0.65625f);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(pBlockEntity.fanAngle + pPartialTick * (pBlockEntity.fanSpeed)));
        } else if(dir == Direction.EAST) {
            pPoseStack.translate(0.34375f, 0.59375f, 0.5f);
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(pBlockEntity.fanAngle + pPartialTick * (pBlockEntity.fanSpeed)));
            pPoseStack.mulPose(Axis.YP.rotationDegrees(270));
        } else if(dir == Direction.SOUTH) {
            pPoseStack.translate(0.5f, 0.59375f, 0.34375f);
            pPoseStack.mulPose(Axis.XN.rotationDegrees(pBlockEntity.fanAngle + pPartialTick * (pBlockEntity.fanSpeed)));
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
        } else if(dir == Direction.WEST) {
            pPoseStack.translate(0.65625f, 0.59375f, 0.5f);
            pPoseStack.mulPose(Axis.ZN.rotationDegrees(pBlockEntity.fanAngle + pPartialTick * (pBlockEntity.fanSpeed)));
            pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
        }

        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_FANS, pPoseStack, pPackedLight, pPackedOverlay);

        pPoseStack.popPose();
    }
}
