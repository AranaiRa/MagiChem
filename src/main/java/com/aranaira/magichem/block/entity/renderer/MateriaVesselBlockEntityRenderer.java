package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.aranaira.magichem.item.EssentiaItem;
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
        if(mvbe.getCurrentStockPercent() > 0) {
            VertexConsumer buffer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS));

            PoseStack.Pose last = poseStack.last();
            MateriaVesselContentsRenderUtil.renderFluidContents(last.pose(), last.normal(), buffer, mvbe.getCurrentStockPercent(), mvbe.getMateriaType().getMateriaColor());

            if(mvbe.getMateriaType() instanceof EssentiaItem ei) {
                MateriaVesselContentsRenderUtil.renderEssentiaLabel(last.pose(), last.normal(), buffer, ei, mvbe.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
            }
        }
    }
}
