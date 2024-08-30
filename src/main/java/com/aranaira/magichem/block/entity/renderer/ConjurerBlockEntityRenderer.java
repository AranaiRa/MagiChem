package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ConjurerBlockEntity;
import com.aranaira.magichem.block.entity.VariegatorBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.util.render.ColorUtils;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.math.Vector3;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ConjurerBlockEntityRenderer implements BlockEntityRenderer<ConjurerBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_SHARD = new ResourceLocation(MagiChemMod.MODID, "obj/special/variegator_shard");

    private static final int
            SHARD_ROT_X_PERIOD = 300, SHARD_ROT_Z_PERIOD = 450, SHARD_BOB_PERIOD = 400, SHARD_DRIFT_PERIOD = 600, SHARD_ORBIT_PERIOD = 300,
            ITEM_ROTATE_PERIOD = 800;
    private static final float
            SHARD_BOB_HEIGHT = 0.09375f, SHARD_DRIFT_DISTANCE = 0.0625f;

    public ConjurerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(ConjurerBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if(pBlockEntity.getRecipe() != null) {
            this.renderItem(pBlockEntity, pPoseStack, pBuffer, pPartialTick, pPackedLight, pPackedOverlay);
            if(pBlockEntity.getMateriaType() != null && pBlockEntity.getMateriaAmount() > 0)
                this.renderShards(pBlockEntity, pPoseStack, pBuffer, pPartialTick, pPackedLight, pPackedOverlay);
        }
    }

    private void renderItem(ConjurerBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, float pPartialTick, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        ItemStack stack = new ItemStack(pBlockEntity.getRecipe().getCatalyst());

        if(stack.getItem() instanceof BlockItem) {
            float rotY = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % ITEM_ROTATE_PERIOD) / ITEM_ROTATE_PERIOD) * 360f;

            pPoseStack.pushPose();

            pPoseStack.translate(0.5, 1.5, 0.5);
            pPoseStack.scale(0.25f, 0.25f, 0.25f);
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(45));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(35));
            pPoseStack.mulPose(Axis.YP.rotationDegrees(rotY));
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, world, 0);

            pPoseStack.popPose();
        } else if(stack != ItemStack.EMPTY) {
            float rotY = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % ITEM_ROTATE_PERIOD) / ITEM_ROTATE_PERIOD) * 360f;

            pPoseStack.pushPose();

            pPoseStack.translate(0.5, 1.5, 0.5);
            pPoseStack.scale(0.25f, 0.25f, 0.25f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(rotY));
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, world, 0);

            pPoseStack.popPose();
        }
    }

    private void renderShards(ConjurerBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, float pPartialTick, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        int packedColor = pBlockEntity.getMateriaType().getMateriaColor();
        int r = (packedColor & 0x00ff0000) >> 16;
        int g = (packedColor & 0x0000ff00) >> 8;
        int b = (packedColor & 0x000000ff);
        float[] color = new float[]{r / 255f, g / 255f, b / 255f, 1.0f};

        float rotX = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % SHARD_ROT_X_PERIOD) / SHARD_ROT_X_PERIOD) * 360f;
        float rotY = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % SHARD_ORBIT_PERIOD) / SHARD_ORBIT_PERIOD) * 360f;
        float rotZ = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % SHARD_ROT_Z_PERIOD) / SHARD_ROT_Z_PERIOD) * 360f;
        float drift = (float)Math.sin(((pBlockEntity.getLevel().getGameTime() + pPartialTick) % SHARD_DRIFT_PERIOD) / (float)SHARD_DRIFT_PERIOD * Math.PI * 2) * SHARD_DRIFT_DISTANCE;
        float bob = (float)Math.sin(((pBlockEntity.getLevel().getGameTime() + pPartialTick) % SHARD_BOB_PERIOD) / (float)SHARD_BOB_PERIOD * Math.PI * 2) * SHARD_BOB_HEIGHT;

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0, 0.5);
        for(int i=0; i<4; i++) {
            int flipFlop = i % 2 == 0 ? -1 : 1;

            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.YP.rotationDegrees(-rotY + 90*i));
            pPoseStack.translate(0.25 + (drift * -flipFlop), 1.5 + (bob * flipFlop), 0);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(rotX));
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SHARD, pPoseStack, pPackedLight, pPackedOverlay, color);
            pPoseStack.popPose();
        }
        pPoseStack.popPose();
    }
}
