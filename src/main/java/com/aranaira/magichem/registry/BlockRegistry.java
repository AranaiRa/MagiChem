package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.*;
import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.aranaira.magichem.item.MateriaVesselItem;
import com.aranaira.magichem.item.PowerSpikeItem;
import com.aranaira.magichem.item.TooltipLoreBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
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
                    .strength(0.75f).noOcclusion())
    );

    public static final RegistryObject<Block> CENTRIFUGE = registerBlock("centrifuge",
            () -> new CentrifugeBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion())
    );

    public static final RegistryObject<Block> CENTRIFUGE_ROUTER = registerBlock("centrifuge_router",
            () -> new CentrifugeRouterBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion())
    );

    public static final RegistryObject<Block> ADMIXER = registerBlock("admixer",
            () -> new AdmixerBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion())
    );

    public static final RegistryObject<Block> CIRCLE_POWER = registerBlock("circle_power",
            () -> new CirclePowerBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion())
    );

    public static final RegistryObject<Block> CIRCLE_FABRICATION = registerBlock("circle_fabrication",
            () -> new CircleFabricationBlock(BlockBehaviour.Properties.of()
                    .strength(0.75f).noOcclusion())
    );

    public static final RegistryObject<Block> POWER_SPIKE = registerBlock("power_spike",
            () -> new PowerSpikeBlock(BlockBehaviour.Properties.of()
                    .strength(0.5f).noOcclusion())
    );

    public static final RegistryObject<Block> MATERIA_VESSEL = registerBlock("materia_vessel",
            () -> new MateriaVesselBlock(BlockBehaviour.Properties.of()
                    .strength(0.5f).noOcclusion())
    );

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return switch (name) {
            case "materia_vessel" -> ItemRegistry.ITEMS.register(name, () -> new MateriaVesselItem(block.get(), new Item.Properties()));
            case "power_spike" -> ItemRegistry.ITEMS.register(name, () -> new PowerSpikeItem(block.get(), new Item.Properties()));
            default -> ItemRegistry.ITEMS.register(name, () -> new TooltipLoreBlockItem(block.get(), new Item.Properties()));
        };
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
