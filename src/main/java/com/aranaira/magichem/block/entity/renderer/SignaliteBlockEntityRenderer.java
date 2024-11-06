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

public class SignaliteBlockEntityRenderer implements BlockEntityRenderer<SignaliteBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_CORE = new ResourceLocation(MagiChemMod.MODID, "obj/special/signalite_core");
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

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0.5, 0.5);
//        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_CORE, pPoseStack, pPackedLight, pPackedOverlay);
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SPIKE_GATEKEEPING, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.mulPose(Axis.YN.rotationDegrees(90));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SPIKE, pPoseStack, pPackedLight, pPackedOverlay);

        pPoseStack.popPose();
    }
}
