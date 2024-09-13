package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.*;
import com.aranaira.magichem.item.compat.occultism.*;
import com.aranaira.magichem.registry.compat.OccultismItemRegistry;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
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

    public static final RegistryObject<Item> SUPERHEATED_GLASS_PANE = ITEMS.register("superheated_glass_pane",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> PERFECTED_ELECTRUM = ITEMS.register("perfected_electrum",
            () -> new Item(new Item.Properties())
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

    public static final RegistryObject<TooltipLoreItem> GLASS_CUTTING_TOOLS = ITEMS.register("glass_cutting_tools",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> HARMONISCOPE = ITEMS.register("harmoniscope",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> NETHERITE_SHAPING_PLANE = ITEMS.register("netherite_shaping_plane",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> LABORATORY_CHARM = ITEMS.register("laboratory_charm",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> CATALYTIC_CARBON = ITEMS.register("catalytic_carbon",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<ThunderstoneItem> THUNDERSTONE = ITEMS.register("thunderstone",
            () -> new ThunderstoneItem(new Item.Properties())
    );

    public static final RegistryObject<WinterChargeItem> WINTER_CHARGE = ITEMS.register("winter_charge",
            () -> new WinterChargeItem(new Item.Properties())
    );

    public static final RegistryObject<ChargingTalismanItem> CHARGING_TALISMAN = ITEMS.register("charging_talisman",
            () -> new ChargingTalismanItem(new Item.Properties().stacksTo(1))
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
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> RUINED_PROJECTION_APPARATUS = ITEMS.register("auxiliary_circle_array_single_damaged",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(24))
    );

    public static final RegistryObject<TooltipLoreItem> CIRCLE_PROJECTION_APPARATUS = ITEMS.register("auxiliary_circle_array_single",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(4))
    );

    public static final RegistryObject<TooltipLoreItem> LIGHTWRACKED_PROJECTION_GEM = ITEMS.register("auxiliary_circle_array_gem_damaged",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(4))
    );

    public static final RegistryObject<TooltipLoreItem> CORONAL_PROJECTION_GEM = ITEMS.register("auxiliary_circle_array_gem",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(4))
    );

    public static final RegistryObject<TooltipLoreItem> DISSONANT_CRYSTAL_CORE = ITEMS.register("auxiliary_circle_array_core_damaged",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(4))
    );

    public static final RegistryObject<TooltipLoreItem> CONCORDANT_CRYSTAL_CORE = ITEMS.register("auxiliary_circle_array_core",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(4))
    );

    public static final RegistryObject<TooltipLoreItem> BEFOULED_PROJECTION_CASING = ITEMS.register("auxiliary_circle_array_casing_damaged",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(4))
    );

    public static final RegistryObject<TooltipLoreItem> PRISTINE_PROJECTION_CASING = ITEMS.register("auxiliary_circle_array_casing",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(4))
    );

    public static final RegistryObject<TooltipLoreItem> CONTORTED_PENNON = ITEMS.register("auxiliary_circle_array_pennon_damaged",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(8))
    );

    public static final RegistryObject<TooltipLoreItem> GUIDANCE_PENNON = ITEMS.register("auxiliary_circle_array_pennon",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(8))
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

    ///////////////
    // MAGICHEM DUMMIES AND HIDDEN ITEMS
    ///////////////

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

    public static final RegistryObject<Item> DUMMY_PROCESS_FABRICATION = ITEMS.register("dummy/process_fabrication",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_PROCESS_COLORATION = ITEMS.register("dummy/process_coloration",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> SUBLIMATION_IN_PROGRESS = ITEMS.register("sublimation_in_progress",
            () -> new SublimationInProgressItem(new Item.Properties().stacksTo(1))
    );

    ///////////////
    // TECHNICAL HELPERS
    ///////////////

    public static final List<RegistryObject<Item>> ITEMS_EXCLUDED_FROM_TABS = Arrays.asList(
            SUBLIMATION_IN_PROGRESS,
            DUMMY_PROCESS_DISTILLATION, DUMMY_PROCESS_FABRICATION, DUMMY_PROCESS_FIXATION, DUMMY_PROCESS_SEPARATION, DUMMY_PROCESS_SUBLIMATION, DUMMY_PROCESS_SUBLIMATION_RITUAL, DUMMY_PROCESS_COLORATION, DUMMY_PROCESS_CONJURATION
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
