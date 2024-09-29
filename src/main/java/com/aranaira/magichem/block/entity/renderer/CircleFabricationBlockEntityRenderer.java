package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.CircleFabricationBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.util.render.ColorUtils;
import com.aranaira.magichem.util.render.ConstructRenderHelper;
import com.mna.tools.math.Vector3;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class CircleFabricationBlockEntityRenderer implements BlockEntityRenderer<CircleFabricationBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_CIRCLE = new ResourceLocation(MagiChemMod.MODID, "obj/special/circle_fabrication_circle");
    public static final ResourceLocation RENDERER_MODEL_BOWLFILL = new ResourceLocation(MagiChemMod.MODID, "obj/special/circle_fabrication_bowlfill");

    public CircleFabricationBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CircleFabricationBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        //circle
        {
            pPoseStack.pushPose();

            Direction facing = state.getValue(MagiChemBlockStateProperties.FACING);
            if(facing == Direction.EAST) {
                pPoseStack.mulPose(Axis.YN.rotationDegrees(90));
                pPoseStack.translate(0, 0, -1);
            }
            else if(facing == Direction.SOUTH) {
                pPoseStack.mulPose(Axis.YN.rotationDegrees(180));
                pPoseStack.translate(-1, 0, -1);
            }
            else if(facing == Direction.WEST) {
                pPoseStack.mulPose(Axis.YN.rotationDegrees(270));
                pPoseStack.translate(-1, 0, 0);
            }

            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_CIRCLE, pPoseStack, pPackedLight, pPackedOverlay, RenderType.translucent());

            pPoseStack.popPose();
        }

        //bowls with materia
        {
            final ItemStack[] contentsOfInputSlots = pBlockEntity.getContentsOfInputSlots();
            boolean has1 = !contentsOfInputSlots[0].isEmpty() || !contentsOfInputSlots[1].isEmpty();
            boolean has2 = !contentsOfInputSlots[2].isEmpty() || !contentsOfInputSlots[3].isEmpty();
            boolean has3 = !contentsOfInputSlots[4].isEmpty() || !contentsOfInputSlots[5].isEmpty();
            boolean has4 = !contentsOfInputSlots[6].isEmpty() || !contentsOfInputSlots[7].isEmpty();
            boolean has5 = !contentsOfInputSlots[8].isEmpty() || !contentsOfInputSlots[9].isEmpty();

            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 0, 0.5);

            if (has1) {
                float[] color = ColorUtils.getRGBAFloatTintFromPackedInt((
                        contentsOfInputSlots[0].isEmpty() ?
                                (MateriaItem) contentsOfInputSlots[1].getItem() :
                                (MateriaItem) contentsOfInputSlots[0].getItem()
                ).getMateriaColor());

                pPoseStack.pushPose();
                pPoseStack.translate(0, 0, -1.1875);
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_BOWLFILL, pPoseStack, pPackedLight, pPackedOverlay, color);
                pPoseStack.popPose();
            }

            pPoseStack.mulPose(Axis.YN.rotationDegrees(72));
            if (has2) {
                float[] color = ColorUtils.getRGBAFloatTintFromPackedInt((
                        contentsOfInputSlots[2].isEmpty() ?
                                (MateriaItem) contentsOfInputSlots[3].getItem() :
                                (MateriaItem) contentsOfInputSlots[2].getItem()
                ).getMateriaColor());

                pPoseStack.pushPose();
                pPoseStack.translate(0, 0, -1.1875);
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_BOWLFILL, pPoseStack, pPackedLight, pPackedOverlay, color);
                pPoseStack.popPose();
            }

            pPoseStack.mulPose(Axis.YN.rotationDegrees(72));
            if (has3) {
                float[] color = ColorUtils.getRGBAFloatTintFromPackedInt((
                        contentsOfInputSlots[4].isEmpty() ?
                                (MateriaItem) contentsOfInputSlots[5].getItem() :
                                (MateriaItem) contentsOfInputSlots[4].getItem()
                ).getMateriaColor());

                pPoseStack.pushPose();
                pPoseStack.translate(0, 0, -1.1875);
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_BOWLFILL, pPoseStack, pPackedLight, pPackedOverlay, color);
                pPoseStack.popPose();
            }

            pPoseStack.mulPose(Axis.YN.rotationDegrees(72));
            if (has4) {
                float[] color = ColorUtils.getRGBAFloatTintFromPackedInt((
                        contentsOfInputSlots[6].isEmpty() ?
                                (MateriaItem) contentsOfInputSlots[7].getItem() :
                                (MateriaItem) contentsOfInputSlots[6].getItem()
                ).getMateriaColor());

                pPoseStack.pushPose();
                pPoseStack.translate(0, 0, -1.1875);
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_BOWLFILL, pPoseStack, pPackedLight, pPackedOverlay, color);
                pPoseStack.popPose();
            }

            pPoseStack.mulPose(Axis.YN.rotationDegrees(72));
            if (has5) {
                float[] color = ColorUtils.getRGBAFloatTintFromPackedInt((
                        contentsOfInputSlots[8].isEmpty() ?
                                (MateriaItem) contentsOfInputSlots[9].getItem() :
                                (MateriaItem) contentsOfInputSlots[8].getItem()
                ).getMateriaColor());

                pPoseStack.pushPose();
                pPoseStack.translate(0, 0, -1.1875);
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_BOWLFILL, pPoseStack, pPackedLight, pPackedOverlay, color);
                pPoseStack.popPose();
            }

            pPoseStack.popPose();
        }

        //output item
        {
            final ItemStack outputItem = pBlockEntity.getOutputInLastSlot();
            if(outputItem != null) {
                pPoseStack.pushPose();

                //TODO: take facing into account
                pPoseStack.translate(0.5, 0.25, 0.5 + 0.122994);
                pPoseStack.scale(0.4f, 0.4f, 0.4f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees((((world.getGameTime() + pPartialTick) % 500) / 500f) * 360));
                Minecraft.getInstance().getItemRenderer().renderStatic(outputItem, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, world, 0);

                pPoseStack.popPose();
            }
        }
    }
}
