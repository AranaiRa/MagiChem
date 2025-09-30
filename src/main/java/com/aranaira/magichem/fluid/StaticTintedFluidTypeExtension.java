package com.aranaira.magichem.fluid;

import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

public class StaticTintedFluidTypeExtension implements IClientFluidTypeExtensions {
    protected final FluidType type;
    private final int tint;

    public StaticTintedFluidTypeExtension(FluidType pType, int pTint) {
        this.type = pType;
        this.tint = pTint;
    }

    @Override
    public int getTintColor() {
        return tint;
    }
}
