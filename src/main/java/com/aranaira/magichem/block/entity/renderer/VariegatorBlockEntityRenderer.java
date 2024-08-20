package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.VariegatorBlockEntity;
import com.aranaira.magichem.foundation.MagiChemBlockStateProperties;
import com.aranaira.magichem.util.render.ColorUtils;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mna.items.ItemInit;
import com.mna.particles.types.render.ParticleRenderTypes;
import com.mna.tools.math.Vector3;
import com.mna.tools.render.MARenderTypes;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class VariegatorBlockEntityRenderer implements BlockEntityRenderer<VariegatorBlockEntity> {
    public static final ResourceLocation RENDERER_MODEL_LIMB_ABOVE = new ResourceLocation(MagiChemMod.MODID, "obj/special/variegator_limb_above");
    public static final ResourceLocation RENDERER_MODEL_LIMB_ABOVE_TINTABLE = new ResourceLocation(MagiChemMod.MODID, "obj/special/variegator_limb_above_tintable");
    public static final ResourceLocation RENDERER_MODEL_LIMB_LONG = new ResourceLocation(MagiChemMod.MODID, "obj/special/variegator_limb_long");
    public static final ResourceLocation RENDERER_MODEL_LIMB_LONG_TINTABLE = new ResourceLocation(MagiChemMod.MODID, "obj/special/variegator_limb_long_tintable");
    public static final ResourceLocation RENDERER_MODEL_LIMB_SHORT = new ResourceLocation(MagiChemMod.MODID, "obj/special/variegator_limb_short");
    public static final ResourceLocation RENDERER_MODEL_LIMB_SHORT_TINTABLE = new ResourceLocation(MagiChemMod.MODID, "obj/special/variegator_limb_short_tintable");
    public static final ResourceLocation RENDERER_MODEL_SHARD = new ResourceLocation(MagiChemMod.MODID, "obj/special/variegator_shard");
    public static final ResourceLocation RENDERER_MODEL_SHELL_TINTABLE = new ResourceLocation(MagiChemMod.MODID, "obj/special/variegator_shell_tintable");
    public static final ResourceLocation BODY_TEXTURE = new ResourceLocation(MagiChemMod.MODID, "block/grand_distillery");

    private static final int
            SHARD_ROT_X_PERIOD = 300, SHARD_ROT_Z_PERIOD = 450, SHARD_BOB_PERIOD = 400, SHARD_DRIFT_PERIOD = 600, SHARD_ORBIT_PERIOD = 300,
            CENTER_CRYSTAL_ROT_PERIOD = 400, CENTER_CRYSTAL_BOB_PERIOD = 400, ITEM_ROTATE_PERIOD = 800;
    private static final float
            SHARD_BOB_HEIGHT = 0.03125f, SHARD_DRIFT_DISTANCE = 0.0625f,
            CENTER_CRYSTAL_BOB_HEIGHT = 0.0625f,
            BEAM_TEX_U1 = 0.0f, BEAM_TEX_V1 = 0.515625f, BEAM_TEX_U2 = 0.015625f, BEAM_TEX_V2 = 0.53125f;
    private static final DyeColor[] COLOR_GUI_ORDER = {
            DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.GRAY, DyeColor.BLACK,
            DyeColor.BROWN, DyeColor.RED, DyeColor.ORANGE, DyeColor.YELLOW,
            DyeColor.LIME, DyeColor.GREEN, DyeColor.CYAN, DyeColor.LIGHT_BLUE,
            DyeColor.BLUE, DyeColor.PURPLE, DyeColor.MAGENTA, DyeColor.PINK
    };

    final TextureAtlasSprite bodyTexture;

    public VariegatorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        bodyTexture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(BODY_TEXTURE);
    }

    @Override
    public void render(VariegatorBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        float rotL = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % 180) / 180f) * 360f;
        float rotS = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % 360) / 360f) * -360f;
        int colorID = pBlockEntity.selectedColor;
        DyeColor color;
        if(colorID != -1)
            color = COLOR_GUI_ORDER[colorID];
        else
            color = DyeColor.WHITE; //temp

        pPoseStack.pushPose();
        if(!pBlockEntity.getBlockState().getValue(MagiChemBlockStateProperties.GROUNDED)){
            pPoseStack.mulPose(Axis.XP.rotationDegrees(180));
            pPoseStack.translate(0, -1, -1);
        }

        this.renderItem(pBlockEntity, pPoseStack, pBuffer, pPartialTick, pPackedLight, pPackedOverlay, rotL, rotS);
        this.renderLimbs(pBlockEntity, pPoseStack, pBuffer, pPartialTick, pPackedLight, pPackedOverlay, color, rotL, rotS);
        this.renderBeams(pBlockEntity, pPoseStack, pBuffer, pPartialTick, pPackedLight, pPackedOverlay, color, rotL, rotS);
        this.renderShards(pBlockEntity, pPoseStack, pBuffer, pPartialTick, pPackedLight, pPackedOverlay, color);
        this.renderColorShell(pBlockEntity, pPoseStack, pBuffer, pPartialTick, pPackedLight, pPackedOverlay, color);

        pPoseStack.popPose();
    }

    private void renderItem(VariegatorBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, float pPartialTick, int pPackedLight, int pPackedOverlay, float pRotL, float pRotS) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        ItemStack stack = VariegatorBlockEntity.getCurrentProcessingStack(pBlockEntity);

        if(stack == ItemStack.EMPTY) {
            SimpleContainer outputs = pBlockEntity.getContentsOfOutputSlots();
            for(int i=outputs.getContainerSize()-1; i>=0; i--) {
                stack = outputs.getItem(i);
                if(stack != ItemStack.EMPTY)
                    break;
            }
        }

        if(stack.getItem() instanceof BlockItem) {
            float rotY = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % ITEM_ROTATE_PERIOD) / ITEM_ROTATE_PERIOD) * 360f;

            pPoseStack.pushPose();

            pPoseStack.translate(0.5, 1.5, 0.5);
            pPoseStack.scale(0.25f, 0.25f, 0.25f);
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(45));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(35));
            pPoseStack.mulPose(Axis.YP.rotationDegrees(rotY));
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, world, 0);

            pPoseStack.popPose();
        } else if(stack != ItemStack.EMPTY) {
            float rotY = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % ITEM_ROTATE_PERIOD) / ITEM_ROTATE_PERIOD) * 360f;

            pPoseStack.pushPose();

            pPoseStack.translate(0.5, 1.5, 0.5);
            pPoseStack.scale(0.25f, 0.25f, 0.25f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(rotY));
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, world, 0);

            pPoseStack.popPose();
        }
    }

    private void renderLimbs(VariegatorBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, float pPartialTick, int pPackedLight, int pPackedOverlay, DyeColor pColor, float pLongLimbThetaD, float pShortLimbThetaD) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        float rotY = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % CENTER_CRYSTAL_ROT_PERIOD) / CENTER_CRYSTAL_ROT_PERIOD) * -360f;
        float bob = (float)Math.sin(((pBlockEntity.getLevel().getGameTime() + pPartialTick) % CENTER_CRYSTAL_BOB_PERIOD) / (float)CENTER_CRYSTAL_BOB_PERIOD * Math.PI * 2) * CENTER_CRYSTAL_BOB_HEIGHT;

        pPoseStack.pushPose();

        pPoseStack.translate(0.5, 0, 0.5);

        pPoseStack.pushPose();
        pPoseStack.translate(0, bob, 0);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_LIMB_ABOVE, pPoseStack, pPackedLight, pPackedOverlay);
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_LIMB_ABOVE_TINTABLE, pPoseStack, pPackedLight, pPackedOverlay, ColorUtils.getRGBAFloatTint(pColor, 1.0f));
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.mulPose(Axis.YP.rotationDegrees(pLongLimbThetaD));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_LIMB_LONG, pPoseStack, pPackedLight, pPackedOverlay);
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_LIMB_LONG_TINTABLE, pPoseStack, pPackedLight, pPackedOverlay, ColorUtils.getRGBAFloatTint(pColor, 1.0f));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_LIMB_LONG, pPoseStack, pPackedLight, pPackedOverlay);
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_LIMB_LONG_TINTABLE, pPoseStack, pPackedLight, pPackedOverlay, ColorUtils.getRGBAFloatTint(pColor, 1.0f));
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.mulPose(Axis.YP.rotationDegrees(pShortLimbThetaD));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_LIMB_SHORT, pPoseStack, pPackedLight, pPackedOverlay);
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_LIMB_SHORT_TINTABLE, pPoseStack, pPackedLight, pPackedOverlay, ColorUtils.getRGBAFloatTint(pColor, 1.0f));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(120));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_LIMB_SHORT, pPoseStack, pPackedLight, pPackedOverlay);
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_LIMB_SHORT_TINTABLE, pPoseStack, pPackedLight, pPackedOverlay, ColorUtils.getRGBAFloatTint(pColor, 1.0f));
        pPoseStack.mulPose(Axis.YP.rotationDegrees(120));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_LIMB_SHORT, pPoseStack, pPackedLight, pPackedOverlay);
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_LIMB_SHORT_TINTABLE, pPoseStack, pPackedLight, pPackedOverlay, ColorUtils.getRGBAFloatTint(pColor, 1.0f));
        pPoseStack.popPose();

        pPoseStack.popPose();
    }

    private void renderBeams(VariegatorBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, float pPartialTick, int pPackedLight, int pPackedOverlay, DyeColor pColor, float pLongLimbThetaD, float pShortLimbThetaD) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        //temp until animation drivers are set up
        {
            ItemStack stack = VariegatorBlockEntity.getCurrentProcessingStack(pBlockEntity);

            if(stack == ItemStack.EMPTY) {
                SimpleContainer outputs = pBlockEntity.getContentsOfOutputSlots();
                for(int i=outputs.getContainerSize()-1; i>=0; i--) {
                    stack = outputs.getItem(i);
                    if(stack != ItemStack.EMPTY)
                        break;
                }
            }

            if(pBlockEntity.beamFill == 0)
                return;
        }

        Vector3 endPos = new Vector3(0.501, 1.5, 0.5);

        RenderUtils.generateLinearVolumetricBeam(new Vector3(0.5, 1.96875, 0.5), endPos, 0.0078125f, bodyTexture,
                ColorUtils.getRGBAIntTint(pColor, 255), pBlockEntity.beamFill + VariegatorBlockEntity.FILL_RATE * pPartialTick, pPoseStack, pBuffer, pPackedLight,
                BEAM_TEX_U1, BEAM_TEX_V1, BEAM_TEX_U2, BEAM_TEX_V2);

        //Long limbs
        for(int i=0; i<2; i++) {
            double x = Math.sin(Math.toRadians(pLongLimbThetaD + 180*i)) * 0.404197;
            double z = Math.cos(Math.toRadians(pLongLimbThetaD + 180*i)) * 0.404197;

            RenderUtils.generateLinearVolumetricBeam(new Vector3(0.5 + x, 1.66742, 0.5 + z), endPos, 0.0078125f, bodyTexture,
                    ColorUtils.getRGBAIntTint(pColor, 255), pBlockEntity.beamFill + VariegatorBlockEntity.FILL_RATE * pPartialTick, pPoseStack, pBuffer, pPackedLight,
                    BEAM_TEX_U1, BEAM_TEX_V1, BEAM_TEX_U2, BEAM_TEX_V2);
        }

        //Short limbs
        for(int i=0; i<3; i++) {
            double x = Math.sin(Math.toRadians(pShortLimbThetaD - 120*i)) * 0.404197;
            double z = Math.cos(Math.toRadians(pShortLimbThetaD - 120*i)) * 0.404197;

            RenderUtils.generateLinearVolumetricBeam(new Vector3(0.5 + x, 1.33258, 0.5 + z), endPos, 0.0078125f, bodyTexture,
                    ColorUtils.getRGBAIntTint(pColor, 255), pBlockEntity.beamFill + VariegatorBlockEntity.FILL_RATE * pPartialTick, pPoseStack, pBuffer, pPackedLight,
                    BEAM_TEX_U1, BEAM_TEX_V1, BEAM_TEX_U2, BEAM_TEX_V2);
        }


    }

    private void renderShards(VariegatorBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, float pPartialTick, int pPackedLight, int pPackedOverlay, DyeColor pColor) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        float rotX = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % SHARD_ROT_X_PERIOD) / SHARD_ROT_X_PERIOD) * 360f;
        float rotY = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % SHARD_ORBIT_PERIOD) / SHARD_ORBIT_PERIOD) * 360f;
        float rotZ = (((pBlockEntity.getLevel().getGameTime() + pPartialTick) % SHARD_ROT_Z_PERIOD) / SHARD_ROT_Z_PERIOD) * 360f;
        float drift = (float)Math.sin(((pBlockEntity.getLevel().getGameTime() + pPartialTick) % SHARD_DRIFT_PERIOD) / (float)SHARD_DRIFT_PERIOD * Math.PI * 2) * SHARD_DRIFT_DISTANCE;
        float bob = (float)Math.sin(((pBlockEntity.getLevel().getGameTime() + pPartialTick) % SHARD_BOB_PERIOD) / (float)SHARD_BOB_PERIOD * Math.PI * 2) * SHARD_BOB_HEIGHT;

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0, 0.5);
        for(int i=0; i<6; i++) {
            int flipFlop = i % 2 == 0 ? -1 : 1;

            pPoseStack.pushPose();
            pPoseStack.mulPose(Axis.YP.rotationDegrees(-rotY + 60*i));
            pPoseStack.translate(0.375 + (drift * -flipFlop), 2 + (bob * flipFlop), 0);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(rotX));
            pPoseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));
            ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SHARD, pPoseStack, pPackedLight, pPackedOverlay, ColorUtils.getRGBAFloatTint(pColor, 1.0f));
            pPoseStack.popPose();
        }
        pPoseStack.popPose();
    }

    private void renderColorShell(VariegatorBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, float pPartialTick, int pPackedLight, int pPackedOverlay, DyeColor pColor) {
        Level world = pBlockEntity.getLevel();
        BlockPos pos = pBlockEntity.getBlockPos();
        BlockState state = pBlockEntity.getBlockState();

        float fill = pBlockEntity.beamFill;

        pPoseStack.pushPose();

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0.625, 0.5);
        pPoseStack.scale(1.0f, fill, 1.0f);
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SHELL_TINTABLE, pPoseStack, pPackedLight, pPackedOverlay, ColorUtils.getRGBAFloatTint(pColor, 1.0f));
        pPoseStack.popPose();
        pPoseStack.translate(0.5, 0.4375, 0.5);
        pPoseStack.scale(1.0f, fill, 1.0f);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(180));
        ModelUtils.renderModel(pBuffer, world, pos, state, RENDERER_MODEL_SHELL_TINTABLE, pPoseStack, pPackedLight, pPackedOverlay, ColorUtils.getRGBAFloatTint(pColor, 1.0f));

        pPoseStack.popPose();
    }
}
