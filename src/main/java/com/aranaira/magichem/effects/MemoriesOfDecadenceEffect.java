package com.aranaira.magichem.effects;

import com.aranaira.magichem.registry.MobEffectsRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

import java.util.ArrayList;

public class MemoriesOfDecadenceEffect extends MobEffect {
    public MemoriesOfDecadenceEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xffd67175);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if(pLivingEntity instanceof Player player) {
            final FoodData foodData = player.getFoodData();
            final float foo = foodData.getFoodLevel();
            final float sat = foodData.getSaturationLevel();

            if(foo <= 18 || sat <= 2) {
                foodData.setFoodLevel(20);
                foodData.setSaturation(20);
                foodData.setExhaustion(0);

                pLivingEntity.removeEffect(this);
                if(pAmplifier > 0) {
                    pLivingEntity.addEffect(new MobEffectInstance(MobEffectsRegistry.MEMORIES_OF_DECADENCE.get(), -1,pAmplifier - 1, true, false));
                }
            }
        }

        super.applyEffectTick(pLivingEntity, pAmplifier);
    }
}
