package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.gui.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, MagiChemMod.MODID);

    public static final RegistryObject<MenuType<CircleFabricationMenu>> CIRCLE_FABRICATION_MENU =
            registerMenuType(CircleFabricationMenu::new, "circle_fabrication");

    public static final RegistryObject<MenuType<GrandCircleFabricationMenu>> GRAND_CIRCLE_FABRICATION_MENU =
            registerMenuType(GrandCircleFabricationMenu::new, "grand_circle_fabrication");

    public static final RegistryObject<MenuType<CirclePowerMenu>> CIRCLE_POWER_MENU =
            registerMenuType(CirclePowerMenu::new, "circle_power");

    public static final RegistryObject<MenuType<ConjurerMenu>> CONJURER_MENU =
            registerMenuType(ConjurerMenu::new, "conjurer");

    public static final RegistryObject<MenuType<AlembicMenu>> ALEMBIC_MENU =
            registerMenuType(AlembicMenu::new, "alembic");

    public static final RegistryObject<MenuType<DistilleryMenu>> DISTILLERY_MENU =
            registerMenuType(DistilleryMenu::new, "distillery");

    public static final RegistryObject<MenuType<CentrifugeMenu>> CENTRIFUGE_MENU =
            registerMenuType(CentrifugeMenu::new, "centrifuge");

    public static final RegistryObject<MenuType<FuseryMenu>> FUSERY_MENU =
            registerMenuType(FuseryMenu::new, "admixer");

    public static final RegistryObject<MenuType<GrandDistilleryMenu>> GRAND_DISTILLERY_MENU =
            registerMenuType(GrandDistilleryMenu::new, "grand_distillery");

    public static final RegistryObject<MenuType<AlchemicalNexusMenu>> ALCHEMICAL_NEXUS_MENU =
            registerMenuType(AlchemicalNexusMenu::new, "alchemical_nexus");

    public static final RegistryObject<MenuType<ActuatorWaterMenu>> ACTUATOR_WATER_MENU =
            registerMenuType(ActuatorWaterMenu::new, "actuator_water");

    public static final RegistryObject<MenuType<ActuatorFireMenu>> ACTUATOR_FIRE_MENU =
            registerMenuType(ActuatorFireMenu::new, "actuator_fire");

    public static final RegistryObject<MenuType<ActuatorEarthMenu>> ACTUATOR_EARTH_MENU =
            registerMenuType(ActuatorEarthMenu::new, "actuator_earth");

    public static final RegistryObject<MenuType<ActuatorAirMenu>> ACTUATOR_AIR_MENU =
            registerMenuType(ActuatorAirMenu::new, "actuator_air");

    public static final RegistryObject<MenuType<ActuatorArcaneMenu>> ACTUATOR_ARCANE_MENU =
            registerMenuType(ActuatorArcaneMenu::new, "actuator_arcane");

    public static final RegistryObject<MenuType<VariegatorMenu>> VARIEGATOR_MENU =
            registerMenuType(VariegatorMenu::new, "variegator");

    public static final RegistryObject<MenuType<ChargingTalismanMenu>> CHARGING_TALISMAN_MENU =
            registerMenuType(ChargingTalismanMenu::new, "charging_talisman");

    public static final RegistryObject<MenuType<MateriaManifestMenu>> MATERIA_MANIFEST_MENU =
            registerMenuType(MateriaManifestMenu::new, "materia_manifest");

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
