package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.AlembicBlock;
import com.aranaira.magichem.block.CircleFabricationBlock;
import com.aranaira.magichem.block.CirclePowerBlock;
import com.aranaira.magichem.block.PowerSpikeBlock;
import com.aranaira.magichem.item.PowerSpikeItem;
import com.aranaira.magichem.item.TooltipLoreBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MagiChemMod.MODID);

    public static final RegistryObject<Block> ALEMBIC = registerBlock("alembic",
            () -> new AlembicBlock(BlockBehaviour.Properties.of(Material.STONE)
                    .strength(0.75f).noOcclusion()), CreativeModeTabs.MAGICHEM_TAB
    );

    public static final RegistryObject<Block> CIRCLE_POWER = registerBlock("circle_power",
            () -> new CirclePowerBlock(BlockBehaviour.Properties.of(Material.STONE)
                    .strength(0.75f).noOcclusion()), CreativeModeTabs.MAGICHEM_TAB
    );

    public static final RegistryObject<Block> CIRCLE_FABRICATION = registerBlock("circle_fabrication",
            () -> new CircleFabricationBlock(BlockBehaviour.Properties.of(Material.STONE)
                    .strength(0.75f).noOcclusion()), CreativeModeTabs.MAGICHEM_TAB
    );

    public static final RegistryObject<Block> POWER_SPIKE = registerBlock("power_spike",
            () -> new PowerSpikeBlock(BlockBehaviour.Properties.of(Material.STONE)
                    .strength(0.5f).noOcclusion()), CreativeModeTabs.MAGICHEM_TAB
    );

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, CreativeModeTab tab) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn, tab);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block, CreativeModeTab tab) {
        switch(name) {
            case "power_spike": return ItemRegistry.ITEMS.register(name, () -> new PowerSpikeItem(block.get(), new Item.Properties().tab(tab)));
            default: return ItemRegistry.ITEMS.register(name, () -> new TooltipLoreBlockItem(block.get(), new Item.Properties().tab(tab)));
        }
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
