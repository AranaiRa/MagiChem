package com.aranaira.magichem.item;

import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.particles.MAParticleType;
import com.mna.api.particles.ParticleInit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class SlumberingIdolItem extends Item {
    public static final Random r = new Random();

    public SlumberingIdolItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        if(!pPlayer.getCooldowns().isOnCooldown(stack.getItem())) {
            AABB zone = new AABB(
                    pPlayer.getX() - ServerConfig.slumberingIdolRange, pPlayer.getY() - ServerConfig.slumberingIdolRange, pPlayer.getZ() - ServerConfig.slumberingIdolRange,
                    pPlayer.getX() + ServerConfig.slumberingIdolRange, pPlayer.getY() + ServerConfig.slumberingIdolRange, pPlayer.getZ() + ServerConfig.slumberingIdolRange
            );
            for (Phantom phantomInZone : pPlayer.level().getEntitiesOfClass(Phantom.class, zone)) {
                //particles
                if (pLevel.isClientSide()) {
                    int particleCount = 20;
                    float speed = 0.0625f;
                    for (int i = 0; i < particleCount; i++) {
                        pLevel.addParticle(new MAParticleType(ParticleInit.DUST.get())
                                        .setScale(0.375f).setMaxAge(60 + r.nextInt(80)).setGravity(0)
                                        .setColor(92, 172, 147, 48),
                                phantomInZone.getX(), phantomInZone.getY(), phantomInZone.getZ(),
                                (r.nextFloat() - 0.5f) * speed, 0.025f + (r.nextFloat() * 0.05f), (r.nextFloat() - 0.5f) * speed);
                    }

                    particleCount = 40;
                    speed = 0.75f;
                    for (int i = 0; i < particleCount; i++) {
                        pLevel.addParticle(new MAParticleType(ParticleInit.SPARKLE_VELOCITY.get())
                                        .setScale(0.25f).setMaxAge(15 + r.nextInt(30)).setPhysics(true).setGravity(0.03f),
                                phantomInZone.getX(), phantomInZone.getY(), phantomInZone.getZ(),
                                (r.nextFloat() - 0.5f) * speed, (r.nextFloat() - 0.5f) * speed, (r.nextFloat() - 0.5f) * speed);
                    }
                } else
                    phantomInZone.kill();
            }
        }

        pPlayer.getCooldowns().addCooldown(ItemRegistry.SLUMBERING_IDOL.get().asItem(), 1350);
        pPlayer.swing(pUsedHand);
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(
                Component.translatable("tooltip.magichem.slumbering_idol")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
