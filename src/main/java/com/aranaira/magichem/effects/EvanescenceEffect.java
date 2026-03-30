package com.aranaira.magichem.effects;

import com.aranaira.magichem.registry.MobEffectsRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;

import static com.aranaira.magichem.registry.MobEffectsRegistry.EVANESCENCE_REGEN_TICK_RATE;

public class EvanescenceEffect extends MobEffect {
    public EvanescenceEffect() {
        super(MobEffectCategory.NEUTRAL, 0xffbbe1c3);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % EVANESCENCE_REGEN_TICK_RATE[Math.min(pAmplifier, EVANESCENCE_REGEN_TICK_RATE.length)] == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        pLivingEntity.heal(0.5f);

        super.applyEffectTick(pLivingEntity, pAmplifier);
    }
}
