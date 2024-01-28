package com.aranaira.magichem.item.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.MateriaVesselItem;
import com.aranaira.magichem.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class MateriaVesselItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final ResourceLocation MATERIA_VESSEL = new ResourceLocation(MagiChemMod.MODID, "item/special/materia_vessel");
    public static final ResourceLocation FLUID_TEXTURE = new ResourceLocation("minecraft", "block/water_still");

    public MateriaVesselItemRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);
    }

    @Override
    public void renderByItem(ItemStack pStack, ItemTransforms.TransformType pTransformType, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if(pStack.getItem() instanceof MateriaVesselItem mvi) {
            TextureAtlasSprite tx = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FLUID_TEXTURE);

            //renderFace(Direction face, Matrix4f pose, Matrix3f normal, VertexConsumer consumer, TextureAtlasSprite texture, float x, float y, float z, float w, float h, int color)
            PoseStack.Pose last = pPoseStack.last();
            VertexConsumer buffer = pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
            RenderUtils.renderFace(Direction.UP, last.pose(), last.normal(), buffer, tx, 0, 0, 0.5f, 1, 1, 0xffffffff);
        }

        super.renderByItem(pStack, pTransformType, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }
}
