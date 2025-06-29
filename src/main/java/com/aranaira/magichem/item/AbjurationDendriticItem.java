package com.aranaira.magichem.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AbjurationDendriticItem extends Item {
    public AbjurationDendriticItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        int uses = 0;
        if(pStack.hasTag() && pStack.getTag().contains("uses")) {
            uses = pStack.getTag().getInt("uses");
        }
        pTooltipComponents.add(
                Component.empty()
                        .append(Component.translatable("tooltip.magichem.abjuration_dendritic.line1").withStyle(ChatFormatting.DARK_GRAY))
        );
        pTooltipComponents.add(
                Component.empty()
                        .append(Component.translatable("tooltip.magichem.abjuration_dendritic.line2.part1").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(""+(3 - uses)).withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable("tooltip.magichem.abjuration_dendritic.line2.part2").withStyle(ChatFormatting.DARK_GRAY))
        );

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
