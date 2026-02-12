package com.aranaira.magichem.ritual;

import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.rituals.IRitualContext;
import com.mna.rituals.effects.RitualEffectCreateEssence;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class RitualEffectCelestialAwakeningLunar extends RitualEffectCreateEssence {

    public static final int RITUAL_LIFESPAN = 160;
    private static final Random r = new Random();

    public RitualEffectCelestialAwakeningLunar(ResourceLocation ritualName) {
        super(ritualName);
    }

    @Override
    public ItemStack getOutputStack() {
        return new ItemStack(ItemRegistry.SELARGYROS.get());
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
        return context.getLevel().isDay() ? Component.translatable("feedback.ritual.celestial_awakening.lunar") : null;
    }
}
