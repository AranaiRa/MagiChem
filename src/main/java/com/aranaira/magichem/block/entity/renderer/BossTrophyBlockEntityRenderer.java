package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.BossTrophyBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.math.MathUtils;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Iterator;

public class BossTrophyBlockEntityRenderer implements BlockEntityRenderer<BossTrophyBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_SUMMER_1 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_summer_1");
    public static final ResourceLocation RENDERER_MODEL_SUMMER_2 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_summer_2");
    public static final ResourceLocation RENDERER_MODEL_SUMMER_3 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_summer_3");
    public static final ResourceLocation RENDERER_MODEL_WINTER_1 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_winter_1");
    public static final ResourceLocation RENDERER_MODEL_WINTER_2 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_winter_2");
    public static final ResourceLocation RENDERER_MODEL_WINTER_3 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_winter_3");
    public static final ResourceLocation RENDERER_MODEL_WATER = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_undead_water");
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

        pPoseStack.pushPose();
//        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_STEAM_VENTS, pPoseStack, pPackedLight, pPackedOverlay);
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
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SUMMER_1, pPoseStack, pPackedLight, pPackedOverlay, RenderType.cutout());
        else if((dayTime >= 800 && dayTime < 1600) || (dayTime >= 10400 && dayTime < 11200))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SUMMER_2, pPoseStack, pPackedLight, pPackedOverlay, RenderType.cutout());
        else if((dayTime >= 1600 && dayTime < 10400))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SUMMER_3, pPoseStack, pPackedLight, pPackedOverlay, RenderType.cutout());
        else if((dayTime >= 13600 && dayTime < 14400) || (dayTime >= 21600 && dayTime < 22400))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_WINTER_1, pPoseStack, pPackedLight, pPackedOverlay, RenderType.translucent());
        else if((dayTime >= 14400 && dayTime < 15200) || (dayTime >= 20800 && dayTime < 21600))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_WINTER_2, pPoseStack, pPackedLight, pPackedOverlay, RenderType.translucent());
        else if((dayTime >= 15200 && dayTime < 20800))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_WINTER_3, pPoseStack, pPackedLight, pPackedOverlay, RenderType.translucent());
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

        float h1 = 0.325f;
        float h2 = 0.325f + (float)(1 - Math.sin(phaseProgress * Math.PI)) * 0.05f;

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
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(RENDERER_MODEL_WATER);
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
