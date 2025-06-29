package com.aranaira.magichem.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class TeranticMightEffect extends MobEffect {
    private static final String id = "cb9e6573-ea97-4d9d-830b-aabe18d9e5cd";

    public TeranticMightEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
        this.addAttributeModifier(Attributes.MAX_HEALTH, id, 1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
