package com.aranaira.magichem.capabilities.grime;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ext.AbstractBlockEntityWithEfficiency;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class GrimeProvider implements ICapabilitySerializable<Tag> {
    public static final Capability<IGrimeCapability> GRIME = CapabilityManager.get(new CapabilityToken<>() {} );

    private final LazyOptional<IGrimeCapability> holder = LazyOptional.of(GrimeCapability::new);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return GRIME.orEmpty(cap, this.holder);
    }

    @Override
    public Tag serializeNBT() {
        IGrimeCapability instance = this.holder.orElse(new GrimeCapability());
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("grime", instance.getGrime());
        return nbt;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        IGrimeCapability instance = this.holder.orElse(new GrimeCapability());
        if(nbt instanceof CompoundTag ct) {
            if(ct.contains("grime")) {
                int grime = ct.getInt("grime");
                instance.setGrime(grime);
            }
        }
    }

    public static IGrimeCapability getCapability(AbstractBlockEntityWithEfficiency entity) {
        Optional<IGrimeCapability> grimeCapability = entity.getCapability(GrimeProvider.GRIME).resolve();
        if(grimeCapability.isEmpty()) {
            entity.setRemoved();
            String errorMessage = "TileEntity at "+entity.getBlockPos()+" had no Grime capability!";
            MagiChemMod.LOGGER.error(errorMessage);
            throw new Error(errorMessage);
        }
        return grimeCapability.get();
    }
}
