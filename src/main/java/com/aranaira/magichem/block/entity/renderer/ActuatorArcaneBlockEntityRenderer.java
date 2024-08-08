package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ActuatorArcaneBlockEntity;
import com.aranaira.magichem.block.entity.ActuatorEarthBlockEntity;
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

public class ActuatorArcaneBlockEntityRenderer implements BlockEntityRenderer<ActuatorArcaneBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_CUBE_VAR1 = new ResourceLocation(MagiChemMod.MODID, "obj/special/actuator_arcane_cube_var1");
    public static final ResourceLocation RENDERER_MODEL_CUBE_VAR2 = new ResourceLocation(MagiChemMod.MODID, "obj/special/actuator_arcane_cube_var2");

    public ActuatorArcaneBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(ActuatorArcaneBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderCube(pBlockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        this.renderMagicCircles(pBlockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
    }

    private void renderCube(ActuatorArcaneBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();
    }

    private void renderMagicCircles(ActuatorArcaneBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
    }


}
