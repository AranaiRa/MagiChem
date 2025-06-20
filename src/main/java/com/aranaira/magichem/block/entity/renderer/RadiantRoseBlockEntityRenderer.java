package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.RadiantRoseBlockEntity;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Vector2d;

public class RadiantRoseBlockEntityRenderer implements BlockEntityRenderer<RadiantRoseBlockEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/radiant_rose_halo");

    public RadiantRoseBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(RadiantRoseBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player != null) {
            VertexConsumer buffer = pBuffer.getBuffer(RenderType.cutout());
            PoseStack.Pose last = pPoseStack.last();

            TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE);

            double pX = player.getX();
            double pZ = player.getZ();

            double eX = pBlockEntity.getBlockPos().getX() + 0.5625;
            double eZ = pBlockEntity.getBlockPos().getZ() + 0.5;

            Vector2d dVec = new Vector2d(pX - eX, pZ - eZ);

            pPoseStack.pushPose();

            pPoseStack.translate(0.5, 0.5, 0.5);
            pPoseStack.mulPose(Axis.YN.rotation((float)Math.atan2(dVec.y, dVec.x)));

            RenderUtils.renderFaceWithUV(Direction.WEST, pPoseStack.last().pose(), pPoseStack.last().normal(), buffer, texture,
                    -0.5625f, -0.5f, 1.0f, 1f, 1f,
                    0f, 1f, 0f, 1f,
                    0xffffffff, pPackedLight);

            pPoseStack.popPose();
        }
    }
}
