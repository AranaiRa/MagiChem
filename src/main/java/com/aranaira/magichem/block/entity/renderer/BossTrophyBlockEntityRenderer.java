package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.BossTrophyBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.registry.BlockRegistry;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2d;

import java.util.Iterator;

public class BossTrophyBlockEntityRenderer implements BlockEntityRenderer<BossTrophyBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_COUNCIL_CRYSTAL_INNER = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_council_crystal_inner");
    public static final ResourceLocation RENDERER_MODEL_COUNCIL_CRYSTAL_OUTER = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_council_crystal_outer");
    public static final ResourceLocation RENDERER_MODEL_COUNCIL_SLATE = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_council_slate");
    public static final ResourceLocation RENDERER_MODEL_FEY_SUMMER_1 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_summer_1");
    public static final ResourceLocation RENDERER_MODEL_FEY_SUMMER_2 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_summer_2");
    public static final ResourceLocation RENDERER_MODEL_FEY_SUMMER_3 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_summer_3");
    public static final ResourceLocation RENDERER_MODEL_FEY_WINTER_1 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_winter_1");
    public static final ResourceLocation RENDERER_MODEL_FEY_WINTER_2 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_winter_2");
    public static final ResourceLocation RENDERER_MODEL_FEY_WINTER_3 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_winter_3");
    public static final ResourceLocation RENDERER_MODEL_UNDEAD_WATER = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_undead_water");
    private static final ResourceLocation TEXTURE_FEY = new ResourceLocation(MagiChemMod.MODID, "block/boss_trophy_fey");
    private static final RandomSource rSource = RandomSource.create(81234L);

    public BossTrophyBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(BossTrophyBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if(pBlockEntity.getBlockState().getBlock() == BlockRegistry.BOSS_TROPHY_COUNCIL.get()) this.renderCouncil(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        else if(pBlockEntity.getBlockState().getBlock() == BlockRegistry.BOSS_TROPHY_FEY.get()) this.renderFey(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        else if(pBlockEntity.getBlockState().getBlock() == BlockRegistry.BOSS_TROPHY_UNDEAD.get()) this.renderUndead(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    private void renderCouncil(BossTrophyBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        VertexConsumer buffer = pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        PoseStack.Pose last = pPoseStack.last();

        int gt, period;
        float rotYAxis, bob;

        pPoseStack.pushPose();

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0.5, 0.5);
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_COUNCIL_CRYSTAL_INNER, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();

        period = 400;
        gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
        rotYAxis = ((((float)gt + pPartialTick) % (float)period) / (float)period) * (float)Math.PI * 2;
        period = 250;
        gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
        bob = (float)Math.sin(((((float)gt + pPartialTick) % (float)period) / (float)period) * (float)Math.PI * 2) * 0.015625f;

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0.96875 - bob, 0.5);
        pPoseStack.mulPose(Axis.YN.rotation(rotYAxis));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_COUNCIL_CRYSTAL_OUTER, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0.03125 + bob, 0.5);
        pPoseStack.mulPose(Axis.YP.rotation(rotYAxis));
        pPoseStack.mulPose(Axis.XP.rotationDegrees(180));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_COUNCIL_CRYSTAL_OUTER, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();

        period = 375;
        gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
        bob = (float)Math.sin(((((float)gt + pPartialTick) % (float)period) / (float)period) * (float)Math.PI * 2) * 0.0625f;

        LocalPlayer player = Minecraft.getInstance().player;
        double pX = player.getX();
        double pZ = player.getZ();
        double eX = pBlockEntity.getBlockPos().getX() + 0.5625;
        double eZ = pBlockEntity.getBlockPos().getZ() + 0.5;
        Vector2d dVec = new Vector2d(pX - eX, pZ - eZ);

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0.5 + bob, 0.5);
        pPoseStack.mulPose(Axis.YN.rotation((float)Math.atan2(dVec.y, dVec.x) - (float)(Math.PI / 2)));
        pPoseStack.translate(-0.425, 0, 0.25);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(45));
        pPoseStack.mulPose(Axis.XP.rotationDegrees(60));
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(15));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_COUNCIL_SLATE, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();

        pPoseStack.popPose();
    }

    private void renderFey(BossTrophyBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        VertexConsumer buffer = pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        PoseStack.Pose last = pPoseStack.last();

        //Summer and Winter effects
        long dayTime = world.getDayTime() % 24000;
        boolean isNight = dayTime >= 12800 && dayTime < 23200;

        pPoseStack.pushPose();
        if((dayTime >= 0 && dayTime < 800) || (dayTime >= 11200 && dayTime < 12000))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_FEY_SUMMER_1, pPoseStack, pPackedLight, pPackedOverlay, RenderType.cutout());
        else if((dayTime >= 800 && dayTime < 1600) || (dayTime >= 10400 && dayTime < 11200))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_FEY_SUMMER_2, pPoseStack, pPackedLight, pPackedOverlay, RenderType.cutout());
        else if((dayTime >= 1600 && dayTime < 10400))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_FEY_SUMMER_3, pPoseStack, pPackedLight, pPackedOverlay, RenderType.cutout());
        else if((dayTime >= 13600 && dayTime < 14400) || (dayTime >= 21600 && dayTime < 22400))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_FEY_WINTER_1, pPoseStack, pPackedLight, pPackedOverlay, RenderType.translucent());
        else if((dayTime >= 14400 && dayTime < 15200) || (dayTime >= 20800 && dayTime < 21600))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_FEY_WINTER_2, pPoseStack, pPackedLight, pPackedOverlay, RenderType.translucent());
        else if((dayTime >= 15200 && dayTime < 20800))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_FEY_WINTER_3, pPoseStack, pPackedLight, pPackedOverlay, RenderType.translucent());
        pPoseStack.popPose();

        //Gnomon shadow
        double phaseProgress = isNight ? (double)(dayTime - 12800) / 10400d : (double)((dayTime + 800) % 24000) / 13600d;

        double fTheta = (phaseProgress * Math.PI) + Math.PI; //Forward vector
        double fX = Math.cos(fTheta);
        double fZ = Math.sin(fTheta);

        double pTheta = fTheta + (Math.PI / 2); //Perpendicular vector
        double pX = Math.cos(pTheta);
        double pZ = Math.sin(pTheta);

        double bRadius = 0.03125; //Base width
        double tRadius = 0.00391; //Tip width
        double lRadius = 0.28125 + (1 - Math.sin(phaseProgress * Math.PI)) * 0.125; //Length

        float h1 = 0.3125f;
        float h2 = 0.3150f + (float)(1 - Math.sin(phaseProgress * Math.PI)) * 0.050f;

        pPoseStack.pushPose();
        {
            VertexConsumer vertexBuilder = pBuffer.getBuffer(RenderType.translucent());
            Matrix4f renderMatrix = pPoseStack.last().pose();
            Matrix3f normalMatrix = pPoseStack.last().normal();
            TextureAtlasSprite resolvedTexture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE_FEY);
            int tint = 50;
            int alpha = 196;

            vertexBuilder.vertex(renderMatrix, (float)(0.5 + (pX * bRadius)), h1, (float)(0.5 + (pZ * bRadius))).color(tint,tint,tint,alpha).uv(resolvedTexture.getU(1), resolvedTexture.getV(0)).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(normalMatrix, 0, 0, 0).endVertex();
            vertexBuilder.vertex(renderMatrix, (float)(0.5 + (fX * lRadius) + (pX * tRadius)), h2, (float)(0.5 + (fZ * lRadius) + (pZ * tRadius))).color(tint,tint,tint,alpha).uv(resolvedTexture.getU(1), resolvedTexture.getV(0.0625)).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(normalMatrix, 0, 0, 0).endVertex();
            vertexBuilder.vertex(renderMatrix, (float)(0.5 + (fX * lRadius) + (pX * -tRadius)), h2, (float)(0.5 + (fZ * lRadius) + (pZ * -tRadius))).color(tint,tint,tint,alpha).uv(resolvedTexture.getU(0.9375), resolvedTexture.getV(0.0625)).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(normalMatrix, 0, 0, 0).endVertex();
            vertexBuilder.vertex(renderMatrix, (float)(0.5 + (pX * -bRadius)), h1, (float)(0.5 + (pZ * -bRadius))).color(tint,tint,tint,alpha).uv(resolvedTexture.getU(0.9375), resolvedTexture.getV(0)).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(normalMatrix, 0, 0, 0).endVertex();
        }
        pPoseStack.popPose();
    }

    private void renderUndead(BossTrophyBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        VertexConsumer buffer = pBuffer.getBuffer(RenderType.translucent());
        PoseStack.Pose last = pPoseStack.last();
        Direction facing = pBlockEntity.getBlockState().getValue(MagiChemBlockStateProperties.FACING);

        pPoseStack.pushPose();

        switch (facing) {
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

        {
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(RENDERER_MODEL_UNDEAD_WATER);
            ModelData worldModelData = world.getModelDataManager().getAt(pos);
            ModelData data = model.getModelData(world, pos, state, worldModelData == null ? ModelData.EMPTY : worldModelData);
            Iterator var13 = model.getQuads(state, (Direction)null, rSource, data, (RenderType)null).iterator();

            while(var13.hasNext()) {
                BakedQuad quad = (BakedQuad)var13.next();
                buffer.putBulkData(pPoseStack.last(), quad, 0.15F, 0.1F, 0.2F, 1.0F, pPackedLight, pPackedOverlay, true);
            }
        }
        pPoseStack.popPose();
    }
}
