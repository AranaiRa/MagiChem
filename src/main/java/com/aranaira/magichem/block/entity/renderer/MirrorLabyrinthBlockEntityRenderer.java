package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.GrandCentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.MirrorLabyrinthBlockEntity;
import com.aranaira.magichem.util.MathHelper;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.math.MathUtils;
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

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.HAS_LABORATORY_UPGRADE;

public class MirrorLabyrinthBlockEntityRenderer implements BlockEntityRenderer<MirrorLabyrinthBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_MIRROR = new ResourceLocation(MagiChemMod.MODID, "obj/special/mirror_labyrinth_mirror");
    public static final ResourceLocation RENDERER_MODEL_MATRIX = new ResourceLocation(MagiChemMod.MODID, "obj/special/mirror_labyrinth_matrix");
    public static final ResourceLocation CIRCLE_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/actuator_water");

    final TextureAtlasSprite circleTexture;

    public MirrorLabyrinthBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        circleTexture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CIRCLE_TEXTURE);
    }

    @Override
    public void render(MirrorLabyrinthBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderMirrors(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        this.renderConstruct(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        this.renderMatrix(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        this.renderMagicCircle(pBlockEntity, pPoseStack, pBuffer, pPartialTick, pPackedLight);
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
