package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.EldrinOrreryBlockEntity;
import com.aranaira.magichem.util.MathHelper;
import com.aranaira.magichem.util.render.ColorUtils;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import com.mna.tools.render.MARenderTypes;
import com.mna.tools.render.ModelUtils;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class EldrinOrreryBlockEntityRenderer implements BlockEntityRenderer<EldrinOrreryBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_ORB_PLANET = new ResourceLocation(MagiChemMod.MODID, "obj/special/eldrin_orrery_orb_planet");
    public static final ResourceLocation RENDERER_MODEL_ORB_MOON = new ResourceLocation(MagiChemMod.MODID, "obj/special/eldrin_orrery_orb_moon");
    public static final ResourceLocation RENDERER_MODEL_ORB_SUN = new ResourceLocation(MagiChemMod.MODID, "obj/special/eldrin_orrery_orb_sun");
    public static final ResourceLocation RENDERER_MODEL_RING_SMALL = new ResourceLocation(MagiChemMod.MODID, "obj/special/eldrin_orrery_ring_small");
    public static final ResourceLocation RENDERER_MODEL_RING_LARGE = new ResourceLocation(MagiChemMod.MODID, "obj/special/eldrin_orrery_ring_large");

    public static final Vec2[] WELLSPRING_STARTS = new Vec2[]{
            new Vec2(-0.625f, 0.5f),
            new Vec2(-0.0625f, -0.47428f),
            new Vec2(1.0625f, -0.47428f),
            new Vec2(1.625f, 0.5f),
            new Vec2(1.0625f, 1.47428f),
            new Vec2(-0.0625f, 1.47428f),
    };
    public static final int[][] WELLSPRING_COLORS = new int[][]{
            new int[]{255,  74, 244}, //ARCANE
            new int[]{ 29,  93, 189}, //WATER
            new int[]{117,  70,  15}, //EARTH
            new int[]{ 99,  29, 144}, //ENDER
            new int[]{178, 177, 158}, //AIR
            new int[]{229,  62,  29}  //FIRE
    };

    public EldrinOrreryBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EldrinOrreryBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        float sunPercent = pBlockEntity.sunPercent + pPartialTick * 0.0125f;
        float moonPercent = pBlockEntity.moonPercent + pPartialTick * 0.0125f;
        float innerRingActivation =
                pBlockEntity.innerRingActivation == 0 ? 0 :
                pBlockEntity.innerRingActivation < 1f ? MathHelper.doubleExponentialSigmoid(pBlockEntity.innerRingActivation + pPartialTick * 0.0125f, 0.5f) : 1f;
        float outerRingActivation =
                pBlockEntity.outerRingActivation == 0 ? 0 :
                pBlockEntity.outerRingActivation < 1f ? MathHelper.doubleExponentialSigmoid(pBlockEntity.outerRingActivation + pPartialTick * 0.0125f, 0.5f) : 1f;

        int period = 850;
        int gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
        float planetRot = -(((gt + pPartialTick) % period) / period) * (float)Math.PI * 2;

        //Planet
        {
            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 2.0, 0.5);
            pPoseStack.mulPose(Axis.YP.rotation(planetRot));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_ORB_PLANET, pPoseStack, pPackedLight, pPackedOverlay);
            pPoseStack.popPose();
        }

        //Inner Ring and Sun/Moon
        {
            period = 250;
            gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
            float ringRot = -(((gt + pPartialTick) % period) / period) * (float)Math.PI * 2;

            float zRotProg = MathUtils.lerpf(0, 36, innerRingActivation);
            float yRotProg = MathUtils.lerpf(0, ringRot, innerRingActivation);
            float yPosProg = MathUtils.lerpf(1.03125f, 2.0f, innerRingActivation);

            pPoseStack.pushPose();
            pPoseStack.translate(0.5, yPosProg, 0.5);

            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(zRotProg));
            pPoseStack.mulPose(Axis.YN.rotation(yRotProg));

            if(sunPercent > 0) {
                float scalar = sunPercent < 1f ? MathHelper.doubleExponentialSigmoid(sunPercent, 0.5f) : 1f;

                pPoseStack.pushPose();
                pPoseStack.translate(0.435859, 0.0, 0.0);
                pPoseStack.scale(scalar, scalar, scalar);
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_ORB_SUN, pPoseStack, pPackedLight, pPackedOverlay);
                pPoseStack.popPose();
            }

            if(moonPercent > 0) {
                float scalar = moonPercent < 1f ? MathHelper.doubleExponentialSigmoid(moonPercent, 0.5f) : 1f;

                pPoseStack.pushPose();
                pPoseStack.translate(-0.435859, 0.0, 0.0);
                pPoseStack.scale(scalar, scalar, scalar);
                ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_ORB_MOON, pPoseStack, pPackedLight, pPackedOverlay);
                pPoseStack.popPose();
            }

            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_RING_SMALL, pPoseStack, pPackedLight, pPackedOverlay);

            pPoseStack.popPose();
            pPoseStack.popPose();
        }

        //Inner Ring and Sun/Moon
        {
            period = 250;
            gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
            float ringYRot = -(((gt + pPartialTick) % period) / period) * (float)Math.PI * 2;

            float zRotProg = MathUtils.lerpf(0, -36, outerRingActivation);
            float yRotProg = MathUtils.lerpf(0, ringYRot, outerRingActivation);
            float yPosProg = MathUtils.lerpf(1.03125f, 2.0f, outerRingActivation);

            pPoseStack.pushPose();
            pPoseStack.translate(0.5, yPosProg, 0.5);

            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(zRotProg));
            pPoseStack.mulPose(Axis.YN.rotation(yRotProg));

            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_RING_LARGE, pPoseStack, pPackedLight, pPackedOverlay);

            pPoseStack.popPose();
            pPoseStack.popPose();
        }

        //Wellspring Beams
        for(int i=0; i<6; i++){
            Vec3 start = new Vec3(WELLSPRING_STARTS[i].x, 0.75f, WELLSPRING_STARTS[i].y);
            Vec3 end = new Vec3(WELLSPRING_STARTS[i].x, 3.5f, WELLSPRING_STARTS[i].y);

            float prog = pBlockEntity.wellspringPercent < 1f ? MathHelper.doubleExponentialSigmoid(pBlockEntity.wellspringPercent, 0.5f) : 1f;

            pPoseStack.pushPose();
            pPoseStack.translate(start.x, start.y, start.z);
            WorldRenderUtils.renderBeam(world, pPartialTick, pPoseStack, pBuffer, pPackedLight,
                    start, start.lerp(end, prog), 1.0f, WELLSPRING_COLORS[i], 128, 0.125f, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
            int[] centerColor = i == 3 ? new int[]{0, 0, 0} : new int[]{255, 255, 255};
            WorldRenderUtils.renderBeam(world, pPartialTick, pPoseStack, pBuffer, pPackedLight,
                    start, start.lerp(end, prog), 1.0f, centerColor, 255, 0.03125f, MARenderTypes.SPELL_BEAM_RENDER_TYPE);
            pPoseStack.popPose();
        }
    }
}
