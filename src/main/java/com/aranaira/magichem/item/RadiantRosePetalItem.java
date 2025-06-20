package com.aranaira.magichem.item;

import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.MobEffectsRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RadiantRosePetalItem extends Item {
    public RadiantRosePetalItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        final ItemCooldowns cooldowns = pPlayer.getCooldowns();
        if(!cooldowns.isOnCooldown(ItemRegistry.RADIANT_ROSE_PETAL.get())) {
            pPlayer.addEffect(new MobEffectInstance(MobEffectsRegistry.RADIANT_RESOLVE.get(), 120));
            cooldowns.addCooldown(ItemRegistry.RADIANT_ROSE_PETAL.get(), 900);
            pPlayer.getItemInHand(pUsedHand).shrink(1);
            pLevel.playSound((Player)null, pPlayer.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 2.0F, 1.0f);
        }

        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(
                Component.translatable("tooltip.magichem.radiantrosepetal")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
