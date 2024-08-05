package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.GrandDistilleryBlock;
import com.aranaira.magichem.block.entity.GrandDistilleryBlockEntity;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec2;
import org.joml.Quaternionf;

public class GrandDistilleryBlockEntityRenderer implements BlockEntityRenderer<GrandDistilleryBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_PLUG_BASE = new ResourceLocation(MagiChemMod.MODID, "obj/special/grand_distillery_plug_base");
    public static final ResourceLocation RENDERER_MODEL_PLUG_UPGRADED = new ResourceLocation(MagiChemMod.MODID, "obj/special/grand_distillery_plug_upgraded");
    public static final ResourceLocation CIRCLE_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/actuator_water");

    final TextureAtlasSprite circleTexture;

    public GrandDistilleryBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        circleTexture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CIRCLE_TEXTURE);
    }

    @Override
    public void render(GrandDistilleryBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderVariablePlugs(pBlockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        this.renderMagicCircle(pBlockEntity, pPoseStack, pBuffer, pPartialTick, pPackedLight);
    }

    private void renderVariablePlugs(GrandDistilleryBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
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

        if(state.getValue(GrandDistilleryBlock.HAS_LABORATORY_UPGRADE))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_PLUG_UPGRADED, pPoseStack, pPackedLight, pPackedOverlay);
        else
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_PLUG_BASE, pPoseStack, pPackedLight, pPackedOverlay);

        pPoseStack.popPose();
    }

    private void renderMagicCircle(GrandDistilleryBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, float pPartialTick, int pPackedLight) {
        Vector3 center = new Vector3(0, 0, 0);
        float circleRotation = -(((pBlockEntity.getLevel().getGameTime() + pPartialTick) % 400) / 400) * (float)Math.PI * 2;
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

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, 0.01, 0)),
                4, 0.85f, 0.09375f, circleRotation + (float)(Math.PI * 5 / 4), circleTexture,
                new Vec2(0, 4.5f), new Vec2(12, 5.5f), 0.75f,
                pBlockEntity.circlePercent, pPoseStack, pBuffer, pPackedLight);

        RenderUtils.generateMagicCircleRing(center.add(new Vector3(0, 0.02, 0)),
                4, 0.85f, 0.09375f, circleRotation, circleTexture,
                new Vec2(0, 4.5f), new Vec2(12, 5.5f), 0.75f,
                pBlockEntity.circlePercent, pPoseStack, pBuffer, pPackedLight);

        pPoseStack.popPose();
        pPoseStack.popPose();
        pPoseStack.popPose();
    }
}
