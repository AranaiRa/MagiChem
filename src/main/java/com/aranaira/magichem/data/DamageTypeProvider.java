package com.aranaira.magichem.data;

import com.aranaira.magichem.MagiChemMod;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageType;

import static com.aranaira.magichem.data.MagiChemDamageTypes.ACID;
import static com.aranaira.magichem.data.MagiChemDamageTypes.SURGERY;

public class DamageTypeProvider implements RegistrySetBuilder.RegistryBootstrap<DamageType> {

    public static void register(RegistrySetBuilder rsb) {
        rsb.add(Registries.DAMAGE_TYPE, new DamageTypeProvider());
    }

    @Override
    public void run(BootstapContext<DamageType> pContext) {
        pContext.register(ACID, new DamageType(MagiChemMod.MODID+".acid", 0.1f, DamageEffects.BURNING));
        pContext.register(SURGERY, new DamageType(MagiChemMod.MODID+".surgery", 4.0f, DamageEffects.POKING));
    }
}
