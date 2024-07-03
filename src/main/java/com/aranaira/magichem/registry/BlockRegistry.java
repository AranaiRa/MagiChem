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
import net.minecraft.world.level.block.state.BlockBehaviour;
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

    public static final RegistryObject<Block> CIRCLE_POWER = registerBlock("circle_power",
            () -> new CirclePowerBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion().isSuffocating((pState, pLevel, pPos) -> false))
    );

    public static final RegistryObject<Block> CIRCLE_FABRICATION = registerBlock("circle_fabrication",
            () -> new CircleFabricationBlock(BlockBehaviour.Properties.of()
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
