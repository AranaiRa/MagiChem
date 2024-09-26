package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.CircleToilBlockEntity;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CircleToilBlockEntityRenderer implements BlockEntityRenderer<CircleToilBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_HANDLE = new ResourceLocation(MagiChemMod.MODID, "obj/special/circle_toil_handle");

    public CircleToilBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CircleToilBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        float temp = ((((world.getGameTime()) + pPartialTick) % 900) / 900f) * 360;

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0, 0.5);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(temp));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_HANDLE, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();
    }
}
