package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.DistilleryBlockEntity;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Random;

public class DistilleryBlockEntityRenderer implements BlockEntityRenderer<DistilleryBlockEntity> {
    public static final ResourceLocation TEXTURE_FIRE = new ResourceLocation("minecraft", "block/fire_0");
    public static final ResourceLocation TEXTURE_DISTILLERY = new ResourceLocation(MagiChemMod.MODID, "block/distillery");

    public DistilleryBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(DistilleryBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderFireAndGrating(pBlockEntity, pPoseStack, pBuffer, pPackedLight);
    }

    private void renderFireAndGrating(DistilleryBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        BlockState state = pBlockEntity.getBlockState();
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction renderDir = dir;

        float
            y = 0.125f,
            fx = -0.25f, fz = 0.15625f,
            fw = 0.5f, fh = 0.3125f,
            fu1 = -1f, fu2 = -1f, fv1 = -1f, fv2 = -1f,
            g1x = -0.25f, g1z = 0.125f, g2x = 0.0f, g2z = 0.125f,
            gw = 0.25f, gh = 0.21875f,
            g1u1 = -1f, g1u2 = -1f, g1v1 = -1f, g1v2 = -1f,
            g2u1 = -1f, g2u2 = -1f, g2v1 = -1f, g2v2 = -1f;

        if(dir == Direction.NORTH) {
            fu1 = 1.0f;
            fu2 = 0.0f;
            fv1 = 0.875f;
            fv2 = 0.25f;
            g1u1 = 0.3671875f;
            g1u2 = 0.4921875f;
            g1v1 = 0.21875f;
            g1v2 = 0.109375f;
            g2u1 = 0.4921875f;
            g2u2 = 0.3671875f;
            g2v1 = 0.21875f;
            g2v2 = 0.109375f;
            renderDir = dir.getOpposite();
        } else if(dir == Direction.EAST) {
            fu1 = 1.0f;
            fu2 = 0.0f;
            fv1 = 0.875f;
            fv2 = 0.25f;
            g1u1 = 0.3671875f;
            g1u2 = 0.4921875f;
            g1v1 = 0.21875f;
            g1v2 = 0.109375f;
            g2u1 = 0.4921875f;
            g2u2 = 0.3671875f;
            g2v1 = 0.21875f;
            g2v2 = 0.109375f;
        } else if(dir == Direction.SOUTH) {
            fx = 0.75f;
            fu1 = 0.0f;
            fu2 = 1.0f;
            fv1 = 0.25f;
            fv2 = 0.875f;
            g1x = 1.0f;
            g2x = 0.75f;
            g1u1 = 0.4921875f;
            g1u2 = 0.3671875f;
            g1v1 = 0.109375f;
            g1v2 = 0.21875f;
            g2u1 = 0.3671875f;
            g2u2 = 0.4921875f;
            g2v1 = 0.109375f;
            g2v2 = 0.21875f;
            renderDir = dir.getOpposite();
        } else if(dir == Direction.WEST) {
            fx = 0.75f;
            fu1 = 0.0f;
            fu2 = 1.0f;
            fv1 = 0.25f;
            fv2 = 0.875f;
            g1x = 1.0f;
            g2x = 0.75f;
            g1u1 = 0.4921875f;
            g1u2 = 0.3671875f;
            g1v1 = 0.109375f;
            g1v2 = 0.21875f;
            g2u1 = 0.3671875f;
            g2u2 = 0.4921875f;
            g2v1 = 0.109375f;
            g2v2 = 0.21875f;
        }

        int heat = pBlockEntity.getHeatFromData();

        if(pBlockEntity.getHeatFromData() > 0) {
            RenderUtils.renderFaceWithUV(renderDir, pPoseStack.last().pose(), pPoseStack.last().normal(),
                    pBuffer.getBuffer(RenderType.cutout()),
                    Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE_FIRE),
                    fx, y, fz, fw, fh, fu1, fu2, fv1, fv2, 0xFFFFFFFF, pPackedLight);
        }

        RenderUtils.renderFaceWithUV(renderDir, pPoseStack.last().pose(), pPoseStack.last().normal(),
                pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS)),
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE_DISTILLERY),
                g1x, y, g1z, gw, gh, g1u1, g1u2, g1v1, g1v2, 0xFFFFFFFF, pPackedLight);

        RenderUtils.renderFaceWithUV(renderDir, pPoseStack.last().pose(), pPoseStack.last().normal(),
                pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS)),
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE_DISTILLERY),
                g2x, y, g2z, gw, gh, g2u1, g2u2, g2v1, g2v2, 0xFFFFFFFF, pPackedLight);
    }
}
