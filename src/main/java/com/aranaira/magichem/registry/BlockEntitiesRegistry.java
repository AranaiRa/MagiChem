package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.AlembicBlockEntity;
import com.aranaira.magichem.block.entity.CirclePowerBlockEntity;
import com.aranaira.magichem.block.entity.PowerSpikeBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntitiesRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MagiChemMod.MODID);

    public static final RegistryObject<BlockEntityType<AlembicBlockEntity>> ALEMBIC_BE = BLOCK_ENTITIES.register("alembic", () ->
            BlockEntityType.Builder.of(AlembicBlockEntity::new, BlockRegistry.ALEMBIC.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<CirclePowerBlockEntity>> CIRCLE_POWER_BE = BLOCK_ENTITIES.register("circle_power", () ->
            BlockEntityType.Builder.of(CirclePowerBlockEntity::new, BlockRegistry.CIRCLE_POWER.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<PowerSpikeBlockEntity>> POWER_SPIKE_BE = BLOCK_ENTITIES.register("power_spike", () ->
            BlockEntityType.Builder.of(PowerSpikeBlockEntity::new, BlockRegistry.POWER_SPIKE.get()).build(null)
    );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register((eventBus));
    }
}
