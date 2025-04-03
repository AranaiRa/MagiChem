package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ActuatorArcaneBlockEntity;
import com.aranaira.magichem.block.entity.ActuatorEnderBlockEntity;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.math.Vector3;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
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
import org.joml.Quaternionf;

public class ActuatorEnderBlockEntityRenderer implements BlockEntityRenderer<ActuatorEnderBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_LOCATOR = new ResourceLocation(MagiChemMod.MODID, "obj/special/actuator_ender_locator");
    public static final ResourceLocation CIRCLE_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/actuator_water");

    final TextureAtlasSprite circleTexture;

    public ActuatorEnderBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        circleTexture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CIRCLE_TEXTURE);
    }

    @Override
    public void render(ActuatorEnderBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        int period = 215;
        int gt = (int)((pBlockEntity.getLevel().getGameTime()) % (period * 2));
        float swivelY = (float)Math.sin(((double)(gt + pPartialTick) / (double)period) * Math.PI * 2) * 25f;
        period = 124;
        gt = (int)((pBlockEntity.getLevel().getGameTime()) % (period * 2));
        float swivelX = (float)Math.sin(((double)(gt + pPartialTick) / (double)period) * Math.PI * 2) * 5f;
        period = 188;
        gt = (int)((pBlockEntity.getLevel().getGameTime()) % (period * 2));
        float swivelZ = (float)Math.sin(((double)(gt + pPartialTick) / (double)period) * Math.PI * 2) * 5f;

        int baseRot = 0;
        final Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if(facing == Direction.EAST) baseRot = -90;
        else if(facing == Direction.SOUTH) baseRot = 180;
        else if(facing == Direction.WEST) baseRot = 90;

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 1.5, 0.5);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(baseRot + swivelY)); //TODO: Figure out aim vector towards mirror
        pPoseStack.mulPose(Axis.XP.rotationDegrees(swivelX));
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(swivelZ));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_LOCATOR, pPoseStack, pPackedLight, pPackedOverlay);

        //internal magic circle
        {
            Vector3 center = new Vector3(0, 0, 0);

            period = 400;
            gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
            float circleRotation = -((float)((gt + pPartialTick) % period) / (float)period) * (float)Math.PI * 2;

            Vector3 translation = Vector3.zero();

            pPoseStack.pushPose();
            pPoseStack.translate(translation.x, translation.y, translation.z);
            pPoseStack.pushPose();
            pPoseStack.scale(0.5f, 0.5f, 0.5f);
            pPoseStack.pushPose();

            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.ZN.rotationDegrees(90));
            RenderUtils.generateMagicCircleRing(center,
                    6, 0.5f, 0.125f, -circleRotation, circleTexture,
                    new Vec2(0, 6.5f), new Vec2(12, 4.5f), 0.75f,
                    1, pPoseStack, pBuffer, pPackedLight);
            pPoseStack.popPose();

            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.ZN.rotationDegrees(-90));
            RenderUtils.generateMagicCircleRing(center,
                    4, 0.3f, 0.0625f, -circleRotation*4, circleTexture,
                    new Vec2(0, 4.5f), new Vec2(12, 5.5f), 0.75f,
                    1, pPoseStack, pBuffer, pPackedLight);
            pPoseStack.popPose();

            pPoseStack.popPose();
            pPoseStack.popPose();
            pPoseStack.popPose();
        }

        pPoseStack.popPose();

        //base magic circle
        {
            Vector3 center = Vector3.zero();
            int CIRCLE_SPIN_PERIOD = 400;
            int GLYPH_SPIN_PERIOD = 900;

            gt = (int)(pBlockEntity.getLevel().getGameTime() % (CIRCLE_SPIN_PERIOD * 2));
            float circleRotation = -(((gt + pPartialTick) % CIRCLE_SPIN_PERIOD) / CIRCLE_SPIN_PERIOD) * (float)Math.PI * 2;
            float CIRCLE_BUILDER_ROT_ZAXIS = (float)Math.PI * (1f / 12f);

            gt = (int)(pBlockEntity.getLevel().getGameTime() % (GLYPH_SPIN_PERIOD * 2));
            float rotYAxis = (float)(Math.PI * 2f) * (((gt + pPartialTick) % (float)GLYPH_SPIN_PERIOD) / (float)GLYPH_SPIN_PERIOD);

            pPoseStack.pushPose();
            {
                pPoseStack.translate(0.5, 1.0, 0.5);
                pPoseStack.scale(0.5f, 0.5f, 0.5f);

                RenderUtils.generateMagicCircleRing(center,
                        4, 0.75f, 0.125f, circleRotation, circleTexture,
                        new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                        1, pPoseStack, pBuffer, pPackedLight);

                RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, -0.125, 0)),
                        4, 0.375f, 0.0625f, (-circleRotation + (float)(Math.PI / 4)), circleTexture,
                        new Vec2(0, 4.5f), new Vec2(12, 5.5f), 0.75f,
                        1, pPoseStack, pBuffer, pPackedLight);
            }
            pPoseStack.popPose();
        }
    }
}
