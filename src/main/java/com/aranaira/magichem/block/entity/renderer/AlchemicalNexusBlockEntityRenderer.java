package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ActuatorAirBlockEntity;
import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
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

public class AlchemicalNexusBlockEntityRenderer implements BlockEntityRenderer<AlchemicalNexusBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_CRYSTAL = new ResourceLocation(MagiChemMod.MODID, "obj/special/alchemical_nexus_crystal");
    public static final ResourceLocation FLUID_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/fluid/experience_still");

    public AlchemicalNexusBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(AlchemicalNexusBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderFans(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);

        float fluidPercent = (float)pBlockEntity.getFluidInTank(0).getAmount() / (float) Config.fuseryTankCapacity;

        if(fluidPercent > 0) {
            this.renderTankFluid(fluidPercent, pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        }
    }

    private void renderTankFluid(float pFluidPercent, AlchemicalNexusBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        BlockState state = pBlockEntity.getBlockState();
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction renderDir = dir;

        float mappedFluidH = Math.min(Math.max(pFluidPercent - 0.25f, 0), 0.75f) / 0.75f;
        float fluidHVariance = 0.5625f;
        Direction facing = Direction.UP;
        float
                y = 0.0625f,
                fx1 = -1f, fz1 = -1f,
                fxt = 0, fyt = 0, fzt = 0,
                fw = 0.125f, fh = fluidHVariance * mappedFluidH,
                fwt = 1, fht = 1,

                fu1 = 0.25f, fu2 = 0.375f, fut = 0.0f,
                fv1 = 0.0f, fv2 = fluidHVariance * mappedFluidH, fvt = 0.25f;

        if(dir == Direction.NORTH) {
            facing = Direction.NORTH;
            fx1 = 0.4375f;
            fz1 = -0.90625f;
            fxt = 0.4375f;
            fyt = -0.90625f;
            fzt = y + fluidHVariance * mappedFluidH;
            fwt = 0.125f;
            fht = 0.3125f;
            fut = fwt;
            fvt = fht;
        } else if(dir == Direction.EAST) {
            facing = Direction.WEST;
            fx1 = 0.4375f;
            fz1 = -0.90625f;
            fxt = 1.59375f;
            fyt = 0.4375f;
            fzt = y + fluidHVariance * mappedFluidH;
            fwt = 0.3125f;
            fht = 0.125f;
            fut = fwt;
            fvt = fht;
        } else if(dir == Direction.SOUTH) {
            facing = Direction.SOUTH;
            fx1 = 0.4375f;
            fz1 = -0.90625f;
            fxt = 0.4375f;
            fyt = 1.59375f;
            fzt = y + fluidHVariance * mappedFluidH;
            fwt = 0.125f;
            fht = 0.3125f;
            fut = fwt;
            fvt = fht;
        } else if(dir == Direction.WEST) {
            facing = Direction.EAST;
            fx1 = 0.4375f;
            fz1 = -0.90625f;
            fxt = -0.90625f;
            fyt = 0.4375f;
            fzt = y + fluidHVariance * mappedFluidH;
            fwt = 0.3125f;
            fht = 0.125f;
            fut = fwt;
            fvt = fht;
        }

        RenderUtils.renderFaceWithUV(facing, pPoseStack.last().pose(), pPoseStack.last().normal(),
                pBuffer.getBuffer(RenderType.cutout()),
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FLUID_TEXTURE),
                fx1, y, fz1, fw, fh, fu1, fu2, fv1, fv2, 0xFFFFFFFF, pPackedLight);

        RenderUtils.renderFaceWithUV(Direction.UP, pPoseStack.last().pose(), pPoseStack.last().normal(),
                pBuffer.getBuffer(RenderType.cutout()),
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FLUID_TEXTURE),
                fxt, fyt, fzt, fwt, fht, 0, fut, 0, fvt, 0xFFFFFFFF, pPackedLight);
    }

    private void renderFans(AlchemicalNexusBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        pPoseStack.pushPose();

        pPoseStack.translate(0.5f, 1.3125f, 0.5f);
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_CRYSTAL, pPoseStack, pPackedLight, pPackedOverlay);

        pPoseStack.popPose();
    }
}
