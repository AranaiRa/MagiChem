package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ActuatorArcaneBlockEntity;
import com.aranaira.magichem.block.entity.ActuatorEarthBlockEntity;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.math.Vector3;
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
import net.minecraft.world.phys.Vec2;

public class ActuatorArcaneBlockEntityRenderer implements BlockEntityRenderer<ActuatorArcaneBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_CUBE_VAR1 = new ResourceLocation(MagiChemMod.MODID, "obj/special/actuator_arcane_cube_var1");
    public static final ResourceLocation RENDERER_MODEL_CUBE_VAR2 = new ResourceLocation(MagiChemMod.MODID, "obj/special/actuator_arcane_cube_var2");
    public static final ResourceLocation CIRCLE_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/actuator_water");

    final TextureAtlasSprite circleTexture;

    public ActuatorArcaneBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        circleTexture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CIRCLE_TEXTURE);
    }

    @Override
    public void render(ActuatorArcaneBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderCube(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        this.renderMagicCircles(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    private void renderCube(ActuatorArcaneBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        int SLIDE_PERIOD = 240;
        int BOB_PERIOD = 180;
        int SPIN_PERIOD = 300;
        float SLIDE_INTENSITY = 0.005f;
        float BOB_INTENSITY = 0.03125f;

        float CUBE_BUILDER_ROT_STEP = (float)Math.PI / 2f;
        float CUBE_BUILDER_ROT_XAXIS = (float)Math.PI / 4f; // 45 degrees
        float CUBE_BUILDER_ROT_ZAXIS = (float)Math.PI * (7f / 36f); //35 degrees
        float rotYAxis = (float)(Math.PI * 2f) * (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % (float)SPIN_PERIOD) / (float)SPIN_PERIOD);
        double slideMagnitude = ((double)(pBlockEntity.getLevel().getGameTime() + pPartialTick) % (double)SLIDE_PERIOD) / (double)SLIDE_PERIOD;
        double slideDist = Math.sin(slideMagnitude * Math.PI * 2) * SLIDE_INTENSITY;
        double bobMagnitude = ((double)(pBlockEntity.getLevel().getGameTime() + pPartialTick) % (double)BOB_PERIOD) / (double)BOB_PERIOD;
        double bobDist = Math.sin(bobMagnitude * Math.PI * 2) * BOB_INTENSITY;

        pPoseStack.pushPose();
        {
            pPoseStack.translate(0.5, 1.375 + bobDist, 0.5);
            pPoseStack.mulPose(Axis.YP.rotation(rotYAxis));

            pPoseStack.pushPose();
            {
                pPoseStack.mulPose(Axis.ZP.rotation(CUBE_BUILDER_ROT_ZAXIS));
                pPoseStack.mulPose(Axis.XP.rotation(CUBE_BUILDER_ROT_XAXIS));

                pPoseStack.pushPose();
                pPoseStack.translate(slideDist, -slideDist, slideDist);
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_CUBE_VAR1, pPoseStack, pPackedLight, pPackedOverlay);
                pPoseStack.popPose();

                pPoseStack.mulPose(Axis.YP.rotation(CUBE_BUILDER_ROT_STEP));

                pPoseStack.pushPose();
                pPoseStack.translate(slideDist, -slideDist, slideDist);
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_CUBE_VAR2, pPoseStack, pPackedLight, pPackedOverlay);
                pPoseStack.popPose();

                pPoseStack.mulPose(Axis.YP.rotation(CUBE_BUILDER_ROT_STEP));

                pPoseStack.pushPose();
                pPoseStack.translate(slideDist, -slideDist, slideDist);
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_CUBE_VAR1, pPoseStack, pPackedLight, pPackedOverlay);
                pPoseStack.popPose();

                pPoseStack.mulPose(Axis.YP.rotation(CUBE_BUILDER_ROT_STEP));

                pPoseStack.pushPose();
                pPoseStack.translate(slideDist, -slideDist, slideDist);
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_CUBE_VAR2, pPoseStack, pPackedLight, pPackedOverlay);
                pPoseStack.popPose();

                pPoseStack.mulPose(Axis.ZP.rotation(CUBE_BUILDER_ROT_STEP));

                pPoseStack.pushPose();
                pPoseStack.translate(slideDist, -slideDist, slideDist);
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_CUBE_VAR1, pPoseStack, pPackedLight, pPackedOverlay);
                pPoseStack.popPose();

                pPoseStack.mulPose(Axis.XN.rotation(CUBE_BUILDER_ROT_STEP));

                pPoseStack.pushPose();
                pPoseStack.translate(slideDist, -slideDist, slideDist);
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_CUBE_VAR2, pPoseStack, pPackedLight, pPackedOverlay);
                pPoseStack.popPose();

                pPoseStack.mulPose(Axis.XN.rotation(CUBE_BUILDER_ROT_STEP));

                pPoseStack.pushPose();
                pPoseStack.translate(slideDist, -slideDist, slideDist);
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_CUBE_VAR1, pPoseStack, pPackedLight, pPackedOverlay);
                pPoseStack.popPose();

                pPoseStack.mulPose(Axis.XN.rotation(CUBE_BUILDER_ROT_STEP));

                pPoseStack.pushPose();
                pPoseStack.translate(slideDist, -slideDist, slideDist);
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_CUBE_VAR2, pPoseStack, pPackedLight, pPackedOverlay);
                pPoseStack.popPose();
            }
            pPoseStack.popPose();
        }
        pPoseStack.popPose();
    }

    private void renderMagicCircles(ActuatorArcaneBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Vector3 center = Vector3.zero();
        int CIRCLE_SPIN_PERIOD = 400;
        int GLYPH_SPIN_PERIOD = 900;
        float circleRotation = -(((pBlockEntity.getLevel().getGameTime() + pPartialTick) % CIRCLE_SPIN_PERIOD) / CIRCLE_SPIN_PERIOD) * (float)Math.PI * 2;
        float CIRCLE_BUILDER_ROT_ZAXIS = (float)Math.PI * (1f / 12f);
        float rotYAxis = (float)(Math.PI * 2f) * (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % (float)GLYPH_SPIN_PERIOD) / (float)GLYPH_SPIN_PERIOD);

        pPoseStack.pushPose();
        {
            pPoseStack.translate(0.5, 1.0, 0.5);
            pPoseStack.scale(0.5f, 0.5f, 0.5f);

            RenderUtils.generateMagicCircleRing(center,
                    4, 0.75f, 0.125f, -circleRotation, circleTexture,
                    new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                    1, pPoseStack, pBuffer, pPackedLight);

            RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, -0.125, 0)),
                    4, 0.375f, 0.0625f, (circleRotation + (float)(Math.PI / 4)), circleTexture,
                    new Vec2(0, 4.5f), new Vec2(12, 5.5f), 0.75f,
                    1, pPoseStack, pBuffer, pPackedLight);

            pPoseStack.mulPose(Axis.YP.rotation(rotYAxis));
            pPoseStack.pushPose();
            {
                pPoseStack.translate(0.0, 0.75, 0.0);
                pPoseStack.mulPose(Axis.ZP.rotation(CIRCLE_BUILDER_ROT_ZAXIS));

                RenderUtils.generateMagicCircleRing(center,
                        7, 1.0f, 0.1875f, (circleRotation + (float)(Math.PI / 4)), circleTexture,
                        new Vec2(0, 3f), new Vec2(12, 4.5f), 0.75f,
                        1, pPoseStack, pBuffer, pPackedLight);

                pPoseStack.mulPose(Axis.ZN.rotation(CIRCLE_BUILDER_ROT_ZAXIS * 2));
                RenderUtils.generateMagicCircleRing(center,
                        7, 1.0f, 0.1875f, (circleRotation + (float)(Math.PI / 4)), circleTexture,
                        new Vec2(0, 3f), new Vec2(12, 4.5f), 0.75f,
                        1, pPoseStack, pBuffer, pPackedLight);
            }
            pPoseStack.popPose();
        }
        pPoseStack.popPose();
    }


}
