package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.*;
import com.aranaira.magichem.item.MateriaJarItem;
import com.aranaira.magichem.item.MateriaVesselItem;
import com.aranaira.magichem.item.PowerSpikeItem;
import com.aranaira.magichem.item.TooltipLoreBlockItem;
import com.mna.blocks.decoration.SimpleRotationalBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MagiChemMod.MODID);

    public static final RegistryObject<Block> ALEMBIC = registerBlock("alembic",
            () -> new AlembicBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> MAGICHEMICAL_MECHANISM = registerBlock("magichemical_mechanism",
            () -> new SimpleRotationalBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f))
    );

    public static final RegistryObject<Block> CENTRIFUGE = registerBlock("centrifuge",
            () -> new CentrifugeBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> CENTRIFUGE_ROUTER = registerBlock("centrifuge_router",
            () -> new CentrifugeRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> DISTILLERY = registerBlock("distillery",
            () -> new DistilleryBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> DISTILLERY_ROUTER = registerBlock("distillery_router",
            () -> new DistilleryRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> FUSERY = registerBlock("fusery",
            () -> new FuseryBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> FUSERY_ROUTER = registerBlock("fusery_router",
            () -> new FuseryRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> ALCHEMICAL_NEXUS = registerBlock("alchemical_nexus",
            () -> new AlchemicalNexusBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> ALCHEMICAL_NEXUS_ROUTER = registerBlock("alchemical_nexus_router",
            () -> new AlchemicalNexusRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> GRAND_DISTILLERY = registerBlock("grand_distillery",
            () -> new GrandDistilleryBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> GRAND_DISTILLERY_ROUTER = registerBlock("grand_distillery_router",
            () -> new GrandDistilleryRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> GRAND_CENTRIFUGE = registerBlock("grand_centrifuge",
            () -> new GrandCentrifugeBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> GRAND_CENTRIFUGE_ROUTER = registerBlock("grand_centrifuge_router",
            () -> new GrandCentrifugeRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> CONJURER_ROUTER = registerBlock("conjurer_router",
            () -> new ConjurerRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> CIRCLE_TOIL = registerBlock("circle_toil",
            () -> new CircleToilBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> CIRCLE_POWER = registerBlock("circle_power",
            () -> new CirclePowerBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> CIRCLE_POWER_ROUTER = registerBlock("circle_power_router",
            () -> new CirclePowerRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> CIRCLE_FABRICATION = registerBlock("circle_fabrication",
            () -> new CircleFabricationBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> CIRCLE_FABRICATION_ROUTER = registerBlock("circle_fabrication_router",
            () -> new CircleFabricationRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> GRAND_CIRCLE_FABRICATION = registerBlock("grand_circle_fabrication",
            () -> new GrandCircleFabricationBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> GRAND_CIRCLE_FABRICATION_ROUTER = registerBlock("grand_circle_fabrication_router",
            () -> new GrandCircleFabricationRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> POWER_SPIKE = registerBlock("power_spike",
            () -> new PowerSpikeBlock(BlockBehaviour.Properties.of()
                    .strength(0.5f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> MATERIA_JAR = registerBlock("materia_jar",
            () -> new MateriaJarBlock(BlockBehaviour.Properties.of()
                    .strength(0.5f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> MATERIA_VESSEL = registerBlock("materia_vessel",
            () -> new MateriaVesselBlock(BlockBehaviour.Properties.of()
                    .strength(0.5f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> ACTUATOR_FIRE = registerBlock("actuator_fire",
            () -> new ActuatorFireBlock(BlockBehaviour.Properties.of()
                    .strength(0.5f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> ACTUATOR_WATER = registerBlock("actuator_water",
            () -> new ActuatorWaterBlock(BlockBehaviour.Properties.of()
                    .strength(0.5f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> ACTUATOR_EARTH = registerBlock("actuator_earth",
            () -> new ActuatorEarthBlock(BlockBehaviour.Properties.of()
                    .strength(0.5f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> ACTUATOR_AIR = registerBlock("actuator_air",
            () -> new ActuatorAirBlock(BlockBehaviour.Properties.of()
                    .strength(0.5f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> ACTUATOR_ARCANE = registerBlock("actuator_arcane",
            () -> new ActuatorArcaneBlock(BlockBehaviour.Properties.of()
                    .strength(0.5f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> BASE_ACTUATOR_ROUTER = registerBlock("base_actuator_router",
            () -> new BaseActuatorRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> ACTUATOR_FIRE_ROUTER = registerBlock("actuator_fire_router",
            () -> new ActuatorFireRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> ACTUATOR_WATER_ROUTER = registerBlock("actuator_water_router",
            () -> new ActuatorWaterRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> ACTUATOR_EARTH_ROUTER = registerBlock("actuator_earth_router",
            () -> new ActuatorEarthRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> ACTUATOR_AIR_ROUTER = registerBlock("actuator_air_router",
            () -> new ActuatorAirRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> ACTUATOR_ARCANE_ROUTER = registerBlock("actuator_arcane_router",
            () -> new ActuatorArcaneRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> COLORING_CAULDRON = registerBlock("coloring_cauldron",
            () -> new ColoringCauldronBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> VARIEGATOR = registerBlock("variegator",
            () -> new VariegatorBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> VARIEGATOR_ROUTER = registerBlock("variegator_router",
            () -> new VariegatorRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> CONJURER = registerBlock("conjurer",
            () -> new ConjurerBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> MATERIA_MANIFEST = registerBlock("materia_manifest",
            () -> new MateriaManifestBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> MATERIA_MANIFEST_ROUTER = registerBlock("materia_manifest_router",
            () -> new MateriaManifestRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> EXPERIENCE_EXCHANGER = registerBlock("experience_exchanger",
            () -> new ExperienceExchangerBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> SILVER_BUTTON = registerBlock("silver_button",
            () -> new SilverButtonBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> SILVER_PRESSURE_PLATE = registerBlock("silver_pressure_plate",
            () -> new SilverPressurePlateBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> CONSTRUCT_PRESSURE_PLATE = registerBlock("construct_pressure_plate",
            () -> new ConstructPressurePlateBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> TWO_STATE_LEVER = registerBlock("two_state_lever",
            () -> new MultiStateLeverBlock(1, BlockBehaviour.Properties.of()
                    .strength(0.5f).noOcclusion().noCollission().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> THREE_STATE_LEVER = registerBlock("three_state_lever",
            () -> new MultiStateLeverBlock(2, BlockBehaviour.Properties.of()
                    .strength(0.5f).noOcclusion().noCollission().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> FOUR_STATE_LEVER = registerBlock("four_state_lever",
            () -> new MultiStateLeverBlock(3, BlockBehaviour.Properties.of()
                    .strength(0.5f).noOcclusion().noCollission().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> FIVE_STATE_LEVER = registerBlock("five_state_lever",
            () -> new MultiStateLeverBlock(4, BlockBehaviour.Properties.of()
                    .strength(0.5f).noOcclusion().noCollission().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> SIX_STATE_LEVER = registerBlock("six_state_lever",
            () -> new MultiStateLeverBlock(5, BlockBehaviour.Properties.of()
                    .strength(0.5f).noOcclusion().noCollission().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> BLEACHED_AMETHYST_BLOCK = registerBlock("bleached_amethyst_block",
            () -> new BleachedAmethystBlock(BlockBehaviour.Properties.of()
                    .strength(1.5f).sound(SoundType.AMETHYST).pushReaction(PushReaction.DESTROY))
    );

    public static final RegistryObject<Block> VINTEUM_CRYSTAL_BLOCK = registerBlock("vinteum_crystal_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(1.5f).forceSolidOn().noOcclusion().randomTicks().sound(SoundType.AMETHYST).pushReaction(PushReaction.DESTROY))
    );

    public static final RegistryObject<Block> BUDDING_VINTEUM_CRYSTAL_BLOCK = registerBlock("vinteum_crystal_block_budding",
            () -> new BuddingVinteumCrystalBlock(BlockBehaviour.Properties.of()
                    .strength(1.5f).forceSolidOn().noOcclusion().randomTicks().sound(SoundType.AMETHYST).pushReaction(PushReaction.DESTROY))
    );

    public static final RegistryObject<Block> VINTEUM_CLUSTER = registerBlock("cluster_vinteum",
            () -> new CrystalClusterBudBlock(7, 3, BlockBehaviour.Properties.of()
                    .strength(1.5f).forceSolidOn().noOcclusion().randomTicks().sound(SoundType.AMETHYST).lightLevel(param -> 5).pushReaction(PushReaction.DESTROY))
    );

    public static final RegistryObject<Block> LARGE_VINTEUM_CLUSTER = registerBlock("cluster_vinteum_large",
            () -> new CrystalClusterBudBlock(5, 3, BlockBehaviour.Properties.of()
                    .strength(1.5f).forceSolidOn().noOcclusion().randomTicks().sound(SoundType.AMETHYST).lightLevel(param -> 5).pushReaction(PushReaction.DESTROY))
    );

    public static final RegistryObject<Block> MEDIUM_VINTEUM_CLUSTER = registerBlock("cluster_vinteum_medium",
            () -> new CrystalClusterBudBlock(4, 3, BlockBehaviour.Properties.of()
                    .strength(1.5f).forceSolidOn().noOcclusion().randomTicks().sound(SoundType.AMETHYST).lightLevel(param -> 5).pushReaction(PushReaction.DESTROY))
    );

    public static final RegistryObject<Block> SMALL_VINTEUM_CLUSTER = registerBlock("cluster_vinteum_small",
            () -> new CrystalClusterBudBlock(3, 4, BlockBehaviour.Properties.of()
                    .strength(1.5f).forceSolidOn().noOcclusion().randomTicks().sound(SoundType.AMETHYST).lightLevel(param -> 5).pushReaction(PushReaction.DESTROY))
    );

    public static final RegistryObject<Block> CRYSTAL_CANDLE = registerBlock("crystal_candle",
            () -> new CrystalCandleBlock(BlockBehaviour.Properties.of()
                    .strength(0.125f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false).noCollission())
    );

    public static final RegistryObject<Block> OCCULTED_CINDER = registerBlock("occulted_cinder",
            () -> new OccultedCinderBlock(BlockBehaviour.Properties.of()
                    .noCollission().instabreak().noOcclusion().isSuffocating((pState, pLevel, pPos) -> false), false)
    );

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return switch (name) {
            case "materia_jar" -> ItemRegistry.ITEMS.register(name, () -> new MateriaJarItem(block.get(), new Item.Properties()));
            case "materia_vessel" -> ItemRegistry.ITEMS.register(name, () -> new MateriaVesselItem(block.get(), new Item.Properties()));
            case "power_spike" -> ItemRegistry.ITEMS.register(name, () -> new PowerSpikeItem(block.get(), new Item.Properties()));
            default -> ItemRegistry.ITEMS.register(name, () -> new TooltipLoreBlockItem(block.get(), new Item.Properties()));
        };
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
