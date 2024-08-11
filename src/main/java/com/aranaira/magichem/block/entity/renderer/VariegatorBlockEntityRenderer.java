package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.VariegatorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class VariegatorBlockEntityRenderer implements BlockEntityRenderer<VariegatorBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_LIMB_ABOVE = new ResourceLocation(MagiChemMod.MODID, "obj/special/variegator_limb_above");
    public static final ResourceLocation RENDERER_MODEL_LIMB_LONG = new ResourceLocation(MagiChemMod.MODID, "obj/special/variegator_limb_long");
    public static final ResourceLocation RENDERER_MODEL_LIMB_SHORT = new ResourceLocation(MagiChemMod.MODID, "obj/special/variegator_limb_short");
    public static final ResourceLocation RENDERER_MODEL_SHARD = new ResourceLocation(MagiChemMod.MODID, "obj/special/variegator_shard");
    public static final ResourceLocation BODY_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/grand_distillery");

    final TextureAtlasSprite bodyTexture;

    public VariegatorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        bodyTexture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(BODY_TEXTURE);
    }

    @Override
    public void render(VariegatorBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderItem(pBlockEntity, pPoseStack, pBuffer, pPartialTick, pPackedLight, pPackedOverlay);
        this.renderLimbs(pBlockEntity, pPoseStack, pBuffer, pPartialTick, pPackedLight, pPackedOverlay);
        this.renderShards(pBlockEntity, pPoseStack, pBuffer, pPartialTick, pPackedLight, pPackedOverlay);
//        this.renderColorShell(pBlockEntity, pPoseStack, pBuffer, pPartialTick, pPackedLight, pPackedOverlay);
    }

    private void renderItem(VariegatorBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, float pPartialTick, int pPackedLight, int pPackedOverlay) {

    }

    private void renderLimbs(VariegatorBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, float pPartialTick, int pPackedLight, int pPackedOverlay) {
        
    }

    private void renderShards(VariegatorBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, float pPartialTick, int pPackedLight, int pPackedOverlay) {

    }

//    private void renderColorShell(VariegatorBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, float pPartialTick, int pPackedLight, int pPackedOverlay) {
//
//    }
}
