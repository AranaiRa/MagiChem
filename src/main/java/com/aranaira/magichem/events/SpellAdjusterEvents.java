package com.aranaira.magichem.events;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.spell.WisdomSpellAdjuster;
import com.mna.spells.SpellCaster;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

@Mod.EventBusSubscriber(
        modid= MagiChemMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class SpellAdjusterEvents {
    public SpellAdjusterEvents() {

    }

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        SpellCaster.registerAdjuster(WisdomSpellAdjuster::checkSpellRadiusAttribute, WisdomSpellAdjuster::modifySpellRadiusAttribute);
    }
}
