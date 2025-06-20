package com.aranaira.magichem.effects;

import com.mna.api.affinity.Affinity;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.effects.EffectInit;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

import java.util.ArrayList;
import java.util.Map;

public class RadiantResolveEffect extends MobEffect {
    public RadiantResolveEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xffffffff);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 10 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        ArrayList<MobEffect> effectsToRemove = new ArrayList<>();

        for(MobEffectInstance effect : pLivingEntity.getActiveEffects()) {
            if(effect.getEffect().getCategory() == MobEffectCategory.HARMFUL && !effectsToRemove.contains(effect.getEffect())) {
                effectsToRemove.add(effect.getEffect());
            }
        }

        for(MobEffect effect : effectsToRemove) {
            pLivingEntity.removeEffect(effect);
        }

        super.applyEffectTick(pLivingEntity, pAmplifier);
    }
}
