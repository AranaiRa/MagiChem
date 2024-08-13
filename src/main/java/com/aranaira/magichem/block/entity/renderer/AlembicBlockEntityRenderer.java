package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.AlembicBlock;
import com.aranaira.magichem.block.entity.AlembicBlockEntity;
import com.aranaira.magichem.block.entity.DistilleryBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class AlembicBlockEntityRenderer implements BlockEntityRenderer<AlembicBlockEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/alembic");
    public static ResourceLocation PASSIVE_HEAT_TAG = new ResourceLocation("minecraft:alembic_passive_heat_source");

    public AlembicBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(AlembicBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        this.renderBrightSwatch(pBlockEntity, pPoseStack, pBuffer, pPackedLight);
    }

    private void renderBrightSwatch(AlembicBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        BlockState state = pBlockEntity.getBlockState();
        BlockState below = pBlockEntity.getLevel().getBlockState(pBlockEntity.getBlockPos().below());
        Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction renderDir = dir;

        boolean isOn = pBlockEntity.getRemainingHeat() > 0;
        boolean hasPassiveHeat = state.getValue(MagiChemBlockStateProperties.HAS_PASSIVE_HEAT);
        boolean hasSuperHeat = false;
        if(below.getBlock() == Blocks.BLAST_FURNACE || below.getBlock() == Blocks.SMOKER) {
            hasSuperHeat = below.getValue(BlockStateProperties.LIT) && isOn;
        }

        float
            y = 0.1875f,
            x = 0.4375f, z = 0.77f,
            w = 0.125f, h = 0.125f,
            gu1 = -1f, gu2 = -1f, gv1 = 0.4375f, gv2 = 0.5f;

        if(hasSuperHeat) {
            gu1 = 0.75f;
            gu2 = 0.8125f;
        } else if(isOn) {
            gu1 = 0.8125f;
            gu2 = 0.875f;
        } else if(hasPassiveHeat) {
            gu1 = 0.875f;
            gu2 = 0.9375f;
        }

        if(dir == Direction.NORTH || dir == Direction.SOUTH) {
            renderDir = dir.getOpposite();
        }

        if(isOn) {
            RenderUtils.renderFaceWithUV(renderDir, pPoseStack.last().pose(), pPoseStack.last().normal(),
                    pBuffer.getBuffer(RenderType.entityTranslucentEmissive(InventoryMenu.BLOCK_ATLAS)),
                    Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(TEXTURE),
                    x, y, z, w, h, gu1, gu2, gv1, gv2, 0xFFFFFFFF, pPackedLight);
        }
    }
}
