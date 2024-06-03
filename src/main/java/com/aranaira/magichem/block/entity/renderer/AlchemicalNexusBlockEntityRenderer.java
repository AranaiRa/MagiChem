package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ActuatorAirBlockEntity;
import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class AlchemicalNexusBlockEntityRenderer implements BlockEntityRenderer<AlchemicalNexusBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_CRYSTAL = new ResourceLocation(MagiChemMod.MODID, "obj/special/alchemical_nexus_crystal");

    public AlchemicalNexusBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(AlchemicalNexusBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderFans(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
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
