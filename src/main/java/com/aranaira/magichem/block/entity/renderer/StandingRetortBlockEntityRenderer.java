package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.block.entity.MateriaJarBlockEntity;
import com.aranaira.magichem.block.entity.StandingRetortBlockEntity;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.util.render.MateriaVesselContentsRenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;

public class StandingRetortBlockEntityRenderer implements BlockEntityRenderer<StandingRetortBlockEntity> {
    public StandingRetortBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(StandingRetortBlockEntity srbe, float pPartialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if(!srbe.getMateria().isEmpty()) {
            int color = 0;
            if(srbe.getMateria().getItem() instanceof MateriaItem mi)
                color = mi.getMateriaColor();

            float f = Math.min(48f, srbe.getMateria().getCount());
            float fill = Math.min(1f, f / 48f);

            VertexConsumer buffer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS));

            PoseStack.Pose last = poseStack.last();
            MateriaVesselContentsRenderUtil.renderRetortFluidContents(last.pose(), last.normal(), buffer, fill, color, packedLight);
        }
    }
}
