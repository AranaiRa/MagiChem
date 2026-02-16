package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.effects.*;
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
    public static final RegistryObject<MobEffect> GIGANTIC_VIGOR = EFFECTS.register("gigantic_vigor", () -> new GiganticVigorEffect(MobEffectCategory.BENEFICIAL, 0xffbe2049));
    public static final RegistryObject<MobEffect> TERANTIC_MIGHT = EFFECTS.register("terantic_might", () -> new TeranticMightEffect(MobEffectCategory.BENEFICIAL, 0xffe3003b));
    public static final RegistryObject<MobEffect> DISSOLUTION = EFFECTS.register("dissolution", () -> new DissolutionEffect(MobEffectCategory.HARMFUL, 0xffb4e51b));
    public static final RegistryObject<MobEffect> ABATED_DISSOLUTION = EFFECTS.register("abated_dissolution", () -> new DissolutionEffect(MobEffectCategory.HARMFUL, 0xffb4e51b));
    public static final RegistryObject<MobEffect> ACID_WARD = EFFECTS.register("acid_ward", () -> new SimpleEffect(MobEffectCategory.BENEFICIAL, 0xff583481));
    public static final RegistryObject<MobEffect> MEMORIES_OF_DECADENCE = EFFECTS.register("memories_of_decadence", MemoriesOfDecadenceEffect::new);
    public static final RegistryObject<MobEffect> EQUANIMITY = EFFECTS.register("equanimity", () -> new SimpleEffect(MobEffectCategory.NEUTRAL, 0xffb27dc4));
    public static final RegistryObject<MobEffect> EVANESCENCE = EFFECTS.register("evanescence", EvanescenceEffect::new);
    public static final RegistryObject<MobEffect> BRUTALITY = EFFECTS.register("brutality", () -> new SimpleEffect(MobEffectCategory.NEUTRAL, 0xff8f3631));
    public static final RegistryObject<MobEffect> MALICE = EFFECTS.register("malice", () -> new SimpleEffect(MobEffectCategory.NEUTRAL, 0xff747456));
    public static final RegistryObject<MobEffect> CHAINSPELL = EFFECTS.register("chainspell", () -> new SimpleEffect(MobEffectCategory.BENEFICIAL, 0xff3139d8));

    public static final float[] BRUTALITY_INCOMING_DAMAGE_REDUCTION = {0.82f, 0.74f, 0.66f, 0.58f, 0.5f};
    public static final float[] BRUTALITY_BASE_DAMAGE_INCREASE = {0.5f, 1.0f, 1.5f, 2.0f, 2.5f};
    public static final float[] BRUTALITY_DAMAGE_AMPLIFICATION = {1.05f, 1.10f, 1.25f, 1.45f, 1.70f};
    public static final int[] EVANESCENCE_REGEN_TICK_RATE = {13, 9, 6, 4, 3};
    public static final int[] EVANESCENCE_EVASION_RATE = {25, 30, 40, 50, 67};
    public static final float[] EQUANIMITY_HEAL_PER_MANA = {0.018f, 0.024f, 0.030f, 0.036f, 0.042f};
    public static final float[] EQUANIMITY_MANA_PER_HEART = {80, 120, 180, 270, 405};
    public static final int[] MALICE_WITHER_LEVEL = {0, 0, 1, 1, 2};
    public static final float[] MALICE_RADIUS = {5.0f, 6.5f, 8.0f, 9.5f, 11.0f};
    public static final float[] MALICE_DISCHARGE_DAMAGE = {3.0f, 4.0f, 5.0f, 6.5f, 8.0f};

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
