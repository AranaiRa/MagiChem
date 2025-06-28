package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.AstralObserverBlockEntity;
import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.foundation.enums.LuminType;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.MathHelper;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.math.MathUtils;
import com.mna.tools.math.Vector3;
import com.mna.tools.render.MARenderTypes;
import com.mna.tools.render.ModelUtils;
import com.mna.tools.render.WorldRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

public class AstralObserverBlockEntityRenderer implements BlockEntityRenderer<AstralObserverBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_TELESCOPE = new ResourceLocation(MagiChemMod.MODID, "obj/special/astral_observer_telescope");
    public static final ResourceLocation CIRCLE_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/actuator_water");
    public static final ItemStack ITEMSTACK_CRYSTAL = new ItemStack(ItemRegistry.IMMACULATE_VINTEUM_CRYSTAL.get());

    public AstralObserverBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(AstralObserverBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        double period = 227;
        double gt = (int) (world.getGameTime() % (int) (period * 2));
        double bob = Math.sin(Math.toRadians(((double) (gt + pPartialTick) % period) / period) * 360d) * 0.03125d;

        float time = (world.getTimeOfDay(pPartialTick) + 0.75f) % 1f;
        float angle = 0;

        //Telescope
        {
            //0.995..1.0 == reset
            if (time > 0.995f) {
                float lerp = MathHelper.doubleExponentialSeat((time - 0.995f) / 0.005f, 2f);
                angle = (1 - lerp) * 180;
            }
            //0.5-0.995 == day
            else if (time > 0.5f) {
                float lerp = (time - 0.5f) / 0.5f;
                angle = lerp * 180;
            }
            //0.495..0.5 == reset
            else if (time > 0.495f) {
                float lerp = MathHelper.doubleExponentialSeat((time - 0.495f) / 0.005f, 2f);
                angle = (1 - lerp) * 180;
            }
            //0..0.495 == night
            else {
                float lerp = time / 0.5f;
                angle = lerp * 180;
            }

            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 1.5625, 0.5);
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(angle));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_TELESCOPE, pPoseStack, pPackedLight, pPackedOverlay);
            pPoseStack.popPose();
        }

        //Crystal
        {
            double pX = Minecraft.getInstance().player.getX();
            double pZ = Minecraft.getInstance().player.getZ();

            double eX = pBlockEntity.getBlockPos().getX() + 0.5625;
            double eZ = pBlockEntity.getBlockPos().getZ() + 0.5;

            Vector2d dVec = new Vector2d(pX - eX, pZ - eZ);

            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 1.5625 + bob, 0.5);
            pPoseStack.mulPose(Axis.YN.rotation((float) Math.atan2(dVec.y, dVec.x) + (float) (Math.PI * 0.5)));
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(135));
            pPoseStack.translate(0, 0, -0.0625);
            pPoseStack.scale(0.375f, 0.375f, 0.375f);
            Minecraft.getInstance().getItemRenderer().renderStatic(ITEMSTACK_CRYSTAL, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);
            pPoseStack.popPose();
        }

        //Beams
        if(true){ //sky check later
            LuminType phase = pBlockEntity.getLuminPhase(true);
            int[] color = LuminType.getParticleColor(pBlockEntity.getLuminPhase(true));
            if(pBlockEntity.doColorLerp) {
                int[] colorFrom = LuminType.getParticleColor(pBlockEntity.getInverseLuminPhase(false));
                color[0] = (int)MathUtils.lerpf(MathUtils.lerpf(colorFrom[0], color[0], pBlockEntity.colorLerp), 255, 0.425f);
                color[1] = (int)MathUtils.lerpf(MathUtils.lerpf(colorFrom[1], color[1], pBlockEntity.colorLerp), 255, 0.425f);
                color[2] = (int)MathUtils.lerpf(MathUtils.lerpf(colorFrom[2], color[2], pBlockEntity.colorLerp), 255, 0.425f);
            } else {
                color[0] = (int)MathUtils.lerpf(color[0], 255, 0.425f);
                color[1] = (int)MathUtils.lerpf(color[1], 255, 0.425f);
                color[2] = (int)MathUtils.lerpf(color[2], 255, 0.425f);
            }

            double radius = 0.375;
            double theta = (angle / 180d) * Math.PI;
            Vec3 start = new Vec3(0.5 + Math.cos(theta)*radius, 1.5625 + Math.sin(theta)*radius, 0.5);
            Vec3 end = new Vec3(0.5, 1.5625, 0.5);

            pPoseStack.pushPose();
            pPoseStack.translate(start.x, start.y, start.z);
            WorldRenderUtils.renderBeam(world, pPartialTick, pPoseStack, pBuffer, pPackedLight,
                    start, end, 1.0f, color, 255, 0.0625f, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
            pPoseStack.popPose();

            start = end;
            end = new Vec3(0.5, 0.9375, 0.5);

            pPoseStack.pushPose();
            pPoseStack.translate(start.x, start.y, start.z);
            WorldRenderUtils.renderBeam(world, pPartialTick, pPoseStack, pBuffer, pPackedLight,
                    start, end, 1.0f, color, 255, 0.0625f, MARenderTypes.RITUAL_BEAM_RENDER_TYPE);
            pPoseStack.popPose();
        }

        //Item
        {
            period = 500;
            gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
            float rot = ((float)((gt + pPartialTick) % period) / (float)period) * 360f;

            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 1.125, 0.5);
            pPoseStack.mulPose(Axis.YN.rotationDegrees(rot));
            pPoseStack.scale(0.375f, 0.375f, 0.375f);
            Minecraft.getInstance().getItemRenderer().renderStatic(pBlockEntity.getItem(), ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);
            pPoseStack.popPose();
        }

        //Charging Magic Circle
        {
            final TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CIRCLE_TEXTURE);
            Vector3 center = new Vector3(0.5, 1.0625, 0.5);

            period = 370;
            gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
            float rot = ((float)((gt + pPartialTick) % period) / (float)period) * (float)Math.PI * 2;

            RenderUtils.generateMagicCircleRing(center,
                    7, 0.75f, 0.375f, -rot, texture,
                    new Vec2(0, 0), new Vec2(12, 3f), 0.75f,
                    pBlockEntity.getProgressPercent(), pPoseStack, pBuffer, pPackedLight);
        }
    }
}
