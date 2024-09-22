package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.MateriaManifestBlockEntity;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.math.Vector3;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;

import static com.aranaira.magichem.foundation.MagiChemBlockStateProperties.FACING;

public class MateriaManifestBlockEntityRenderer implements BlockEntityRenderer<MateriaManifestBlockEntity> {
    public static final ResourceLocation BODY_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/grand_distillery");
    final TextureAtlasSprite bodyTexture;
    private static final float
            BEAM_TEX_U1 = 0.0f, BEAM_TEX_V1 = 0.515625f, BEAM_TEX_U2 = 0.015625f, BEAM_TEX_V2 = 0.53125f;

    public MateriaManifestBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        bodyTexture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(BODY_TEXTURE);
    }

    @Override
    public void render(MateriaManifestBlockEntity mmbe, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if(mmbe.tetherTarget != null) {
            final Direction facing = mmbe.getBlockState().getValue(FACING);
            double x = 0, z = 0;
            if(facing == Direction.NORTH) z = -0.3125;
            else if(facing == Direction.SOUTH) z = 0.3125;

            Vector3 startPos = new Vector3(0.5 + x, 1.78125, 0.5 + z);
            Vector3 endPos = new Vector3(mmbe.tetherTarget.getBlockPos().getX()+0.5, mmbe.tetherTarget.getBlockPos().getY()+0.5, mmbe.tetherTarget.getBlockPos().getZ()+0.5).sub(new Vector3(mmbe.getBlockPos().getX(), mmbe.getBlockPos().getY(), mmbe.getBlockPos().getZ()));

            int colorInt = mmbe.tetherTarget.getMateriaType().getMateriaColor();
            int intR = (colorInt & 0x00ff0000) >> 16;
            int intG = (colorInt & 0x0000ff00) >> 8;
            int intB = (colorInt & 0x000000ff) >> 0;

            RenderUtils.generateLinearVolumetricBeam(startPos, endPos, 0.03125f, bodyTexture, new int[]{intR, intG, intB, 255}, 1, poseStack, bufferSource, packedLight, BEAM_TEX_U1, BEAM_TEX_V1, BEAM_TEX_U2, BEAM_TEX_V2);
        }
    }
}
