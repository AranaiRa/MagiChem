package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.block.entity.MateriaJarBlockEntity;
import com.aranaira.magichem.block.entity.MateriaJarQuadBlockEntity;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.util.render.ColorUtils;
import com.aranaira.magichem.util.render.MateriaVesselContentsRenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;

public class MateriaJarQuadBlockEntityRenderer implements BlockEntityRenderer<MateriaJarQuadBlockEntity> {
    public static final float[]
        X_OFFSET = {-0.25f,  0.25f, -0.25f, 0.25f},
        Z_OFFSET = {-0.25f, -0.25f,  0.25f, 0.25f};

    public MateriaJarQuadBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(MateriaJarQuadBlockEntity mvqbe, float pPartialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        PoseStack.Pose last = poseStack.last();

        for(int i=0; i<4; i++) {
            MateriaItem type = mvqbe.getMateriaTypeInSlot(i);
            if(type != null && mvqbe.getMateriaAmountInSlot(i) > 0) {
                int color = type.getMateriaColor();
                if(type.getMateriaName().equals("color")) {
                    int period = 200;
                    int gt = (int)(mvqbe.getLevel().getGameTime() % (period * 2));
                    float pScaledTime = ((float)((gt + pPartialTick) % period)) / (float)period;

                    color = ColorUtils.getLerpedRainbowColor(pScaledTime);
                }

                MateriaVesselContentsRenderUtil.renderJarFluidContentsWithXZOffset(last.pose(), last.normal(), buffer, mvqbe.getCurrentStockPercent(type), color, packedLight, X_OFFSET[i], Z_OFFSET[i]);
            }
        }
    }
}
