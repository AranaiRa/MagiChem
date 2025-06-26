package com.aranaira.magichem.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;

public class SimpleEffect extends MobEffect {
    public SimpleEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }
}
