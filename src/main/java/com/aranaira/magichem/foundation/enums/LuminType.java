package com.aranaira.magichem.foundation.enums;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

public enum LuminType {
    NONE,
    SOLAR,
    LUNAR,
    SIDEREAL;

    public static LuminType luminTypeFromOrdinal(int pOrdinal) {
        if(pOrdinal == 1) return SOLAR;
        else if(pOrdinal == 2) return LUNAR;
        else if(pOrdinal == 3) return SIDEREAL;
        return NONE;
    }

    public static MutableComponent luminComponentFromOrdinal(int pOrdinal) {
        if(pOrdinal == 1) return Component.translatable("mechanic.lumins.solar");
        else if(pOrdinal == 2) return Component.translatable("mechanic.lumins.lunar");
        else if(pOrdinal == 3) return Component.translatable("mechanic.lumins.sidereal");
        return Component.empty();
    }

    public static ChatFormatting luminComponentFormattingFromOrdinal(int pOrdinal) {
        if(pOrdinal == 1) return ChatFormatting.GOLD;
        else if(pOrdinal == 2) return ChatFormatting.GRAY;
        else if(pOrdinal == 3) return ChatFormatting.BLUE;
        return ChatFormatting.RESET;
    }

    public static int[] getParticleColor(LuminType pType) {
        if(pType == SOLAR) return new int[]{240,170,15};
        else if(pType == LUNAR) return new int[]{150,150,170};
        else if(pType == SIDEREAL) return new int[]{85,100,240};

        return new int[]{0,0,0};
    }
}
