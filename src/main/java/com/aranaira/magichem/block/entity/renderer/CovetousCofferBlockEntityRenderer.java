package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.CovetousCofferBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CovetousCofferBlockEntityRenderer implements BlockEntityRenderer<CovetousCofferBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_LID = new ResourceLocation(MagiChemMod.MODID, "obj/special/covetous_coffer_lid");

    public CovetousCofferBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(CovetousCofferBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderLid(pBlockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    private void renderLid(CovetousCofferBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        pPoseStack.pushPose();

        int rot = 0;
        if(dir == Direction.EAST) {
            rot = 90;
            pPoseStack.translate(0.375f, 0.0f, 0.375f);
        }
        else if(dir == Direction.SOUTH) {
            rot = 180;
            pPoseStack.translate(0.0f, 0.0f, 0.75f);
        }
        else if(dir == Direction.WEST) {
            rot = 270;
            pPoseStack.translate(-0.375f, 0.0f, 0.375f);
        }

        pPoseStack.translate(0.5f, 0.375f, 0.125f);
        pPoseStack.mulPose(Axis.YN.rotationDegrees(rot));
        pPoseStack.mulPose(Axis.XN.rotationDegrees(45));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_LID, pPoseStack, pPackedLight, pPackedOverlay);

        pPoseStack.popPose();
    }
}
