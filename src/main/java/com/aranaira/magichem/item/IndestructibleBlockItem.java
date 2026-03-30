package com.aranaira.magichem.item;

import com.aranaira.magichem.registry.BlockRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IndestructibleBlockItem extends BlockItem {
    public IndestructibleBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if(pStack.getItem() == BlockRegistry.BOSS_TROPHY_COUNCIL.get().asItem() || pStack.getItem() == BlockRegistry.BOSS_TROPHY_DEMONS.get().asItem() || pStack.getItem() == BlockRegistry.BOSS_TROPHY_FEY.get().asItem() || pStack.getItem() == BlockRegistry.BOSS_TROPHY_UNDEAD.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.boss_trophy")
                            .withStyle(ChatFormatting.GOLD)
            );
        }
        pTooltipComponents.add(
                Component.translatable("tooltip.magichem."+this.toString())
                        .withStyle(ChatFormatting.DARK_GRAY)
        );

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public boolean canBeHurtBy(DamageSource pDamageSource) {
        return false;
    }
}
