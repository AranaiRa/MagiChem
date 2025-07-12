package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.SelenolabeBlock;
import com.aranaira.magichem.block.SelenolabeBlock.MoonPhase;
import com.aranaira.magichem.block.entity.SelenolabeBlockEntity;
import com.aranaira.magichem.util.render.RenderUtils;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector2d;

import java.util.HashMap;

public class SelenolabeBlockEntityRenderer implements BlockEntityRenderer<SelenolabeBlockEntity> {
    public static final ResourceLocation TEXTURE_MOON_1 = new ResourceLocation(MagiChemMod.MODID, "block/decorator/moon_1");
    public static final ResourceLocation TEXTURE_MOON_2 = new ResourceLocation(MagiChemMod.MODID, "block/decorator/moon_2");

    private static HashMap<MoonPhase, Vec2> MOON_TEX_STARTS = new HashMap<>();

    public SelenolabeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        if(MOON_TEX_STARTS.size() == 0) {
            MOON_TEX_STARTS.put(MoonPhase.FULL_MOON, new Vec2(0.0f, 0.0f));
            MOON_TEX_STARTS.put(MoonPhase.WANING_GIBBOUS, new Vec2(0.5f, 0.0f));
            MOON_TEX_STARTS.put(MoonPhase.WANING_HALF, new Vec2(0.0f, 0.0f));
            MOON_TEX_STARTS.put(MoonPhase.WANING_CRESCENT, new Vec2(0.5f, 0.0f));
            MOON_TEX_STARTS.put(MoonPhase.NEW_MOON, new Vec2(0.0f, 0.5f));
            MOON_TEX_STARTS.put(MoonPhase.WAXING_CRESCENT, new Vec2(0.5f, 0.5f));
            MOON_TEX_STARTS.put(MoonPhase.WAXING_HALF, new Vec2(0.0f, 0.5f));
            MOON_TEX_STARTS.put(MoonPhase.WAXING_GIBBOUS, new Vec2(0.5f, 0.5f));
        }
    }

    private ResourceLocation getTextureFromMoonPhase(MoonPhase pPhase) {
        return switch (pPhase) {
            case NONE, FULL_MOON, WANING_GIBBOUS, NEW_MOON, WAXING_CRESCENT -> TEXTURE_MOON_1;
            case WANING_HALF, WANING_CRESCENT, WAXING_HALF, WAXING_GIBBOUS -> TEXTURE_MOON_2;
        };
    }

    @Override
    public void render(SelenolabeBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player != null) {
            VertexConsumer buffer = pBuffer.getBuffer(RenderType.cutout());
            PoseStack.Pose last = pPoseStack.last();

            final MoonPhase phase = SelenolabeBlock.getMoonPhaseFromWorldTime(player.level());
            TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(getTextureFromMoonPhase(phase));

            double pX = player.getX();
            double pZ = player.getZ();

            double eX = pBlockEntity.getBlockPos().getX() + 0.5;
            double eZ = pBlockEntity.getBlockPos().getZ() + 0.5;

            int period = 1150;
            int gt = (int)(player.level().getGameTime() % (period * 2));
            double posBob = Math.sin((((gt + pPartialTick) % period) / (double)period) * (Math.PI * 2) * Math.PI * 2) * 0.03125;

            Vector2d dVec = new Vector2d(pX - eX, pZ - eZ);

            pPoseStack.pushPose();

            pPoseStack.translate(0.5, 0.625 + posBob, 0.5);
            pPoseStack.mulPose(Axis.YN.rotation((float)Math.atan2(dVec.y, dVec.x)));

            RenderUtils.renderFaceWithUV(Direction.WEST, pPoseStack.last().pose(), pPoseStack.last().normal(), buffer, texture,
                    -0.25f, -0.25f, 1.0f, 0.5f, 0.5f,
                    MOON_TEX_STARTS.get(phase).x, MOON_TEX_STARTS.get(phase).x+0.5f,
                    MOON_TEX_STARTS.get(phase).y,MOON_TEX_STARTS.get(phase).y+0.5f,
                    0xffffffff, pPackedLight);

            pPoseStack.popPose();
        }
    }
}
