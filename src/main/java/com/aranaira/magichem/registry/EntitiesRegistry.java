package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.entities.SublimationRitualVFXEntity;
import com.aranaira.magichem.entities.ShlorpEntity;
import com.aranaira.magichem.entities.ThrownThunderstoneEntity;
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
    public static final RegistryObject<EntityType<SublimationRitualVFXEntity>> INFUSION_RITUAL_VFX_ENTITY;
    public static final RegistryObject<EntityType<ThrownThunderstoneEntity>> THROWN_THUNDERSTONE_ENTITY;

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    static {
        ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MagiChemMod.MODID);

        SHLORP_ENTITY = ENTITY_TYPES.register("shlorp", () ->
                Builder.of(ShlorpEntity::new, MobCategory.MISC)
                .sized(0.2f, 0.2f)
                .build(MagiChemMod.MODID+":shlorp")
        );

        INFUSION_RITUAL_VFX_ENTITY = ENTITY_TYPES.register("infusion_ritual_vfx", () ->
                Builder.of(SublimationRitualVFXEntity::new, MobCategory.MISC)
                .sized(0.1f, 0.8f)
                .build(MagiChemMod.MODID+":infusion_ritual_vfx")
        );

        THROWN_THUNDERSTONE_ENTITY = ENTITY_TYPES.register("thrown_thunderstone", () ->
                Builder.of(ThrownThunderstoneEntity::new, MobCategory.MISC)
                .sized(0.1f, 0.8f)
                .build(MagiChemMod.MODID+":thrown_thunderstone")
        );
    }
}
