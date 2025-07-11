package com.aranaira.magichem.registry;

import com.mna.api.entities.construct.FluidParameterRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.api.sound.SFX;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ConstructFluidSprayRegistry {
    public static void register() {
        final FluidParameterRegistry instance = FluidParameterRegistry.INSTANCE;

        instance.register(new FluidParameterRegistry.FluidParameter(
                FluidRegistry.SIMPLE_ACID.get(), false, true, SFX.Entity.Construct.SPRAY_GENERIC,
                new MAParticleType((ParticleType) ParticleInit.DUST.get()).setColor(74, 99, 26), 0.5f, 20,
                (living, isFriendly, construct) -> {
                    return !isFriendly && ConstructFluidSprayRegistry.canAffectWithAcid(living, 1);
                }, (vel) -> {
                    Vec3 adjusted = vel.yRot((float)(-0.25D + Math.random() * 0.5D)).normalize().scale(0.75D + Math.random() * 0.25D);
                    adjusted = adjusted.subtract(0.0D, adjusted.y, 0.0D);
                    return adjusted;
                }
        ));
        instance.register(new FluidParameterRegistry.FluidParameter(
                FluidRegistry.AQUA_FORTIS.get(), false, true, SFX.Entity.Construct.SPRAY_GENERIC,
                new MAParticleType((ParticleType) ParticleInit.DUST.get()).setColor(81, 89, 91), 0.5f, 20,
                (living, isFriendly, construct) -> {
                    return !isFriendly && ConstructFluidSprayRegistry.canAffectWithAcid(living, 2);
                }, (vel) -> {
                    Vec3 adjusted = vel.yRot((float)(-0.25D + Math.random() * 0.5D)).normalize().scale(0.75D + Math.random() * 0.25D);
                    adjusted = adjusted.subtract(0.0D, adjusted.y, 0.0D);
                    return adjusted;
                }
        ));
        instance.register(new FluidParameterRegistry.FluidParameter(
                FluidRegistry.AQUA_REGIA.get(), false, true, SFX.Entity.Construct.SPRAY_GENERIC,
                new MAParticleType((ParticleType) ParticleInit.DUST.get()).setColor(113, 105, 90), 0.5f, 20,
                (living, isFriendly, construct) -> {
                    return !isFriendly && ConstructFluidSprayRegistry.canAffectWithAcid(living, 3);
                }, (vel) -> {
                    Vec3 adjusted = vel.yRot((float)(-0.25D + Math.random() * 0.5D)).normalize().scale(0.75D + Math.random() * 0.25D);
                    adjusted = adjusted.subtract(0.0D, adjusted.y, 0.0D);
                    return adjusted;
                }
        ));
        instance.register(new FluidParameterRegistry.FluidParameter(
                FluidRegistry.OIL_OF_VITRIOL.get(), false, true, SFX.Entity.Construct.SPRAY_GENERIC,
                new MAParticleType((ParticleType) ParticleInit.DUST.get()).setColor(149, 40, 40), 0.5f, 20,
                (living, isFriendly, construct) -> {
                    return !isFriendly && ConstructFluidSprayRegistry.canAffectWithAcid(living, 4);
                }, (vel) -> {
                    Vec3 adjusted = vel.yRot((float)(-0.25D + Math.random() * 0.5D)).normalize().scale(0.75D + Math.random() * 0.25D);
                    adjusted = adjusted.subtract(0.0D, adjusted.y, 0.0D);
                    return adjusted;
                }
        ));
        instance.register(new FluidParameterRegistry.FluidParameter(
                FluidRegistry.AZOTH.get(), false, true, SFX.Entity.Construct.SPRAY_GENERIC,
                new MAParticleType((ParticleType) ParticleInit.DUST.get()).setColor(41, 54, 151), 0.5f, 20,
                (living, isFriendly, construct) -> {
                    return !isFriendly && ConstructFluidSprayRegistry.canAffectWithAcid(living, 5);
                }, (vel) -> {
                    Vec3 adjusted = vel.yRot((float)(-0.25D + Math.random() * 0.5D)).normalize().scale(0.75D + Math.random() * 0.25D);
                    adjusted = adjusted.subtract(0.0D, adjusted.y, 0.0D);
                    return adjusted;
                }
        ));
    }

    private static boolean canAffectWithAcid(Entity pEntity, int pAcidStrength) {
        if(pEntity instanceof LivingEntity le) {
            MobEffectInstance dissolution = le.getEffect(MobEffectsRegistry.DISSOLUTION.get());
            MobEffectInstance abatedDissolution = le.getEffect(MobEffectsRegistry.ABATED_DISSOLUTION.get());
            MobEffectInstance acidWard = le.getEffect(MobEffectsRegistry.ACID_WARD.get());

            if (acidWard != null) {
                int wardStrength = acidWard.getAmplifier() + 1;
                return wardStrength < pAcidStrength;
            } else if (abatedDissolution != null) {
                return abatedDissolution.getAmplifier() + 1 < pAcidStrength;
            } else if (dissolution != null) {
                return dissolution.getAmplifier() + 1 < pAcidStrength;
            }

            return true;
        }
        return false;
    }
}
