package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.DisintegrationPyreBlockEntity;
import com.aranaira.magichem.util.render.ColorUtils;
import com.aranaira.magichem.util.render.RenderUtils;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;

public class DisintegrationPyreBlockEntityRenderer implements BlockEntityRenderer<DisintegrationPyreBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_FLAME_SMALL = new ResourceLocation(MagiChemMod.MODID, "obj/special/disintegration_pyre_flame_small");
    public static final ResourceLocation RENDERER_MODEL_FLAME_LARGE = new ResourceLocation(MagiChemMod.MODID, "obj/special/disintegration_pyre_flame_large");
    public static final ResourceLocation CIRCLE_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/actuator_water");

    final TextureAtlasSprite circleTexture;

    public DisintegrationPyreBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        circleTexture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CIRCLE_TEXTURE);
    }

    @Override
    public void render(DisintegrationPyreBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        int period = 850;
        int gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
        float rot = -(((gt + pPartialTick) % period) / period) * (float)Math.PI * 2;

        pPoseStack.pushPose();

        ModelUtils.renderModel(pBuffer, world, pos, state, pBlockEntity.getDropletsPercent() >= 0.5f ? RENDERER_MODEL_FLAME_LARGE : RENDERER_MODEL_FLAME_SMALL, pPoseStack, pPackedLight, pPackedOverlay, RenderType.cutout());

        pPoseStack.popPose();
    }
}
