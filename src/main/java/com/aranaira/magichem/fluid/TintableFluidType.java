package com.aranaira.magichem.fluid;

import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

import java.util.function.Consumer;

public class TintableFluidType extends FluidType {
    private final int tint;

    public TintableFluidType(Properties pProperties, int pTint) {
        super(pProperties);

        this.tint = pTint;
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public int getTintColor() {
                return 0xffc15a36;//tint;
            }
        });
    }
}
