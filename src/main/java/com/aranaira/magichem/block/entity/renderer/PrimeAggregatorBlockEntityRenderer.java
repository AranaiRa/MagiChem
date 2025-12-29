package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.PrimeAggregatorBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.util.MathHelper;
import com.aranaira.magichem.util.render.ColorUtils;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.math.Vector3;
import com.mna.tools.render.MARenderTypes;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import static com.aranaira.magichem.block.entity.PrimeAggregatorBlockEntity.*;

public class PrimeAggregatorBlockEntityRenderer implements BlockEntityRenderer<PrimeAggregatorBlockEntity> {
    public static final ResourceLocation CIRCLE_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/actuator_water");
    public static final int PERIOD = 200;

    public PrimeAggregatorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PrimeAggregatorBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();
        Vector3[] crystalPositions = CRYSTAL_POSITIONS.get(state.getValue(MagiChemBlockStateProperties.FACING));
        final int[] WHITE = new int[]{255, 255, 255};

        if(pBlockEntity.getAnimStage() == ANIM_STAGE_IDLE) {
            int period = 500;
            int gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
            float rot = ((float)((gt + pPartialTick) % period) / (float)period) * 360f;

            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 1.815, 0.5);
            pPoseStack.scale(0.4f, 0.4f, 0.4f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(rot));
            Minecraft.getInstance().getItemRenderer().renderStatic(pBlockEntity.getFirstOutputItem(), ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);
            pPoseStack.popPose();
        } else if(pBlockEntity.getAnimStage() == ANIM_STAGE_GATHERING_ELDRIN) {
            Pair<Integer, Integer> eldrinData = pBlockEntity.getEldrin();
            //int color = ColorUtils.getLerpedRainbowColor((float)((pBlockEntity.getLevel().getGameTime() % 120f) + pPartialTick) / 120f);

            float progThroughPhase = (float) eldrinData.getFirst() / (float)eldrinData.getSecond();
            Vec3 orbPos = new Vec3(pos.getX() + 0.5, pos.getY() + 3.0, pos.getZ() + 0.5);

            for(int i=0; i<6; i++) {
                Vec3 crystalPos = new Vec3(pos.getX() + crystalPositions[i].x, pos.getY() + crystalPositions[i].y, pos.getZ() + crystalPositions[i].z);
                int color = ColorUtils.getLerpedRainbowColor((float)(((pBlockEntity.getLevel().getGameTime() + 20 * i) % 120f) + pPartialTick) / 120f);

                pPoseStack.pushPose();
                pPoseStack.translate(crystalPositions[i].x, crystalPositions[i].y, crystalPositions[i].z);
                WorldRenderUtils.renderBeam(world, pPartialTick, pPoseStack, pBuffer, pPackedLight,
                        crystalPos, orbPos, 1.0f, ColorUtils.getRGBAIntTintFromPackedInt(color), 128, 0.06f * progThroughPhase, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
                WorldRenderUtils.renderBeam(world, pPartialTick, pPoseStack, pBuffer, pPackedLight,
                        crystalPos, orbPos, 1.0f, WHITE, 255, 0.04f * progThroughPhase, MARenderTypes.SPELL_BEAM_RENDER_TYPE);
                pPoseStack.popPose();
            }
        } else if(pBlockEntity.getAnimStage() == ANIM_STAGE_TO_SLURRY) {
            float progThroughPhase = (float)pBlockEntity.getProgress() / (float)TO_SLURRY_DURATION;
            Vec3 orbPos = new Vec3(pos.getX() + 0.5, pos.getY() + 1.8125 + MathHelper.doubleExponentialSeat(1 - progThroughPhase, 3) * 1.1875, pos.getZ() + 0.5);

            for(int i=0; i<6; i++) {
                Vec3 crystalPos = new Vec3(pos.getX() + crystalPositions[i].x, pos.getY() + crystalPositions[i].y, pos.getZ() + crystalPositions[i].z);
                int color = ColorUtils.getLerpedRainbowColor((float)(((pBlockEntity.getLevel().getGameTime() + 20 * i) % 120f) + pPartialTick) / 120f);

                pPoseStack.pushPose();
                pPoseStack.translate(crystalPositions[i].x, crystalPositions[i].y, crystalPositions[i].z);
                WorldRenderUtils.renderBeam(world, pPartialTick, pPoseStack, pBuffer, pPackedLight,
                        crystalPos, orbPos, 1.0f, ColorUtils.getRGBAIntTintFromPackedInt(color), 128, 0.03f + 0.03f * (1 - progThroughPhase), MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
                WorldRenderUtils.renderBeam(world, pPartialTick, pPoseStack, pBuffer, pPackedLight,
                        crystalPos, orbPos, 1.0f, WHITE, 255, 0.02f + 0.02f * (1 - progThroughPhase), MARenderTypes.SPELL_BEAM_RENDER_TYPE);
                pPoseStack.popPose();
            }
        } else if(pBlockEntity.getAnimStage() == ANIM_STAGE_GATHERING_SLURRY) {
            float progThroughPhase = (float)pBlockEntity.getSlurry().getFirst() / (float)pBlockEntity.getSlurry().getSecond();
            Vec3 orbPos = new Vec3(pos.getX() + 0.5, pos.getY() + 1.8125, pos.getZ() + 0.5);

            //ELDRIN BEAMS
            for(int i=0; i<6; i++) {
                Vec3 crystalPos = new Vec3(pos.getX() + crystalPositions[i].x, pos.getY() + crystalPositions[i].y, pos.getZ() + crystalPositions[i].z);
                int color = ColorUtils.getLerpedRainbowColor((float)(((pBlockEntity.getLevel().getGameTime() + 20 * i) % 120f) + pPartialTick) / 120f);

                pPoseStack.pushPose();
                pPoseStack.translate(crystalPositions[i].x, crystalPositions[i].y, crystalPositions[i].z);
                WorldRenderUtils.renderBeam(world, pPartialTick, pPoseStack, pBuffer, pPackedLight,
                        crystalPos, orbPos, 1.0f, ColorUtils.getRGBAIntTintFromPackedInt(color), 128, 0.03f, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
                WorldRenderUtils.renderBeam(world, pPartialTick, pPoseStack, pBuffer, pPackedLight,
                        crystalPos, orbPos, 1.0f, WHITE, 255, 0.02f, MARenderTypes.SPELL_BEAM_RENDER_TYPE);
                pPoseStack.popPose();
            }

            //MAGIC CIRCLE
            final TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CIRCLE_TEXTURE);
            Direction facing = state.getValue(MagiChemBlockStateProperties.FACING);

            int gt = (int)(pBlockEntity.getLevel().getGameTime() % (PERIOD * 2));
            float rotation = -(((gt + pPartialTick) % PERIOD) / PERIOD) * (float)Math.PI * 2;

            float latterProg = Math.min(1, Math.max(0, (progThroughPhase - 0.5f) / 0.5f));
            float formerProg = Math.min(1, progThroughPhase / 0.5f);

            renderCircles(pPoseStack, pBuffer, pPackedLight, texture, facing, rotation, latterProg, formerProg);
        } else if(pBlockEntity.getAnimStage() == ANIM_STAGE_CRAFTING) {
            float progThroughPhase = (float)pBlockEntity.getProgress() / (float)CRAFTING_DURATION;
            Vec3 orbPos = new Vec3(pos.getX() + 0.5, pos.getY() + 1.8125, pos.getZ() + 0.5);
            float beamLength = 1f - Math.min(1,progThroughPhase / 0.2f);
            float circleFormerPercent = 1f - Math.max(0,Math.min(1,(progThroughPhase - 0.6f) / 0.3f));
            float circleLatterPercent = 1f - Math.max(0,Math.min(1,(progThroughPhase - 0.3f) / 0.3f));

            //ELDRIN BEAMS
            for(int i=0; i<6; i++) {
                Vec3 crystalPos = new Vec3(pos.getX() + crystalPositions[i].x, pos.getY() + crystalPositions[i].y, pos.getZ() + crystalPositions[i].z);
                int color = ColorUtils.getLerpedRainbowColor((float)(((pBlockEntity.getLevel().getGameTime() + 20 * i) % 120f) + pPartialTick) / 120f);

                pPoseStack.pushPose();
                pPoseStack.translate(crystalPositions[i].x, crystalPositions[i].y, crystalPositions[i].z);
                WorldRenderUtils.renderBeam(world, pPartialTick, pPoseStack, pBuffer, pPackedLight,
                        crystalPos, orbPos, beamLength, ColorUtils.getRGBAIntTintFromPackedInt(color), 128, 0.03f, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
                WorldRenderUtils.renderBeam(world, pPartialTick, pPoseStack, pBuffer, pPackedLight,
                        crystalPos, orbPos, beamLength, WHITE, 255, 0.02f, MARenderTypes.SPELL_BEAM_RENDER_TYPE);
                pPoseStack.popPose();
            }

            //MAGIC CIRCLE
            final TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CIRCLE_TEXTURE);
            Direction facing = state.getValue(MagiChemBlockStateProperties.FACING);

            int gt = (int)(pBlockEntity.getLevel().getGameTime() % (PERIOD * 2));
            float rotation = -(((gt + pPartialTick) % PERIOD) / PERIOD) * (float)Math.PI * 2;

            renderCircles(pPoseStack, pBuffer, pPackedLight, texture, facing, rotation, circleLatterPercent, circleFormerPercent);
        }
    }

    private void renderCircles(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, TextureAtlasSprite texture, Direction facing, float rotation, float latterProg, float formerProg) {
        pPoseStack.pushPose();
        if(facing == Direction.NORTH) {
            pPoseStack.translate(0.5, 1.8125, 0.5);
        } else if(facing == Direction.EAST) {
            pPoseStack.mulPose(Axis.YN.rotationDegrees(90));
            pPoseStack.translate(0.5, 1.8125, -0.5);
        } else if(facing == Direction.SOUTH) {
            pPoseStack.mulPose(Axis.YN.rotationDegrees(180));
            pPoseStack.translate(-0.5, 1.8125, -0.5);
        } else if(facing == Direction.WEST) {
            pPoseStack.mulPose(Axis.YN.rotationDegrees(270));
            pPoseStack.translate(-0.5, 1.8125, 0.5);
        }

        pPoseStack.mulPose(Axis.XP.rotationDegrees(60));
        pPoseStack.scale(0.5f, 0.5f, 0.5f);

        RenderUtils.generateMagicCircleRing(Vector3.zero(),
                3, 1.5f, 0.375f, rotation, texture,
                new Vec2(0, 0), new Vec2(12, 3f), 0.75f,
                formerProg, pPoseStack, pBuffer, pPackedLight);


        RenderUtils.generateMagicCircleRing(Vector3.zero(),
                12, 1.625f, 0.125f, rotation, texture,
                new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                latterProg, pPoseStack, pBuffer, pPackedLight);

        pPoseStack.mulPose(Axis.YN.rotationDegrees(180));
        pPoseStack.translate(0.0, 0.25, 0.0);

        RenderUtils.generateMagicCircleRing(Vector3.zero(),
                3, 1.375f, 0.125f, rotation, texture,
                new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                formerProg, pPoseStack, pBuffer, pPackedLight);

        pPoseStack.translate(0.0, -0.5, 0.0);

        RenderUtils.generateMagicCircleRing(Vector3.zero(),
                3, 1.375f, 0.125f, rotation, texture,
                new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                formerProg, pPoseStack, pBuffer, pPackedLight);

        pPoseStack.translate(0.0, 1.0, 0.0);

        RenderUtils.generateMagicCircleRing(Vector3.zero(),
                6, 0.625f, 0.125f, rotation, texture,
                new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                latterProg, pPoseStack, pBuffer, pPackedLight);

        pPoseStack.popPose();
    }
}
