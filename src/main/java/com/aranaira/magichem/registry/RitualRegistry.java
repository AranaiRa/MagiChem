package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.ritual.*;
import com.mna.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(
        modid= MagiChemMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class RitualRegistry {

    @SubscribeEvent
    public static void registerRitualEffects(RegisterEvent event) {
        event.register(((IForgeRegistry)Registries.RitualEffect.get()).getRegistryKey(), (helper) -> {
                    //Ritual of the Balanced Scales
                    helper.register(
                            new ResourceLocation(MagiChemMod.MODID, "ritual-effect-balanced_scales"),
                            new RitualEffectAlchemicalSublimation(new ResourceLocation(MagiChemMod.MODID, "rituals/balanced_scales"))
                    );

                    //Ritual of the Reborn Rose
                    helper.register(
                            new ResourceLocation(MagiChemMod.MODID, "ritual-effect-reborn_rose"),
                            new RitualEffectRebornRose(new ResourceLocation(MagiChemMod.MODID, "rituals/reborn_rose"))
                    );

                    //Rituals of Celestial Awakening
                    helper.register(
                            new ResourceLocation(MagiChemMod.MODID, "ritual-effect-celestial_awakening_solar"),
                            new RitualEffectCelestialAwakeningSolar(new ResourceLocation(MagiChemMod.MODID, "rituals/celestial_awakening_solar"))
                    );
                    helper.register(
                            new ResourceLocation(MagiChemMod.MODID, "ritual-effect-celestial_awakening_lunar"),
                            new RitualEffectCelestialAwakeningLunar(new ResourceLocation(MagiChemMod.MODID, "rituals/celestial_awakening_lunar"))
                    );

                    //Ritual of Surgical Cultivation
                    helper.register(
                            new ResourceLocation(MagiChemMod.MODID, "ritual-effect-surgical_cultivation"),
                            new RitualEffectSurgicalCultivation(new ResourceLocation(MagiChemMod.MODID, "rituals/surgical_cultivation"))
                    );
                });
    }

}
