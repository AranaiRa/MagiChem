package com.aranaira.magichem.item;

import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.MobEffectsRegistry;
import com.mna.effects.EffectInit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DrinkableItem extends Item {
    private static final int DRINK_DURATION = 32;

    public DrinkableItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.DRINK;
    }

    public int getUseDuration(ItemStack pStack) {
        return DRINK_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        if(stack.getItem() == ItemRegistry.SHIMMERING_WINE_BOTTLE.get() && pPlayer.hasEffect(MobEffectsRegistry.TERANTIC_MIGHT.get()))
            return InteractionResultHolder.fail(stack);
        else
            return ItemUtils.startUsingInstantly(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if(pStack.getItem() == ItemRegistry.SWEETBERRY_WINE_BOTTLE.get()) {
            pLivingEntity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 12000, 2, false, false, true));
            //debuffs
            pLivingEntity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 1200, 0, false, false, true));
        }
        else if(pStack.getItem() == ItemRegistry.SHIMMERING_WINE_BOTTLE.get()) {
            //buffs
            pLivingEntity.addEffect(new MobEffectInstance(MobEffectsRegistry.GIGANTIC_VIGOR.get(), 36000, 0, false, false, true));
            pLivingEntity.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 9, false, false, true));
            //debuffs
            pLivingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 6000, 0, false, false, true));
            pLivingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 12000, 0, false, false, true));
            pLivingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 72000, 2, false, false, true));
            pLivingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 72000, 2, false, false, true));
            pLivingEntity.addEffect(new MobEffectInstance(EffectInit.FRAILTY.get(), 72000, 2, false, false, true));
        }
        else if(pStack.getItem() == ItemRegistry.SHIMMERING_VINTAGE_WINE_BOTTLE.get()) {
            //can't coexist with regular Vigor
            pLivingEntity.removeEffect(MobEffectsRegistry.GIGANTIC_VIGOR.get());
            //buffs
            pLivingEntity.addEffect(new MobEffectInstance(MobEffectsRegistry.TERANTIC_MIGHT.get(), 144000, 0, false, false, true));
            pLivingEntity.addEffect(new MobEffectInstance(MobEffects.HEAL, 1, 99, false, false, true));
            //debuffs
            pLivingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 144000, 0, false, false, true));
            pLivingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 288000, 0, false, false, true));
            pLivingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 288000, 3, false, false, true));
            pLivingEntity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 288000, 3, false, false, true));
            pLivingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 576000, 5, false, false, true));
            pLivingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 576000, 8, false, false, true));
            pLivingEntity.addEffect(new MobEffectInstance(EffectInit.FRAILTY.get(), 576000, 8, false, false, true));
            pLivingEntity.addEffect(new MobEffectInstance(EffectInit.SUNDER.get(), 576000, 8, false, false, true));
            pLivingEntity.addEffect(new MobEffectInstance(EffectInit.SILENCE.get(), 576000, 0, false, false, true));
        }

        if(pLivingEntity instanceof Player p && !p.isCreative()) {
            pStack.shrink(1);
            ItemEntity ie = new ItemEntity(pLevel, pLivingEntity.getX(), pLivingEntity.getY(), pLivingEntity.getZ(), new ItemStack(Items.GLASS_BOTTLE));
            pLevel.addFreshEntity(ie);
        }
        return pStack;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
//        pTooltipComponents.add(
//                Component.translatable("tooltip.magichem."+this.toString())
//                        .withStyle(ChatFormatting.DARK_GRAY)
//        );

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
