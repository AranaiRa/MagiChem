package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ActuatorEarthBlockEntity;
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

public class ActuatorEarthBlockEntityRenderer implements BlockEntityRenderer<ActuatorEarthBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_STAMPER = new ResourceLocation(MagiChemMod.MODID, "obj/special/actuator_earth_stamper");
    private static final ResourceLocation TEXTURE_SAND = new ResourceLocation("minecraft", "block/sand");

    public ActuatorEarthBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(ActuatorEarthBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderStamper(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        this.renderSandPile(pBlockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    private void renderStamper(ActuatorEarthBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        VertexConsumer buffer = pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS));

        pPoseStack.pushPose();
        PoseStack.Pose last = pPoseStack.last();

        switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> {
                //pPoseStack.mulPose(Axis.YP.rotationDegrees(pBlockEntity.wheelAngle + pPartialTick * (pBlockEntity.wheelSpeed)));
            }
            case EAST -> {
                pPoseStack.translate(1.0f, 0.0f, 0.0f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(270));
            }
            case SOUTH -> {
                pPoseStack.translate(1.0f, 0.0f, 1.0f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
            }
            case WEST -> {
                pPoseStack.translate(0.0f, 0.0f, 1.0f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
            }
        }

        pPoseStack.pushPose();
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_STAMPER, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();

        pPoseStack.popPose();
    }

    private void renderSandPile(ActuatorEarthBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        VertexConsumer buffer = pBuffer.getBuffer(RenderType.translucent());
        PoseStack.Pose last = pPoseStack.last();

        TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE_SAND);

        float width = 0.5f;
        float height = 0.5f + 0.15625f * (pBlockEntity.getSandPercent() / 100.0f);

        pPoseStack.pushPose();
        RenderUtils.renderFace(Direction.UP, last.pose(), last.normal(), buffer, texture,
                0.25f, 0.25f, height, width, width,  0xffffffff, pPackedLight);
        pPoseStack.popPose();
    }


}
