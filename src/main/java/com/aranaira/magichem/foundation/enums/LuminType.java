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
}
