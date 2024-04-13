package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ActuatorWaterBlockEntity;
import com.aranaira.magichem.block.entity.ExperienceExchangerBlockEntity;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ExperienceExchangerBlockEntityRenderer implements BlockEntityRenderer<ExperienceExchangerBlockEntity> {
    public static final ResourceLocation RENDER_MODEL_RING1 = new ResourceLocation(MagiChemMod.MODID, "obj/special/experience_exchanger_ring1");
    public static final ResourceLocation RENDER_MODEL_RING2 = new ResourceLocation(MagiChemMod.MODID, "obj/special/experience_exchanger_ring2");
    public static final ResourceLocation RENDER_MODEL_COM = new ResourceLocation(MagiChemMod.MODID, "obj/special/experience_exchanger_com");

    public ExperienceExchangerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(ExperienceExchangerBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        VertexConsumer buffer = pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS));

        pPoseStack.pushPose();
        PoseStack.Pose last = pPoseStack.last();

        renderRings(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, state, last, buffer, world, pos);

        pPoseStack.popPose();
    }

    private void renderRings(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay, BlockState state, PoseStack.Pose last, VertexConsumer buffer, Level world, BlockPos pos) {
        pPoseStack.pushPose();
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDER_MODEL_RING1, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();
    }
}
