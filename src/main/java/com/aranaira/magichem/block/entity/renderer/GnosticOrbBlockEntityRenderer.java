package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.GnosticOrbBlockEntity;
import com.aranaira.magichem.util.render.ColorUtils;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.tools.math.Vector3;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
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

public class GnosticOrbBlockEntityRenderer implements BlockEntityRenderer<GnosticOrbBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_BODY = new ResourceLocation(MagiChemMod.MODID, "obj/special/gnostic_orb_body");
    public static final ResourceLocation RENDERER_MODEL_ACTIVE = new ResourceLocation(MagiChemMod.MODID, "obj/special/gnostic_orb_active");
    public static final ResourceLocation RENDERER_MODEL_INACTIVE = new ResourceLocation(MagiChemMod.MODID, "obj/special/gnostic_orb_inactive");
    public static final ResourceLocation CIRCLE_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/actuator_water");

    final TextureAtlasSprite circleTexture;

    public GnosticOrbBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        circleTexture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CIRCLE_TEXTURE);
    }

    @Override
    public void render(GnosticOrbBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        int period = 850;
        int gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
        float rot = -(((gt + pPartialTick) % period) / period) * (float)Math.PI * 2;

        pPoseStack.pushPose();

        pPoseStack.translate(0.5, 0, 0.5);
        pPoseStack.mulPose(Axis.YP.rotation(rot));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_BODY, pPoseStack, pPackedLight, pPackedOverlay);
        if(pBlockEntity.hasProphecyCooking())
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_ACTIVE, pPoseStack, pPackedLight, pPackedOverlay, ColorUtils.getRGBAFloatTintFromPackedInt(pBlockEntity.getMateriaColor()));
        else
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_INACTIVE, pPoseStack, pPackedLight, pPackedOverlay);

        pPoseStack.popPose();

        pPoseStack.pushPose();

        gt = (int)(pBlockEntity.getLevel().getGameTime() % (period * 2));
        rot = -(((gt + pPartialTick) % period) / period) * (float)Math.PI * 2;

        float circlePercent = pBlockEntity.getProgressPercent();

        pPoseStack.scale(0.5f, 0.5f, 0.5f);
        RenderUtils.generateMagicCircleRing(new Vector3(1.0, 1.0, 1.0),
                7, 1.25f, 0.375f, rot, circleTexture,
                new Vec2(0, 0), new Vec2(12, 3f), 0.75f,
                circlePercent, pPoseStack, pBuffer, pPackedLight);
        pPoseStack.scale(2.0f, 2.0f, 2.0f);

        pPoseStack.popPose();
    }
}
