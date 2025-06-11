package com.aranaira.magichem.fluid;

import net.minecraftforge.fluids.FluidType;

public class AcidFluidType extends FluidType {
    private int acidStrength = -1;

    public AcidFluidType(Properties properties, int pAcidStrength) {
        super(properties);
        acidStrength = pAcidStrength;
    }

    public int getAcidStrength() {
        return acidStrength;
    }
}
