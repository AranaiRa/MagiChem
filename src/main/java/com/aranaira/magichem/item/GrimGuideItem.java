package com.aranaira.magichem.item;

import com.aranaira.magichem.capabilities.enhancement.EnhancementCapability;
import com.aranaira.magichem.capabilities.enhancement.EnhancementProvider;
import com.mna.api.blocks.DirectionalPoint;
import com.mna.api.items.IPositionalItem;
import com.mna.items.runes.ItemRuneMarking;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GrimGuideItem extends Item implements IPositionalItem<ItemRuneMarking> {
    public GrimGuideItem(Item.Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void setLocation(ItemStack itemStack, DirectionalPoint directionalPoint) {

    }

    @Override
    public void setLocation(ItemStack itemStack, BlockPos blockPos, Direction direction) {

    }

    @Override
    public void setLocation(ItemStack itemStack, BlockPos blockPos, Direction direction, Level level) {

    }

    @Override
    public void copyPositionFrom(ItemStack itemStack, ItemStack itemStack1) {

    }

    @Nullable
    @Override
    public BlockPos getLocation(ItemStack itemStack) {
        if (itemStack.hasTag()) {
            final CompoundTag tag = itemStack.getTag();
            if(tag.contains("stored_point")) {
                return BlockPos.of(tag.getLong("stored_point"));
            }
        }
        return null;
    }

    @Override
    public Direction getFace(ItemStack itemStack) {
        return Direction.UP;
    }

    @Nullable
    @Override
    public Component getBlockName(ItemStack itemStack) {
        return null;
    }

    @Nullable
    @Override
    public DirectionalPoint getDirectionalPoint(ItemStack itemStack) {
        BlockPos pos = getLocation(itemStack);
        if(pos != null)
            return new DirectionalPoint(pos, Direction.UP, null, null);
        return null;
    }

    @Nullable
    @Override
    public String getBlockTranslateKey(ItemStack itemStack) {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getMarkedBlockId(ItemStack itemStack) {
        return null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        super.onInventoryTick(stack, level, player, slotIndex, selectedIndex);

        if(!level.isClientSide() && level.getGameTime() % 30 == 0) {
            EnhancementProvider.getCapability(player).ifPresent(cap -> {
                if(cap.hasLastDeathTargetLocation()) {
                    final Pair<BlockPos, ResourceLocation> deathData = cap.getLastDeathTargetLocation();
                    CompoundTag tag = stack.getOrCreateTag();
                    if(tag.contains("stored_point")) {
                        long query = tag.getLong("stored_point");
                        long newPos = deathData.getFirst().asLong();
                        if(query != newPos) {
                            tag.putLong("stored_point", newPos);
                        }
                    } else {
                        tag.putLong("stored_point", deathData.getFirst().asLong());
                    }
                }
            });
        }
    }
}
