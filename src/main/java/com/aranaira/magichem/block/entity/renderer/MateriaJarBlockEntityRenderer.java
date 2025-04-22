package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.block.entity.MateriaJarBlockEntity;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.util.render.ColorUtils;
import com.aranaira.magichem.util.render.MateriaVesselContentsRenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public class MateriaJarBlockEntityRenderer implements BlockEntityRenderer<MateriaJarBlockEntity> {
    public MateriaJarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(MateriaJarBlockEntity mvbe, float pPartialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if(mvbe.getCurrentStockPercent() > 0 && mvbe.getMateriaType() != null) {
            VertexConsumer buffer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS));

            int color = mvbe.getMateriaType().getMateriaColor();
            if(mvbe.getMateriaType().getMateriaName().equals("color")) {
                int period = 200;
                int gt = (int)(mvbe.getLevel().getGameTime() % (period * 2));
                float pScaledTime = ((float)((gt + pPartialTick) % period)) / (float)period;

                color = ColorUtils.getLerpedRainbowColor(pScaledTime);
            }

            PoseStack.Pose last = poseStack.last();
            MateriaVesselContentsRenderUtil.renderJarFluidContents(last.pose(), last.normal(), buffer, mvbe.getCurrentStockPercent(), color, packedLight);
        }
    }
}
