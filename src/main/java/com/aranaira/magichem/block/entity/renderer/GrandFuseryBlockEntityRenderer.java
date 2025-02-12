package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.GrandFuseryBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.util.render.MateriaVesselContentsRenderUtil;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.math.Vector3;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec2;
import org.joml.Quaternionf;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.HAS_LABORATORY_UPGRADE;

public class GrandFuseryBlockEntityRenderer implements BlockEntityRenderer<GrandFuseryBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_PLUG_BASE = new ResourceLocation(MagiChemMod.MODID, "obj/special/grand_distillery_plug_base");
    public static final ResourceLocation RENDERER_MODEL_PLUG_UPGRADED = new ResourceLocation(MagiChemMod.MODID, "obj/special/grand_distillery_plug_upgraded");
    public static final ResourceLocation CIRCLE_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/actuator_water");
    public static final ResourceLocation SLURRY_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/fluid/experience_still");

    final TextureAtlasSprite circleTexture;

    public GrandFuseryBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        circleTexture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CIRCLE_TEXTURE);
    }

    @Override
    public void render(GrandFuseryBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderVariablePlugs(pBlockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        this.renderMagicCircle(pBlockEntity, pPoseStack, pBuffer, pPartialTick, pPackedLight);
        this.renderTankContents(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    private void renderTankContents(GrandFuseryBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {

        int color1 = 0, color2 = 0, color3 = 0, color4 = 0, color5 = 0;
        float fill1 = 0, fill2 = 0, fill3 = 0, fill4 = 0, fill5 = 0, fillS;
        boolean has1 = false, has2 = false, has3 = false, has4 = false, has5 = false;
        Direction facing = pBlockEntity.getBlockState().getValue(MagiChemBlockStateProperties.FACING);

        final SimpleContainer contentsOfInputSlots = pBlockEntity.getContentsOfInputSlots(GrandFuseryBlockEntity::getVar);
        if(!contentsOfInputSlots.getItem(0).isEmpty() || !contentsOfInputSlots.getItem(1).isEmpty()) {
            ItemStack stack = contentsOfInputSlots.getItem(0).isEmpty() ?
                    contentsOfInputSlots.getItem(1) :
                    contentsOfInputSlots.getItem(0);

            color1 = ((MateriaItem)stack.getItem()).getMateriaColor();
            fill1 = Math.min(1, (
                    (contentsOfInputSlots.getItem(0).isEmpty() ? 0 : contentsOfInputSlots.getItem(0).getCount()) +
                    (contentsOfInputSlots.getItem(1).isEmpty() ? 0 : contentsOfInputSlots.getItem(1).getCount())
            ) / 16f);
            has1 = true;
        }
        if(!contentsOfInputSlots.getItem(2).isEmpty() || !contentsOfInputSlots.getItem(3).isEmpty()) {
            ItemStack stack = contentsOfInputSlots.getItem(2).isEmpty() ?
                    contentsOfInputSlots.getItem(3) :
                    contentsOfInputSlots.getItem(2);

            color2 = ((MateriaItem)stack.getItem()).getMateriaColor();
            fill2 = Math.min(1, (
                    (contentsOfInputSlots.getItem(2).isEmpty() ? 0 : contentsOfInputSlots.getItem(2).getCount()) +
                    (contentsOfInputSlots.getItem(3).isEmpty() ? 0 : contentsOfInputSlots.getItem(3).getCount())
            ) / 16f);
            has2 = true;
        }
        if(!contentsOfInputSlots.getItem(4).isEmpty() || !contentsOfInputSlots.getItem(5).isEmpty()) {
            ItemStack stack = contentsOfInputSlots.getItem(4).isEmpty() ?
                    contentsOfInputSlots.getItem(5) :
                    contentsOfInputSlots.getItem(4);

            color3 = ((MateriaItem)stack.getItem()).getMateriaColor();
            fill3 = Math.min(1, (
                    (contentsOfInputSlots.getItem(4).isEmpty() ? 0 : contentsOfInputSlots.getItem(4).getCount()) +
                    (contentsOfInputSlots.getItem(5).isEmpty() ? 0 : contentsOfInputSlots.getItem(5).getCount())
            ) / 16f);
            has3 = true;
        }
        if(!contentsOfInputSlots.getItem(6).isEmpty() || !contentsOfInputSlots.getItem(7).isEmpty()) {
            ItemStack stack = contentsOfInputSlots.getItem(6).isEmpty() ?
                    contentsOfInputSlots.getItem(7) :
                    contentsOfInputSlots.getItem(6);

            color4 = ((MateriaItem)stack.getItem()).getMateriaColor();
            fill4 = Math.min(1, (
                    (contentsOfInputSlots.getItem(6).isEmpty() ? 0 : contentsOfInputSlots.getItem(6).getCount()) +
                    (contentsOfInputSlots.getItem(7).isEmpty() ? 0 : contentsOfInputSlots.getItem(7).getCount())
            ) / 16f);
            has4 = true;
        }
        if(!contentsOfInputSlots.getItem(8).isEmpty() || !contentsOfInputSlots.getItem(9).isEmpty()) {
            ItemStack stack = contentsOfInputSlots.getItem(8).isEmpty() ?
                    contentsOfInputSlots.getItem(9) :
                    contentsOfInputSlots.getItem(8);

            color5 = ((MateriaItem)stack.getItem()).getMateriaColor();
            fill5 = Math.min(1, (
                    (contentsOfInputSlots.getItem(8).isEmpty() ? 0 : contentsOfInputSlots.getItem(8).getCount()) +
                    (contentsOfInputSlots.getItem(9).isEmpty() ? 0 : contentsOfInputSlots.getItem(9).getCount())
            ) / 16f);
            has5 = true;
        }
        fillS = Math.min(1,Math.max(0,pBlockEntity.getFluidInTank(0).getAmount() / (float)ServerConfig.grandFuseryTankCapacity));

        pPoseStack.pushPose();

        //Slurry tank
        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 1.125, 0.5);
        MateriaVesselContentsRenderUtil.renderGrandFuserySlurryContents(pPoseStack.last().pose(), pPoseStack.last().normal(), pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS)), fillS, 0xffffffff, pPackedLight, SLURRY_TEXTURE);
        pPoseStack.popPose();

        if(facing == Direction.EAST) {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(270));
            pPoseStack.translate(0,0,-1);
        } else if(facing == Direction.SOUTH) {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
            pPoseStack.translate(-1,0,-1);
        } else if(facing == Direction.WEST) {
            pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
            pPoseStack.translate(-1,0,0);
        }

        pPoseStack.pushPose();
        pPoseStack.translate(-0.37525, 1.21875, 0.5);
        if(has4)
            MateriaVesselContentsRenderUtil.renderGrandFuseryMateriaContents(pPoseStack.last().pose(), pPoseStack.last().normal(), pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS)), fill4, color4, pPackedLight);
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.translate(-0.12525, 1.34375, -0.125);
        if(has2)
            MateriaVesselContentsRenderUtil.renderGrandFuseryMateriaContents(pPoseStack.last().pose(), pPoseStack.last().normal(), pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS)), fill2, color2, pPackedLight);
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 1.46875, -0.375);
        if(has1)
            MateriaVesselContentsRenderUtil.renderGrandFuseryMateriaContents(pPoseStack.last().pose(), pPoseStack.last().normal(), pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS)), fill1, color1, pPackedLight);
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.translate(1.12475, 1.34375, -0.125);
        if(has3)
            MateriaVesselContentsRenderUtil.renderGrandFuseryMateriaContents(pPoseStack.last().pose(), pPoseStack.last().normal(), pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS)), fill3, color3, pPackedLight);
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.translate(1.37475, 1.21875, 0.5);
        if(has5)
            MateriaVesselContentsRenderUtil.renderGrandFuseryMateriaContents(pPoseStack.last().pose(), pPoseStack.last().normal(), pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS)), fill5, color5, pPackedLight);
        pPoseStack.popPose();

        pPoseStack.popPose();
    }

    private void renderVariablePlugs(GrandFuseryBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        pPoseStack.pushPose();
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        if(dir == Direction.EAST) {
            pPoseStack.translate(1.0f, 0.0f, 0.0f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(270));
        } else if(dir == Direction.SOUTH) {
            pPoseStack.translate(1.0f, 0.0f, 1.0f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
        } else if(dir == Direction.WEST) {
            pPoseStack.translate(0.0f, 0.0f, 1.0f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
        }

        if(state.getValue(HAS_LABORATORY_UPGRADE))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_PLUG_UPGRADED, pPoseStack, pPackedLight, pPackedOverlay);
        else
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_PLUG_BASE, pPoseStack, pPackedLight, pPackedOverlay);

        pPoseStack.popPose();
    }

    private void renderMagicCircle(GrandFuseryBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, float pPartialTick, int pPackedLight) {
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

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, 0.02, 0)),
                3, 0.825f, 0.09375f, circleRotation + (float)(Math.PI / 2), circleTexture,
                new Vec2(0, 4.5f), new Vec2(12, 5.5f), 0.75f,
                pBlockEntity.circlePercent, pPoseStack, pBuffer, pPackedLight);

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, 0.01, 0)),
                3, 0.825f, 0.09375f, circleRotation + (float)(Math.PI * 3 / 2), circleTexture,
                new Vec2(0, 4.5f), new Vec2(12, 5.5f), 0.75f,
                pBlockEntity.circlePercent, pPoseStack, pBuffer, pPackedLight);

        pPoseStack.popPose();
        pPoseStack.popPose();
        pPoseStack.popPose();
    }
}
