package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.mna.api.tools.RLoc;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CentrifugeBlockEntityRenderer implements BlockEntityRenderer<CentrifugeBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_COG = new ResourceLocation(MagiChemMod.MODID, "obj/special/centrifuge_cog");
    public static final ResourceLocation RENDERER_MODEL_WHEEL = new ResourceLocation(MagiChemMod.MODID, "obj/special/centrifuge_wheel");
    private BakedModel bakedModel;

    public CentrifugeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(CentrifugeBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderWheel(pBlockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        this.renderCog(pBlockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    private void renderWheel(CentrifugeBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        pPoseStack.pushPose();
        switch((Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH: {
                pPoseStack.mulPose(Axis.YP.rotationDegrees(pBlockEntity.wheelAngle));
                break;
            }
            case EAST: {
                pPoseStack.translate(1.0f, 0.0f, 0.0f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(pBlockEntity.wheelAngle));
                break;
            }
            case SOUTH: {
                pPoseStack.translate(1.0f, 0.0f, 1.0f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(pBlockEntity.wheelAngle));
                break;
            }
            case WEST: {
                pPoseStack.translate(0.0f, 0.0f, 1.0f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(pBlockEntity.wheelAngle));
                break;
            }
        }

        pPoseStack.pushPose();

        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_WHEEL, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();

        pPoseStack.popPose();
    }

    private void renderCog(CentrifugeBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        pPoseStack.pushPose();
        switch((Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH: {
                pPoseStack.translate(-0.28125f, 0.5f, -0.71875f);
                pPoseStack.mulPose(Axis.ZN.rotationDegrees(pBlockEntity.cogAngle));
                break;
            }
            case EAST: {
                pPoseStack.translate(1.71875f, 0.5f, -0.28125f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
                pPoseStack.mulPose(Axis.ZP.rotationDegrees(pBlockEntity.cogAngle));
                break;
            }
            case SOUTH: {
                pPoseStack.translate(1.28125f, 0.5f, 1.71875f);
                pPoseStack.mulPose(Axis.ZP.rotationDegrees(pBlockEntity.cogAngle));
                break;
            }
            case WEST: {
                pPoseStack.translate(-0.71875f, 0.5f, 1.28125f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
                pPoseStack.mulPose(Axis.ZN.rotationDegrees(pBlockEntity.cogAngle));
                break;
            }
        }

        pPoseStack.pushPose();

        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_COG, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();

        pPoseStack.popPose();
    }
}
