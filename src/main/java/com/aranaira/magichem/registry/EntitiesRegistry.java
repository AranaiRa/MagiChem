package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.entities.ShlorpEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(
    modid = MagiChemMod.MODID,
    bus = Mod.EventBusSubscriber.Bus.MOD
)
public class EntitiesRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES;
    public static final RegistryObject<EntityType<ShlorpEntity>> SHLORP_ENTITY;

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    static {
        ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MagiChemMod.MODID);
        SHLORP_ENTITY = ENTITY_TYPES.register("shlorp", () -> {
            return Builder.of(ShlorpEntity::new, MobCategory.MISC)
                    .sized(0.2f, 0.2f)
                    .build(MagiChemMod.MODID+":shlorp");
        });
    }
}
