package com.aranaira.magichem.capabilities.enhancement;

import com.aranaira.magichem.capabilities.enhancement.IEnhancementCapability.EnhancedHeartType;
import com.aranaira.magichem.capabilities.wisdom.IWisdomCapability;
import com.mna.api.spells.attributes.Attribute;

import java.util.HashMap;

public class EnhancementCapability implements IEnhancementCapability {
    private EnhancedHeartType enhancedHeartType = EnhancedHeartType.NONE;

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
