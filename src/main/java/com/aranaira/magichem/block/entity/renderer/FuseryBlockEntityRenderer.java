package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.FuseryBlockEntity;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FuseryBlockEntityRenderer implements BlockEntityRenderer<FuseryBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_COG = new ResourceLocation(MagiChemMod.MODID, "obj/special/centrifuge_cog");
    public static final ResourceLocation FLUID_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/fluid/experience_still");

    public FuseryBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(FuseryBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderCog(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);

        float fluidPercent = (float)pBlockEntity.getFluidInTank(0).getAmount() / (float)Config.fuseryTankCapacity;

        if(fluidPercent > 0) {
            this.renderTankFluid(fluidPercent, pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
            this.renderCauldronFluid(fluidPercent, pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        }
    }

    private void renderTankFluid(float pFluidPercent, FuseryBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        BlockState state = pBlockEntity.getBlockState();
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction renderDir = dir;

        float mappedFluidH = Math.min(Math.max(pFluidPercent - 0.25f, 0), 0.75f) / 0.75f;
        float fluidHVariance = 0.25f;
        Direction
                innerFace = Direction.UP,
                outerFace = Direction.UP;
        float
                y = 0.5625f,
                fx1 = -1f, fz1 = -1f,
                fx2 = -1f, fz2 = -1f,
                fxt = 0, fyt = 0, fzt = 0,
                fw = 0.125f, fh = fluidHVariance * mappedFluidH,
                fwt = 1, fht = 1,

                fu1 = 0.25f, fu2 = 0.375f, fut = 0.0f,
                fv1 = 0.0f, fv2 = fluidHVariance * mappedFluidH, fvt = 0.25f;

        if(dir == Direction.NORTH) {
            innerFace = Direction.WEST;
            outerFace = Direction.EAST;
            fx1 = 0.4375f;
            fz1 = 1.34375f;
            fx2 = 0.4375f;
            fz2 = -0.65625f;
            fxt = -0.65625f;
            fyt = 0.4375f;
            fzt = y + fluidHVariance * mappedFluidH;
            fwt = 0.3125f;
            fht = 0.125f;
            fut = fwt;
            fvt = fht;
        } else if(dir == Direction.EAST) {
            innerFace = Direction.NORTH;
            outerFace = Direction.SOUTH;
            fx1 = 0.4375f;
            fz1 = -0.65625f;
            fx2 = 0.4375f;
            fz2 = 1.34375f;
            fxt = 0.4375f;
            fyt = -0.65625f;
            fzt = y + fluidHVariance * mappedFluidH;
            fwt = 0.125f;
            fht = 0.3125f;
            fut = fwt;
            fvt = fht;
        } else if(dir == Direction.SOUTH) {
            innerFace = Direction.EAST;
            outerFace = Direction.WEST;
            fx1 = 0.4375f;
            fz1 = 1.34375f;
            fx2 = 0.4375f;
            fz2 = -0.65625f;
            fxt = 1.34375f;
            fyt = 0.4375f;
            fzt = y + fluidHVariance * mappedFluidH;
            fwt = 0.3125f;
            fht = 0.125f;
            fut = fwt;
            fvt = fht;
        } else if(dir == Direction.WEST) {
            innerFace = Direction.SOUTH;
            outerFace = Direction.NORTH;
            fx1 = 0.4375f;
            fz1 = -0.65625f;
            fx2 = 0.4375f;
            fz2 = 1.34375f;
            fxt = 0.4375f;
            fyt = 1.34375f;
            fzt = y + fluidHVariance * mappedFluidH;
            fwt = 0.125f;
            fht = 0.3125f;
            fut = fwt;
            fvt = fht;
        }

        RenderUtils.renderFaceWithUV(innerFace, pPoseStack.last().pose(), pPoseStack.last().normal(),
                pBuffer.getBuffer(RenderType.cutout()),
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FLUID_TEXTURE),
                fx1, y, fz1, fw, fh, fu1, fu2, fv1, fv2, 0xFFFFFFFF, pPackedLight);

        RenderUtils.renderFaceWithUV(outerFace, pPoseStack.last().pose(), pPoseStack.last().normal(),
                pBuffer.getBuffer(RenderType.cutout()),
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FLUID_TEXTURE),
                fx2, y, fz2, fw, fh, fu1, fu2, fv1, fv2, 0xFFFFFFFF, pPackedLight);

        RenderUtils.renderFaceWithUV(Direction.UP, pPoseStack.last().pose(), pPoseStack.last().normal(),
                pBuffer.getBuffer(RenderType.cutout()),
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FLUID_TEXTURE),
                fxt, fyt, fzt, fwt, fht, 0, fut, 0, fvt, 0xFFFFFFFF, pPackedLight);
    }

    private void renderCauldronFluid(float pFluidPercent, FuseryBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        BlockState state = pBlockEntity.getBlockState();
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction renderDir = dir;

        float mappedFluidH = Math.min(Math.max(pFluidPercent, 0), 0.25f) / 0.25f;
        float fluidHVariance = 0.21875f;
        float
                y = 0.125f,
                fx = -0.0625f, fz = -0.0625f,
                fw = 0.625f, fh = 0.625f,
                fu1 = 0.0f, fu2 = 0.625f, fv1 = 0.0f, fv2 = 0.625f;

        if(dir == Direction.NORTH) {
            renderDir = dir.getOpposite();
        } else if(dir == Direction.EAST) {
            fx = 0.4375f;
        } else if(dir == Direction.SOUTH) {
            fx = 0.4375f;
            fz = 0.4375f;
            renderDir = dir.getOpposite();
        } else if(dir == Direction.WEST) {
            fz = 0.4375f;
        }

        RenderUtils.renderFaceWithUV(Direction.UP, pPoseStack.last().pose(), pPoseStack.last().normal(),
                pBuffer.getBuffer(RenderType.cutout()),
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FLUID_TEXTURE),
                fx, fz, y + mappedFluidH * fluidHVariance, fw, fh, fu1, fu2, fv1, fv2, 0xFFFFFFFF, pPackedLight);
    }

    private void renderCog(FuseryBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        pPoseStack.pushPose();
        switch((Direction)state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH: {
                pPoseStack.translate(-0.71875f, 0.5f, -0.28125f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(270.0f));
                pPoseStack.mulPose(Axis.ZN.rotationDegrees(pBlockEntity.cogAngle + pPartialTick * (pBlockEntity.cogSpeed)));
                break;
            }
            case EAST: {
                pPoseStack.translate(1.28125f, 0.5f, -0.71875f);
                pPoseStack.mulPose(Axis.ZP.rotationDegrees(pBlockEntity.cogAngle + pPartialTick * (pBlockEntity.cogSpeed)));
                break;
            }
            case SOUTH: {
                pPoseStack.translate(1.71875f, 0.5f, 1.28125f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(270.0f));
                pPoseStack.mulPose(Axis.ZP.rotationDegrees(pBlockEntity.cogAngle + pPartialTick * (pBlockEntity.cogSpeed)));
                break;
            }
            case WEST: {
                pPoseStack.translate(-0.28125f, 0.5f, 1.71875f);
                pPoseStack.mulPose(Axis.ZN.rotationDegrees(pBlockEntity.cogAngle + pPartialTick * (pBlockEntity.cogSpeed)));
                break;
            }
        }

        pPoseStack.pushPose();

        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_COG, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();

        pPoseStack.popPose();
    }
}
