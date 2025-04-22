package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.util.render.ColorUtils;
import com.aranaira.magichem.util.render.MateriaVesselContentsRenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public class MateriaVesselBlockEntityRenderer implements BlockEntityRenderer<MateriaVesselBlockEntity> {
    public MateriaVesselBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(MateriaVesselBlockEntity mvbe, float pPartialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
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
            MateriaVesselContentsRenderUtil.renderVesselFluidContents(last.pose(), last.normal(), buffer, mvbe.getCurrentStockPercent(), color, packedLight);

            if(mvbe.getMateriaType() instanceof EssentiaItem ei) {
                MateriaVesselContentsRenderUtil.renderVesselEssentiaLabel(last.pose(), last.normal(), buffer, ei, mvbe.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING), packedLight);
            }
        }
    }
}
