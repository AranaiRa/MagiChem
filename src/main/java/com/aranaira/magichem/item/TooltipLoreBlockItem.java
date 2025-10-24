package com.aranaira.magichem.item;

import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.capabilities.grime.GrimeProvider;
import com.aranaira.magichem.foundation.IHasNonStandardTooltipLore;
import com.aranaira.magichem.foundation.IKeepsInventoryOnBreak;
import com.aranaira.magichem.registry.BlockRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TooltipLoreBlockItem extends BlockItem {

    public TooltipLoreBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if(pStack.getItem() instanceof IHasNonStandardTooltipLore ihtl) {
            ihtl.addTooltipComponents(pTooltipComponents);
        }
        else if(pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_TRIM_WOOD.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE_TRIM_WOOD.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_TRIM_SILVER.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE_TRIM_SILVER.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_TRIM_ELECTRUM.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE_TRIM_ELECTRUM.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_TRIM_GOLD.get().asItem() ||
                pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE_TRIM_GOLD.get().asItem()
        ) {
            if(pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_TRIM_WOOD.get().asItem() ||
               pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE_TRIM_WOOD.get().asItem()) {
                pTooltipComponents.add(
                        Component.translatable("tooltip.magichem.alchemically_treated_glass.wood")
                                .withStyle(ChatFormatting.DARK_AQUA)
                );
            }
            else if(pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_TRIM_SILVER.get().asItem() ||
               pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE_TRIM_SILVER.get().asItem()) {
                pTooltipComponents.add(
                        Component.translatable("tooltip.magichem.alchemically_treated_glass.silver")
                                .withStyle(ChatFormatting.DARK_AQUA)
                );
            }
            else if(pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_TRIM_ELECTRUM.get().asItem() ||
               pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE_TRIM_ELECTRUM.get().asItem()) {
                pTooltipComponents.add(
                        Component.translatable("tooltip.magichem.alchemically_treated_glass.electrum")
                                .withStyle(ChatFormatting.DARK_AQUA)
                );
            }
            else if(pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_TRIM_GOLD.get().asItem() ||
               pStack.getItem() == BlockRegistry.ALCHEMICALLY_TREATED_GLASS_PANE_TRIM_GOLD.get().asItem()) {
                pTooltipComponents.add(
                        Component.translatable("tooltip.magichem.alchemically_treated_glass.gold")
                                .withStyle(ChatFormatting.DARK_AQUA)
                );
            }
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.alchemically_treated_glass")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == BlockRegistry.MAGICHEMICAL_MECHANISM.get().asItem()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.magichemical_mechanism")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(Component.empty());
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.magichemical_mechanism.ext")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem."+this.toString())
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public InteractionResult place(BlockPlaceContext pContext) {
        ItemStack stack = pContext.getItemInHand();

        if(stack.hasTag()) {
            CompoundTag nbt = stack.getOrCreateTag();
            InteractionResult result = super.place(pContext);
            BlockEntity be = pContext.getLevel().getBlockEntity(pContext.getClickedPos());

            if (result != InteractionResult.FAIL) {
                if(be instanceof IKeepsInventoryOnBreak keep) {
                    keep.unpackDataFromNBT(nbt);
                }
            }
            return result;
        }
        return super.place(pContext);
    }
}
