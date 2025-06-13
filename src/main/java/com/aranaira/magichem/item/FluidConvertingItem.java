package com.aranaira.magichem.item;

import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FluidConvertingItem extends Item {
    public FluidConvertingItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {

        if(pStack.getItem() == ItemRegistry.ARISTOSE.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.aristose")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
