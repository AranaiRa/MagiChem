package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.aranaira.magichem.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluids;

public class MateriaVesselBlockEntityRenderer implements BlockEntityRenderer<MateriaVesselBlockEntity> {
    public MateriaVesselBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    public static final ResourceLocation FLUID_TEXTURE = new ResourceLocation("minecraft", "block/water_still");

    @Override
    public void render(MateriaVesselBlockEntity mvbe, float pPartialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if(mvbe.getCurrentStockPercent() > 0) {
            VertexConsumer buffer = bufferSource.getBuffer(ItemBlockRenderTypes.getRenderLayer(Fluids.WATER.defaultFluidState()));

            PoseStack.Pose last = poseStack.last();
            renderFluidContents(last.pose(), last.normal(), buffer, mvbe.getCurrentStockPercent(), mvbe.getMateriaType().getMateriaColor());
        }
    }

    private static final float
        FLUID_START_XZ = 0.25F,
        FLUID_START_Y = 0.0625F,
        FLUID_WIDTH = 0.5F,
        FLUID_HEIGHT_MAX = 0.75F;

    private void renderFluidContents(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float fillAmount, int color) {
        float height = FLUID_HEIGHT_MAX * fillAmount;
        TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FLUID_TEXTURE);

        RenderUtils.renderFace(Direction.UP, pose, normal, consumer, texture,
                FLUID_START_XZ, FLUID_START_XZ, FLUID_START_Y+height, FLUID_WIDTH, FLUID_WIDTH, color);

        RenderUtils.renderFace(Direction.DOWN, pose, normal, consumer, texture,
                FLUID_START_XZ, FLUID_START_XZ, 1.0f - FLUID_START_Y, FLUID_WIDTH, FLUID_WIDTH, color);

        RenderUtils.renderFace(Direction.NORTH, pose, normal, consumer, texture,
                FLUID_START_XZ, FLUID_START_Y, FLUID_START_XZ, FLUID_WIDTH, height, color);

        RenderUtils.renderFace(Direction.EAST, pose, normal, consumer, texture,
                FLUID_START_XZ, FLUID_START_Y, FLUID_START_XZ, FLUID_WIDTH, height, color);

        RenderUtils.renderFace(Direction.SOUTH, pose, normal, consumer, texture,
                FLUID_START_XZ, FLUID_START_Y, FLUID_START_XZ, FLUID_WIDTH, height, color);

        RenderUtils.renderFace(Direction.WEST, pose, normal, consumer, texture,
                FLUID_START_XZ, FLUID_START_Y, FLUID_START_XZ, FLUID_WIDTH, height, color);
    }
}
