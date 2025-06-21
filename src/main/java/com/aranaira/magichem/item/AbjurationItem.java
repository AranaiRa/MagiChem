package com.aranaira.magichem.item;

import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.aranaira.magichem.block.entity.MirrorLabyrinthBlockEntity.TRAIL_PARTICLE_COLORS;

public class AbjurationItem extends Item {
    public static final Random r = new Random();

    public AbjurationItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        boolean foundCurse = false;

        if(!pPlayer.getCooldowns().isOnCooldown(ItemRegistry.ABJURATION.get())) {
            for (ItemStack stackWorn : pPlayer.getInventory().armor) {
                final Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stackWorn);
                for (Enchantment e : enchantments.keySet()) {
                    foundCurse = e.isCurse();

                    if(foundCurse && !pLevel.isClientSide()) {
                        enchantments.remove(e);
                        EnchantmentHelper.setEnchantments(enchantments, stackWorn);
                        final ItemStack stack = pPlayer.getItemInHand(pUsedHand);
                        int uses = 1;
                        CompoundTag nbt = null;
                        if(stack.hasTag()) {
                            nbt = stack.getTag();
                            if(nbt.contains("uses")) {
                                uses = nbt.getInt("uses") + 1;
                                if(uses >= 3) {
                                    pPlayer.sendSystemMessage(Component.translatable("feedback.ritual.rebornrose.abjuration"));
                                }
                            }
                        }
                        if(uses >= 3) {
                            stack.shrink(1);
                            pLevel.playSound((Player)null, pPlayer.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 2.0F, 1.0f);
                        } else {
                            if (nbt == null) nbt = new CompoundTag();
                            nbt.putInt("uses", uses);
                            stack.setTag(nbt);
                        }
                    }

                    if (foundCurse) break;
                }
                if (foundCurse) break;
            }

            if (foundCurse) {
                pPlayer.swing(pUsedHand);
                pPlayer.getCooldowns().addCooldown(ItemRegistry.ABJURATION.get(), 50);

                for(int i=0; i<21; i++) {
                    int colorIndex = i % 7;
                    pLevel.addParticle(new MAParticleType(ParticleInit.TRAIL_ORBIT.get())
                                    .setPhysics(false).setScale(0.0625f).setMaxAge(80)
                                    .setColor(TRAIL_PARTICLE_COLORS[colorIndex][0], TRAIL_PARTICLE_COLORS[colorIndex][1], TRAIL_PARTICLE_COLORS[colorIndex][2]),
                            pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                            r.nextDouble(0.125) + 0.025, 0.02 + r.nextDouble(0.04), 0.2 + r.nextDouble(0.4));
                }

                for(int i=0; i<21; i++) {
                    pLevel.addParticle(new MAParticleType(ParticleInit.DUST.get())
                                    .setPhysics(false).setScale(0.25f).setMaxAge(120).setGravity(0)
                                    .setColor(29, 55, 129, 128),
                            pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                            r.nextDouble(0.0625) - 0.03125, 0.02 + r.nextDouble(0.04), r.nextDouble(0.0625) - 0.03125);
                }
            }
        }

        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        int uses = 0;
        if(pStack.hasTag() && pStack.getTag().contains("uses")) {
            uses = pStack.getTag().getInt("uses");
        }
        pTooltipComponents.add(
                Component.empty()
                        .append(Component.translatable("tooltip.magichem.abjuration.line1").withStyle(ChatFormatting.DARK_GRAY))
        );
        pTooltipComponents.add(
                Component.empty()
                        .append(Component.translatable("tooltip.magichem.abjuration.line2.part1").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(""+(3 - uses)).withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable("tooltip.magichem.abjuration.line2.part2").withStyle(ChatFormatting.DARK_GRAY))
        );

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
