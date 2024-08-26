package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ColoringCauldronBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.util.render.ColorUtils;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

import static com.aranaira.magichem.block.entity.ColoringCauldronBlockEntity.*;

public class ColoringCauldronBlockEntityRenderer implements BlockEntityRenderer<ColoringCauldronBlockEntity> {
    private static final ResourceLocation TEXTURE_WATER = new ResourceLocation("minecraft", "block/water_still");

    private static final DyeColor[] COLOR_GUI_ORDER = {
            DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.BLACK,
            DyeColor.BROWN, DyeColor.RED, DyeColor.ORANGE, DyeColor.YELLOW,
            DyeColor.LIME, DyeColor.GREEN, DyeColor.CYAN, DyeColor.LIGHT_BLUE,
            DyeColor.BLUE, DyeColor.PURPLE, DyeColor.MAGENTA, DyeColor.PINK
    };

    public ColoringCauldronBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ColoringCauldronBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderItem(pBlockEntity, pPoseStack, pBuffer, pPartialTick, pPackedLight, pPackedOverlay);
        this.renderWater(pBlockEntity, pPoseStack, pBuffer, pPartialTick, pPackedLight, pPackedOverlay);
    }

    private void renderItem(ColoringCauldronBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, float pPartialTick, int pPackedLight, int pPackedOverlay) {
        ItemStack stack = pBlockEntity.getItem();
        if(!stack.isEmpty()) {
            Level world = pBlockEntity.getLevel();
            BlockPos pos = pBlockEntity.getBlockPos();
            BlockState state = pBlockEntity.getBlockState();

            long gt = world.getGameTime();

            if(pBlockEntity.isReadyToCollect()) {

                float rotY = -((gt % 720) / 720f) * 360;
                float bobY = (float) Math.sin((gt % 180) / 180f * Math.PI * 2) * 0.03125f;

                pPoseStack.pushPose();

                pPoseStack.translate(0.5, 1.25f + bobY, 0.5);
                pPoseStack.scale(0.25f, 0.25f, 0.25f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(rotY));
                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, world, 0);

                pPoseStack.popPose();

            } else {

                float rotY = -((gt % 2230) / 2230f) * 360;
                float bobY = (float) Math.sin((gt % 130) / 130f * Math.PI * 2) * 0.015625f;

                pPoseStack.pushPose();

                pPoseStack.translate(0.5, 0.8f + bobY, 0.5);
                pPoseStack.scale(0.25f, 0.25f, 0.25f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(rotY));
                pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, world, 0);

                pPoseStack.popPose();

            }
        }
    }

    private void renderWater(ColoringCauldronBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, float pPartialTick, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        long gt = world.getGameTime();
        int tint = 0x88ffffff;

        if(pBlockEntity.hasColors()) {
            ArrayList<DyeColor> colors = pBlockEntity.getColorsFromInverseBitpack();
            int bitpackedColors = pBlockEntity.getBitpackedColors();
            int inverseBitpackedColors = ~bitpackedColors & 65535;
            int filter = BITPACK_BLACK | BITPACK_GRAY | BITPACK_LIGHT_GRAY | BITPACK_WHITE;
            if((inverseBitpackedColors & ~filter) > 0) {
                colors.removeIf(c -> c == DyeColor.BLACK || c == DyeColor.GRAY || c == DyeColor.LIGHT_GRAY || c == DyeColor.WHITE);
            }

            final int period = 100;

            float spectrumTime = (gt + pPartialTick) % (colors.size() * period);
            float delta = (spectrumTime % period) / period; //0..1 range for lerping
            int index = (int) Math.floor(spectrumTime / period);

            DyeColor current;
            DyeColor previous;
            if(colors.size() > 1) {
                current = colors.get(index);
                if(index == 0) previous = colors.get(colors.size() - 1);
                else previous = colors.get(index - 1);
            } else if(colors.size() == 1) {
                current = colors.get(0);
                previous = colors.get(0);
            } else {
                current = DyeColor.WHITE;
                previous = DyeColor.WHITE;
            }

            int[] currentRGB = ColorUtils.getRGBIntTint(current);
            int[] previousRGB = ColorUtils.getRGBIntTint(previous);

            int currentColor = 0x88000000 | currentRGB[0] << 16 | currentRGB[1] << 8 | currentRGB[2];
            int previousColor = 0x88000000 | previousRGB[0] << 16 | previousRGB[1] << 8 | previousRGB[2];

            tint = MathUtils.lerpColor(previousColor, currentColor, delta);
        }

        RenderUtils.renderFaceWithUV(Direction.UP, pPoseStack.last().pose(), pPoseStack.last().normal(),
                pBuffer.getBuffer(RenderType.translucent()),
                Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE_WATER),
                0.25f, 0.25f, 0.78125f, 0.5f, 0.5f, 0.25f, 0.75f, 0.25f, 0.75f, tint, pPackedLight);

    }
}
