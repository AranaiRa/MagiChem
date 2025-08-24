package com.aranaira.magichem.item;

import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.MobEffectsRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StatusGrantingConsumableItem extends Item {
    private final boolean doParticles, multipleApplicationsStack;
    private final int cooldownTime, statusDuration, levelBoost, multipleApplicationStackLimit;
    private final MobEffect effect;
    private final SoundEvent sound;

    public StatusGrantingConsumableItem(Properties pProperties, MobEffect pEffect, int pStatusDuration, int pCooldownTime, SoundEvent pSound, int pLevelBoost, boolean pDoParticles, boolean pMultipleApplicationsStack, int pMultipleApplicationStackLimit) {
        super(pProperties);
        this.effect = pEffect;
        this.statusDuration = pStatusDuration;
        this.cooldownTime = pCooldownTime;
        this.sound = pSound;
        this.levelBoost = pLevelBoost;
        this.doParticles = pDoParticles;
        this.multipleApplicationsStack = pMultipleApplicationsStack;
        this.multipleApplicationStackLimit = pMultipleApplicationStackLimit;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        final ItemCooldowns cooldowns = pPlayer.getCooldowns();
        if(!cooldowns.isOnCooldown(this)) {
            if(!multipleApplicationsStack) {
                pPlayer.addEffect(new MobEffectInstance(effect, statusDuration, levelBoost, false, doParticles));
                cooldowns.addCooldown(this, cooldownTime);
                pPlayer.getItemInHand(pUsedHand).shrink(1);
                pLevel.playSound((Player) null, pPlayer.blockPosition(), sound, SoundSource.PLAYERS, 2.0F, 1.0f);
            } else {
                final MobEffectInstance existingEffect = pPlayer.getEffect(this.effect);
                if(existingEffect != null) {
                    if(existingEffect.getAmplifier() + 1 < multipleApplicationStackLimit) {
                        pPlayer.addEffect(new MobEffectInstance(effect, statusDuration, existingEffect.getAmplifier() + 1, false, doParticles));
                        cooldowns.addCooldown(this, cooldownTime);
                        pPlayer.getItemInHand(pUsedHand).shrink(1);
                        pLevel.playSound((Player) null, pPlayer.blockPosition(), sound, SoundSource.PLAYERS, 2.0F, 1.0f);
                    }
                } else {
                    pPlayer.addEffect(new MobEffectInstance(effect, statusDuration, levelBoost, false, doParticles));
                    cooldowns.addCooldown(this, cooldownTime);
                    pPlayer.getItemInHand(pUsedHand).shrink(1);
                    pLevel.playSound((Player) null, pPlayer.blockPosition(), sound, SoundSource.PLAYERS, 2.0F, 1.0f);
                }
            }
        }

        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(
                Component.translatable("tooltip.magichem."+this.toString())
                        .withStyle(ChatFormatting.DARK_GRAY)
        );

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
