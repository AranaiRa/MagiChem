package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.SignaliteBlock;
import com.aranaira.magichem.block.SignaliteBlock.SignaliteBlockType;
import com.aranaira.magichem.block.SignalitePairBlock;
import com.aranaira.magichem.block.entity.SignaliteBlockEntity;
import com.aranaira.magichem.block.entity.SignalitePairBlockEntity;
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

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING_OMNI;

public class SignalitePairBlockEntityRenderer implements BlockEntityRenderer<SignalitePairBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_SPIKE = new ResourceLocation(MagiChemMod.MODID, "obj/special/signalite_spike");
    public static final ResourceLocation RENDERER_MODEL_SINGING = new ResourceLocation(MagiChemMod.MODID, "obj/special/signalite_singing_body");
    public static final ResourceLocation RENDERER_MODEL_LISTENING = new ResourceLocation(MagiChemMod.MODID, "obj/special/signalite_listening_body");

    public SignalitePairBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(SignalitePairBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if(pBlockEntity.hidden)
            return;

        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();
        Direction facing = state.getValue(FACING_OMNI);
        int signalStrength = state.getValue(BlockStateProperties.POWER);

        float color = (signalStrength / 15f) * 0.4f + 0.3f + (signalStrength > 0 ? 0.3f : 0f);

        float posIndex = Math.abs(pos.getX() % 4) + Math.abs(pos.getY() % 4) + Math.abs(pos.getZ() % 4);
        int bobPeriod = 182;
        int gt = (int)(world.getGameTime() % (bobPeriod * 2));
        double bob = (float) Math.sin(((float)((gt + pPartialTick + (posIndex / 12f) * 360f) % bobPeriod) / (float)bobPeriod) * Math.PI * 2);

        int xPeriod = 216;
        gt = (int)(world.getGameTime() % (xPeriod * 2));
        float xTime = (float) Math.sin(((float)((gt + pPartialTick + (posIndex / 12f) * 240f) % xPeriod) / (float)xPeriod) * Math.PI * 2);

        int yPeriod = 432;
        gt = (int)(world.getGameTime() % (yPeriod * 2));
        float yTime = (float) Math.sin(((float)((gt + pPartialTick + (posIndex / 12f) * 240f) % yPeriod) / (float)yPeriod) * Math.PI * 2);

        int zPeriod = 288;
        gt = (int)(world.getGameTime() % (zPeriod * 2));
        float zTime = (float) Math.sin(((float)((gt + pPartialTick + (posIndex / 12f) * 240f) % zPeriod) / (float)zPeriod) * Math.PI * 2);

        //if the seer is vertical also give it some slow Y rotation
        int verticalSpinPeriod = 984;
        gt = (int)(world.getGameTime() % (verticalSpinPeriod * 2));
        float yVerticalRotDegrees = ((float)((gt + pPartialTick + (posIndex / 12f) * 240f) % verticalSpinPeriod) / (float)verticalSpinPeriod) * 360f;

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0.5 + bob * 0.015625, 0.5);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(2 * xTime));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(2 * yTime));
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(2 * zTime));

        if(facing == Direction.NORTH) {
            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
            ModelUtils.renderModel(pBuffer, world, pos, state, getBodyModel(pBlockEntity), pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SPIKE, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            pPoseStack.popPose();
        }
        else if(facing == Direction.SOUTH) {
            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.YP.rotationDegrees(270));
            ModelUtils.renderModel(pBuffer, world, pos, state, getBodyModel(pBlockEntity), pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SPIKE, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            pPoseStack.popPose();
        }
        else if(facing == Direction.EAST) {
            pPoseStack.pushPose();
            ModelUtils.renderModel(pBuffer, world, pos, state, getBodyModel(pBlockEntity), pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SPIKE, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            pPoseStack.popPose();
        }
        else if(facing == Direction.WEST) {
            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
            ModelUtils.renderModel(pBuffer, world, pos, state, getBodyModel(pBlockEntity), pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SPIKE, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            pPoseStack.popPose();
        }
        else if (facing == Direction.UP) {
            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(90));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(yVerticalRotDegrees));
            ModelUtils.renderModel(pBuffer, world, pos, state, getBodyModel(pBlockEntity), pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SPIKE, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            pPoseStack.popPose();
        }
        else if (facing == Direction.DOWN) {
            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(270));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(yVerticalRotDegrees));
            ModelUtils.renderModel(pBuffer, world, pos, state, getBodyModel(pBlockEntity), pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SPIKE, pPoseStack, pPackedLight, pPackedOverlay, new float[]{color, color, color, 1f});
            pPoseStack.popPose();
        }

        pPoseStack.popPose();
    }

    private static ResourceLocation getBodyModel(SignalitePairBlockEntity pEntity) {
        if(pEntity.getBlockState().getBlock() instanceof SignalitePairBlock spb) {
            return spb.getType() == SignalitePairBlock.SignalitePairType.SINGING ? RENDERER_MODEL_SINGING : RENDERER_MODEL_LISTENING;
        }
        return RENDERER_MODEL_SPIKE;
    }
}
