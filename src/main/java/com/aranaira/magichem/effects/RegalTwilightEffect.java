package com.aranaira.magichem.effects;

import com.aranaira.magichem.registry.MobEffectsRegistry;
import com.mna.api.affinity.Affinity;
import com.mna.api.capabilities.IPlayerMagic;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.playerdata.magic.PlayerMagic;
import com.mna.capabilities.playerdata.magic.PlayerMagicProvider;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.effects.EffectInit;
import com.mna.entities.EntityInit;
import com.mna.entities.faction.Pixie;
import com.mna.factions.Factions;
import com.mna.tools.SummonUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

public class RegalTwilightEffect extends MobEffect {
    private static final ResourceLocation FACTION_FEY = new ResourceLocation("mna:fey");

    public RegalTwilightEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public void applyEffectTick(LivingEntity pLivingEntity, int pAmplifier) {
        if(pLivingEntity instanceof Player player) {
            final LazyOptional<IPlayerProgression> progLazy = player.getCapability(PlayerProgressionProvider.PROGRESSION);
            progLazy.ifPresent(pCap -> {
                if(pCap.getAlliedFaction() != null && pCap.getAlliedFaction().is(FACTION_FEY)) {
                    boolean daytime = player.level().isDay();
                    boolean summer = player.getPersistentData().getInt("faction_casting_resource_idx") == 0;

                    if((daytime && summer) || (!daytime && !summer)) {
                        if (summer) {
                            player.getPersistentData().putInt("faction_casting_resource_idx", 1);
                        } else {
                            player.getPersistentData().putInt("faction_casting_resource_idx", 0);
                        }

                        updatePixies(player);

                        final LazyOptional<IPlayerMagic> magicLazy = player.getCapability(PlayerMagicProvider.MAGIC);
                        magicLazy.ifPresent(mCap -> mCap.setCastingResourceType(Factions.FEY.getCastingResource(player)));
                    }
                }
            });
        }
    }

    private void updatePixies(Player pPlayer) {
        int existingPixies = 0;
        //TODO: Force pixie elements and set custom names by element once the API allows element forcing
        boolean hasEarth = false, hasWater = false, hasAir = false, hasFire = false;
        for (Mob summon : SummonUtils.getSummons(pPlayer)) {
            if(summon instanceof Pixie pixie) {
                existingPixies++;
                Affinity affinity = pixie.getAffinity();
                if(affinity == Affinity.EARTH) hasEarth = true;
                if(affinity == Affinity.WATER) hasWater = true;
                if(affinity == Affinity.WIND) hasAir = true;
                if(affinity == Affinity.FIRE) hasFire = true;
            }
        }

        for(int i=0; i<4-existingPixies; i++) {
            Pixie pixie = new Pixie(EntityInit.PIXIE.get(), pPlayer.level());
            pixie.setPos(pPlayer.blockPosition().getCenter());
            pixie.setTier(2);
            pPlayer.level().addFreshEntity(pixie);
            pixie.addEffect(new MobEffectInstance(EffectInit.REDUCE.get(), -1, 6, false, false));
            pixie.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, -1, 10, false, false));
            pixie.addEffect(new MobEffectInstance(MobEffectsRegistry.EVANESCENCE.get(), -1, 2, false, false));
            SummonUtils.setSummon(pixie, pPlayer, 0);
        }
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return pDuration % 100 == 0;
    }
}
