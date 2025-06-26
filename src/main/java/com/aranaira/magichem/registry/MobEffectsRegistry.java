package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.effects.RadiantResolveEffect;
import com.aranaira.magichem.effects.SimpleEffect;
import com.aranaira.magichem.effects.SixfoldPathEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MobEffectsRegistry {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MagiChemMod.MODID);

    public static final RegistryObject<MobEffect> SIXFOLD_PATH = EFFECTS.register("sixfold_path", SixfoldPathEffect::new);
    public static final RegistryObject<MobEffect> RADIANT_RESOLVE = EFFECTS.register("radiant_resolve", RadiantResolveEffect::new);
    public static final RegistryObject<MobEffect> SUNS_GRACE = EFFECTS.register("suns_grace", () -> new SimpleEffect(MobEffectCategory.BENEFICIAL, 0xffffb527));
    public static final RegistryObject<MobEffect> SUNS_SCORN = EFFECTS.register("suns_scorn", () -> new SimpleEffect(MobEffectCategory.BENEFICIAL, 0xffb13a1a));

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
