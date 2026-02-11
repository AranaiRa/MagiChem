package com.aranaira.magichem.capabilities.enhancement;

import com.aranaira.magichem.MagiChemMod;
import com.mna.api.spells.attributes.Attribute;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

public interface IEnhancementCapability {
    ResourceLocation ENHANCEMENT = new ResourceLocation(MagiChemMod.MODID, "enhancement");

    void setHeart(EnhancedHeartType pHeartType);

    EnhancedHeartType getHeart();

    boolean hasHeart();

    void copyFrom(IEnhancementCapability pCapability);

    enum EnhancedHeartType {
        NONE,
        IMMORTAL
    }
}
