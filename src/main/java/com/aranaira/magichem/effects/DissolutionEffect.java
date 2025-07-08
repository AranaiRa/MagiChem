package com.aranaira.magichem.effects;

import com.aranaira.magichem.data.DamageTypeProvider;
import com.aranaira.magichem.data.MagiChemDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class DissolutionEffect extends MobEffect {
    private static final String id = "cb9e6573-ea97-4d9d-830b-aabe18d9e5ce";

    public DissolutionEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
        this.addAttributeModifier(Attributes.ARMOR, id, -3D, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS, id, -3D, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        int ampShave = Math.min(4, pAmplifier) * 5;
        return pDuration % (35 - ampShave) == 0;
    }

    @Override
    public double getAttributeModifierValue(int pAmplifier, AttributeModifier pModifier) {
        return -3.0 * (pAmplifier + 1);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        final DamageSource source = MagiChemDamageTypes.source(pLivingEntity.level().registryAccess(), MagiChemDamageTypes.ACID);
        int amount = 3 + pAmplifier;
        pLivingEntity.hurt(source, amount);

        super.applyEffectTick(pLivingEntity, pAmplifier);
    }
}
