package com.aranaira.magichem.util.render;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.BlockRendererCoords;
import com.aranaira.magichem.item.EssentiaItem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class MateriaVesselContentsRenderUtil {
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

    private static final float
            FLUID_START_XZ = 0.25F,
            FLUID_START_Y = 0.1875F,
            FLUID_WIDTH = 0.5F,
            FLUID_HEIGHT_MAX = 0.625F;

    public static void renderFluidContents(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float fillAmount, int color) {
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

    public static void renderEssentiaLabel(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, EssentiaItem ei, Direction dir) {

        TextureAtlasSprite textureMain = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(getTextureByEssentiaName(ei.getMateriaName()));
        TextureAtlasSprite textureBookend = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(LABEL_TEXTURE_BOOKENDS);

        BlockRendererCoords coords = getCoordsByEssentiaName(ei.getMateriaName());
        float x = coords.x;
        float y = coords.y;
        float z = 0.171875f;
        float w = coords.w;
        float h = coords.h;
        float u = coords.u;
        float uw = coords.uw;
        float v = coords.v;
        float vh = coords.vh;

        BlockRendererCoords bookendLeft = getLeftBookend(ei.getMateriaName());
        float blx = bookendLeft.x;
        float bly = bookendLeft.y;
        float blz = 0.171875f;
        float blw = bookendLeft.w;
        float blh = bookendLeft.h;
        float blu = bookendLeft.u;
        float bluw = bookendLeft.uw;
        float blv = bookendLeft.v;
        float blvh = bookendLeft.vh;

        BlockRendererCoords bookendRight = getRightBookend(ei.getMateriaName());
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
                x, y, z, w, h, u, uw, v, vh, 0xFFFFFFFF
        );

        RenderUtils.renderFaceWithUV(dir.getOpposite(), pose, normal, consumer, textureBookend,
                blx, bly, blz, blw, blh, blu, bluw, blv, blvh, 0xFFFFFFFF
        );

        RenderUtils.renderFaceWithUV(dir.getOpposite(), pose, normal, consumer, textureBookend,
                brx, bry, brz, brw, brh, bru, bruw, brv, brvh, 0xFFFFFFFF
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

    private static BlockRendererCoords getCoordsByEssentiaName(String in) {
        BlockRendererCoords out = switch(in) {
            case "nigredo", "albedo", "citrinitas", "rubedo" -> new BlockRendererCoords(0.359375f, 0.359375f, 0.28125f, 0.28125f);
            default -> new BlockRendererCoords(0.390625f, 0.359375f, 0.21875f, 0.28125f);
        };

        switch(in) {
            case "ender", "earth", "water", "air", "fire", "conceptual", "fleshy", "rotten", "wrought" -> out.setUV(0.0f, 0.4375f, 0.5625f, 0.0f);
            case "arcane", "verdant", "nourishing", "mineral", "precious" -> out.setUV(0.4375f, 0.875f, 0.5625f, 0.0f);
            case "nigredo", "albedo", "citrinitas", "rubedo" -> out.setUV(0.4375f, 1.0f, 0.5625f, 0.0f);
        }

        return out;
    }

    private static BlockRendererCoords getLeftBookend(String in) {
        BlockRendererCoords out = switch(in) {
            case "nigredo", "albedo", "citrinitas", "rubedo" -> new BlockRendererCoords(0.265625f, 0.359375f, 0.09375f, 0.28125f);
            default -> new BlockRendererCoords(0.265625f, 0.359375f, 0.125f, 0.28125f);
        };

        switch(in) {
            case "nigredo", "albedo", "citrinitas", "rubedo" -> out.setUV(0.0f, 0.4375f, 0.5625f, 0.0f);
            default -> out.setUV(0.0f, 0.25f, 0.5625f, 0.0f);
        }

        return out;
    }

    private static BlockRendererCoords getRightBookend(String in) {
        BlockRendererCoords out = switch(in) {
            case "nigredo", "albedo", "citrinitas", "rubedo" -> new BlockRendererCoords(0.640625f, 0.359375f, 0.09375f, 0.28125f);
            default -> new BlockRendererCoords(0.609375f, 0.359375f, 0.125f, 0.28125f);
        };

        switch(in) {
            case "nigredo", "albedo", "citrinitas", "rubedo" -> out.setUV(0.3125f, 0.5f, 0.5625f, 0.0f);
            default -> out.setUV(0.25f, 0.5f, 0.5625f, 0.0f);
        }

        return out;
    }
}
