package com.aranaira.magichem.item;

import com.aranaira.magichem.registry.BlockRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TooltipLoreBlockItem extends BlockItem {

    public TooltipLoreBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if(pStack.getItem() == BlockRegistry.CIRCLE_POWER.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.circlepower")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.CIRCLE_FABRICATION.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.circlefabrication")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.ALEMBIC.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.alembic")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.DISTILLERY.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.distillery")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.CENTRIFUGE.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.centrifuge")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.FUSERY.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.admixer")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.ACTUATOR_WATER.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.actuator.water")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.ACTUATOR_FIRE.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.actuator.fire")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.ACTUATOR_EARTH.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.actuator.earth")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.EXPERIENCE_EXCHANGER.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.experienceexchanger")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
