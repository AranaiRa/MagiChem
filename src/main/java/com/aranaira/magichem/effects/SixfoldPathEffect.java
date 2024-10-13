package com.aranaira.magichem.effects;

import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.effects.EffectInit;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

import java.util.Map;

public class SixfoldPathEffect extends MobEffect {
    public SixfoldPathEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xff8174af);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if(pLivingEntity instanceof Player player) {
            player.getCapability(PlayerMagicProvider.MAGIC).ifPresent((m) -> {
                final Map<Affinity, Float> sortedAffinities = m.getSortedAffinityDepths();
                Affinity highestKey = Affinity.EARTH;
                float highestValue = -1;
                for (Affinity affinity : sortedAffinities.keySet()) {
                    float query = sortedAffinities.get(affinity);
                    if(query > highestValue) {
                        highestValue = query;
                        highestKey = affinity;
                    }
                }

                if(highestKey == Affinity.ARCANE) {
                    player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false));
                } else if(highestKey == Affinity.EARTH) {
                    final FoodData foodData = player.getFoodData();
                    foodData.setExhaustion(foodData.getExhaustionLevel() / 2);
                } else if(highestKey == Affinity.ENDER) {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 60, 0, false, false));
                } else if(highestKey == Affinity.FIRE) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false));
                } else if(highestKey == Affinity.WATER) {
                    player.addEffect(new MobEffectInstance(EffectInit.WATER_WALKING.get(), 60, 0, false, false));
                } else if(highestKey == Affinity.WIND) {
                    player.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 0, false, false));
                }
            });
        }

        super.applyEffectTick(pLivingEntity, pAmplifier);
    }
}
