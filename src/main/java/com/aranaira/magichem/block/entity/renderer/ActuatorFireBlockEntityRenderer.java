package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ActuatorFireBlockEntity;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
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

import java.util.Random;

public class ActuatorFireBlockEntityRenderer implements BlockEntityRenderer<ActuatorFireBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_PIPE_LEFT = new ResourceLocation(MagiChemMod.MODID, "obj/special/actuator_fire_pipeleft");
    public static final ResourceLocation RENDERER_MODEL_PIPE_RIGHT = new ResourceLocation(MagiChemMod.MODID, "obj/special/actuator_fire_piperight");
    public static final ResourceLocation RENDERER_MODEL_PIPE_CENTER = new ResourceLocation(MagiChemMod.MODID, "obj/special/actuator_fire_pipecenter");

    public static final ResourceLocation TEXTURE_FIRE = new ResourceLocation("minecraft", "block/fire_0");
    public static final ResourceLocation TEXTURE_ACTUATOR = new ResourceLocation(MagiChemMod.MODID, "block/actuator_fire");

    public static final float
        WIGGLE_INTENSITY_SIDE = 0.025f, WIGGLE_INTENSITY_CENTER = 0.0125f;

    public static final Random random = new Random();

    public ActuatorFireBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(ActuatorFireBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderPipes(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        this.renderFireAndGrating(pBlockEntity, pPoseStack, pBuffer, pPackedLight);
    }

    private void renderFireAndGrating(ActuatorFireBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        BlockState state = pBlockEntity.getBlockState();
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction renderDir = dir;

        float
            y = 0.125f,
            fx = 0.375f, fz = 0.34375f,
            fw = 0.25f, fh = 0.3125f,
            fu1 = -1f, fu2 = -1f, fv1 = -1f, fv2 = -1f,
            g1x = 0.375f, g1z = 0.3125f, g2x = 0.5f, g2z = 0.3125f,
            gw = 0.125f, gh = 0.1875f,
            g1u1 = -1f, g1u2 = -1f, g1v1 = -1f, g1v2 = -1f,
            g2u1 = -1f, g2u2 = -1f, g2v1 = -1f, g2v2 = -1f;

        if(dir == Direction.NORTH) {
            fu1 = 0.75f;
            fu2 = 0.25f;
            fv1 = 0.625f;
            fv2 = 0.0f;
            g1u1 = 0.3203125f;
            g1u2 = 0.2578125f;
            g1v1 = 0.15625f;
            g1v2 = 0.0625f;
            g2u1 = 0.2578125f;
            g2u2 = 0.3203125f;
            g2v1 = 0.15625f;
            g2v2 = 0.0625f;
            renderDir = dir.getOpposite();
        } else if(dir == Direction.EAST) {
            fu1 = 0.75f;
            fu2 = 0.25f;
            fv1 = 0.625f;
            fv2 = 0.0f;
            g1u1 = 0.3203125f;
            g1u2 = 0.2578125f;
            g1v1 = 0.15625f;
            g1v2 = 0.0625f;
            g2u1 = 0.2578125f;
            g2u2 = 0.3203125f;
            g2v1 = 0.15625f;
            g2v2 = 0.0625f;
        } else if(dir == Direction.SOUTH) {
            fu1 = 0.25f;
            fu2 = 0.75f;
            fv1 = 0.0f;
            fv2 = 0.625f;
            g1u1 = 0.2578125f;
            g1u2 = 0.3203125f;
            g1v1 = 0.0625f;
            g1v2 = 0.15625f;
            g2u1 = 0.3203125f;
            g2u2 = 0.2578125f;
            g2v1 = 0.0625f;
            g2v2 = 0.15625f;
            renderDir = dir.getOpposite();
        } else if(dir == Direction.WEST) {
            fu1 = 0.25f;
            fu2 = 0.75f;
            fv1 = 0.0f;
            fv2 = 0.625f;
            g1u1 = 0.2578125f;
            g1u2 = 0.3203125f;
            g1v1 = 0.0625f;
            g1v2 = 0.15625f;
            g2u1 = 0.3203125f;
            g2u2 = 0.2578125f;
            g2v1 = 0.0625f;
            g2v2 = 0.15625f;
        }

        if(pBlockEntity.getPipeVibrationIntensity() > 0) {
            RenderUtils.renderFaceWithUV(renderDir, pPoseStack.last().pose(), pPoseStack.last().normal(),
                    pBuffer.getBuffer(RenderType.cutout()),
                    Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE_FIRE),
                    fx, y, fz, fw, fh, fu1, fu2, fv1, fv2, 0xFFFFFFFF, pPackedLight);
        }

        RenderUtils.renderFaceWithUV(renderDir, pPoseStack.last().pose(), pPoseStack.last().normal(),
                pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS)),
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE_ACTUATOR),
                g1x, y, g1z, gw, gh, g1u1, g1u2, g1v1, g1v2, 0xFFFFFFFF, pPackedLight);

        RenderUtils.renderFaceWithUV(renderDir, pPoseStack.last().pose(), pPoseStack.last().normal(),
                pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS)),
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE_ACTUATOR),
                g2x, y, g2z, gw, gh, g2u1, g2u2, g2v1, g2v2, 0xFFFFFFFF, pPackedLight);
    }

    private void renderPipes(ActuatorFireBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        pBlockEntity.handleAnimationDrivers();

        float mShift = random.nextFloat() * WIGGLE_INTENSITY_CENTER * pBlockEntity.getPipeVibrationIntensity();
        float sShift = random.nextFloat() * WIGGLE_INTENSITY_SIDE * pBlockEntity.getPipeVibrationIntensity();

        if(Minecraft.getInstance().isPaused()) {
            mShift = 0;
            sShift = 0;
        }

        pPoseStack.pushPose();
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if(dir == Direction.EAST) {
            pPoseStack.translate(1.0f, 0.0f, 0.0f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(270));
        } else if(dir == Direction.SOUTH) {
            pPoseStack.translate(1.0f, 0.0f, 1.0f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
        } else if(dir == Direction.WEST) {
            pPoseStack.translate(0.0f, 0.0f, 1.0f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
        }

        pPoseStack.translate(0.0f, 0.0f, sShift);
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_PIPE_LEFT, pPoseStack, pPackedLight, pPackedOverlay);
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_PIPE_RIGHT, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.translate(0.0f, 0.0f, -sShift);

        pPoseStack.translate(0.0f, 0.0f, mShift);
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_PIPE_CENTER, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.translate(0.0f, 0.0f, -mShift);

        pPoseStack.popPose();
    }
}
