package com.aranaira.magichem.capabilities.grime;

import com.aranaira.magichem.MagiChemMod;
import net.minecraft.resources.ResourceLocation;

public interface IGrimeCapability {
    ResourceLocation GRIME = new ResourceLocation(MagiChemMod.MODID, "grime");

    int getGrime();

    void setGrime(int value);
}
