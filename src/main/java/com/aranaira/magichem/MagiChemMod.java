package com.aranaira.magichem;

import com.aranaira.magichem.gui.*;
import com.aranaira.magichem.item.renderer.MateriaVesselItemRenderer;
import com.aranaira.magichem.registry.*;
import com.mna.api.guidebook.RegisterGuidebooksEvent;
import com.mna.items.base.INoCreativeTab;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MagiChemMod.MODID)
public class MagiChemMod
{
    public static final String MODID = "magichem";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);


    public MagiChemMod()
    {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        CreativeTabsRegistry.register(eventBus);
        ItemRegistry.register(eventBus);
        BlockRegistry.register(eventBus);
        BlockEntitiesRegistry.register(eventBus);
        MenuRegistry.register(eventBus);
        MateriaRegistry.register(eventBus);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> {
            return () -> {
                eventBus.register(BlockEntitiesClientRegistry.class);
            };
        });

        RecipeRegistry.register(eventBus);

        eventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        eventBus.addListener(this::fillCreativeTabs);

        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);


        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        //Only uncomment this nonsense if we need to generate the custom JSON files again
        //FixationSeparationRecipeGenerator.parseRecipeTable();
        //FixationSeparationRecipeGenerator.generateRecipes();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {

        });

        PacketRegistry.register();
    }

    private void fillCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        /*if(event.getTab() == CreativeTabsRegistry.MAGICHEM_TAB.get()) {
            ITEMS.getEntries().stream().map(RegistryObject::get).forEach((item) -> {
                if(!ItemRegistry.ITEMS_EXCLUDED_FROM_TABS.contains(item))
                    event.accept(item);
            });
        }
        else if(event.getTab() == CreativeTabsRegistry.MAGICHEM_MATERIA_TAB.get()) {
            ItemRegistry.ESSENTIA.getEntries().stream().map(RegistryObject::get).forEach((item) -> {
                event.accept(item);
            });
            ItemRegistry.ADMIXTURES.getEntries().stream().map(RegistryObject::get).forEach((item) -> {
                event.accept(item);
            });
        }*/
    }

    private static boolean isInCreativeTab(Item item, List<Item> excluded) {
        return !(item instanceof INoCreativeTab) && !excluded.contains(item);
    }

    @SubscribeEvent
    public void onRegisterGuidebooks(RegisterGuidebooksEvent event) {
        event.getRegistry().addGuidebookPath(new ResourceLocation(MODID, "guide"));

        event.getRegistry().registerGuidebookCategory("magichem", new ResourceLocation(MagiChemMod.MODID, "iris_argenti"));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            MenuScreens.register(MenuRegistry.CIRCLE_FABRICATION_MENU.get(), CircleFabricationScreen::new);
            MenuScreens.register(MenuRegistry.CIRCLE_POWER_MENU.get(), CirclePowerScreen::new);
            MenuScreens.register(MenuRegistry.ALEMBIC_MENU.get(), AlembicScreen::new);
            MenuScreens.register(MenuRegistry.CENTRIFUGE_MENU.get(), CentrifugeScreen::new);
            MenuScreens.register(MenuRegistry.ADMIXER_MENU.get(), AdmixerScreen::new);
        }

        @SubscribeEvent
        public static void onTextureStitch(TextureStitchEvent event) {
            //Jar labels
            /*
            event.addSprite(new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_ender-arcane"));
            event.addSprite(new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_earth-nigredo"));
            event.addSprite(new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_water-albedo"));
            event.addSprite(new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_air-citrinitas"));
            event.addSprite(new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_fire-rubedo"));
            event.addSprite(new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_conceptual-verdant"));
            event.addSprite(new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_fleshy-nourishing"));
            event.addSprite(new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_rotten-mineral"));
            event.addSprite(new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_wrought-precious"));
            event.addSprite(new ResourceLocation(MagiChemMod.MODID, "block/decorator/jar_label_bookends"));
             */
        }

        @SubscribeEvent
        public static void onRegisterSpecialRenderers(ModelEvent.RegisterAdditional event) {
            event.register(MateriaVesselItemRenderer.SPECIAL_RENDERER);
        }
    }
}
