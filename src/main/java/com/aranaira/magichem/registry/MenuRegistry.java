package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.gui.AlembicMenu;
import com.aranaira.magichem.gui.CircleFabricationMenu;
import com.aranaira.magichem.gui.CirclePowerMenu;
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

    public static final RegistryObject<MenuType<CirclePowerMenu>> CIRCLE_POWER_MENU =
            registerMenuType(CirclePowerMenu::new, "circle_power");

    public static final RegistryObject<MenuType<AlembicMenu>> ALEMBIC_MENU =
            registerMenuType(AlembicMenu::new, "alembic");

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
