package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.SignaliteBlock;
import com.aranaira.magichem.block.SignaliteBlock.SignaliteBlockType;
import com.aranaira.magichem.block.entity.SignaliteBlockEntity;
import com.aranaira.magichem.block.entity.SignaliteSeerBlockEntity;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public class SignaliteSeerBlockEntityRenderer implements BlockEntityRenderer<SignaliteSeerBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_SEER_BODY = new ResourceLocation(MagiChemMod.MODID, "obj/special/signalite_seer_body");
    public static final ResourceLocation RENDERER_MODEL_SEER_TORCH = new ResourceLocation(MagiChemMod.MODID, "obj/special/signalite_seer_torch");
    public static final ResourceLocation RENDERER_MODEL_SEER_TORCH_SHELL = new ResourceLocation(MagiChemMod.MODID, "obj/special/signalite_seer_torch_shell");

    public SignaliteSeerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(SignaliteSeerBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if(pBlockEntity.hidden)
            return;

        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();
        int signalStrength = state.getValue(BlockStateProperties.POWER);
        Direction dir = state.getValue(BlockStateProperties.FACING);

        float color = (signalStrength / 15f) * 0.4f + 0.3f + (signalStrength > 0 ? 0.3f : 0f);

        //if the seer is vertical also give it some slow Y rotation

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

        if (dir == Direction.SOUTH) {
            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.YP.rotationDegrees(270));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_BODY, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            if(signalStrength > 0) {
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_TORCH_SHELL, pPoseStack, pPackedLight, pPackedOverlay, new float[]{1f, color, color, 1f});
            }
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_TORCH, pPoseStack, pPackedLight, pPackedOverlay, RenderType.cutout());

            pPoseStack.popPose();
        }
        else if (dir == Direction.NORTH) {
            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_BODY, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            if(signalStrength > 0) {
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_TORCH_SHELL, pPoseStack, pPackedLight, pPackedOverlay, new float[]{1f, color, color, 1f});
            }
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_TORCH, pPoseStack, pPackedLight, pPackedOverlay, RenderType.cutout());
            pPoseStack.popPose();
        }
        else if (dir == Direction.EAST) {
            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.YP.rotationDegrees(0));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_BODY, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            if(signalStrength > 0) {
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_TORCH_SHELL, pPoseStack, pPackedLight, pPackedOverlay, new float[]{1f, color, color, 1f});
            }
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_TORCH, pPoseStack, pPackedLight, pPackedOverlay, RenderType.cutout());
            pPoseStack.popPose();
        }
        else if (dir == Direction.WEST) {
            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_BODY, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            if(signalStrength > 0) {
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_TORCH_SHELL, pPoseStack, pPackedLight, pPackedOverlay, new float[]{1f, color, color, 1f});
            }
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_TORCH, pPoseStack, pPackedLight, pPackedOverlay, RenderType.cutout());
            pPoseStack.popPose();
        }
        else if (dir == Direction.UP) {
            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(90));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(45));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_BODY, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            if(signalStrength > 0) {
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_TORCH_SHELL, pPoseStack, pPackedLight, pPackedOverlay, new float[]{1f, color, color, 1f});
            }
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_TORCH, pPoseStack, pPackedLight, pPackedOverlay, RenderType.cutout());
            pPoseStack.popPose();
        }
        else if (dir == Direction.DOWN) {
            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(270));
            pPoseStack.mulPose(Axis.XN.rotationDegrees(45));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_BODY, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            if(signalStrength > 0) {
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_TORCH_SHELL, pPoseStack, pPackedLight, pPackedOverlay, new float[]{1f, color, color, 1f});
            }
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SEER_TORCH, pPoseStack, pPackedLight, pPackedOverlay, RenderType.cutout());
            pPoseStack.popPose();
        }

        pPoseStack.popPose();
    }
}
