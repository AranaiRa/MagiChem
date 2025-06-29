package com.aranaira.magichem.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class GiganticVigorEffect extends MobEffect {
    private static final String id = "cb9e6573-ea97-4d9d-830b-aabe18d9e5cc";

    public GiganticVigorEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
        this.addAttributeModifier(Attributes.MAX_HEALTH, id, 20.0D, AttributeModifier.Operation.ADDITION);
    }
}
