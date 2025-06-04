package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.block.entity.SkywrathAltarBlockEntity;
import com.mna.tools.math.Vector3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

public class SkywrathAltarBlockEntityRenderer implements BlockEntityRenderer<SkywrathAltarBlockEntity> {
    public static final Random random = new Random();

    public SkywrathAltarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(SkywrathAltarBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {

        int period = 850;
        int gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
        double posBob = Math.sin((((gt) % period) / (double)period) * (Math.PI * 2) * Math.PI * 2) * 0.03125 * 0.707 + 0.1875;

        period = 500;
        gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
        float rot = ((float)((gt + pPartialTick) % period) / (float)period) * 360f;

        final ItemStack outputItem = pBlockEntity.getHeldItem();

        if(outputItem != null) {
            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 1.1 + posBob, 0.5);
            pPoseStack.scale(0.4f, 0.4f, 0.4f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(rot));
            Minecraft.getInstance().getItemRenderer().renderStatic(outputItem, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);
            pPoseStack.popPose();
        }
    }
}
