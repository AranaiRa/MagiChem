package com.aranaira.magichem.events;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.registry.MobEffectsRegistry;
import com.aranaira.magichem.spell.WisdomSpellAdjuster;
import com.mna.api.events.SpellCastEvent;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.spells.SpellCaster;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

import static com.aranaira.magichem.registry.MobEffectsRegistry.EQUANIMITY_HEAL_PER_MANA;

@Mod.EventBusSubscriber(
        modid= MagiChemMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class SpellAdjusterEvents {
    public SpellAdjusterEvents() {

    }

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        SpellCaster.registerAdjuster(WisdomSpellAdjuster::checkSpellDamageAttribute, WisdomSpellAdjuster::modifySpellDamageAttribute);
        SpellCaster.registerAdjuster(WisdomSpellAdjuster::checkSpellDelayAttribute, WisdomSpellAdjuster::modifySpellDelayAttribute);
        SpellCaster.registerAdjuster(WisdomSpellAdjuster::checkSpellDurationAttribute, WisdomSpellAdjuster::modifySpellDurationAttribute);
        SpellCaster.registerAdjuster(WisdomSpellAdjuster::checkSpellLesserMagnitudeAttribute, WisdomSpellAdjuster::modifySpellLesserMagnitudeAttribute);
        SpellCaster.registerAdjuster(WisdomSpellAdjuster::checkSpellMagnitudeAttribute, WisdomSpellAdjuster::modifySpellMagnitudeAttribute);
        SpellCaster.registerAdjuster(WisdomSpellAdjuster::checkSpellRadiusAttribute, WisdomSpellAdjuster::modifySpellRadiusAttribute);
        SpellCaster.registerAdjuster(WisdomSpellAdjuster::checkSpellRangeAttribute, WisdomSpellAdjuster::modifySpellRangeAttribute);
        SpellCaster.registerAdjuster(WisdomSpellAdjuster::checkSpellSpeedAttribute, WisdomSpellAdjuster::modifySpellSpeedAttribute);
    }
}
