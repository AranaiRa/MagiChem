package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.CirclePowerBlockEntity;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.api.affinity.Affinity;
import com.mna.tools.math.Vector3;
import com.mna.tools.render.MARenderTypes;
import com.mna.tools.render.ModelUtils;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CirclePowerBlockEntityRenderer implements BlockEntityRenderer<CirclePowerBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_REAGENT_2 = new ResourceLocation(MagiChemMod.MODID, "obj/special/circle_power_reagent_2");
    public static final ResourceLocation RENDERER_MODEL_REAGENT_3 = new ResourceLocation(MagiChemMod.MODID, "obj/special/circle_power_reagent_3");
    public static final ResourceLocation CIRCLE_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/actuator_water");

    public static int
            REAGENT_1_ROTATION_PERIOD = 720,
            REAGENT_2_ROTATION_PERIOD = 480, REAGENT_2_BOB_PERIOD = 200,
            REAGENT_3_ROTATION_PERIOD = 960, REAGENT_3_BOB_PERIOD = 360;
    public static float
            REAGENT_2_BOB_HEIGHT = 0.0625f,
            REAGENT_3_BOB_HEIGHT = 0.0625f;

    public CirclePowerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    public static float getReagent1Rotation(Level pWorld, float pPartialTick) {
        return (float) ((((pWorld.getGameTime() + pPartialTick) % REAGENT_1_ROTATION_PERIOD) / (float) REAGENT_1_ROTATION_PERIOD) * Math.PI * 2);
    }

    public static float getReagent2Rotation(Level pWorld, float pPartialTick) {
        return (float) ((((pWorld.getGameTime() + pPartialTick) % REAGENT_2_ROTATION_PERIOD) / (float) REAGENT_2_ROTATION_PERIOD) * Math.PI * 2);
    }

    public static float getReagent3Rotation(Level pWorld, float pPartialTick) {
        return (float) ((((pWorld.getGameTime() + pPartialTick) % REAGENT_3_ROTATION_PERIOD) / (float) REAGENT_3_ROTATION_PERIOD) * Math.PI * 2);
    }

    public static double getReagent2BobHeight(Level pWorld, float pPartialTick) {
        double radians = (((pWorld.getGameTime() + pPartialTick) % REAGENT_2_BOB_PERIOD) / (double)REAGENT_2_BOB_PERIOD) * Math.PI * 2;
        return Math.sin(radians) * REAGENT_2_BOB_HEIGHT;
    }

    public static double getReagent3BobHeight(Level pWorld, float pPartialTick) {
        double radians = (((pWorld.getGameTime() + pPartialTick) % REAGENT_3_BOB_PERIOD) / (double)REAGENT_3_BOB_PERIOD) * Math.PI * 2;
        return Math.sin(radians) * REAGENT_3_BOB_HEIGHT;
    }

    @Override
    public void render(CirclePowerBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        //Grains of Quicksilver
        if(pBlockEntity.hasReagent(1)) {
            Vector3 center = Vector3.zero();// = new Vector3(0.5, 0.75, 0.5);
            final TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CIRCLE_TEXTURE);
            float fill = Math.min(1,Math.max(0,pBlockEntity.circleFillPercent + CirclePowerBlockEntity.CIRCLE_FILL_RATE * pPartialTick));

            int period = 720;
            float circleRot = (float)((((world.getGameTime() + pPartialTick) % period) / (float)period) * Math.PI * 2);

            pPoseStack.pushPose();

            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 0.75, 0.5);
            RenderUtils.generateMagicCircleRing(center,
                    12, 2.0f, 0.25f, circleRot, texture,
                    new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                    fill, pPoseStack, pBuffer, pPackedLight
            );

            pPoseStack.mulPose(Axis.XP.rotationDegrees(180));
            pPoseStack.mulPose(Axis.YP.rotationDegrees(240));
            RenderUtils.generateMagicCircleRing(center,
                    3, 1.725f, 0.25f, -circleRot, texture,
                    new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                    fill, pPoseStack, pBuffer, pPackedLight
            );
            pPoseStack.popPose();

            pPoseStack.popPose();
        }

        //Focusing Catalyst
        if(pBlockEntity.hasReagent(2)) {
            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 1.75 + getReagent2BobHeight(world, pPartialTick), 0.5);
            pPoseStack.mulPose(Axis.YP.rotation(getReagent2Rotation(world, pPartialTick)));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_REAGENT_2, pPoseStack, pPackedLight, pPackedOverlay);
            pPoseStack.popPose();
        }

        //Amplifying Prism
        if(pBlockEntity.hasReagent(3)) {
            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 3.25 + getReagent3BobHeight(world, pPartialTick), 0.5);
            pPoseStack.mulPose(Axis.YN.rotation(getReagent3Rotation(world, pPartialTick)));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_REAGENT_3, pPoseStack, pPackedLight, pPackedOverlay);
            pPoseStack.popPose();

            double v = 3.25 + getReagent3BobHeight(world, pPartialTick);

            //beam
            if(pBlockEntity.hasReagent(2)) {
                pPoseStack.pushPose();
                Vec3 start = Vec3.upFromBottomCenterOf(pBlockEntity.getBlockPos(), v);
                Vec3 end = Vec3.atCenterOf(pBlockEntity.getBlockPos()).subtract(0, 0.25, 0);

                pPoseStack.translate(0.5, v, 0.5);

                WorldRenderUtils.renderBeam(pBlockEntity.getLevel(), pPartialTick, pPoseStack, pBuffer, pPackedLight,
                        start, end, 1, new int[]{255, 255, 255}, 255, 0.0625f, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
                pPoseStack.popPose();
            }

            //radiance
            pPoseStack.pushPose();
            pPoseStack.translate(0.5, v, 0.5);
            WorldRenderUtils.renderRadiant((world.getGameTime() + pPartialTick), pPoseStack, pBuffer, Affinity.ARCANE.getSecondaryColor(), Affinity.ARCANE.getColor(), 128, 3, false);
            pPoseStack.popPose();
        }
    }
}
