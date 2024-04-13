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
import net.minecraft.world.item.ItemStack;
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

        renderRings(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay, state, last, buffer, world, pos);

        pPoseStack.popPose();
    }

    private void renderRings(ExperienceExchangerBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay, BlockState state, PoseStack.Pose last, VertexConsumer buffer, Level world, BlockPos pos) {
        pPoseStack.pushPose();

        float ringRot = pBlockEntity.ringRotation + ((pBlockEntity.ringRotationNextTick - pBlockEntity.ringRotation) * pPartialTick);
        float crystalRot = pBlockEntity.crystalRotation + ((pBlockEntity.crystalRotationNextTick - pBlockEntity.crystalRotation) * pPartialTick);
        float crystalBob = pBlockEntity.crystalBob + ((pBlockEntity.crystalBobNextTick - pBlockEntity.crystalBob) * pPartialTick);

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0.3125, 0.5);
        pPoseStack.mulPose(Axis.YP.rotation(ringRot));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDER_MODEL_RING1, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0.8125, 0.5);
        pPoseStack.mulPose(Axis.YP.rotation(-ringRot));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDER_MODEL_RING2, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();

        if(pBlockEntity.getItem() != ItemStack.EMPTY) {
            pPoseStack.pushPose();
            pPoseStack.scale(0.5f, 0.5f, 0.5f);
            pPoseStack.translate(0.5, 0.59375 + crystalBob, 0.5);
            pPoseStack.rotateAround(Axis.YP.rotation(pBlockEntity.getIsPushMode() ? -crystalRot : crystalRot), 0.5f, 0, 0.5f);
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDER_MODEL_COM, pPoseStack, pPackedLight, pPackedOverlay);
            pPoseStack.scale(2.0f, 2.0f, 2.0f);
            pPoseStack.popPose();
        }

        pPoseStack.popPose();
    }
}
