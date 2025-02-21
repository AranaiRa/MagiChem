package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.PrismaticConduitBlockEntity;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.ManaAndArtifice;
import com.mna.api.tools.RLoc;
import com.mna.blocks.tileentities.EldrinConduitTile;
import com.mna.tools.math.Vector3;
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
import net.minecraft.world.phys.Vec2;

import java.util.Random;

public class PrismaticConduitBlockEntityRenderer implements BlockEntityRenderer<PrismaticConduitBlockEntity> {
    private final ResourceLocation inner = RLoc.create("block/eldrin/supplier_lower_interior");
    private final ResourceLocation frame = RLoc.create("block/eldrin/supplier_lower_exterior");
    private final ResourceLocation crystal = RLoc.create("block/eldrin/supplier_upper");
    public static final ResourceLocation CIRCLE_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/actuator_water");
    private static final Random RANDOM = new Random(31100L);

    final TextureAtlasSprite circleTexture;

    public PrismaticConduitBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        circleTexture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CIRCLE_TEXTURE);
    }

    public void render(PrismaticConduitBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if (!pBlockEntity.isLesser()) {
            Level world = pBlockEntity.getLevel();
            BlockPos pos = pBlockEntity.getBlockPos();
            BlockState state = pBlockEntity.getBlockState();
            float partialTick = (float) ManaAndArtifice.instance.proxy.getGameTicks() + pPartialTick;
            float radians = (float)((double)partialTick * Math.PI / 180.0D);

            pPoseStack.pushPose();
            pPoseStack.translate(0.5F, 0.75F, 0.5F);
            pPoseStack.mulPose(Axis.YP.rotation(radians));
            float scale = 0.6F;
            pPoseStack.scale(scale, scale, scale);
            float colorMod = 0.15F;
            this.renderModelWithRandomColor(pPoseStack, pBuffer.getBuffer(RenderType.endPortal()), world, pos, state, colorMod, pPackedLight, pPackedOverlay);
            ModelUtils.renderModel(pBuffer, world, pos, state, this.frame, pPoseStack, pPackedLight, pPackedOverlay);
            pPoseStack.mulPose(Axis.YP.rotation(-radians * 2.0F));
            ModelUtils.renderModel(pBuffer, world, pos, state, this.crystal, pPoseStack, pPackedLight, pPackedOverlay);
            pPoseStack.popPose();

            int period = 800;
            int gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
            float circleRotation = ((float)((gt + pPartialTick) % (float)period) / (float)period) * (float)Math.PI * 2;

            period = 162;
            gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
            double circleBob = Math.sin(((double)((gt + pPartialTick) % (double)period) / (double)period) * (double)Math.PI * 2);

            pPoseStack.pushPose();
            pPoseStack.scale(0.5f, 0.5f, 0.5f);
            RenderUtils.generateMagicCircleRing(new Vector3(1.0, 2.5 + circleBob * 0.0625, 1.0),
                    6, 0.825f, 0.375f, circleRotation, circleTexture,
                    new Vec2(0, 0), new Vec2(12, 3f), 0.75f,
                    1.0f, pPoseStack, pBuffer, pPackedLight);

            RenderUtils.generateMagicCircleRing(new Vector3(1.0, 0.5 + -circleBob * 0.0625, 1.0),
                    6, 0.825f, 0.375f, -circleRotation, circleTexture,
                    new Vec2(0, 0), new Vec2(12, 3f), 0.75f,
                    1.0f, pPoseStack, pBuffer, pPackedLight);
            pPoseStack.popPose();
        }
        else {
            int period = 800;
            int gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
            float circleRotation = ((float)((gt + pPartialTick) % (float)period) / (float)period) * (float)Math.PI * 2;

            period = 162;
            gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
            double circleBob = Math.sin(((double)((gt + pPartialTick) % (double)period) / (double)period) * (double)Math.PI * 2);

            pPoseStack.pushPose();
            pPoseStack.scale(0.5f, 0.5f, 0.5f);
            RenderUtils.generateMagicCircleRing(new Vector3(1.0, 2.25 + circleBob * 0.0625, 1.0),
                    4, 0.875f, 0.375f, circleRotation, circleTexture,
                    new Vec2(0, 0), new Vec2(12, 3f), 0.75f,
                    1.0f, pPoseStack, pBuffer, pPackedLight);
            pPoseStack.popPose();
        }
    }

    private void renderModelWithRandomColor(PoseStack stack, VertexConsumer builder, Level world, BlockPos pos, BlockState state, float colorMod, int light, int overlay) {
        float r = (RANDOM.nextFloat() * 0.5F + 0.1F) * colorMod;
        float g = (RANDOM.nextFloat() * 0.5F + 0.4F) * colorMod;
        float b = (RANDOM.nextFloat() * 0.5F + 0.5F) * colorMod;
        ModelUtils.renderModel(builder, world, pos, state, this.inner, stack, new float[]{1.0F, r, g, b}, light, overlay);
    }
}