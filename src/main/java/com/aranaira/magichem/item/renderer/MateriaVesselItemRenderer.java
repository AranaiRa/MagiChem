package com.aranaira.magichem.item.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.MateriaVesselItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class MateriaVesselItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final ResourceLocation MATERIA_VESSEL = new ResourceLocation(MagiChemMod.MODID, "item/special/materia_vessel");

    public MateriaVesselItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);
    }

    @Override
    public void renderByItem(ItemStack pStack, ItemTransforms.TransformType pTransformType, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if(pStack.getItem() instanceof MateriaVesselItem mvi) {

        }

        super.renderByItem(pStack, pTransformType, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }
}
