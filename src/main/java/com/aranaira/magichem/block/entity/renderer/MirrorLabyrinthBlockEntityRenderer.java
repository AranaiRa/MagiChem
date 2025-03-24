package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.GrandCentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.MirrorLabyrinthBlockEntity;
import com.aranaira.magichem.util.MathHelper;
import com.aranaira.magichem.util.render.ConstructRenderHelper;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec2;
import org.joml.Quaternionf;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.HAS_LABORATORY_UPGRADE;

public class MirrorLabyrinthBlockEntityRenderer implements BlockEntityRenderer<MirrorLabyrinthBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_MIRROR = new ResourceLocation(MagiChemMod.MODID, "obj/special/mirror_labyrinth_mirror");
    public static final ResourceLocation RENDERER_MODEL_MATRIX = new ResourceLocation(MagiChemMod.MODID, "obj/special/mirror_labyrinth_matrix");
    public static final ResourceLocation CIRCLE_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/actuator_water");

    private static final CompoundTag DEFAULT_CONSTRUCT = new CompoundTag();

    final TextureAtlasSprite circleTexture;

    public MirrorLabyrinthBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        circleTexture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CIRCLE_TEXTURE);

        if(DEFAULT_CONSTRUCT.size() == 0) {
            DEFAULT_CONSTRUCT.putString("HEAD", "mna:constructs/construct_smart_head_stone");
            DEFAULT_CONSTRUCT.putString("TORSO", "mna:constructs/construct_basic_torso_stone");
            DEFAULT_CONSTRUCT.putString("LEFT_ARM", "mna:constructs/construct_caster_arm_left_stone");
            DEFAULT_CONSTRUCT.putString("RIGHT_ARM", "mna:constructs/construct_grabber_arm_right_stone");
            DEFAULT_CONSTRUCT.putString("LEGS", "mna:constructs/construct_basic_legs_stone");
        }
    }

    @Override
    public void render(MirrorLabyrinthBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if(pBlockEntity.constructDataChanged || pBlockEntity.renderData.size() == 0) {
            if(pBlockEntity.hasConstruct()) {
                pBlockEntity.renderData = ConstructRenderHelper.getRenderDataFromTag(pBlockEntity.getStoredConstructComposition());
                pBlockEntity.constructDataChanged = false;
                ConstructRenderHelper.replaceEyesInRenderData(pBlockEntity.renderData, ConstructRenderHelper.ConstructMoods.NEUTRAL);
            } else {
                pBlockEntity.renderData.clear();
                pBlockEntity.constructDataChanged = false;
            }
        }

        this.renderMirrors(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        this.renderConstruct(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        this.renderMatrix(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
//        this.renderMagicCircle(pBlockEntity, pPoseStack, pBuffer, pPartialTick, pPackedLight);
    }

    private void renderMirrors(MirrorLabyrinthBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        int period = 540;
        int gt = (int)(pBlockEntity.getLevel().getGameTime() % (int)(period * 2));
        float bob = (float)Math.sin((float)((gt + pPartialTick) % period) / (float)period * Math.PI * 2) * 0.125f;

        period = 380;
        gt = (int)(pBlockEntity.getLevel().getGameTime() % (int)(period * 2));
        float zWobble = (float)Math.sin((float)((gt + pPartialTick) % period) / (float)period * Math.PI * 2) * 2.0f;

        period = 460;
        gt = (int)(pBlockEntity.getLevel().getGameTime() % (int)(period * 2));
        float xWobble = (float)Math.sin((float)((gt + pPartialTick) % period) / (float)period * Math.PI * 2) * 0.75f;

        pPoseStack.pushPose();

        if(dir == Direction.NORTH) {
            pPoseStack.translate(0.5, 1.5, -0.5);
            pPoseStack.mulPose(Axis.YN.rotationDegrees(180+45));
        } else if(dir == Direction.EAST) {
            pPoseStack.translate(1.5, 1.5, 0.5);
            pPoseStack.mulPose(Axis.YN.rotationDegrees(270+45));
        } else if(dir == Direction.SOUTH) {
            pPoseStack.translate(0.5, 1.5, 1.5);
            pPoseStack.mulPose(Axis.YN.rotationDegrees(45));
        } else if(dir == Direction.WEST) {
            pPoseStack.translate(-0.5, 1.5, 0.5);
            pPoseStack.mulPose(Axis.YN.rotationDegrees(90+45));
        }

        float activationPercent = MathUtils.clamp(pBlockEntity.mirrorActivationPercent + (pPartialTick * pBlockEntity.mirrorActivationSpeed), 0, 1);
        activationPercent = MathHelper.doubleExponentialSigmoid(activationPercent, 0.607f);

        for(int i=0; i<7; i++) {
            pPoseStack.pushPose();
            float xRot = MathUtils.lerpf(
                    30,
                    i % 2 == 1 ? xWobble : -xWobble,
                    activationPercent);
            float zRot = MathUtils.lerpf(
                    0,
                    i % 2 == 1 ? zWobble : -zWobble,
                    activationPercent);
            float yRot = 45 * i;
            double yShift = MathUtils.lerpf(
                    -1.53125f,
                    (i % 2 == 1) ? bob : -bob,
                    activationPercent);
            double zShift = MathUtils.lerpf(
                    -2.225f,
                    -2.0f,
                    activationPercent);


            pPoseStack.mulPose(Axis.YN.rotationDegrees(yRot));
            pPoseStack.translate(0, yShift, zShift);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(xRot));
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(zRot));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_MIRROR, pPoseStack, pPackedLight, pPackedOverlay);
            pPoseStack.popPose();
        }

        pPoseStack.popPose();
    }

    private void renderConstruct(MirrorLabyrinthBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        pPoseStack.pushPose();

        if(dir == Direction.NORTH) {
            pPoseStack.translate(0.5, 1.375, -0.5);
            pPoseStack.mulPose(Axis.YN.rotationDegrees(180));
        } else if(dir == Direction.EAST) {
            pPoseStack.translate(1.5, 1.375, 0.5);
            pPoseStack.mulPose(Axis.YN.rotationDegrees(270));
        } else if(dir == Direction.SOUTH) {
            pPoseStack.translate(0.5, 1.375, 1.5);
        } else if(dir == Direction.WEST) {
            pPoseStack.translate(-0.5, 1.375, 0.5);
            pPoseStack.mulPose(Axis.YN.rotationDegrees(90));
        }

        if(pBlockEntity.renderData.size() > 0) {
            Pair<ResourceLocation, Vector3> currentPiece;
            int period = 360;
            int gt = (int)(pBlockEntity.getLevel().getGameTime() % (int)(period * 2));
            float bob = (float)Math.sin((float)((gt + pPartialTick) % period) / (float)period * Math.PI * 2) * 0.125f;
            pPoseStack.translate(0, bob, 0);

            double spin = ConstructRenderHelper.mappedSinusoidalAngle(world.getGameTime(), pPartialTick, 376, 13, -65, 65);
            pPoseStack.mulPose(Axis.YP.rotationDegrees((float)spin));

            period = 12220;
            gt = (int)(pBlockEntity.getLevel().getGameTime() % (int)(period * 2));
            float circleRot = (float)(((gt + pPartialTick) % period) / (float)period) * 360f;

            currentPiece = pBlockEntity.renderData.get(ConstructRenderHelper.ConstructPartType.TORSO);
            double torsoRot = ConstructRenderHelper.mappedSinusoidalAngle(world.getGameTime(), pPartialTick, 260, 0, -3, 3);

            pPoseStack.pushPose();
            {
                pPoseStack.translate(currentPiece.getSecond().x, currentPiece.getSecond().y, currentPiece.getSecond().z);
                pPoseStack.rotateAround(Axis.XP.rotationDegrees((float) torsoRot), 0, -currentPiece.getSecond().y * 2 - 0.25f, 0);
                pPoseStack.translate(-currentPiece.getSecond().x, -currentPiece.getSecond().y, -currentPiece.getSecond().z);
                ModelUtils.renderModel(pBuffer, world, pos, state, currentPiece.getFirst(), pPoseStack, pPackedLight, pPackedOverlay);

                currentPiece = pBlockEntity.renderData.get(ConstructRenderHelper.ConstructPartType.HEAD);
                double headRot = ConstructRenderHelper.mappedSinusoidalAngle(world.getGameTime(), pPartialTick, 286, 13, -60, 60);

                pPoseStack.pushPose();
                {
                    pPoseStack.translate(currentPiece.getSecond().x, currentPiece.getSecond().y, currentPiece.getSecond().z);
                    pPoseStack.rotateAround(Axis.YP.rotationDegrees((float) headRot), 0, -currentPiece.getSecond().y * 2 - 0.25f, 0);

                    pPoseStack.translate(-currentPiece.getSecond().x, -currentPiece.getSecond().y, -currentPiece.getSecond().z);
                    ModelUtils.renderModel(pBuffer, world, pos, state, currentPiece.getFirst(), pPoseStack, pPackedLight, pPackedOverlay);
                    ModelUtils.renderModel(pBuffer, world, pos, state, pBlockEntity.renderData.get(ConstructRenderHelper.ConstructPartType.EYES).getFirst(), pPoseStack, pPackedLight, pPackedOverlay);
                    pPoseStack.popPose();
                }

                currentPiece = pBlockEntity.renderData.get(ConstructRenderHelper.ConstructPartType.ARM_LEFT);
                boolean isCasterArm = ConstructRenderHelper.isCasterArm(currentPiece.getFirst());
                double casterArmRotX = ConstructRenderHelper.mappedSinusoidalAngle(world.getGameTime(), pPartialTick, 118, 0, 65, 75);
                double casterArmRotZ = ConstructRenderHelper.mappedSinusoidalAngle(world.getGameTime(), pPartialTick, 74, 0, -10, 10);
                double nonCasterArmRotX = ConstructRenderHelper.mappedSinusoidalAngle(world.getGameTime(), pPartialTick, 96, 0, 45, 60);
                double nonCasterArmRotZ = ConstructRenderHelper.mappedSinusoidalAngle(world.getGameTime(), pPartialTick, 54, 0, -3, 3);

                pPoseStack.pushPose();
                {
                    pPoseStack.translate(currentPiece.getSecond().x, currentPiece.getSecond().y, currentPiece.getSecond().z);
                    if(isCasterArm) {
                        pPoseStack.rotateAround(Axis.XP.rotationDegrees((float)casterArmRotX), -currentPiece.getSecond().x, -currentPiece.getSecond().y * 2, -currentPiece.getSecond().z * 2);
                        pPoseStack.rotateAround(Axis.ZP.rotationDegrees((float)casterArmRotZ), -currentPiece.getSecond().x, -currentPiece.getSecond().y * 2, -currentPiece.getSecond().z * 2);
                    } else {
                        pPoseStack.rotateAround(Axis.XP.rotationDegrees((float)nonCasterArmRotX), -currentPiece.getSecond().x, -currentPiece.getSecond().y * 2, -currentPiece.getSecond().z * 2);
                        pPoseStack.rotateAround(Axis.ZP.rotationDegrees((float)nonCasterArmRotZ), -currentPiece.getSecond().x, -currentPiece.getSecond().y * 2, -currentPiece.getSecond().z * 2);

                        pPoseStack.pushPose();
                        pPoseStack.scale(0.5f, 0.5f, 0.5f);
                        pPoseStack.translate(-1.825, 3.0, -0.0625);
                        pPoseStack.mulPose(Axis.XP.rotationDegrees(180));
                        RenderUtils.generateMagicCircleRing(Vector3.zero(),
                                7, 0.75f, 0.375f, -circleRot, circleTexture,
                                new Vec2(0, 0), new Vec2(12, 3f), 0.75f,
                                pBlockEntity.circlePercent, pPoseStack, pBuffer, pPackedLight);
                        pPoseStack.translate(0, -0.01, 0);
                        pPoseStack.popPose();
                    }

                    pPoseStack.translate(-currentPiece.getSecond().x, -currentPiece.getSecond().y, -currentPiece.getSecond().z);
                    ModelUtils.renderModel(pBuffer, world, pos, state, currentPiece.getFirst(), pPoseStack, pPackedLight, pPackedOverlay);
                    pPoseStack.popPose();
                }

                currentPiece = pBlockEntity.renderData.get(ConstructRenderHelper.ConstructPartType.ARM_RIGHT);
                isCasterArm = ConstructRenderHelper.isCasterArm(currentPiece.getFirst());

                pPoseStack.pushPose();
                {
                    pPoseStack.translate(currentPiece.getSecond().x, currentPiece.getSecond().y, currentPiece.getSecond().z);
                    if(isCasterArm) {
                        pPoseStack.rotateAround(Axis.XP.rotationDegrees((float)casterArmRotX), -currentPiece.getSecond().x, -currentPiece.getSecond().y * 2, -currentPiece.getSecond().z * 2);
                        pPoseStack.rotateAround(Axis.ZP.rotationDegrees((float)casterArmRotZ), -currentPiece.getSecond().x, -currentPiece.getSecond().y * 2, -currentPiece.getSecond().z * 2);
                    } else {
                        pPoseStack.rotateAround(Axis.XP.rotationDegrees((float)nonCasterArmRotX), -currentPiece.getSecond().x, -currentPiece.getSecond().y * 2, -currentPiece.getSecond().z * 2);
                        pPoseStack.rotateAround(Axis.ZP.rotationDegrees((float)nonCasterArmRotZ), -currentPiece.getSecond().x, -currentPiece.getSecond().y * 2, -currentPiece.getSecond().z * 2);

                        pPoseStack.pushPose();
                        pPoseStack.scale(0.5f, 0.5f, 0.5f);
                        pPoseStack.translate(1.825, 3.0, -0.0625);
                        pPoseStack.mulPose(Axis.XP.rotationDegrees(180));
                        RenderUtils.generateMagicCircleRing(Vector3.zero(),
                                7, 0.75f, 0.375f, -circleRot, circleTexture,
                                new Vec2(0, 0), new Vec2(12, 3f), 0.75f,
                                pBlockEntity.circlePercent, pPoseStack, pBuffer, pPackedLight);
                        pPoseStack.translate(0, -0.01, 0);
                        pPoseStack.popPose();
                    }

                    pPoseStack.translate(-currentPiece.getSecond().x, -currentPiece.getSecond().y, -currentPiece.getSecond().z);
                    ModelUtils.renderModel(pBuffer, world, pos, state, currentPiece.getFirst(), pPoseStack, pPackedLight, pPackedOverlay);
                    pPoseStack.popPose();
                }

                pPoseStack.popPose();
            }

            double legPeriod = 144;
            currentPiece = pBlockEntity.renderData.get(ConstructRenderHelper.ConstructPartType.LEG_LEFT);
            double legRot = ConstructRenderHelper.mappedSinusoidalAngle(world.getGameTime(), pPartialTick, legPeriod, legPeriod * 0.5, 350, 360);

            pPoseStack.pushPose();
            {
                pPoseStack.translate(currentPiece.getSecond().x, currentPiece.getSecond().y, currentPiece.getSecond().z);
                pPoseStack.rotateAround(Axis.XP.rotationDegrees((float) legRot), -currentPiece.getSecond().x, -currentPiece.getSecond().y * 2, -currentPiece.getSecond().z * 2);

                pPoseStack.translate(-currentPiece.getSecond().x, -currentPiece.getSecond().y, -currentPiece.getSecond().z);
                ModelUtils.renderModel(pBuffer, world, pos, state, currentPiece.getFirst(), pPoseStack, pPackedLight, pPackedOverlay);
                pPoseStack.popPose();
            }

            currentPiece = pBlockEntity.renderData.get(ConstructRenderHelper.ConstructPartType.LEG_RIGHT);
            legRot = ConstructRenderHelper.mappedSinusoidalAngle(world.getGameTime(), pPartialTick, legPeriod, legPeriod * 0.5, 360, 370);

            pPoseStack.pushPose();
            {
                pPoseStack.translate(currentPiece.getSecond().x, currentPiece.getSecond().y, currentPiece.getSecond().z);
                pPoseStack.rotateAround(Axis.XN.rotationDegrees((float) legRot), -currentPiece.getSecond().x, -currentPiece.getSecond().y * 2, -currentPiece.getSecond().z * 2);

                pPoseStack.translate(-currentPiece.getSecond().x, -currentPiece.getSecond().y, -currentPiece.getSecond().z);
                ModelUtils.renderModel(pBuffer, world, pos, state, currentPiece.getFirst(), pPoseStack, pPackedLight, pPackedOverlay);
                pPoseStack.popPose();
            }
        }

        pPoseStack.popPose();
    }

    private void renderMatrix(MirrorLabyrinthBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        float activationPercent = MathUtils.clamp(pBlockEntity.matrixActivationPercent + (pPartialTick * pBlockEntity.matrixActivationSpeed), 0, 1);
        activationPercent = MathHelper.doubleExponentialSigmoid(activationPercent, 0.607f);

        int period = 720;
        int gt = (int)(pBlockEntity.getLevel().getGameTime() % (int)(period * 2));
        float rotY = MathUtils.lerpf(
                0f,
                (((float)(gt + pPartialTick) % period) / (float)period) * 360f,
                activationPercent);
        float yShift = MathUtils.lerpf(
                -3.1875f,
                0f,
                activationPercent);

        pPoseStack.pushPose();

        if(dir == Direction.NORTH) {
            pPoseStack.translate(0.5, 3.75, -0.5);
        } else if(dir == Direction.EAST) {
            pPoseStack.translate(1.5, 3.75, 0.5);
        } else if(dir == Direction.SOUTH) {
            pPoseStack.translate(0.5, 3.75, 1.5);
        } else if(dir == Direction.WEST) {
            pPoseStack.translate(-0.5, 3.75, 0.5);
        }

        pPoseStack.mulPose(Axis.YN.rotationDegrees(rotY));
        pPoseStack.translate(0,yShift, 0);

        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_MATRIX, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();
    }

    private void renderMagicCircle(MirrorLabyrinthBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, float pPartialTick, int pPackedLight) {
        Vector3 center = new Vector3(0, 0, 0);

        int period = 400;
        int gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
        float circleRotation = -((float)((gt + pPartialTick) % period) / (float)period) * (float)Math.PI * 2;
        Direction facing = pBlockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

        Vector3 translation = Vector3.zero();
        Quaternionf axisRotation = Axis.ZN.rotation(1);

        if(facing == Direction.NORTH) {
            translation = new Vector3(0.5, 1.25, 1.5);
            axisRotation = Axis.XP.rotation(0.261799f);
        } else if(facing == Direction.EAST) {
            translation = new Vector3(-0.5, 1.25, 0.5);
            axisRotation = Axis.ZP.rotation(0.261799f);
        } else if(facing == Direction.SOUTH) {
            translation = new Vector3(0.5, 1.25, -0.5);
            axisRotation = Axis.XN.rotation(0.261799f);
        } else if(facing == Direction.WEST) {
            translation = new Vector3(1.5, 1.25, 0.5);
            axisRotation = Axis.ZN.rotation(0.261799f);
        }

        pPoseStack.pushPose();
        pPoseStack.translate(translation.x, translation.y, translation.z);
        pPoseStack.pushPose();
        pPoseStack.scale(0.5f, 0.5f, 0.5f);
        pPoseStack.pushPose();
        pPoseStack.mulPose(axisRotation);

        RenderUtils.generateMagicCircleRing(center,
                7, 1.25f, 0.375f, -circleRotation, circleTexture,
                new Vec2(0, 0), new Vec2(12, 3f), 0.75f,
                pBlockEntity.circlePercent, pPoseStack, pBuffer, pPackedLight);

        pPoseStack.pushPose();

        pPoseStack.mulPose(Axis.YP.rotation(-circleRotation));

        pPoseStack.pushPose();
        pPoseStack.translate(0.265, 0, 0);
        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, 0.01, 0)),
                3, 0.625f, 0.09375f, (float)Math.PI, circleTexture,
                new Vec2(0, 4.5f), new Vec2(12, 5.5f), 0.75f,
                pBlockEntity.circlePercent, pPoseStack, pBuffer, pPackedLight);
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.mulPose(Axis.YP.rotationDegrees(120));
        pPoseStack.translate(0.265, -0.01, 0);
        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, 0.01, 0)),
                3, 0.625f, 0.09375f, (float)Math.PI, circleTexture,
                new Vec2(0, 4.5f), new Vec2(12, 5.5f), 0.75f,
                pBlockEntity.circlePercent, pPoseStack, pBuffer, pPackedLight);
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.mulPose(Axis.YP.rotationDegrees(240));
        pPoseStack.translate(0.265, 0.01, 0);
        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, 0.01, 0)),
                3, 0.625f, 0.09375f, (float)Math.PI, circleTexture,
                new Vec2(0, 4.5f), new Vec2(12, 5.5f), 0.75f,
                pBlockEntity.circlePercent, pPoseStack, pBuffer, pPackedLight);
        pPoseStack.popPose();

        pPoseStack.popPose();

        pPoseStack.popPose();
        pPoseStack.popPose();
        pPoseStack.popPose();
    }
}
