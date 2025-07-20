package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.SignaliteBlock;
import com.aranaira.magichem.block.SignaliteBlock.SignaliteBlockType;
import com.aranaira.magichem.block.entity.MateriaReflectorBlockEntity;
import com.aranaira.magichem.block.entity.SignaliteBlockEntity;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.util.render.ColorUtils;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class MateriaReflectorBlockEntityRenderer implements BlockEntityRenderer<MateriaReflectorBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_EYE = new ResourceLocation(MagiChemMod.MODID, "obj/special/materia_reflector_eye");
    public static final ResourceLocation RENDERER_MODEL_IRIS = new ResourceLocation(MagiChemMod.MODID, "obj/special/materia_reflector_iris");

    public MateriaReflectorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(MateriaReflectorBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        if(pBlockEntity.getStackForDirection(Direction.UP).getItem() instanceof MateriaItem mi) {
            int packedColor = mi.getMateriaColor();
            if(mi.getMateriaName().equals("color")) {
                int period = 131;
                int gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
                float pScaledTime = ((float)((gt + pPartialTick) % period)) / (float)period;

                packedColor = ColorUtils.getLerpedRainbowColor(pScaledTime);
            }
            float[] color = ColorUtils.packedColorToFloatArray(packedColor);
            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 1.0, 0.5);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(-90));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_EYE, pPoseStack, pPackedLight, pPackedOverlay);
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_IRIS, pPoseStack, pPackedLight, pPackedOverlay, color);
            pPoseStack.popPose();
        }

        if(pBlockEntity.getStackForDirection(Direction.DOWN).getItem() instanceof MateriaItem mi) {
            int packedColor = mi.getMateriaColor();
            if(mi.getMateriaName().equals("color")) {
                int period = 131;
                int gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
                float pScaledTime = ((float)((gt + pPartialTick) % period)) / (float)period;

                packedColor = ColorUtils.getLerpedRainbowColor(pScaledTime);
            }
            float[] color = ColorUtils.packedColorToFloatArray(packedColor);
            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 0.0, 0.5);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(-90));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_EYE, pPoseStack, pPackedLight, pPackedOverlay);
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_IRIS, pPoseStack, pPackedLight, pPackedOverlay, color);
            pPoseStack.popPose();
        }

        if(pBlockEntity.getStackForDirection(Direction.NORTH).getItem() instanceof MateriaItem mi) {
            int packedColor = mi.getMateriaColor();
            if(mi.getMateriaName().equals("color")) {
                int period = 131;
                int gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
                float pScaledTime = ((float)((gt + pPartialTick) % period)) / (float)period;

                packedColor = ColorUtils.getLerpedRainbowColor(pScaledTime);
            }
            float[] color = ColorUtils.packedColorToFloatArray(packedColor);
            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 0.5, 0.0);
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_EYE, pPoseStack, pPackedLight, pPackedOverlay);
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_IRIS, pPoseStack, pPackedLight, pPackedOverlay, color);
            pPoseStack.popPose();
        }

        if(pBlockEntity.getStackForDirection(Direction.SOUTH).getItem() instanceof MateriaItem mi) {
            int packedColor = mi.getMateriaColor();
            if(mi.getMateriaName().equals("color")) {
                int period = 131;
                int gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
                float pScaledTime = ((float)((gt + pPartialTick) % period)) / (float)period;

                packedColor = ColorUtils.getLerpedRainbowColor(pScaledTime);
            }
            float[] color = ColorUtils.packedColorToFloatArray(packedColor);
            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 0.5, 1.0);
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_EYE, pPoseStack, pPackedLight, pPackedOverlay);
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_IRIS, pPoseStack, pPackedLight, pPackedOverlay, color);
            pPoseStack.popPose();
        }

        if(pBlockEntity.getStackForDirection(Direction.EAST).getItem() instanceof MateriaItem mi) {
            int packedColor = mi.getMateriaColor();
            if(mi.getMateriaName().equals("color")) {
                int period = 131;
                int gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
                float pScaledTime = ((float)((gt + pPartialTick) % period)) / (float)period;

                packedColor = ColorUtils.getLerpedRainbowColor(pScaledTime);
            }
            float[] color = ColorUtils.packedColorToFloatArray(packedColor);
            pPoseStack.pushPose();
            pPoseStack.translate(1.0, 0.5, 0.5);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_EYE, pPoseStack, pPackedLight, pPackedOverlay);
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_IRIS, pPoseStack, pPackedLight, pPackedOverlay, color);
            pPoseStack.popPose();
        }

        if(pBlockEntity.getStackForDirection(Direction.WEST).getItem() instanceof MateriaItem mi) {
            int packedColor = mi.getMateriaColor();
            if(mi.getMateriaName().equals("color")) {
                int period = 131;
                int gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
                float pScaledTime = ((float)((gt + pPartialTick) % period)) / (float)period;

                packedColor = ColorUtils.getLerpedRainbowColor(pScaledTime);
            }
            float[] color = ColorUtils.packedColorToFloatArray(packedColor);
            pPoseStack.pushPose();
            pPoseStack.translate(0.0, 0.5, 0.5);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_EYE, pPoseStack, pPackedLight, pPackedOverlay);
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_IRIS, pPoseStack, pPackedLight, pPackedOverlay, color);
            pPoseStack.popPose();
        }
    }
}
