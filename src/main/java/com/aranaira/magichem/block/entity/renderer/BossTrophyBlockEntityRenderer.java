package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.BossTrophyBlockEntity;
import com.aranaira.magichem.registry.BlockRegistry;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.render.ModelUtils;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BossTrophyBlockEntityRenderer implements BlockEntityRenderer<BossTrophyBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_SUMMER_1 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_summer_1");
    public static final ResourceLocation RENDERER_MODEL_SUMMER_2 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_summer_2");
    public static final ResourceLocation RENDERER_MODEL_SUMMER_3 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_summer_3");
    public static final ResourceLocation RENDERER_MODEL_WINTER_1 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_winter_1");
    public static final ResourceLocation RENDERER_MODEL_WINTER_2 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_winter_2");
    public static final ResourceLocation RENDERER_MODEL_WINTER_3 = new ResourceLocation(MagiChemMod.MODID, "obj/special/boss_trophy_fey_winter_3");
    private static final ResourceLocation TEXTURE_FEY = new ResourceLocation(MagiChemMod.MODID, "block/boss_trophy_fey");

    public BossTrophyBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(BossTrophyBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if(pBlockEntity.getBlockState().getBlock() == BlockRegistry.BOSS_TROPHY_COUNCIL.get()) this.renderCouncil(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        else if(pBlockEntity.getBlockState().getBlock() == BlockRegistry.BOSS_TROPHY_FEY.get()) this.renderFey(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        else if(pBlockEntity.getBlockState().getBlock() == BlockRegistry.BOSS_TROPHY_UNDEAD.get()) this.renderUndead(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    private void renderCouncil(BossTrophyBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        VertexConsumer buffer = pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        PoseStack.Pose last = pPoseStack.last();

        pPoseStack.pushPose();
//        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_STEAM_VENTS, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();
    }

    private void renderFey(BossTrophyBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        VertexConsumer buffer = pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        PoseStack.Pose last = pPoseStack.last();

        long dayTime = world.getDayTime() % 24000;

        pPoseStack.pushPose();
        if((dayTime >= 0 && dayTime < 800) || (dayTime >= 11200 && dayTime < 12000))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SUMMER_1, pPoseStack, pPackedLight, pPackedOverlay, RenderType.cutout());
        else if((dayTime >= 800 && dayTime < 1600) || (dayTime >= 10400 && dayTime < 11200))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SUMMER_2, pPoseStack, pPackedLight, pPackedOverlay, RenderType.cutout());
        else if((dayTime >= 1600 && dayTime < 10400))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SUMMER_3, pPoseStack, pPackedLight, pPackedOverlay, RenderType.cutout());
        else if((dayTime >= 13600 && dayTime < 14400) || (dayTime >= 21600 && dayTime < 22400))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_WINTER_1, pPoseStack, pPackedLight, pPackedOverlay, RenderType.translucent());
        else if((dayTime >= 14400 && dayTime < 15200) || (dayTime >= 20800 && dayTime < 21600))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_WINTER_2, pPoseStack, pPackedLight, pPackedOverlay, RenderType.translucent());
        else if((dayTime >= 15200 && dayTime < 20800))
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_WINTER_3, pPoseStack, pPackedLight, pPackedOverlay, RenderType.translucent());
        pPoseStack.popPose();
    }

    private void renderUndead(BossTrophyBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        VertexConsumer buffer = pBuffer.getBuffer(RenderType.armorCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
        PoseStack.Pose last = pPoseStack.last();

        pPoseStack.pushPose();
//        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_STEAM_VENTS, pPoseStack, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();
    }
}
