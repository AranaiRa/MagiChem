package com.aranaira.magichem.data;

import com.aranaira.magichem.MagiChemMod;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class MagiChemDamageTypes {
    public static final ResourceKey<DamageType> ACID = create("acid");

    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MagiChemMod.MODID, name));
    }

    public static DamageSource source(RegistryAccess pAccess, ResourceKey<DamageType> pType, @Nullable Entity pDirect, @Nullable Entity pCausing) {
        return new DamageSource(pAccess.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(pType), pDirect, pCausing);
    }

    public static DamageSource source(RegistryAccess pAccess, ResourceKey<DamageType> pType, @Nullable Entity pEntity) {
        return new DamageSource(pAccess.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(pType), pEntity, pEntity);
    }

    public static DamageSource source(RegistryAccess pAccess, ResourceKey<DamageType> pType) {
        return new DamageSource(pAccess.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(pType), null, null);
    }
}
