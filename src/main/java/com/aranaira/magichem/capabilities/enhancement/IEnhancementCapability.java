package com.aranaira.magichem.capabilities.enhancement;

import com.aranaira.magichem.MagiChemMod;
import com.mna.api.spells.attributes.Attribute;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

public interface IEnhancementCapability {
    public static final String[] BLACKLIST = new String[]{"effect.mna.mana_stunt"};

    ResourceLocation ENHANCEMENT = new ResourceLocation(MagiChemMod.MODID, "enhancement");

    long getBossTrophyUseTargetTime();

    void setBossTrophyUseTargetTime(long pValue);

    boolean hasLastDeathTargetLocation();

    boolean hasDeathRecoveryLocation();

    Pair<BlockPos, ResourceLocation> getLastDeathTargetLocation();

    Pair<BlockPos, ResourceLocation> getDeathRecoveryLocation();

    void setLastDeathTargetLocation(BlockPos pPos, ResourceLocation pDim);

    void setDeathRecoveryLocation(BlockPos pPos, ResourceLocation pDim);

    void setHeart(EnhancedHeartType pHeartType);

    EnhancedHeartType getHeart();

    boolean hasHeart();

    void copyFrom(IEnhancementCapability pCapability);

    enum EnhancedHeartType {
        NONE,
        IMMORTAL
    }
}
