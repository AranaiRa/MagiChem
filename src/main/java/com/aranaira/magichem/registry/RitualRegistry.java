package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.ritual.RitualEffectAlchemicalSublimation;
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
                });
    }

}
