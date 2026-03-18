package com.aranaira.magichem.effects;

import com.aranaira.magichem.registry.MobEffectsRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

public class GoldenResurgenceEffect extends MobEffect {
    public GoldenResurgenceEffect() {
        super(MobEffectCategory.NEUTRAL, 0xffcb9737);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 4 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if(pLivingEntity instanceof Player player) {
            player.heal(50f);
        }

        super.applyEffectTick(pLivingEntity, pAmplifier);
    }
}
