package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ColoringCauldronBlockEntity;
import com.aranaira.magichem.block.entity.CrystalCandleBlockEntity;
import com.aranaira.magichem.util.render.ColorUtils;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.math.MathUtils;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

import static com.aranaira.magichem.block.entity.ColoringCauldronBlockEntity.*;

public class CrystalCandleBlockEntityRenderer implements BlockEntityRenderer<CrystalCandleBlockEntity> {
    public static final ResourceLocation RENDERER_CRYSTAL_CANDLE = new ResourceLocation(MagiChemMod.MODID, "obj/special/crystal_candle");

    public CrystalCandleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CrystalCandleBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        float rot = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % 484) / 484f) * 360f;
        double bob = Math.sin(Math.toRadians(((pBlockEntity.getLevel().getGameTime() + pPartialTick) % 227) / 227d) * 360) * 0.03125;

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0.25 + bob, 0.5);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(rot));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_CRYSTAL_CANDLE, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();
    }
}
