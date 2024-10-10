package com.aranaira.magichem.item;

import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.api.events.EnderfeatherCharmUsedEvent;
import com.mna.tools.TeleportHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HellfeatherCharmItem extends Item {
    public HellfeatherCharmItem(Properties pProperties) {
        super(pProperties);
    }

    //Copied and modified from MnA's enderfeather charm use code
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if(pLevel.isClientSide())
            return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
        else {
            ServerPlayer serverPlayer = (ServerPlayer)pPlayer;
            ItemStack stack = pPlayer.getItemInHand(pUsedHand);
            BlockPos portalPos = null;

            if(serverPlayer.getPersistentData().contains("lastNetherPortal")) {
                long raw = serverPlayer.getPersistentData().getLong("lastNetherPortal");
                portalPos = BlockPos.of(raw);
            }

            if(!pLevel.dimension().location().equals(new ResourceLocation("minecraft:the_nether"))) {
                pPlayer.sendSystemMessage(Component.translatable("feedback.item.hellfeathercharm.wrongdim"));
                return InteractionResultHolder.fail(stack);
            } else if(portalPos == null) {
                pPlayer.sendSystemMessage(Component.translatable("feedback.item.hellfeathercharm.noportalpos"));
                return InteractionResultHolder.fail(stack);
            } else {
                EnderfeatherCharmUsedEvent event = new EnderfeatherCharmUsedEvent(serverPlayer);
                MinecraftForge.EVENT_BUS.post(event);
                if(event.isCanceled()) {
                    serverPlayer.addItem(new ItemStack(this));
                    return InteractionResultHolder.fail(stack);
                } else {
                    pPlayer.awardStat(Stats.ITEM_USED.get(ItemRegistry.HELLFEATHER_CHARM.get()));
                    pLevel.broadcastEntityEvent(serverPlayer, (byte)46);
                    pLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.9F + (float)Math.random() * 0.2F);
                    TeleportHelper.teleportEntity(serverPlayer, pLevel.dimension(), new Vec3(portalPos.getX() + 0.5D, portalPos.getY(), portalPos.getZ() + 0.5D));
                    pLevel.playSound(null, portalPos.getX(), portalPos.getY(), portalPos.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.9F + (float)Math.random() * 0.2F);
                    pLevel.broadcastEntityEvent(serverPlayer, (byte)46);

                    if(!pPlayer.isCreative())
                        pPlayer.setItemInHand(pUsedHand, ItemStack.EMPTY);

                    return InteractionResultHolder.success(stack);
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(
                Component.translatable("tooltip.magichem.hellfeathercharm")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
