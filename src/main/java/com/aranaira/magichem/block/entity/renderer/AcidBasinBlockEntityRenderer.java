package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.block.entity.AcidBasinBlockEntity;
import com.aranaira.magichem.block.entity.routers.AcidBasinRouterBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.render.ModelUtils;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

import static com.aranaira.magichem.block.entity.AcidBasinBlockEntity.TANK_INPUT;
import static com.aranaira.magichem.block.entity.AcidBasinBlockEntity.TANK_OUTPUT;

public class AcidBasinBlockEntityRenderer implements BlockEntityRenderer<AcidBasinBlockEntity> {

    public AcidBasinBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(AcidBasinBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderItems(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);

        if(!pBlockEntity.getFluidInTank(TANK_INPUT).isEmpty()) {
            this.renderMainTankFluid(pBlockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
            this.renderMainTankGauge(pBlockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        }

        if(!pBlockEntity.getFluidInTank(TANK_OUTPUT).isEmpty()) {
            this.renderOutputTankFluid(pBlockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
            this.renderOutputTankGauge(pBlockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        }

        if(Minecraft.getInstance().hitResult instanceof BlockHitResult bhr) {
            BlockEntity be = pBlockEntity.getLevel().getBlockEntity(bhr.getBlockPos());
            AcidBasinBlockEntity basin = null;
            if(be instanceof AcidBasinBlockEntity query) {
                basin = query;
            }
            else if(be instanceof AcidBasinRouterBlockEntity query) {
                basin = query.getMaster();
            }

            if(basin != null) {
                if(basin.getInputItem().isEmpty() || basin.getFluidInTank(TANK_INPUT).isEmpty())
                    return;
                if(basin.hasSufficientItemsForRecipe()) {

                    float fill = Math.max(0,Math.min(1,basin.getProgressPercent()));

                    int color = Mth.hsvToRgb(fill / 3.0F, 1.0F, 1.0F) | -16777216;
                    int r = FastColor.ARGB32.red(color) / 3 * 2;
                    int g = FastColor.ARGB32.green(color) / 3 * 2;
                    int b = FastColor.ARGB32.blue(color) / 3 * 2;

                    for (int i = 0; i < 4; i++) {
                        pPoseStack.pushPose();
                        pPoseStack.translate(-0.5D, 0.0D, 0.5D);
                        pPoseStack.mulPose(Axis.YP.rotationDegrees((float) (i * 90)));
                        pPoseStack.translate(0.0D, 1.1875D, -0.3145D);
                        pPoseStack.scale(0.565F, 0.05F, 1.0F);
                        WorldRenderUtils.renderProgressBar(pPoseStack, pBuffer, fill, new int[]{r, g, b}, 255);
                        pPoseStack.scale(-1.0F, 1.0F, 1.0F);
                        WorldRenderUtils.renderProgressBar(pPoseStack, pBuffer, 1 - fill, new int[]{0, 0, 0}, 255);
                        pPoseStack.popPose();
                    }
                }
            }
        }
    }

    private void renderItems(AcidBasinBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        int period = 850;
        int gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
        double posBob = Math.sin((((gt) % period) / (double)period) * (Math.PI * 2) * Math.PI * 2) * 0.03125 * 0.707 + 0.1875;

        period = 500;
        gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
        float rot = ((float)((gt + pPartialTick) % period) / (float)period) * 360f;

        float height = 0.34375f * ((float)pBlockEntity.getFluidInTank(TANK_INPUT).getAmount() / (float)ServerConfig.acidBasinTankCapacity);

        VertexConsumer buffer = pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        ItemStack input = pBlockEntity.getInputItem();
        ItemStack output = pBlockEntity.getOutputItem();

        if(!input.isEmpty()){
            pPoseStack.pushPose();
            PoseStack.Pose last = pPoseStack.last();

            float x = 0, z = 0;
            switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                case NORTH -> {
                    x = -1f;
                    z = 0f;
                }
                case EAST -> {
                    x = 0f;
                    z = -1f;
                }
                case SOUTH -> {
                    x = 1f;
                    z = 0f;
                }
                case WEST -> {
                    x = 0f;
                    z = 1f;
                }
            }

            pPoseStack.translate(0.5 + x, 0.78125f + height + posBob, 0.5 + z);
            pPoseStack.scale(0.25f, 0.25f, 0.25f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(rot));
            Minecraft.getInstance().getItemRenderer().renderStatic(input, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);

            pPoseStack.popPose();
        }
        if(!output.isEmpty()){
            pPoseStack.pushPose();
            PoseStack.Pose last = pPoseStack.last();

            pPoseStack.translate(0.5, 0.875f + posBob, 0.5);
            pPoseStack.scale(0.25f, 0.25f, 0.25f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(rot));
            Minecraft.getInstance().getItemRenderer().renderStatic(output, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);

            pPoseStack.popPose();
        }
    }

    private void renderMainTankFluid(AcidBasinBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        BlockState state = pBlockEntity.getBlockState();
        VertexConsumer buffer = pBuffer.getBuffer(RenderType.translucent());
        PoseStack.Pose last = pPoseStack.last();

        IClientFluidTypeExtensions extension = IClientFluidTypeExtensions.of(
                pBlockEntity.getFluidInTank(TANK_INPUT).getFluid()
        );
        TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extension.getStillTexture());

        float width = 0.375f;
        float height = 0.34375f * ((float)pBlockEntity.getFluidInTank(TANK_INPUT).getAmount() / (float)ServerConfig.acidBasinTankCapacity);
        float length = 0.8125f;

        pPoseStack.pushPose();
        float x = 0, y = 0, w = 0, l = 0, u = 0, u2 = 0, v = 0, v2 = 0;
        switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> {
                x = -0.125f;
                y = 0.3125f;
                w = length;
                l = width;
                u = 0f;
                u2 = 0.8125f;
                v = 0.3125f;
                v2 = 0.6875f;
            }
            case EAST -> {
                x = 0.3125f;
                y = -0.125f;
                w = width;
                l = length;
                u = 0.3125f;
                u2 = 0.6875f;
                v = 0f;
                v2 = 0.8125f;
            }
            case SOUTH -> {
                x = 0.3125f;
                y = 0.3125f;
                w = length;
                l = width;
                u = 0f;
                u2 = 0.8125f;
                v = 0.3125f;
                v2 = 0.6875f;
            }
            case WEST -> {
                x = 0.3125f;
                y = 0.3125f;
                w = width;
                l = length;
                u = 0.3125f;
                u2 = 0.6875f;
                v = 0f;
                v2 = 0.8125f;
            }
        }

        RenderUtils.renderFaceWithUV(Direction.UP, last.pose(), last.normal(), buffer, texture,
                x, y, 0.828125f, w, l,
                u, u2, v, v2,
                pBlockEntity.getFluidInTank(TANK_INPUT).getFluid() == Fluids.WATER ? 0xff2a76d1 : 0xffffffff,
                pPackedLight);

        pPoseStack.popPose();

        switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> {
                pPoseStack.translate(-1.0f, 0, 0);
            }
            case EAST -> {
                pPoseStack.translate(0, 0, -1.0f);
            }
            case SOUTH -> {
                pPoseStack.translate(1.0f, 0, 0);
            }
            case WEST -> {
                pPoseStack.translate(0, 0, 1.0f);
            }
        }

        pPoseStack.pushPose();
        RenderUtils.renderFace(Direction.UP, last.pose(), last.normal(), buffer, texture,
                0.3125f, 0.3125f, 0.9375f + height, width, width,
                extension.getTintColor(),
                pPackedLight);

        pPoseStack.popPose();

        switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> {
                pPoseStack.translate(1.0f, 0, 0);
            }
            case EAST -> {
                pPoseStack.translate(0, 0, 1.0f);
            }
            case SOUTH -> {
                pPoseStack.translate(-1.0f, 0, 0);
            }
            case WEST -> {
                pPoseStack.translate(0, 0, -1.0f);
            }
        }
    }

    private void renderMainTankGauge(AcidBasinBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        BlockState state = pBlockEntity.getBlockState();
        VertexConsumer buffer = pBuffer.getBuffer(RenderType.translucent());

        pPoseStack.pushPose();
        switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> {
                pPoseStack.translate(-0.125f, 0.6875f, 0.919194f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(45));
            }
            case EAST -> {
                pPoseStack.translate(0.080806f, 0.6875f, -0.125f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(315));
            }
            case SOUTH -> {
                pPoseStack.translate(1.125f, 0.6875f, 0.080806f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(225));
            }
            case WEST -> {
                pPoseStack.translate(0.919194f, 0.6875f, 1.125f);
                pPoseStack.mulPose(Axis.YP.rotationDegrees(135));
            }
        }

        IClientFluidTypeExtensions extension = IClientFluidTypeExtensions.of(
                pBlockEntity.getFluidInTank(TANK_INPUT).getFluid()
        );
        TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extension.getStillTexture());

        float width = 0.0625f;
        float height = 0.25f * ((float)pBlockEntity.getFluidInTank(TANK_INPUT).getAmount() / (float)ServerConfig.acidBasinTankCapacity);

        PoseStack.Pose last = pPoseStack.last();
        RenderUtils.renderFace(Direction.SOUTH, last.pose(), last.normal(), buffer, texture,
                0.0f, 0.0f, 1.0f, width, height,
                extension.getTintColor(),
                pPackedLight);
        pPoseStack.popPose();
    }

    private void renderOutputTankFluid(AcidBasinBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        BlockState state = pBlockEntity.getBlockState();
        VertexConsumer buffer = pBuffer.getBuffer(RenderType.translucent());
        PoseStack.Pose last = pPoseStack.last();

        switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> {
                pPoseStack.translate(-1f, 0f, 1f);
            }
            case EAST -> {
                pPoseStack.translate(-1f, 0f, -1f);
            }
            case SOUTH -> {
                pPoseStack.translate(1f, 0f, -1f);
            }
            case WEST -> {
                pPoseStack.translate(1f, 0f, 1f);
            }
        }

        IClientFluidTypeExtensions extension = IClientFluidTypeExtensions.of(
                pBlockEntity.getFluidInTank(TANK_OUTPUT).getFluid()
        );
        TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extension.getStillTexture());

        float width = 0.25f;
        float height = 0.28125f * ((float)pBlockEntity.getFluidInTank(TANK_OUTPUT).getAmount() / (float)ServerConfig.acidBasinTankCapacity);

        pPoseStack.pushPose();
        RenderUtils.renderFace(Direction.UP, last.pose(), last.normal(), buffer, texture,
                0.375f, 0.375f, 0.75f + height, width, width,
                extension.getTintColor(),
                pPackedLight);
        pPoseStack.popPose();

        switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> {
                pPoseStack.translate(1f, 0f, -1f);
            }
            case EAST -> {
                pPoseStack.translate(1f, 0f, 1f);
            }
            case SOUTH -> {
                pPoseStack.translate(-1f, 0f, 1f);
            }
            case WEST -> {
                pPoseStack.translate(-1f, 0f, -1f);
            }
        }
    }

    private void renderOutputTankGauge(AcidBasinBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        BlockState state = pBlockEntity.getBlockState();
        VertexConsumer buffer = pBuffer.getBuffer(RenderType.translucent());

        pPoseStack.pushPose();
        switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH -> {
                pPoseStack.translate(-1.15625f, 0.5625f, 1.46875f);
            }
            case EAST -> {
                pPoseStack.mulPose(Axis.YN.rotationDegrees(90));
                pPoseStack.translate(-1.15625f, 0.5625f, 0.46875f);
            }
            case SOUTH -> {
                pPoseStack.mulPose(Axis.YN.rotationDegrees(180));
                pPoseStack.translate(-2.15625f, 0.5625f, 0.46875f);
            }
            case WEST -> {
                pPoseStack.mulPose(Axis.YN.rotationDegrees(270));
                pPoseStack.translate(-2.15625f, 0.5625f, 1.46875f);
            }
        }

        IClientFluidTypeExtensions extension = IClientFluidTypeExtensions.of(
                pBlockEntity.getFluidInTank(TANK_OUTPUT).getFluid()
        );
        TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extension.getStillTexture());

        float width = 0.0625f;
        float height = 0.25f * ((float)pBlockEntity.getFluidInTank(TANK_OUTPUT).getAmount() / (float)ServerConfig.acidBasinTankCapacity);

        PoseStack.Pose last = pPoseStack.last();
        RenderUtils.renderFace(Direction.WEST, last.pose(), last.normal(), buffer, texture,
                0.0f, 0.0f, 0.0f, width, height,
                extension.getTintColor(),
                pPackedLight);
        pPoseStack.popPose();
    }
}
