package com.aranaira.magichem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SublimationInProgressItem extends Item {
    public SublimationInProgressItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        CompoundTag nbt = pStack.getOrCreateTag();

        int containedItems = 0;
        if(nbt.contains("savedItems")) {
            ListTag itemsList = (ListTag)nbt.get("savedItems");
            for(int i=0; i<itemsList.size(); i++) {
                CompoundTag entry = (CompoundTag)itemsList.get(i);
                containedItems += entry.getInt("Count");
            }
        }

        int containedMateria = 1;
        if(nbt.contains("savedMateria")) {
            ListTag materiaList = (ListTag)nbt.get("savedMateria");
            for(int i=0; i<materiaList.size(); i++) {
                CompoundTag entry = (CompoundTag)materiaList.get(i);
                containedMateria += entry.getInt("Count");
            }
        }

        pTooltipComponents.add(Component.empty()
                .append(Component.translatable("tooltip.magichem.sublimation_in_progress.line1").withStyle(ChatFormatting.DARK_GRAY))
        );
        if(containedItems + containedMateria > 0) {
            pTooltipComponents.add(Component.empty());
            pTooltipComponents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.sublimation_in_progress.line2").withStyle(ChatFormatting.DARK_GRAY))
            );
            if(containedItems > 0) {
                pTooltipComponents.add(Component.empty()
                        .append(Component.literal("• ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(containedItems + "").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable("tooltip.magichem.sublimation_in_progress.line3.trail_items").withStyle(ChatFormatting.DARK_GRAY))
                );
            } if(containedMateria > 0) {
                pTooltipComponents.add(Component.empty()
                        .append(Component.literal("• ").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(containedMateria + "").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable("tooltip.magichem.sublimation_in_progress.line3.trail_materia").withStyle(ChatFormatting.DARK_GRAY))
                );
            }
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemInHand = pPlayer.getItemInHand(pUsedHand);

        CompoundTag nbt = itemInHand.getOrCreateTag();
        boolean
                noItems = false, noMateria = false;

        if(nbt.contains("savedItems")) {
            ListTag itemsList = (ListTag)nbt.get("savedItems");

            for(int i=0; i<itemsList.size(); i++) {
                CompoundTag entry = (CompoundTag)itemsList.get(i);
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.getString("id")));
                if(item != null) {
                    ItemStack stack = new ItemStack(item, entry.getByte("Count"));
                    pPlayer.drop(stack, false);
                }
            }
            nbt.remove("savedItems");
            noItems = true;
        } else {
            noItems = true;
        }

        //gotta know how many bottles the player has first
        int bottleCount = 0;
        for(ItemStack stack : pPlayer.getInventory().items) {
            if(stack.getItem() == Items.GLASS_BOTTLE)
                bottleCount += stack.getCount();
        }

        if(nbt.contains("savedMateria")) {
            ListTag materiaList = (ListTag)nbt.get("savedMateria");
            ListTag materiaListPost = new ListTag();

            for(int i=0; i<materiaList.size(); i++) {
                CompoundTag entry = (CompoundTag)materiaList.get(i);
                if(bottleCount == 0) {
                    CompoundTag materiaTag = new CompoundTag();
                    materiaTag.putString("id", entry.getString("id"));
                    materiaTag.putByte("Count", entry.getByte("Count"));
                    materiaListPost.add(materiaTag);
                } else {
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.getString("id")));
                    if (item != null) {
                        int amount = entry.getByte("Count");
                        int deduction = Math.min(amount, bottleCount);
                        ItemStack stack = new ItemStack(item, deduction);
                        pPlayer.drop(stack, false);

                        if (bottleCount < amount) {
                            CompoundTag materiaTag = new CompoundTag();
                            materiaTag.putString("id", entry.getString("id"));
                            materiaTag.putByte("Count", (byte)deduction);
                            materiaListPost.add(materiaTag);
                            pPlayer.getInventory().removeItem(new ItemStack(Items.GLASS_BOTTLE, deduction));
                            bottleCount = 0;
                        } else {
                            bottleCount -= amount;
                        }
                    }
                }
            }

            if(materiaListPost.size() == 0)
                noMateria = true;
            else
                nbt.put("savedMateria", materiaListPost);
        } else {
            noMateria = true;
        }

        if(noItems && noMateria)
            pPlayer.setItemInHand(pUsedHand, ItemStack.EMPTY);

        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
