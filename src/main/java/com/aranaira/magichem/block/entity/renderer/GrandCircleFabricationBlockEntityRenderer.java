package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.GrandCircleFabricationBlockEntity;
import com.aranaira.magichem.util.render.ColorUtils;
import com.aranaira.magichem.util.render.MateriaVesselContentsRenderUtil;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.api.affinity.Affinity;
import com.mna.tools.math.Vector3;
import com.mna.tools.render.ModelUtils;
import com.mna.tools.render.WorldRenderUtils;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec2;

public class GrandCircleFabricationBlockEntityRenderer implements BlockEntityRenderer<GrandCircleFabricationBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_FLOATING_VESSEL = new ResourceLocation(MagiChemMod.MODID, "obj/special/grand_circle_fabrication_vessel");
    public static final ResourceLocation CIRCLE_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/actuator_water");

    public GrandCircleFabricationBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(GrandCircleFabricationBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        renderMagicCircle(pBlockEntity, pPartialTick, pPoseStack, pPackedLight, pBuffer, pPackedLight, pPackedOverlay);
        renderFloatingVessels(pBlockEntity, pPartialTick, pPoseStack, pPackedLight, pBuffer, pPackedLight, pPackedOverlay);
    }

    private void renderMagicCircle(GrandCircleFabricationBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, int pPackedLight, MultiBufferSource pBuffer, int pPackedLight1, int pPackedOverlay) {
        Vector3 center = new Vector3(0, 0, 0);
        final TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CIRCLE_TEXTURE);

        int period;
        float loopingTime;

        period = 750;
        loopingTime = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % period) / (float) period) * (float)(Math.PI * 2);
        float rayRot = loopingTime;

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0.9766, -0.2734);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(45));
        pPoseStack.mulPose(Axis.YN.rotation(rayRot));
        for(int i=0; i<9; i++) {
            WorldRenderUtils.renderLightBeam((pBlockEntity.getLevel().getGameTime() + pPartialTick), pPoseStack, pBuffer, ColorUtils.getRGBIntTint(ColorUtils.getDyeColorFromID(i)), ColorUtils.getRGBIntTint(ColorUtils.getDyeColorFromID(i)), 24, 5, .2f, 0, 0);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(360f / 9f));
        }
        pPoseStack.popPose();

        period = 600;
        loopingTime = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % period) / (float) period) * (float)(Math.PI * 2);
        float scale = (float)Math.sin(loopingTime * Math.PI * 2) * 0.0125f + 0.667f;

        period = 1000;
        loopingTime = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % period) / (float) period) * (float)(Math.PI * 2);
        float rotWobble = (float)Math.sin(loopingTime * Math.PI * 2) * 4.25f;

        period = 450;
        loopingTime = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % period) / (float) period) * (float)(Math.PI * 2);
        float pushWobble = (float)Math.sin(loopingTime * Math.PI * 2) * 0.03125f;

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 1.75, 0.5);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(45));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(90 + rotWobble));

        pPoseStack.translate(0, pushWobble, 0);

        pPoseStack.scale(scale, scale, scale);

        RenderUtils.generateMagicCircleRing(center,
                9, 1.125f, 0.125f, 0, texture,
                new Vec2(0, 6.5f), new Vec2(12, 4.5f), 0.75f,
                1, pPoseStack, pBuffer, pPackedLight
        );

        pPoseStack.pushPose();

        RenderUtils.generateMagicCircleRing(center,
                3, 1.0f, 0.125f, 0, texture,
                new Vec2(0, 4.5f), new Vec2(12, 6.5f), 0.75f,
                1, pPoseStack, pBuffer, pPackedLight
        );
        pPoseStack.popPose();

        pPoseStack.translate(-0.0875, 0, 0);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(45));

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, 0.001, 0)),
                4, 0.5375f, 0.09375f, 0, texture,
                new Vec2(0, 4.5f), new Vec2(12, 6.0f), 0.75f,
                1, pPoseStack, pBuffer, pPackedLight
        );

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, 0.002, 0)),
                8, 0.325f, 0.09375f, 0, texture,
                new Vec2(0, 4.5f), new Vec2(12, 6.0f), 0.75f,
                1, pPoseStack, pBuffer, pPackedLight
        );

        pPoseStack.popPose();
    }

    private void renderFloatingVessels(GrandCircleFabricationBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, int pPackedLight, MultiBufferSource pBuffer, int pPackedLight1, int pPackedOverlay) {

        int period;
        float loopingTime;

        period = 670;
        loopingTime = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % period) / (float) period) * 360;
        float vesselRot = loopingTime;

        period = 445;
        loopingTime = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % period) / (float) period) * (float)(Math.PI * 2);
        double vesselBob = Math.sin(loopingTime) * 0.03125;

        pPoseStack.pushPose();

        pPoseStack.pushPose();
        pPoseStack.translate(-0.625, 1.75 + vesselBob, 0.5);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(vesselRot));
        ModelUtils.renderModel(pBuffer, pBlockEntity.getLevel(), pBlockEntity.getBlockPos(), pBlockEntity.getBlockState(), RENDERER_MODEL_FLOATING_VESSEL, pPoseStack, pPackedLight, pPackedOverlay);
        MateriaVesselContentsRenderUtil.renderGrandCircleFabricationFluidContents(pPoseStack.last().pose(), pPoseStack.last().normal(), pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS)), 1, 0xffff0000, pPackedLight);
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.translate(-0.295495, 2.375 - vesselBob, -0.295495);
        pPoseStack.mulPose(Axis.YN.rotationDegrees(vesselRot));
        ModelUtils.renderModel(pBuffer, pBlockEntity.getLevel(), pBlockEntity.getBlockPos(), pBlockEntity.getBlockState(), RENDERER_MODEL_FLOATING_VESSEL, pPoseStack, pPackedLight, pPackedOverlay);
        MateriaVesselContentsRenderUtil.renderGrandCircleFabricationFluidContents(pPoseStack.last().pose(), pPoseStack.last().normal(), pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS)), 1, 0xffffff00, pPackedLight);
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 2.625 + vesselBob, -0.625);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(vesselRot));
        ModelUtils.renderModel(pBuffer, pBlockEntity.getLevel(), pBlockEntity.getBlockPos(), pBlockEntity.getBlockState(), RENDERER_MODEL_FLOATING_VESSEL, pPoseStack, pPackedLight, pPackedOverlay);
        MateriaVesselContentsRenderUtil.renderGrandCircleFabricationFluidContents(pPoseStack.last().pose(), pPoseStack.last().normal(), pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS)), 1, 0xff00ff55, pPackedLight);
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.translate(1.295495, 2.375 - vesselBob, -0.295495);
        pPoseStack.mulPose(Axis.YN.rotationDegrees(vesselRot));
        ModelUtils.renderModel(pBuffer, pBlockEntity.getLevel(), pBlockEntity.getBlockPos(), pBlockEntity.getBlockState(), RENDERER_MODEL_FLOATING_VESSEL, pPoseStack, pPackedLight, pPackedOverlay);
        MateriaVesselContentsRenderUtil.renderGrandCircleFabricationFluidContents(pPoseStack.last().pose(), pPoseStack.last().normal(), pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS)), 1, 0xff0055ff, pPackedLight);
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.translate(1.625, 1.75 + vesselBob, 0.5);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(vesselRot));
        ModelUtils.renderModel(pBuffer, pBlockEntity.getLevel(), pBlockEntity.getBlockPos(), pBlockEntity.getBlockState(), RENDERER_MODEL_FLOATING_VESSEL, pPoseStack, pPackedLight, pPackedOverlay);
        MateriaVesselContentsRenderUtil.renderGrandCircleFabricationFluidContents(pPoseStack.last().pose(), pPoseStack.last().normal(), pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS)), 1, 0xff5500ff, pPackedLight);
        pPoseStack.popPose();

        pPoseStack.popPose();
    }
}
