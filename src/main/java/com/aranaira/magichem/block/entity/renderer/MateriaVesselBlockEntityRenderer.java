package com.aranaira.magichem.block.entity.renderer;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.util.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

public class MateriaVesselBlockEntityRenderer implements BlockEntityRenderer<MateriaVesselBlockEntity> {
    public MateriaVesselBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    public static final ResourceLocation FLUID_TEXTURE = new ResourceLocation("minecraft", "block/water_still");
    public static final ResourceLocation LABEL_TEXTURE_ENDER_ARCANE   = new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_ender-arcane");
    public static final ResourceLocation LABEL_TEXTURE_EARTH_NIGREDO  = new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_earth-nigredo");
    public static final ResourceLocation LABEL_TEXTURE_WATER_ALBEDO   = new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_water-albedo");
    public static final ResourceLocation LABEL_TEXTURE_AIR_CITRINITAS = new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_air-citrinitas");
    public static final ResourceLocation LABEL_TEXTURE_FIRE_RUBEDO    = new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_fire-rubedo");
    public static final ResourceLocation LABEL_TEXTURE_CONCEPTUAL_VERDANT = new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_conceptual-verdant");
    public static final ResourceLocation LABEL_TEXTURE_FLESHY_NOURISHING  = new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_fleshy-nourishing");
    public static final ResourceLocation LABEL_TEXTURE_ROTTEN_MINERAL     = new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_rotten-mineral");
    public static final ResourceLocation LABEL_TEXTURE_WROUGHT_PRECIOUS   = new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_wrought-precious");
    public static final ResourceLocation LABEL_TEXTURE_BOOKENDS = new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_bookends");

    @Override
    public void render(MateriaVesselBlockEntity mvbe, float pPartialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if(mvbe.getCurrentStockPercent() > 0) {
            VertexConsumer buffer = bufferSource.getBuffer(RenderType.solid());

            PoseStack.Pose last = poseStack.last();
            renderFluidContents(last.pose(), last.normal(), buffer, mvbe.getCurrentStockPercent(), mvbe.getMateriaType().getMateriaColor());

            if(mvbe.getMateriaType() instanceof EssentiaItem ei) {
                renderEssentiaLabel(last.pose(), last.normal(), buffer, ei, mvbe.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
            }
        }
    }

    private static final float
        FLUID_START_XZ = 0.25F,
        FLUID_START_Y = 0.1875F,
        FLUID_WIDTH = 0.5F,
        FLUID_HEIGHT_MAX = 0.625F;

    private void renderFluidContents(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float fillAmount, int color) {
        float height = FLUID_HEIGHT_MAX * fillAmount;
        TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(FLUID_TEXTURE);

        RenderUtils.renderFace(Direction.UP, pose, normal, consumer, texture,
                FLUID_START_XZ, FLUID_START_XZ, FLUID_START_Y+height, FLUID_WIDTH, FLUID_WIDTH, color);

        RenderUtils.renderFace(Direction.DOWN, pose, normal, consumer, texture,
                FLUID_START_XZ, FLUID_START_XZ, 1.0f - FLUID_START_Y, FLUID_WIDTH, FLUID_WIDTH, color);

        RenderUtils.renderFace(Direction.NORTH, pose, normal, consumer, texture,
                FLUID_START_XZ, FLUID_START_Y, FLUID_START_XZ, FLUID_WIDTH, height, color);

        RenderUtils.renderFace(Direction.EAST, pose, normal, consumer, texture,
                FLUID_START_XZ, FLUID_START_Y, FLUID_START_XZ, FLUID_WIDTH, height, color);

        RenderUtils.renderFace(Direction.SOUTH, pose, normal, consumer, texture,
                FLUID_START_XZ, FLUID_START_Y, FLUID_START_XZ, FLUID_WIDTH, height, color);

        RenderUtils.renderFace(Direction.WEST, pose, normal, consumer, texture,
                FLUID_START_XZ, FLUID_START_Y, FLUID_START_XZ, FLUID_WIDTH, height, color);
    }

    private void renderEssentiaLabel(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, EssentiaItem ei, Direction dir) {

        TextureAtlasSprite textureMain = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(getTextureByEssentiaName(ei.getMateriaName()));
        TextureAtlasSprite textureBookend = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(LABEL_TEXTURE_BOOKENDS);

        Coords coords = getCoordsByEssentiaName(ei.getMateriaName());
        float x = coords.x;
        float y = coords.y;
        float z = 0.171875f;
        float w = coords.w;
        float h = coords.h;
        float u = coords.u;
        float uw = coords.uw;
        float v = coords.v;
        float vh = coords.vh;

        Coords bookendLeft = getLeftBookend(ei.getMateriaName(), false);
        float blx = bookendLeft.x;
        float bly = bookendLeft.y;
        float blz = 0.171875f;
        float blw = bookendLeft.w;
        float blh = bookendLeft.h;
        float blu = bookendLeft.u;
        float bluw = bookendLeft.uw;
        float blv = bookendLeft.v;
        float blvh = bookendLeft.vh;

        Coords bookendRight = getRightBookend(ei.getMateriaName(), false);
        float brx = bookendRight.x;
        float bry = bookendRight.y;
        float brz = 0.171875f;
        float brw = bookendRight.w;
        float brh = bookendRight.h;
        float bru = bookendRight.u;
        float bruw = bookendRight.uw;
        float brv = bookendRight.v;
        float brvh = bookendRight.vh;

        if (dir == Direction.NORTH) {
            u = coords.uw;
            uw = coords.u;
            v = coords.vh;
            vh = coords.v;
            blv = bookendLeft.vh;
            blvh = bookendLeft.v;
            brv = bookendRight.vh;
            brvh = bookendRight.v;

            dir = dir.getOpposite();
        } else if (dir == Direction.SOUTH) {
            dir = dir.getOpposite();
        } else if (dir == Direction.EAST) {
            u = coords.uw;
            uw = coords.u;
            v = coords.vh;
            vh = coords.v;
            blv = bookendLeft.vh;
            blvh = bookendLeft.v;
            brv = bookendRight.vh;
            brvh = bookendRight.v;
        }

        RenderUtils.renderFaceWithUV(dir.getOpposite(), pose, normal, consumer, textureMain,
                x, y, z, w, h, u, uw, v, vh, 0xFFFFFF
                );

        RenderUtils.renderFaceWithUV(dir.getOpposite(), pose, normal, consumer, textureBookend,
                blx, bly, blz, blw, blh, blu, bluw, blv, blvh, 0xFFFFFF
                );

        RenderUtils.renderFaceWithUV(dir.getOpposite(), pose, normal, consumer, textureBookend,
                brx, bry, brz, brw, brh, bru, bruw, brv, brvh, 0xFFFFFF
                );


    }

    private static ResourceLocation getTextureByEssentiaName(String in) {
        return switch(in) {
            case "ender", "arcane" -> LABEL_TEXTURE_ENDER_ARCANE;
            case "earth", "nigredo" -> LABEL_TEXTURE_EARTH_NIGREDO;
            case "water", "albedo" -> LABEL_TEXTURE_WATER_ALBEDO;
            case "air", "citrinitas" -> LABEL_TEXTURE_AIR_CITRINITAS;
            case "fire", "rubedo" -> LABEL_TEXTURE_FIRE_RUBEDO;
            case "conceptual", "verdant" -> LABEL_TEXTURE_CONCEPTUAL_VERDANT;
            case "fleshy", "nourishing" -> LABEL_TEXTURE_FLESHY_NOURISHING;
            case "rotten", "mineral" -> LABEL_TEXTURE_ROTTEN_MINERAL;
            case "wrought", "precious" -> LABEL_TEXTURE_WROUGHT_PRECIOUS;
            default -> new ResourceLocation("minecraft", "block/redstone_block");
        };
    }

    private static Coords getCoordsByEssentiaName(String in) {
        Coords out = switch(in) {
            case "nigredo", "albedo", "citrinitas", "rubedo" -> new Coords(0.359375f, 0.359375f, 0.28125f, 0.28125f);
            default -> new Coords(0.390625f, 0.359375f, 0.21875f, 0.28125f);
        };

        switch(in) {
            case "ender", "earth", "water", "air", "fire", "conceptual", "fleshy", "rotten", "wrought" -> out.setUV(0.0f, 0.4375f, 0.5625f, 0.0f);
            case "arcane", "verdant", "nourishing", "mineral", "precious" -> out.setUV(0.4375f, 0.875f, 0.5625f, 0.0f);
            case "nigredo", "albedo", "citrinitas", "rubedo" -> out.setUV(0.4375f, 1.0f, 0.5625f, 0.0f);
        }

        return out;
    }

    private static Coords getLeftBookend(String in, boolean flipV) {
        Coords out = switch(in) {
            case "nigredo", "albedo", "citrinitas", "rubedo" -> new Coords(0.265625f, 0.359375f, 0.09375f, 0.28125f);
            default -> new Coords(0.265625f, 0.359375f, 0.125f, 0.28125f);
        };

        switch(in) {
            case "nigredo", "albedo", "citrinitas", "rubedo" -> out.setUV(0.0f, 0.4375f, 0.5625f, 0.0f);
            default -> out.setUV(0.0f, 0.25f, 0.5625f, 0.0f);
        }

        return out;
    }

    private static Coords getRightBookend(String in, boolean flipV) {
        Coords out = switch(in) {
            case "nigredo", "albedo", "citrinitas", "rubedo" -> new Coords(0.640625f, 0.359375f, 0.09375f, 0.28125f);
            default -> new Coords(0.609375f, 0.359375f, 0.125f, 0.28125f);
        };

        switch(in) {
            case "nigredo", "albedo", "citrinitas", "rubedo" -> out.setUV(0.3125f, 0.5f, 0.5625f, 0.0f);
            default -> out.setUV(0.25f, 0.5f, 0.5625f, 0.0f);
        }

        return out;
    }
}
class Coords {
    public float x, y, w, h, u, uw, v, vh;
    public Coords(float pX, float pY, float pW, float pH) {
        this.x = pX;
        this.y = pY;
        this.w = pW;
        this.h = pH;
    }
    public Coords(float pX, float pY, float pW, float pH, float pU, float pUW, float pV, float pVH) {
        this.x = pX;
        this.y = pY;
        this.w = pW;
        this.h = pH;
        this.u = pU;
        this.uw = pUW;
        this.v = pV;
        this.vh = pVH;
    }

    public void setUV(float pU, float pUW, float pV, float pVH) {
        this.u = pU;
        this.uw = pUW;
        this.v = pV;
        this.vh = pVH;
    }
}