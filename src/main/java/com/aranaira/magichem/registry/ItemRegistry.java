package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = MagiChemMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MagiChemMod.MODID);
    public static final DeferredRegister<Item> ESSENTIA = DeferredRegister.create(ForgeRegistries.ITEMS, MagiChemMod.MODID);
    public static final DeferredRegister<Item> ADMIXTURES = DeferredRegister.create(ForgeRegistries.ITEMS, MagiChemMod.MODID);

    public static Item NIGREGO, ALBEDO, CITRINITAS, RUBEDO;

    ///////////////
    // MAGICHEM ITEMS
    ///////////////

    public static final RegistryObject<CodexMateriaItem> CODEX_MATERIA = ITEMS.register("codex_materia",
            () -> new CodexMateriaItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> INERT_WISDOM_STONE = ITEMS.register("inert_wisdom_stone",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<PhilosophersStoneItem> ASHEN_WISDOM_STONE = ITEMS.register("wisdom_stone_nigredo",
            () -> new PhilosophersStoneItem(new Item.Properties().stacksTo(1), 1)
    );

    public static final RegistryObject<PhilosophersStoneItem> BLEACHED_WISDOM_STONE = ITEMS.register("wisdom_stone_albedo",
            () -> new PhilosophersStoneItem(new Item.Properties().stacksTo(1), 2)
    );

    public static final RegistryObject<PhilosophersStoneItem> YELLOWED_WISDOM_STONE = ITEMS.register("wisdom_stone_citrinitas",
            () -> new PhilosophersStoneItem(new Item.Properties().stacksTo(1), 3)
    );

    public static final RegistryObject<PhilosophersStoneItem> FLUSHED_WISDOM_STONE = ITEMS.register("wisdom_stone_rubedo",
            () -> new PhilosophersStoneItem(new Item.Properties().stacksTo(1), 4)
    );

    public static final RegistryObject<PhilosophersStoneItem> PHILOSOPHERS_STONE = ITEMS.register("philosophers_stone",
            () -> new PhilosophersStoneItem(new Item.Properties().stacksTo(1), 5)
    );

    public static final RegistryObject<Item> PHILOSOPHERS_STONE_DUMMY = ITEMS.register("philosophers_stone_dummy",
            () -> new Item(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<SublimationPrimerItem> SUBLIMATION_PRIMER = ITEMS.register("sublimation_primer",
            () -> new SublimationPrimerItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> IRIS_ARGENTI = ITEMS.register("iris_argenti",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> MAGIC_CIRCLE = ITEMS.register("magic_circle",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> ALCHEMICAL_WASTE = ITEMS.register("alchemical_waste",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> RAREFIED_WASTE = ITEMS.register("rarefied_waste",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> CLEANING_BRUSH = ITEMS.register("cleaning_brush",
            () -> new TooltipLoreItem(new Item.Properties().durability(10))
    );

    public static final RegistryObject<TooltipLoreItem> SUPERHEATED_GLASS_PANE = ITEMS.register("superheated_glass_pane",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> PERFECTED_ELECTRUM = ITEMS.register("perfected_electrum",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> TUBE_COMPONENTS = ITEMS.register("tube_components",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> SUBLIME_TUBE_COMPONENTS = ITEMS.register("sublime_tube_components",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> COG_COMPONENTS = ITEMS.register("cog_components",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> SUBLIME_COG_COMPONENTS = ITEMS.register("sublime_cog_components",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> BLEACHED_AMETHYST_SHARD = ITEMS.register("bleached_amethyst_shard",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> VINTEUM_CRYSTAL_SHARD = ITEMS.register("vinteum_crystal_shard",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> VERDIGRIS = ITEMS.register("verdigris",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> COMPACTED_VERDIGRIS = ITEMS.register("compacted_verdigris",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> GLASS_CUTTING_TOOLS = ITEMS.register("glass_cutting_tools",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> GLASS_ORB = ITEMS.register("glass_orb",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> GLASS_LENS = ITEMS.register("glass_lens",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> SOLAR_ORB = ITEMS.register("solar_orb",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> LUNAR_ORB = ITEMS.register("lunar_orb",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> SIDEREAL_ORB = ITEMS.register("sidereal_orb",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> SOLAR_CLOISTER_LENS = ITEMS.register("solar_cloister_lens",
            () -> new TooltipLoreItem(new Item.Properties().durability(600))
    );

    public static final RegistryObject<TooltipLoreItem> LUNAR_CLOISTER_LENS = ITEMS.register("lunar_cloister_lens",
            () -> new TooltipLoreItem(new Item.Properties().durability(600))
    );

    public static final RegistryObject<TooltipLoreItem> SIDEREAL_CLOISTER_LENS = ITEMS.register("sidereal_cloister_lens",
            () -> new TooltipLoreItem(new Item.Properties().durability(600))
    );

    public static final RegistryObject<TooltipLoreItem> SOLAR_FARSIGHT_LENS = ITEMS.register("solar_farsight_lens",
            () -> new TooltipLoreItem(new Item.Properties().durability(600))
    );

    public static final RegistryObject<TooltipLoreItem> LUNAR_FARSIGHT_LENS = ITEMS.register("lunar_farsight_lens",
            () -> new TooltipLoreItem(new Item.Properties().durability(600))
    );

    public static final RegistryObject<TooltipLoreItem> SIDEREAL_FARSIGHT_LENS = ITEMS.register("sidereal_farsight_lens",
            () -> new TooltipLoreItem(new Item.Properties().durability(600))
    );

    public static final RegistryObject<TooltipLoreItem> CELESTIAL_MECHANISM = ITEMS.register("celestial_mechanism",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> ILLUMININK = ITEMS.register("illuminink",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> SEALING_WAX = ITEMS.register("sealing_wax",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> SWEETBERRY_MASH = ITEMS.register("sweetberry_mash",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> GLOWBERRY_MASH = ITEMS.register("glowberry_mash",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<DrinkableItem> SWEETBERRY_WINE_BOTTLE = ITEMS.register("sweetberry_wine_bottle",
            () -> new DrinkableItem(new Item.Properties())
    );

    public static final RegistryObject<DrinkableItem> SHIMMERING_WINE_BOTTLE = ITEMS.register("shimmering_wine_bottle",
            () -> new DrinkableItem(new Item.Properties())
    );

    public static final RegistryObject<DrinkableItem> SHIMMERING_VINTAGE_WINE_BOTTLE = ITEMS.register("shimmering_vintage_wine_bottle",
            () -> new DrinkableItem(new Item.Properties())
    );

    public static final RegistryObject<HarmoniscopeItem> HARMONISCOPE = ITEMS.register("harmoniscope",
            () -> new HarmoniscopeItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> NETHERITE_SHAPING_PLANE = ITEMS.register("netherite_shaping_plane",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> LABORATORY_CHARM = ITEMS.register("laboratory_charm",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TravellersCompassItem> TRAVELLERS_COMPASS = ITEMS.register("travellers_compass",
            () -> new TravellersCompassItem()
    );

    public static final RegistryObject<TooltipLoreItem> CATALYTIC_CARBON = ITEMS.register("catalytic_carbon",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<HellfeatherCharmItem> HELLFEATHER_CHARM = ITEMS.register("hellfeather_charm",
            () -> new HellfeatherCharmItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<SonicBombItem> SONIC_BOMB = ITEMS.register("sonic_bomb",
            () -> new SonicBombItem(new Item.Properties().stacksTo(4))
    );

    public static final RegistryObject<ThunderstoneItem> THUNDERSTONE = ITEMS.register("thunderstone",
            () -> new ThunderstoneItem(new Item.Properties().fireResistant())
    );

    public static final RegistryObject<WinterChargeItem> WINTER_CHARGE = ITEMS.register("winter_charge",
            () -> new WinterChargeItem(new Item.Properties())
    );

    public static final RegistryObject<ChargingTalismanItem> CHARGING_TALISMAN = ITEMS.register("charging_talisman",
            () -> new ChargingTalismanItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> LITANY_BEARING = ITEMS.register("litany_bearing",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> LITANY_INTELLIGENCE = ITEMS.register("litany_intelligence",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> LITANY_RESONANCE = ITEMS.register("litany_resonance",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> LITANY_EMPTINESS = ITEMS.register("litany_emptiness",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> LITANY_RESILIENCE = ITEMS.register("litany_resilience",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> LITANY_SATURATION = ITEMS.register("litany_saturation",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> LITANY_SORCERY = ITEMS.register("litany_sorcery",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> LITANY_TELEPORTATION = ITEMS.register("litany_teleportation",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> LITANY_THRUST = ITEMS.register("litany_thrust",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> INERT_IDOL = ITEMS.register("inert_idol",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<SlumberingIdolItem> SLUMBERING_IDOL = ITEMS.register("slumbering_idol",
            () -> new SlumberingIdolItem(new Item.Properties())
    );

    public static final RegistryObject<AbjurationItem> ABJURATION = ITEMS.register("abjuration",
            () -> new AbjurationItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<AbjurationDendriticItem> DENDRITIC_ABJURATION = ITEMS.register("abjuration_dendritic",
            () -> new AbjurationDendriticItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<AbjurationNecroticItem> NECROTIC_ABJURATION = ITEMS.register("abjuration_necrotic",
            () -> new AbjurationNecroticItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<StatusGrantingConsumableItem> HOROSCOPE_SOLAR = ITEMS.register("horoscope_solar",
            () -> new StatusGrantingConsumableItem(new Item.Properties(), MobEffectsRegistry.SUNS_GRACE.get(), 6000, 6000, SoundEvents.ENCHANTMENT_TABLE_USE, 0, false, false, 1)
    );

    public static final RegistryObject<StatusGrantingConsumableItem> HOROSCOPE_SOLAR_FOREBODING = ITEMS.register("horoscope_solar_foreboding",
            () -> new StatusGrantingConsumableItem(new Item.Properties(), MobEffectsRegistry.SUNS_SCORN.get(), 6000, 6000, SoundEvents.ENCHANTMENT_TABLE_USE, 0, false, false, 1)
    );

    public static final RegistryObject<StatusGrantingConsumableItem> HOROSCOPE_LUNAR = ITEMS.register("horoscope_lunar",
            () -> new StatusGrantingConsumableItem(new Item.Properties(), MobEffects.LUCK, 6000, 9000, SoundEvents.ENCHANTMENT_TABLE_USE, 9, false, false, 9)
    );

    public static final RegistryObject<StatusGrantingConsumableItem> HOROSCOPE_LUNAR_FOREBODING = ITEMS.register("horoscope_lunar_foreboding",
            () -> new StatusGrantingConsumableItem(new Item.Properties(), MobEffects.BAD_OMEN, -1, 30, SoundEvents.ENCHANTMENT_TABLE_USE, 0, false, true, 5)
    );

    public static final RegistryObject<Item> DEBUG_ORB = ITEMS.register("debug_orb",
            () -> new DebugOrbItem(new Item.Properties().stacksTo(1))
    );

    ///////////////
    // CIRCLE OF POWER ITEMS
    ///////////////

    public static final RegistryObject<TooltipLoreItem> SILVER_DUST = ITEMS.register("silver_dust",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> TARNISHED_SILVER_LUMP = ITEMS.register("tarnished_silver_lump",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(4))
    );

    public static final RegistryObject<TooltipLoreItem> FOCUSING_CATALYST = ITEMS.register("focusing_catalyst",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> WARPED_FOCUSING_CATALYST = ITEMS.register("focusing_catalyst_warped",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(4))
    );

    public static final RegistryObject<TooltipLoreItem> DEPLETED_CATALYST_CORE = ITEMS.register("focusing_catalyst_core_depleted",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(4))
    );

    public static final RegistryObject<TooltipLoreItem> CATALYST_CORE = ITEMS.register("focusing_catalyst_core",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(4))
    );

    public static final RegistryObject<TooltipLoreItem> CATALYST_CASING = ITEMS.register("focusing_catalyst_casing",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(4))
    );

    public static final RegistryObject<TooltipLoreItem> AMPLIFYING_PRISM = ITEMS.register("amplifying_prism",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> MALFORMED_BRINDLE_GLASS = ITEMS.register("malformed_brindle_glass",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(4))
    );

    public static final RegistryObject<TooltipLoreItem> REFRACTIVE_CRYSTAL_GRIT = ITEMS.register("refractive_crystal_grit",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> REFRACTIVE_CRYSTAL_SAND = ITEMS.register("refractive_crystal_sand",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> REFRACTIVE_CRYSTAL_GLASS = ITEMS.register("refractive_crystal_glass",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> BRINDLE_GRIT_RED = ITEMS.register("brindle_grit_red",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> BRINDLE_GRIT_YELLOW = ITEMS.register("brindle_grit_yellow",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> BRINDLE_GRIT_GREEN = ITEMS.register("brindle_grit_green",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> BRINDLE_GRIT_CYAN = ITEMS.register("brindle_grit_cyan",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> BRINDLE_GRIT_BLUE = ITEMS.register("brindle_grit_blue",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> BRINDLE_GRIT_MAGENTA = ITEMS.register("brindle_grit_magenta",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> AUXILIARY_CIRCLE_ARRAY = ITEMS.register("auxiliary_circle_array",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> RUINED_PROJECTION_APPARATUS = ITEMS.register("auxiliary_circle_array_single_damaged",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(24))
    );

    public static final RegistryObject<TooltipLoreItem> CIRCLE_PROJECTION_APPARATUS = ITEMS.register("auxiliary_circle_array_single",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(6))
    );

    public static final RegistryObject<TooltipLoreItem> LIGHTWRACKED_PROJECTION_GEM = ITEMS.register("auxiliary_circle_array_gem_damaged",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(6))
    );

    public static final RegistryObject<TooltipLoreItem> CORONAL_PROJECTION_GEM = ITEMS.register("auxiliary_circle_array_gem",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(6))
    );

    public static final RegistryObject<TooltipLoreItem> DISSONANT_CRYSTAL_CORE = ITEMS.register("auxiliary_circle_array_core_damaged",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(6))
    );

    public static final RegistryObject<TooltipLoreItem> CONCORDANT_CRYSTAL_CORE = ITEMS.register("auxiliary_circle_array_core",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(6))
    );

    public static final RegistryObject<TooltipLoreItem> BEFOULED_PROJECTION_CASING = ITEMS.register("auxiliary_circle_array_casing_damaged",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(6))
    );

    public static final RegistryObject<TooltipLoreItem> PRISTINE_PROJECTION_CASING = ITEMS.register("auxiliary_circle_array_casing",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(6))
    );

    public static final RegistryObject<TooltipLoreItem> CONTORTED_PENNON = ITEMS.register("auxiliary_circle_array_pennon_damaged",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(12))
    );

    public static final RegistryObject<TooltipLoreItem> GUIDANCE_PENNON = ITEMS.register("auxiliary_circle_array_pennon",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(12))
    );

    public static final RegistryObject<TooltipLoreItem> SCORCHED_THEOREM = ITEMS.register("scorched_theorem",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> SCORCHED_PROFUNDITY = ITEMS.register("scorched_profundity",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> COLLATED_THEORIES = ITEMS.register("collated_theories",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> PROFOUND_MANUSCRIPT = ITEMS.register("profound_manuscript",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> DAUNTING_ESOTERICA = ITEMS.register("daunting_esoterica",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> OBSCURE_PROGNOSTICATIONS = ITEMS.register("obscure_prognostications",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> ACHROMATIC_MOTE = ITEMS.register("mote_achromatic",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<FluidConvertingItem> ARISTOSE = ITEMS.register("aristose",
            () -> new FluidConvertingItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> REGULUS_OF_GOLD = ITEMS.register("regulus_of_gold",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<EnsorcelledFertilizerItem> ENSORCELLED_FERTILIZER = ITEMS.register("ensorcelled_fertilizer",
            () -> new EnsorcelledFertilizerItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> SALT_OF_SOOT = ITEMS.register("salt_of_soot",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> SALT_OF_BONE = ITEMS.register("salt_of_bone",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> AMARANTINE_RESIN = ITEMS.register("resin_amarantine",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> GRAY_RESIN = ITEMS.register("resin_gray",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> CHIAROSCURO = ITEMS.register("chiaroscuro",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> PREPARED_VINTEUM_CRYSTAL = ITEMS.register("prepared_vinteum_crystal",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> LADEN_VINTEUM_CRYSTAL = ITEMS.register("laden_vinteum_crystal",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> LIGHTLY_ENHANCED_VINTEUM_CRYSTAL = ITEMS.register("lightly_enhanced_vinteum_crystal",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> PREPARED_LIGHTLY_ENHANCED_VINTEUM_CRYSTAL = ITEMS.register("prepared_lightly_enhanced_vinteum_crystal",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> LADEN_LIGHTLY_ENHANCED_VINTEUM_CRYSTAL = ITEMS.register("laden_lightly_enhanced_vinteum_crystal",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> HEAVILY_ENHANCED_VINTEUM_CRYSTAL = ITEMS.register("heavily_enhanced_vinteum_crystal",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> PREPARED_HEAVILY_ENHANCED_VINTEUM_CRYSTAL = ITEMS.register("prepared_heavily_enhanced_vinteum_crystal",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> LADEN_HEAVILY_ENHANCED_VINTEUM_CRYSTAL = ITEMS.register("laden_heavily_enhanced_vinteum_crystal",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> IMMACULATE_VINTEUM_CRYSTAL = ITEMS.register("immaculate_vinteum_crystal",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> BLACK_DIAMOND = ITEMS.register("black_diamond",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    ///////////////
    // MAGICHEM FLUID BUCKETS
    ///////////////

    public static final RegistryObject<Item> ACADEMIC_SLURRY_BUCKET = ITEMS.register("academic_slurry_bucket",
            () -> new BucketItem(FluidRegistry.ACADEMIC_SLURRY, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final RegistryObject<Item> STEAM_BUCKET = ITEMS.register("steam_bucket",
            () -> new BucketItem(FluidRegistry.STEAM, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final RegistryObject<Item> SMOKE_BUCKET = ITEMS.register("smoke_bucket",
            () -> new BucketItem(FluidRegistry.SMOKE, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final RegistryObject<Item> LIQUID_LIGHT_BUCKET = ITEMS.register("liquid_light_bucket",
            () -> new BucketItem(FluidRegistry.LIQUID_LIGHT, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final RegistryObject<Item> LIQUEFACTED_COPPER_BUCKET = ITEMS.register("liquefacted_copper_bucket",
            () -> new BucketItem(FluidRegistry.LIQUEFACTED_COPPER, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final RegistryObject<Item> LIQUEFACTED_IRON_BUCKET = ITEMS.register("liquefacted_iron_bucket",
            () -> new BucketItem(FluidRegistry.LIQUEFACTED_IRON, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final RegistryObject<Item> LIQUEFACTED_GOLD_BUCKET = ITEMS.register("liquefacted_gold_bucket",
            () -> new BucketItem(FluidRegistry.LIQUEFACTED_GOLD, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final RegistryObject<Item> LIQUEFACTED_DEBRIS_BUCKET = ITEMS.register("liquefacted_debris_bucket",
            () -> new BucketItem(FluidRegistry.LIQUEFACTED_DEBRIS, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final RegistryObject<Item> AQUA_VITAE_BUCKET = ITEMS.register("aqua_vitae_bucket",
            () -> new BucketItem(FluidRegistry.AQUA_VITAE, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final RegistryObject<Item> SWEETBERRY_WINE_BUCKET = ITEMS.register("sweetberry_wine_bucket",
            () -> new BucketItem(FluidRegistry.SWEETBERRY_WINE, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final RegistryObject<Item> SHIMMERING_WINE_BUCKET = ITEMS.register("shimmering_wine_bucket",
            () -> new BucketItem(FluidRegistry.SHIMMERING_WINE, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final RegistryObject<Item> SIMPLE_ACID_BUCKET = ITEMS.register("simple_acid_bucket",
            () -> new BucketItem(FluidRegistry.SIMPLE_ACID, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final RegistryObject<Item> AQUA_FORTIS_BUCKET = ITEMS.register("aqua_fortis_bucket",
            () -> new BucketItem(FluidRegistry.AQUA_FORTIS, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final RegistryObject<Item> AQUA_REGIA_BUCKET = ITEMS.register("aqua_regia_bucket",
            () -> new BucketItem(FluidRegistry.AQUA_REGIA, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final RegistryObject<Item> OIL_OF_VITRIOL_BUCKET = ITEMS.register("oil_of_vitriol_bucket",
            () -> new BucketItem(FluidRegistry.OIL_OF_VITRIOL, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final RegistryObject<Item> AZOTH_BUCKET = ITEMS.register("azoth_bucket",
            () -> new BucketItem(FluidRegistry.AZOTH, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    ///////////////
    // RELICS
    ///////////////

    public static final RegistryObject<FractallinePuzzleBoxItem> FRACTALLINE_PUZZLE_BOX = ITEMS.register("fractalline_puzzle_box",
            () -> new FractallinePuzzleBoxItem(new Item.Properties())
    );

    ///////////////
    // RADIANT ROSE PETAL (SO IT APPEARS NEXT TO THE ROSE)
    ///////////////

    public static final RegistryObject<StatusGrantingConsumableItem> RADIANT_ROSE_PETAL = ITEMS.register("radiant_rose_petal",
            () -> new StatusGrantingConsumableItem(new Item.Properties(), MobEffectsRegistry.RADIANT_RESOLVE.get(), 120, 900, SoundEvents.GENERIC_EAT, 0, true, false, 1)
    );

    ///////////////
    // MAGICHEM DUMMIES AND HIDDEN ITEMS
    ///////////////

    public static final RegistryObject<Item> DUMMY_PROCESS_FULMINATION = ITEMS.register("dummy/process_fulmination",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_PROCESS_FIXATION = ITEMS.register("dummy/process_fixation",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_PROCESS_CONJURATION = ITEMS.register("dummy/process_conjuration",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_PROCESS_SUBLIMATION = ITEMS.register("dummy/process_sublimation",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_PROCESS_SUBLIMATION_RITUAL = ITEMS.register("dummy/process_sublimation_ritual",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_PROCESS_SEPARATION = ITEMS.register("dummy/process_separation",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_PROCESS_DISTILLATION = ITEMS.register("dummy/process_distillation",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_PROCESS_FLUID_DISTILLATION = ITEMS.register("dummy/process_fluid_distillation",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_PROCESS_FABRICATION = ITEMS.register("dummy/process_fabrication",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_PROCESS_FLUID_FABRICATION = ITEMS.register("dummy/process_fluid_fabrication",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_PROCESS_EXALTATION = ITEMS.register("dummy/process_exaltation",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_PROCESS_COLORATION = ITEMS.register("dummy/process_coloration",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_ANOINTING = ITEMS.register("dummy/dummy_anointing",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_CONSTRUCT_STUDY_MATERIAL = ITEMS.register("dummy/dummy_construct_study_material",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_INFO_PANEL = ITEMS.register("dummy/dummy_info_panel",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_VITRIOLATION = ITEMS.register("dummy/dummy_vitriolation",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_ILLUMINATION = ITEMS.register("dummy/dummy_illumination",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> SUBLIMATION_IN_PROGRESS = ITEMS.register("sublimation_in_progress",
            () -> new SublimationInProgressItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<Item> ESSENTIA_DROPLETS_ENDER = ITEMS.register("essentia_droplets_ender",
            () -> new EssentiaDropletsItem("ender", "\u0547", "elements", 0, "2e1c45")
    );

    public static final RegistryObject<Item> ESSENTIA_DROPLETS_EARTH = ITEMS.register("essentia_droplets_earth",
            () -> new EssentiaDropletsItem("earth", "\u053D", "elements", 1, "442f17")
    );

    public static final RegistryObject<Item> ESSENTIA_DROPLETS_WATER = ITEMS.register("essentia_droplets_water",
            () -> new EssentiaDropletsItem("water", "\u0539", "elements", 2, "2c66bd")
    );

    public static final RegistryObject<Item> ESSENTIA_DROPLETS_AIR = ITEMS.register("essentia_droplets_air",
            () -> new EssentiaDropletsItem("air", "\u0545", "elements", 3, "c9c3b1")
    );

    public static final RegistryObject<Item> ESSENTIA_DROPLETS_FIRE = ITEMS.register("essentia_droplets_fire",
            () -> new EssentiaDropletsItem("fire", "\u0554", "elements", 4, "ee8015")
    );

    public static final RegistryObject<Item> ESSENTIA_DROPLETS_ARCANE = ITEMS.register("essentia_droplets_arcane",
            () -> new EssentiaDropletsItem("arcane", "\u0556", "elements", 5, "b870ef")
    );

    ///////////////
    // TECHNICAL HELPERS
    ///////////////

    public static final List<RegistryObject<Item>> ITEMS_EXCLUDED_FROM_TABS = Arrays.asList(
            PHILOSOPHERS_STONE_DUMMY, SUBLIMATION_IN_PROGRESS,
            DUMMY_ANOINTING,DUMMY_INFO_PANEL,DUMMY_VITRIOLATION,DUMMY_ILLUMINATION,
            DUMMY_PROCESS_FULMINATION, DUMMY_PROCESS_DISTILLATION, DUMMY_PROCESS_FABRICATION, DUMMY_PROCESS_FIXATION, DUMMY_PROCESS_SEPARATION, DUMMY_PROCESS_SUBLIMATION, DUMMY_PROCESS_SUBLIMATION_RITUAL, DUMMY_PROCESS_COLORATION, DUMMY_PROCESS_CONJURATION,
            DUMMY_PROCESS_FLUID_DISTILLATION, DUMMY_PROCESS_FLUID_FABRICATION, DUMMY_PROCESS_EXALTATION,
            ESSENTIA_DROPLETS_ENDER, ESSENTIA_DROPLETS_EARTH, ESSENTIA_DROPLETS_WATER, ESSENTIA_DROPLETS_AIR, ESSENTIA_DROPLETS_FIRE, ESSENTIA_DROPLETS_ARCANE
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        ESSENTIA.register(eventBus);
        ADMIXTURES.register(eventBus);

        HashMap<String, MateriaItem> materiaMap = getMateriaMap(false, false);
        NIGREGO = materiaMap.get("nigredo");
        ALBEDO = materiaMap.get("albedo");
        CITRINITAS = materiaMap.get("citrinitas");
        RUBEDO = materiaMap.get("rubedo");
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static RegistryObject<Item> getRegistryObject(DeferredRegister<Item> register, String name) {
        return register.getEntries().stream().filter(item -> item.getId().getPath().equals(name)).findFirst().get();
    }

    public static List<EssentiaItem> getEssentia() {
        return ESSENTIA.getEntries().stream().map(RegistryObject::get).map(item -> (EssentiaItem) item).collect(Collectors.toList());
    }

    public static HashMap<String, EssentiaItem> getEssentiaMap(boolean appendModID, boolean appendTypePrefix) {
        HashMap<String, EssentiaItem> output = new HashMap<>();
        ESSENTIA.getEntries().stream().map(RegistryObject::get).map(item -> (EssentiaItem) item).forEach(item -> {
            String prefix = "";
            if(appendModID)
                prefix += MagiChemMod.MODID+":";
            if(appendTypePrefix)
                prefix += "essentia_";
            output.put(prefix+item.getMateriaName(), item);
        });
        return output;
    }

    public static List<AdmixtureItem> getAdmixtures() {
        return ADMIXTURES.getEntries().stream().map(RegistryObject::get).map(item -> (AdmixtureItem) item).collect(Collectors.toList());
    }

    public static HashMap<String, AdmixtureItem> getAdmixturesMap(boolean appendModID, boolean appendTypePrefix) {
        HashMap<String, AdmixtureItem> output = new HashMap<>();
        ADMIXTURES.getEntries().stream().map(RegistryObject::get).map(item -> (AdmixtureItem) item).forEach(item -> {
            String prefix = "";
            if(appendModID)
                prefix += MagiChemMod.MODID+":";
            if(appendTypePrefix)
                prefix += "admixture_";
            output.put(prefix+item.getMateriaName(), item);
        });
        return output;
    }

    public static HashMap<String, MateriaItem> getMateriaMap(boolean appendModID, boolean appendTypePrefix) {
        HashMap<String, MateriaItem> output = new HashMap<>();
        ESSENTIA.getEntries().stream().map(RegistryObject::get).map(item -> (EssentiaItem) item).forEach(item -> {
            String prefix = "";
            if(appendModID)
                prefix += MagiChemMod.MODID+":";
            if(appendTypePrefix)
                prefix += "essentia_";
            output.put(prefix+item.getMateriaName(), item);
        });
        ADMIXTURES.getEntries().stream().map(RegistryObject::get).map(item -> (AdmixtureItem) item).forEach(item -> {
            String prefix = "";
            if(appendModID)
                prefix += MagiChemMod.MODID+":";
            if(appendTypePrefix)
                prefix += "admixture_";
            output.put(prefix+item.getMateriaName(), item);
        });
        return output;
    }
}
