package com.aranaira.magichem.item;

import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import com.mna.effects.EffectInit;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.aranaira.magichem.block.entity.MirrorLabyrinthBlockEntity.TRAIL_PARTICLE_COLORS;

public class AbjurationNecroticItem extends Item {
    public static final Random r = new Random();

    public AbjurationNecroticItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        boolean foundCurse = false;

        if(!pPlayer.getCooldowns().isOnCooldown(ItemRegistry.NECROTIC_ABJURATION.get())) {
            if (pInteractionTarget instanceof ZombieVillager zv) {
                if(!pPlayer.level().isClientSide()) {
                    zv.startConverting(pPlayer.getUUID(), 60);

                    //Lock the villager in place for a little bit to make the VFX work right
                    MobEffectInstance mei = new MobEffectInstance(EffectInit.ENTANGLE.get(), 120, 0, false, false);
                    zv.addEffect(mei);
                }

                int uses = 1;
                CompoundTag nbt = null;
                if (pStack.hasTag()) {
                    nbt = pStack.getTag();
                    if (nbt.contains("uses")) {
                        uses = nbt.getInt("uses") + 1;
                        if (uses >= 9) {
                            if(!pPlayer.level().isClientSide()) pPlayer.sendSystemMessage(Component.translatable("feedback.ritual.rebornrose.abjuration"));
                            pStack.shrink(1);
                        }
                    }
                }
                if (uses >= 9) {
                    pPlayer.setItemInHand(pUsedHand, ItemStack.EMPTY);
                    pPlayer.level().playSound((Player) null, pPlayer.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 2.0F, 1.0f);
                } else {
                    if (nbt == null) nbt = new CompoundTag();
                    nbt.putInt("uses", uses);
                    pStack.setTag(nbt);
                    pPlayer.setItemInHand(pUsedHand, pStack);
                }

                pPlayer.swing(pUsedHand);
                pPlayer.getCooldowns().addCooldown(ItemRegistry.NECROTIC_ABJURATION.get(), 50);

                for (int i = 0; i < 21; i++) {
                    int colorIndex = i % 7;
                    pPlayer.level().addParticle(new MAParticleType(ParticleInit.TRAIL_ORBIT.get())
                                    .setPhysics(false).setScale(0.0625f).setMaxAge(80)
                                    .setColor(TRAIL_PARTICLE_COLORS[colorIndex][0], TRAIL_PARTICLE_COLORS[colorIndex][1], TRAIL_PARTICLE_COLORS[colorIndex][2]),
                            zv.getX(), zv.getY(), zv.getZ(),
                            r.nextDouble()*0.125 + 0.025, 0.02 + r.nextDouble()*0.04, 0.2 + r.nextDouble()*0.4);
                }

                for (int i = 0; i < 21; i++) {
                    pPlayer.level().addParticle(new MAParticleType(ParticleInit.DUST.get())
                                    .setPhysics(false).setScale(0.25f).setMaxAge(120).setGravity(0)
                                    .setColor(58, 55, 44, 128),
                            zv.getX(), zv.getY(), zv.getZ(),
                            r.nextDouble()*0.0625 - 0.03125, 0.02 + r.nextDouble()*0.04, r.nextDouble()*0.0625 - 0.03125);
                }
            }
        }

        return super.interactLivingEntity(pStack, pPlayer, pInteractionTarget, pUsedHand);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        int uses = 0;
        if(pStack.hasTag() && pStack.getTag().contains("uses")) {
            uses = pStack.getTag().getInt("uses");
        }
        pTooltipComponents.add(
                Component.empty()
                        .append(Component.translatable("tooltip.magichem.abjurationnecrotic.line1").withStyle(ChatFormatting.DARK_GRAY))
        );
        pTooltipComponents.add(
                Component.empty()
                        .append(Component.translatable("tooltip.magichem.abjurationnecrotic.line2.part1").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(""+(9 - uses)).withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable("tooltip.magichem.abjurationnecrotic.line2.part2").withStyle(ChatFormatting.DARK_GRAY))
        );

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
