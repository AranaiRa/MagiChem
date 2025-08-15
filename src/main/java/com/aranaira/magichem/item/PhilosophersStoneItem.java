package com.aranaira.magichem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhilosophersStoneItem extends Item {
    int wisdom = 0;

    public PhilosophersStoneItem(Properties pProperties, int pWisdom) {
        super(pProperties);
        this.wisdom = pWisdom;
    }

    public int getWisdom() {
        return wisdom;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(
                Component.translatable("tooltip.magichem."+this.toString()).withStyle(ChatFormatting.DARK_GRAY));

        pTooltipComponents.add(
                Component.translatable("tooltip.magichem.wisdom_stone_recipes").withStyle(ChatFormatting.DARK_GRAY));

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
