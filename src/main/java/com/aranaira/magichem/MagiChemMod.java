package com.aranaira.magichem;

import com.aranaira.magichem.block.entity.renderer.*;
import com.aranaira.magichem.gui.*;
import com.aranaira.magichem.interop.OccultismCompat;
import com.aranaira.magichem.interop.mna.MnAPlugin;
import com.aranaira.magichem.item.renderer.MateriaJarItemRenderer;
import com.aranaira.magichem.item.renderer.MateriaVesselItemRenderer;
import com.aranaira.magichem.item.renderer.SublimationPrimerItemRenderer;
import com.aranaira.magichem.registry.*;
import com.aranaira.magichem.registry.compat.OccultismItemRegistry;
import com.mna.api.guidebook.RegisterGuidebooksEvent;
import com.mna.items.base.INoCreativeTab;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

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

        ItemRegistry.register(eventBus);
        BlockRegistry.register(eventBus);
        BlockEntitiesRegistry.register(eventBus);
        FluidRegistry.register(eventBus);
        MateriaRegistry.register(eventBus);
        MenuRegistry.register(eventBus);
        RecipeRegistry.register(eventBus);
        EntitiesRegistry.register(eventBus);

        if(FMLEnvironment.dist.isClient()) {
            eventBus.register(BlockEntitiesClientRegistry.class);
            eventBus.register(EntitiesClientRegistry.class);
            CreativeTabsRegistry.register(eventBus);
        }

        eventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        //Conditional registration
        ModList modList = ModList.get();

        if(modList.isLoaded("occultism")) {
            OccultismItemRegistry.ITEMS_COMPAT_OCCULTISM.register(eventBus);
            OccultismCompat.handleRegistration(eventBus);
        }

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

    private static boolean isInCreativeTab(Item item, List<Item> excluded) {
        return !(item instanceof INoCreativeTab) && !excluded.contains(item);
    }

    @SubscribeEvent
    public void onRegisterGuidebooks(RegisterGuidebooksEvent event) {
        event.getRegistry().addGuidebookPath(new ResourceLocation(MODID, "guide"));

        event.getRegistry().registerGuidebookCategory("magichem", new ResourceLocation(MagiChemMod.MODID, "materia_vessel"));
        event.getRegistry().registerGuidebookCategory("magichem_wonders", new ResourceLocation(MagiChemMod.MODID, "sublimation_primer"));

        MnAPlugin.register();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
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
            MenuScreens.register(MenuRegistry.DISTILLERY_MENU.get(), DistilleryScreen::new);
            MenuScreens.register(MenuRegistry.CENTRIFUGE_MENU.get(), CentrifugeScreen::new);
            MenuScreens.register(MenuRegistry.FUSERY_MENU.get(), FuseryScreen::new);
            MenuScreens.register(MenuRegistry.GRAND_DISTILLERY_MENU.get(), GrandDistilleryScreen::new);
            MenuScreens.register(MenuRegistry.ALCHEMICAL_NEXUS_MENU.get(), AlchemicalNexusScreen::new);
            MenuScreens.register(MenuRegistry.ACTUATOR_WATER_MENU.get(), ActuatorWaterScreen::new);
            MenuScreens.register(MenuRegistry.ACTUATOR_FIRE_MENU.get(), ActuatorFireScreen::new);
            MenuScreens.register(MenuRegistry.ACTUATOR_EARTH_MENU.get(), ActuatorEarthScreen::new);
            MenuScreens.register(MenuRegistry.ACTUATOR_AIR_MENU.get(), ActuatorAirScreen::new);
            MenuScreens.register(MenuRegistry.ACTUATOR_ARCANE_MENU.get(), ActuatorArcaneScreen::new);
            MenuScreens.register(MenuRegistry.CHARGING_TALISMAN_MENU.get(), ChargingTalismanScreen::new);
        }

        @SubscribeEvent
        public static void onRegisterSpecialRenderers(ModelEvent.RegisterAdditional event) {
            event.register(MateriaVesselItemRenderer.RENDERER_VESSEL);
            event.register(MateriaJarItemRenderer.RENDERER_JAR);

            event.register(CentrifugeBlockEntityRenderer.RENDERER_MODEL_COG);
            event.register(CentrifugeBlockEntityRenderer.RENDERER_MODEL_WHEEL);

            event.register(GrandDistilleryBlockEntityRenderer.RENDERER_MODEL_PLUG_BASE);
            event.register(GrandDistilleryBlockEntityRenderer.RENDERER_MODEL_PLUG_UPGRADED);

            event.register(ActuatorFireBlockEntityRenderer.RENDERER_MODEL_PIPE_LEFT);
            event.register(ActuatorFireBlockEntityRenderer.RENDERER_MODEL_PIPE_RIGHT);
            event.register(ActuatorFireBlockEntityRenderer.RENDERER_MODEL_PIPE_CENTER);

            event.register(ActuatorWaterBlockEntityRenderer.RENDERER_MODEL_STEAM_VENTS);

            event.register(ActuatorEarthBlockEntityRenderer.RENDERER_MODEL_STAMPER);

            event.register(ActuatorAirBlockEntityRenderer.RENDERER_MODEL_FANS);

            event.register(ActuatorArcaneBlockEntityRenderer.RENDERER_MODEL_CUBE_VAR1);
            event.register(ActuatorArcaneBlockEntityRenderer.RENDERER_MODEL_CUBE_VAR2);

            event.register(AlchemicalNexusBlockEntityRenderer.RENDERER_MODEL_CRYSTAL);

            event.register(ExperienceExchangerBlockEntityRenderer.RENDER_MODEL_COM);
            event.register(ExperienceExchangerBlockEntityRenderer.RENDER_MODEL_RING1);
            event.register(ExperienceExchangerBlockEntityRenderer.RENDER_MODEL_RING2);

            event.register(SublimationPrimerItemRenderer.SUBLIMATION_PRIMER_OPEN);
            event.register(SublimationPrimerItemRenderer.SUBLIMATION_PRIMER_CLOSED);
        }
    }
}
