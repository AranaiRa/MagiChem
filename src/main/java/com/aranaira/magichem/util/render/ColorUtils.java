package com.aranaira.magichem.util.render;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.DyeColor;

public class ColorUtils {
    private static final int[]
        INT_RED        = {255, 50, 50},
        INT_ORANGE     = {255, 140, 50},
        INT_YELLOW     = {255, 255, 50},
        INT_LIME       = {170, 255, 50},
        INT_GREEN      = {50, 200, 70},
        INT_CYAN       = {50, 230, 250},
        INT_LIGHT_BLUE = {90, 180, 255},
        INT_BLUE       = {25, 120, 255},
        INT_PURPLE     = {130, 90, 255},
        INT_PINK       = {255, 100, 170},
        INT_BROWN      = {170, 80, 50},
        INT_BLACK      = {50, 50, 50},
        INT_GRAY       = {120, 120, 120},
        INT_LIGHT_GRAY = {170, 170, 170},
        INT_WHITE      = {255, 255, 255};
    private static final float[]
        FLOAT_RED        = {1.00f, 0.20f, 0.20f},
        FLOAT_ORANGE     = {1.00f, 0.55f, 0.20f},
        FLOAT_YELLOW     = {1.00f, 1.00f, 0.20f},
        FLOAT_LIME       = {0.67f, 1.00f, 0.20f},
        FLOAT_GREEN      = {0.20f, 0.78f, 0.27f},
        FLOAT_CYAN       = {0.20f, 0.90f, 1.00f},
        FLOAT_LIGHT_BLUE = {0.35f, 0.71f, 1.00f},
        FLOAT_BLUE       = {0.10f, 0.47f, 1.00f},
        FLOAT_PURPLE     = {0.51f, 0.35f, 1.00f},
        FLOAT_PINK       = {1.00f, 0.39f, 0.67f},
        FLOAT_BROWN      = {0.67f, 0.31f, 0.20f},
        FLOAT_BLACK      = {0.20f, 0.20f, 0.20f},
        FLOAT_GRAY       = {0.47f, 0.47f, 0.47f},
        FLOAT_LIGHT_GRAY = {0.67f, 0.67f, 0.67f},
        FLOAT_WHITE      = {1.00f, 1.00f, 1.00f};

    public static int[] getRGBIntTint(DyeColor pColorCode) {
        return switch(pColorCode) {
            case RED        -> INT_RED;
            case ORANGE     -> INT_ORANGE;
            case YELLOW     -> INT_YELLOW;
            case LIME       -> INT_LIME;
            case GREEN      -> INT_GREEN;
            case CYAN       -> INT_CYAN;
            case LIGHT_BLUE -> INT_LIGHT_BLUE;
            case BLUE       -> INT_BLUE;
            case PURPLE     -> INT_PURPLE;
            case PINK       -> INT_PINK;
            case BROWN      -> INT_BROWN;
            case BLACK      -> INT_BLACK;
            case GRAY       -> INT_GRAY;
            case LIGHT_GRAY -> INT_LIGHT_GRAY;
            default         -> INT_WHITE;
        };
    }

    public static int[] getRGBAIntTint(DyeColor pColorCode, int pAlpha) {
        int[] out = getRGBIntTint(pColorCode);
        return new int[]{out[0], out[1], out[2], pAlpha};
    }

    public static int[] getARGBIntTint(DyeColor pColorCode, int pAlpha) {
        int[] out = getRGBIntTint(pColorCode);
        return new int[]{pAlpha, out[0], out[1], out[2]};
    }

    public static float[] getRGBFloatTint(DyeColor pColorCode) {
        return switch(pColorCode) {
            case RED        -> FLOAT_RED;
            case ORANGE     -> FLOAT_ORANGE;
            case YELLOW     -> FLOAT_YELLOW;
            case LIME       -> FLOAT_LIME;
            case GREEN      -> FLOAT_GREEN;
            case CYAN       -> FLOAT_CYAN;
            case LIGHT_BLUE -> FLOAT_LIGHT_BLUE;
            case BLUE       -> FLOAT_BLUE;
            case PURPLE     -> FLOAT_PURPLE;
            case PINK       -> FLOAT_PINK;
            case BROWN      -> FLOAT_BROWN;
            case BLACK      -> FLOAT_BLACK;
            case GRAY       -> FLOAT_GRAY;
            case LIGHT_GRAY -> FLOAT_LIGHT_GRAY;
            default         -> FLOAT_WHITE;
        };
    }

    public static float[] getRGBAFloatTint(DyeColor pColorCode, float pAlpha) {
        float[] out = getRGBFloatTint(pColorCode);
        return new float[]{out[0], out[1], out[2], pAlpha};
    }

    public static float[] getARGBFloatTint(DyeColor pColorCode, float pAlpha) {
        float[] out = getRGBFloatTint(pColorCode);
        return new float[]{pAlpha, out[0], out[1], out[2]};
    }
}
