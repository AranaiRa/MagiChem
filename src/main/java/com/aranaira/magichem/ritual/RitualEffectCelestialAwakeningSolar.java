package com.aranaira.magichem.ritual;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.rituals.IRitualContext;
import com.mna.api.rituals.RitualEffect;
import com.mna.rituals.effects.RitualEffectCreateEssence;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class RitualEffectCelestialAwakeningSolar extends RitualEffectCreateEssence {

    public static final int RITUAL_LIFESPAN = 160;
    private static final Random r = new Random();

    public RitualEffectCelestialAwakeningSolar(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public ItemStack getOutputStack() {
        return new ItemStack(ItemRegistry.ORICHALKOS.get());
    }

    @Override
    public boolean spawnRitualParticles(IRitualContext context) {
        return true;
    }

    @Override
    protected int getApplicationTicks(IRitualContext iRitualContext) {
        return RITUAL_LIFESPAN;
    }

    @Nullable
    @Override
    public Component canRitualStart(IRitualContext context) {
        return !context.getLevel().isDay() ? Component.translatable("feedback.ritual.celestial_awakening.solar") : null;
    }
}
