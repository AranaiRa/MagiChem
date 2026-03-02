package com.aranaira.magichem;

import com.aranaira.magichem.block.entity.renderer.*;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.data.DamageTypeProvider;
import com.aranaira.magichem.gui.*;
import com.aranaira.magichem.interop.OccultismCompat;
import com.aranaira.magichem.interop.mna.MnAPlugin;
import com.aranaira.magichem.item.renderer.*;
import com.aranaira.magichem.item.renderer.mna.CodexMateriaItemRenderer;
import com.aranaira.magichem.item.renderer.mna.SublimationPrimerItemRenderer;
import com.aranaira.magichem.registry.*;
import com.aranaira.magichem.registry.compat.BloodMagicItemRegistry;
import com.aranaira.magichem.registry.compat.CreateItemRegistry;
import com.aranaira.magichem.registry.compat.OccultismItemRegistry;
import com.mna.api.guidebook.RegisterGuidebooksEvent;
import com.mna.items.base.INoCreativeTab;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
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
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;

import java.lang.management.ManagementFactory;
import java.util.List;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MagiChemMod.MODID)
public class MagiChemMod
{
    public static final String MODID = "magichem";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static MagiChemMod INSTANCE = null;

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public final boolean isDebug = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;

    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MagiChemMod.MODID, "main"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();


    public MagiChemMod()
    {
        INSTANCE = this;

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ItemRegistry.register(eventBus);
        BlockRegistry.register(eventBus);
        BlockEntitiesRegistry.register(eventBus);
        FluidRegistry.register(eventBus);
        MateriaRegistry.register(eventBus);
        MenuRegistry.register(eventBus);
        RecipeRegistry.register(eventBus);
        EntitiesRegistry.register(eventBus);
        LootModifierRegistry.register(eventBus);
        MobEffectsRegistry.register(eventBus);
        MinecraftForge.EVENT_BUS.register(CommandRegistry.class);

        if(FMLEnvironment.dist.isClient()) {
            eventBus.register(BlockEntitiesClientRegistry.class);
            eventBus.register(EntitiesClientRegistry.class);
            CreativeTabsRegistry.register(eventBus);
        }

        eventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);

        //Conditional registration
        ModList modList = ModList.get();

        if(modList.isLoaded("bloodmagic")) {
            BloodMagicItemRegistry.register(eventBus);
        }
        if(modList.isLoaded("create")) {
            CreateItemRegistry.register(eventBus);
        }
        if(modList.isLoaded("occultism")) {
            OccultismItemRegistry.register(eventBus);
            OccultismCompat.handleRegistration(eventBus);
        }

        //Only uncomment this nonsense if we need to generate the custom JSON files again
        //FixationSeparationRecipeGenerator.parseRecipeTable();
        //FixationSeparationRecipeGenerator.generateRecipes();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            CantripRegistry.register();
            ConstructFluidSprayRegistry.register();
            CuriosApi.registerCurio(ItemRegistry.CHARGING_TALISMAN.get(), ItemRegistry.CHARGING_TALISMAN.get());
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
            event.enqueueWork(() -> {
                // Some client setup code
                MenuScreens.register(MenuRegistry.CIRCLE_FABRICATION_MENU.get(), CircleFabricationScreen::new);
                MenuScreens.register(MenuRegistry.GRAND_CIRCLE_FABRICATION_MENU.get(), GrandCircleFabricationScreen::new);
                MenuScreens.register(MenuRegistry.CIRCLE_POWER_MENU.get(), CirclePowerScreen::new);
                MenuScreens.register(MenuRegistry.ALEMBIC_MENU.get(), AlembicScreen::new);
                MenuScreens.register(MenuRegistry.DISTILLERY_MENU.get(), DistilleryScreen::new);
                MenuScreens.register(MenuRegistry.CENTRIFUGE_MENU.get(), CentrifugeScreen::new);
                MenuScreens.register(MenuRegistry.FUSERY_MENU.get(), FuseryScreen::new);
                MenuScreens.register(MenuRegistry.GRAND_DISTILLERY_MENU.get(), GrandDistilleryScreen::new);
                MenuScreens.register(MenuRegistry.GRAND_CENTRIFUGE_MENU.get(), GrandCentrifugeScreen::new);
                MenuScreens.register(MenuRegistry.GRAND_FUSERY_MENU.get(), GrandFuseryScreen::new);
                MenuScreens.register(MenuRegistry.ALCHEMICAL_NEXUS_MENU.get(), AlchemicalNexusScreen::new);
                MenuScreens.register(MenuRegistry.ACTUATOR_WATER_MENU.get(), ActuatorWaterScreen::new);
                MenuScreens.register(MenuRegistry.ACTUATOR_FIRE_MENU.get(), ActuatorFireScreen::new);
                MenuScreens.register(MenuRegistry.ACTUATOR_EARTH_MENU.get(), ActuatorEarthScreen::new);
                MenuScreens.register(MenuRegistry.ACTUATOR_AIR_MENU.get(), ActuatorAirScreen::new);
                MenuScreens.register(MenuRegistry.ACTUATOR_ARCANE_MENU.get(), ActuatorArcaneScreen::new);
                MenuScreens.register(MenuRegistry.ACTUATOR_ENDER_MENU.get(), ActuatorEnderScreen::new);
                MenuScreens.register(MenuRegistry.VARIEGATOR_MENU.get(), VariegatorScreen::new);
                MenuScreens.register(MenuRegistry.CHARGING_TALISMAN_MENU.get(), ChargingTalismanScreen::new);
                MenuScreens.register(MenuRegistry.TRAVELLERS_COMPASS_MENU.get(), TravellersCompassScreen::new);
                MenuScreens.register(MenuRegistry.CONJURER_MENU.get(), ConjurerScreen::new);
                MenuScreens.register(MenuRegistry.MATERIA_MANIFEST_MENU.get(), MateriaManifestScreen::new);
                MenuScreens.register(MenuRegistry.STANDING_RETORT_MENU.get(), StandingRetortScreen::new);
                MenuScreens.register(MenuRegistry.MIRROR_LABYRINTH_MENU.get(), MirrorLabyrinthScreen::new);
                MenuScreens.register(MenuRegistry.MATERIA_REFLECTOR_MENU.get(), MateriaReflectorScreen::new);
                MenuScreens.register(MenuRegistry.ELDRIN_ORRERY_MENU.get(), EldrinOrreryScreen::new);
                MenuScreens.register(MenuRegistry.WISDOM_MENU.get(), WisdomScreen::new);
                MenuScreens.register(MenuRegistry.CODEX_MATERIA_MENU.get(), CodexMateriaScreen::new);
                MenuScreens.register(MenuRegistry.SKYWRATH_CONDENSER_MENU.get(), SkywrathCondenserScreen::new);
                MenuScreens.register(MenuRegistry.ASTRAL_OBSERVER_MENU.get(), AstralObserverScreen::new);
                MenuScreens.register(MenuRegistry.DISINTEGRATION_PYRE_MENU.get(), DisintegrationPyreScreen::new);
                MenuScreens.register(MenuRegistry.COVETOUS_COFFER_MENU.get(), CovetousCofferScreen::new);
                MenuScreens.register(MenuRegistry.PRIME_AGGREGATOR_MENU.get(), PrimeAggregatorScreen::new);

                ItemBlockRenderTypes.setRenderLayer(FluidRegistry.SIMPLE_ACID.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(FluidRegistry.SIMPLE_ACID_FLOWING.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(FluidRegistry.AQUA_FORTIS.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(FluidRegistry.AQUA_FORTIS_FLOWING.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(FluidRegistry.AQUA_REGIA.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(FluidRegistry.AQUA_REGIA_FLOWING.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(FluidRegistry.OIL_OF_VITRIOL.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(FluidRegistry.OIL_OF_VITRIOL_FLOWING.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(FluidRegistry.AZOTH.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(FluidRegistry.AZOTH_FLOWING.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(FluidRegistry.SWEETBERRY_WINE.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(FluidRegistry.SWEETBERRY_WINE_FLOWING.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(FluidRegistry.SHIMMERING_WINE.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(FluidRegistry.SHIMMERING_WINE_FLOWING.get(), RenderType.translucent());
            });
        }

        @SubscribeEvent
        public static void onGatherData(final GatherDataEvent event) {
            RegistrySetBuilder rsb = new RegistrySetBuilder();
            DamageTypeProvider.register(rsb);
        }

        @SubscribeEvent
        public static void onRegisterSpecialRenderers(ModelEvent.RegisterAdditional event) {
            event.register(MasterItemRenderer.RENDERER_JAR);
            event.register(MasterItemRenderer.RENDERER_JAR_QUAD);
            event.register(MasterItemRenderer.RENDERER_VESSEL);

            event.register(CentrifugeBlockEntityRenderer.RENDERER_MODEL_COG);
            event.register(CentrifugeBlockEntityRenderer.RENDERER_MODEL_WHEEL);

            event.register(GrandDistilleryBlockEntityRenderer.RENDERER_MODEL_PLUG_BASE);
            event.register(GrandDistilleryBlockEntityRenderer.RENDERER_MODEL_PLUG_UPGRADED);

            event.register(GrandCentrifugeBlockEntityRenderer.RENDERER_MODEL_WHEEL);

            event.register(ActuatorFireBlockEntityRenderer.RENDERER_MODEL_PIPE_LEFT);
            event.register(ActuatorFireBlockEntityRenderer.RENDERER_MODEL_PIPE_RIGHT);
            event.register(ActuatorFireBlockEntityRenderer.RENDERER_MODEL_PIPE_CENTER);

            event.register(ActuatorWaterBlockEntityRenderer.RENDERER_MODEL_STEAM_VENTS);

            event.register(ActuatorEarthBlockEntityRenderer.RENDERER_MODEL_STAMPER);

            event.register(ActuatorAirBlockEntityRenderer.RENDERER_MODEL_FANS);

            event.register(ActuatorArcaneBlockEntityRenderer.RENDERER_MODEL_CUBE_VAR1);
            event.register(ActuatorArcaneBlockEntityRenderer.RENDERER_MODEL_CUBE_VAR2);

            event.register(ActuatorEnderBlockEntityRenderer.RENDERER_MODEL_LOCATOR);

            event.register(VariegatorBlockEntityRenderer.RENDERER_MODEL_LIMB_ABOVE);
            event.register(VariegatorBlockEntityRenderer.RENDERER_MODEL_LIMB_ABOVE_TINTABLE);
            event.register(VariegatorBlockEntityRenderer.RENDERER_MODEL_LIMB_LONG);
            event.register(VariegatorBlockEntityRenderer.RENDERER_MODEL_LIMB_LONG_TINTABLE);
            event.register(VariegatorBlockEntityRenderer.RENDERER_MODEL_LIMB_SHORT);
            event.register(VariegatorBlockEntityRenderer.RENDERER_MODEL_LIMB_SHORT_TINTABLE);
            event.register(VariegatorBlockEntityRenderer.RENDERER_MODEL_SHARD);
            event.register(VariegatorBlockEntityRenderer.RENDERER_MODEL_SHELL_TINTABLE);

            event.register(AlchemicalNexusBlockEntityRenderer.RENDERER_MODEL_CRYSTAL);

            event.register(ExperienceExchangerBlockEntityRenderer.RENDER_MODEL_COM);
            event.register(ExperienceExchangerBlockEntityRenderer.RENDER_MODEL_RING1);
            event.register(ExperienceExchangerBlockEntityRenderer.RENDER_MODEL_RING2);

            event.register(CrystalCandleBlockEntityRenderer.RENDERER_CRYSTAL_CANDLE);

            event.register(CircleToilBlockEntityRenderer.RENDERER_MODEL_HANDLE);

            event.register(CirclePowerBlockEntityRenderer.RENDERER_MODEL_REAGENT_2);
            event.register(CirclePowerBlockEntityRenderer.RENDERER_MODEL_REAGENT_3);
            event.register(CirclePowerBlockEntityRenderer.RENDERER_MODEL_REAGENT_4);

            event.register(CircleFabricationBlockEntityRenderer.RENDERER_MODEL_CIRCLE);
            event.register(CircleFabricationBlockEntityRenderer.RENDERER_MODEL_BOWLFILL);

            event.register(GrandCircleFabricationBlockEntityRenderer.RENDERER_MODEL_FLOATING_VESSEL);

            event.register(SublimationPrimerItemRenderer.SUBLIMATION_PRIMER_OPEN);
            event.register(SublimationPrimerItemRenderer.SUBLIMATION_PRIMER_CLOSED);

            event.register(CodexMateriaItemRenderer.CODEX_MATERIA_OPEN);
            event.register(CodexMateriaItemRenderer.CODEX_MATERIA_CLOSED);

            event.register(SignaliteBlockEntityRenderer.RENDERER_MODEL_BUTT);
            event.register(SignaliteBlockEntityRenderer.RENDERER_MODEL_SPIKE);
            event.register(SignaliteBlockEntityRenderer.RENDERER_MODEL_SPIKE_AGGREGATING);
            event.register(SignaliteBlockEntityRenderer.RENDERER_MODEL_SPIKE_BURNISHING);
            event.register(SignaliteBlockEntityRenderer.RENDERER_MODEL_SPIKE_CHAOTIC);
            event.register(SignaliteBlockEntityRenderer.RENDERER_MODEL_SPIKE_DEVOURING);
            event.register(SignaliteBlockEntityRenderer.RENDERER_MODEL_SPIKE_EQUATING);
            event.register(SignaliteBlockEntityRenderer.RENDERER_MODEL_SPIKE_GATEKEEPING);
            event.register(SignaliteBlockEntityRenderer.RENDERER_MODEL_SPIKE_METICULOUS);
            event.register(SignaliteBlockEntityRenderer.RENDERER_MODEL_SPIKE_NEGATING);

            event.register(SignaliteSeerBlockEntityRenderer.RENDERER_MODEL_SEER_BODY);
            event.register(SignaliteSeerBlockEntityRenderer.RENDERER_MODEL_SEER_TORCH);
            event.register(SignaliteSeerBlockEntityRenderer.RENDERER_MODEL_SEER_TORCH_SHELL);
            event.register(SignalitePairBlockEntityRenderer.RENDERER_MODEL_SINGING);
            event.register(SignalitePairBlockEntityRenderer.RENDERER_MODEL_LISTENING);

            event.register(MirrorLabyrinthBlockEntityRenderer.RENDERER_MODEL_MIRROR);
            event.register(MirrorLabyrinthBlockEntityRenderer.RENDERER_MODEL_MATRIX);

            event.register(MateriaReflectorBlockEntityRenderer.RENDERER_MODEL_EYE);
            event.register(MateriaReflectorBlockEntityRenderer.RENDERER_MODEL_IRIS);

            event.register(AstralObserverBlockEntityRenderer.RENDERER_MODEL_TELESCOPE);

            event.register(GnosticOrbBlockEntityRenderer.RENDERER_MODEL_BODY);
            event.register(GnosticOrbBlockEntityRenderer.RENDERER_MODEL_ACTIVE);
            event.register(GnosticOrbBlockEntityRenderer.RENDERER_MODEL_INACTIVE);

            event.register(EldrinOrreryBlockEntityRenderer.RENDERER_MODEL_ORB_PLANET);
            event.register(EldrinOrreryBlockEntityRenderer.RENDERER_MODEL_ORB_MOON);
            event.register(EldrinOrreryBlockEntityRenderer.RENDERER_MODEL_ORB_SUN);
            event.register(EldrinOrreryBlockEntityRenderer.RENDERER_MODEL_RING_SMALL);
            event.register(EldrinOrreryBlockEntityRenderer.RENDERER_MODEL_RING_LARGE);

            event.register(DisintegrationPyreBlockEntityRenderer.RENDERER_MODEL_FLAME_SMALL);
            event.register(DisintegrationPyreBlockEntityRenderer.RENDERER_MODEL_FLAME_LARGE);

            event.register(CovetousCofferBlockEntityRenderer.RENDERER_MODEL_LID);

            event.register(BossTrophyBlockEntityRenderer.RENDERER_MODEL_COUNCIL_CRYSTAL_INNER);
            event.register(BossTrophyBlockEntityRenderer.RENDERER_MODEL_COUNCIL_CRYSTAL_OUTER);
            event.register(BossTrophyBlockEntityRenderer.RENDERER_MODEL_COUNCIL_SLATE);
            event.register(BossTrophyBlockEntityRenderer.RENDERER_MODEL_FEY_SUMMER_1);
            event.register(BossTrophyBlockEntityRenderer.RENDERER_MODEL_FEY_SUMMER_2);
            event.register(BossTrophyBlockEntityRenderer.RENDERER_MODEL_FEY_SUMMER_3);
            event.register(BossTrophyBlockEntityRenderer.RENDERER_MODEL_FEY_WINTER_1);
            event.register(BossTrophyBlockEntityRenderer.RENDERER_MODEL_FEY_WINTER_2);
            event.register(BossTrophyBlockEntityRenderer.RENDERER_MODEL_FEY_WINTER_3);
            event.register(BossTrophyBlockEntityRenderer.RENDERER_MODEL_UNDEAD_WATER);
        }
    }
}
