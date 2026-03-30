package com.aranaira.magichem.capabilities.enhancement;

import com.aranaira.magichem.capabilities.enhancement.IEnhancementCapability.EnhancedHeartType;
import com.aranaira.magichem.capabilities.wisdom.IWisdomCapability;
import com.mna.api.spells.attributes.Attribute;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

public class EnhancementCapability implements IEnhancementCapability {
    private EnhancedHeartType enhancedHeartType = EnhancedHeartType.NONE;
    private long worldTimeForNextTrophyUse;
    private BlockPos lastDeathPos = null, deathRecoveryPos = null;
    private ResourceLocation lastDeathDim = null, deathRecoveryDim = null;

    @Override
    public long getBossTrophyUseTargetTime() {
        return worldTimeForNextTrophyUse;
    }

    @Override
    public void setBossTrophyUseTargetTime(long pValue) {
        worldTimeForNextTrophyUse = pValue;
    }

    @Override
    public boolean hasLastDeathTargetLocation() {
        return !(lastDeathPos == null || lastDeathDim == null);
    }

    @Override
    public boolean hasDeathRecoveryLocation() {
        return !(deathRecoveryPos == null || deathRecoveryDim == null);
    }

    @Override
    public Pair<BlockPos, ResourceLocation> getLastDeathTargetLocation() {
        return new Pair<>(lastDeathPos, lastDeathDim);
    }

    @Override
    public Pair<BlockPos, ResourceLocation> getDeathRecoveryLocation() {
        return new Pair<>(deathRecoveryPos, deathRecoveryDim);
    }

    @Override
    public void setLastDeathTargetLocation(BlockPos pPos, ResourceLocation pDim) {
        lastDeathPos = pPos;
        lastDeathDim = pDim;
    }

    @Override
    public void setDeathRecoveryLocation(BlockPos pPos, ResourceLocation pDim) {
        deathRecoveryPos = pPos;
        deathRecoveryDim = pDim;
    }

    @Override
    public void setHeart(EnhancedHeartType pHeartType) {
        enhancedHeartType = pHeartType;
    }

    @Override
    public EnhancedHeartType getHeart() {
        return enhancedHeartType;
    }

    @Override
    public boolean hasHeart() {
        return enhancedHeartType != null && enhancedHeartType != EnhancedHeartType.NONE;
    }

    @Override
    public void copyFrom(IEnhancementCapability pCapability) {
        enhancedHeartType = pCapability.getHeart();
    }
}
