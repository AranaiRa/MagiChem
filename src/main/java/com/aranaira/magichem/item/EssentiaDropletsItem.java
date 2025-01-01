package com.aranaira.magichem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EssentiaDropletsItem extends EssentiaItem {
    public EssentiaDropletsItem(String essentiaName, String essentiaAbbreviation, String essentiaHouse, int essentiaWheel, String essentiaColor) {
        super(essentiaName, essentiaAbbreviation, essentiaHouse, essentiaWheel, essentiaColor);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        pStack.shrink(1);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("tooltip.magichem.droplets").withStyle(ChatFormatting.DARK_GRAY));
    }
}
