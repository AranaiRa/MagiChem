package com.aranaira.magichem.item;

import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.MobEffectsRegistry;
import com.mna.api.capabilities.IPlayerProgression;
import com.mna.api.faction.IFaction;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;
import java.util.Random;

public class ChaliceOfTearsItem extends Item {
    private static final ResourceLocation FACTION_COUNCIL = new ResourceLocation("mna:council");
    private static final ResourceLocation FACTION_FEY = new ResourceLocation("mna:fey");
    private static final ResourceLocation FACTION_DEMONS = new ResourceLocation("mna:demons");
    private static final ResourceLocation FACTION_UNDEAD = new ResourceLocation("mna:undead");
    private static final Random r = new Random();
    private static final int[] HEALTH_STAGES = {0, 8, 18, 30, 45};

    public ChaliceOfTearsItem(Properties pProperties) {
        super(pProperties);
    }

    public static int getDamageAccumulationLimit() {
        return HEALTH_STAGES[HEALTH_STAGES.length - 1];
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        processDrinkChalice(pLevel, pPlayer, pUsedHand);

        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        return processDrinkChalice(pContext.getLevel(), pContext.getPlayer(), pContext.getHand()) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    private boolean processDrinkChalice(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if(pPlayer.getCooldowns().isOnCooldown(ItemRegistry.CHALICE_OF_TEARS.get())) return false;

        final ItemStack chalice = pPlayer.getItemInHand(pUsedHand);
        if(!chalice.hasTag()) return false;

        CompoundTag nbt = chalice.getTag();
        if(!nbt.contains("damageAccumulated")) return false;

        final LazyOptional<IPlayerProgression> lazyProg = pPlayer.getCapability(PlayerProgressionProvider.PROGRESSION);
        if(lazyProg.isPresent()) {
            final Optional<IPlayerProgression> optProg = lazyProg.resolve();
            if (optProg.isPresent()) {
                final IPlayerProgression prog = optProg.get();
                final IFaction faction = prog.getAlliedFaction();

                int damageAccumulated = nbt.getInt("damageAccumulated");
                int amplifier = 0;

                if(damageAccumulated >= HEALTH_STAGES[4])
                    amplifier = 4;
                else if(damageAccumulated >= HEALTH_STAGES[3])
                    amplifier = 3;
                else if(damageAccumulated >= HEALTH_STAGES[2])
                    amplifier = 2;
                else if(damageAccumulated >= HEALTH_STAGES[1])
                    amplifier = 1;

                if (faction != null) {
                    if (faction.is(FACTION_COUNCIL)) {
                        pPlayer.addEffect(new MobEffectInstance(MobEffectsRegistry.EQUANIMITY.get(), 1800, amplifier, false, true));
                    } else if (faction.is(FACTION_FEY)) {
                        pPlayer.addEffect(new MobEffectInstance(MobEffectsRegistry.EVANESCENCE.get(), 1800, amplifier, false, true));
                    } else if (faction.is(FACTION_DEMONS)) {
                        pPlayer.addEffect(new MobEffectInstance(MobEffectsRegistry.BRUTALITY.get(), 1800, amplifier, false, true));
                    } else if (faction.is(FACTION_UNDEAD)) {
                        pPlayer.addEffect(new MobEffectInstance(MobEffectsRegistry.MALICE.get(), 1800, amplifier, false, true));
                    }

                    pPlayer.getCooldowns().addCooldown(ItemRegistry.CHALICE_OF_TEARS.get(), ServerConfig.chaliceOfTearsCooldown * 20);
                    nbt.remove("damageAccumulated");

                    pLevel.playSound((Player)null, pPlayer.blockPosition(), SoundEvents.HONEY_DRINK, SoundSource.BLOCKS, 0.3f, 0.85f + r.nextFloat(0.3f));
                    pLevel.playSound((Player)null, pPlayer.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.3f, 0.7f + r.nextFloat(0.6f));
                }
            }
        }



        return true;
    }
}
