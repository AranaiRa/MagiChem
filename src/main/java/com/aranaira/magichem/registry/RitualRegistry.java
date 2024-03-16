package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.ritual.RitualEffectAlchemicalInfusion;
import com.aranaira.magichem.ritual.RitualEffectCraftPowerReagent2;
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
                    //Ritual of the Rod and Circle
                    helper.register(
                            new ResourceLocation(MagiChemMod.MODID, "ritual-effect-reagent2craft"),
                            new RitualEffectCraftPowerReagent2(new ResourceLocation(MagiChemMod.MODID, "rituals/reagent_2_crafting"))
                    );
                });
    }

}
