package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.*;
import com.mna.items.base.INoCreativeTab;
import com.mna.items.runes.ItemRunePattern;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

    public static final RegistryObject<TooltipLoreItem> SILVER_DUST = ITEMS.register("silver_dust",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> FOCUSING_CATALYST = ITEMS.register("focusing_catalyst",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> WARPED_FOCUSING_CATALYST = ITEMS.register("focusing_catalyst_warped",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> DEPLETED_CATALYST_CORE = ITEMS.register("focusing_catalyst_core_depleted",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> CATALYST_CORE = ITEMS.register("focusing_catalyst_core",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> CATALYST_CASING = ITEMS.register("focusing_catalyst_casing",
            () -> new TooltipLoreItem(new Item.Properties().stacksTo(1))
    );

    public static final RegistryObject<TooltipLoreItem> IRIS_ARGENTI = ITEMS.register("iris_argenti",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> MAGIC_CIRCLE = ITEMS.register("magic_circle",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> TARNISHED_SILVER_LUMP = ITEMS.register("tarnished_silver_lump",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<TooltipLoreItem> ALCHEMICAL_WASTE = ITEMS.register("alchemical_waste",
            () -> new TooltipLoreItem(new Item.Properties())
    );

    public static final RegistryObject<Item> SUPERHEATED_GLASS_PANE = ITEMS.register("superheated_glass_pane",
            () -> new Item(new Item.Properties())
    );

    public static final RegistryObject<Item> DUMMY_PROCESS_FIXATION = ITEMS.register("dummy/process_fixation",
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

    public static final List<RegistryObject<Item>> ITEMS_EXCLUDED_FROM_TABS = Arrays.asList(
            DUMMY_PROCESS_DISTILLATION, DUMMY_PROCESS_FABRICATION, DUMMY_PROCESS_FIXATION, DUMMY_PROCESS_SEPARATION
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        ESSENTIA.register(eventBus);
        ADMIXTURES.register(eventBus);
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
