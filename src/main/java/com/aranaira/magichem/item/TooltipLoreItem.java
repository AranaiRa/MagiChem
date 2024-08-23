package com.aranaira.magichem.item;

import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TooltipLoreItem extends Item {
    public TooltipLoreItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if(pStack.getItem() == ItemRegistry.SILVER_DUST.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.silvergrains")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.powerreagent")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.TARNISHED_SILVER_LUMP.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.wasteproduct")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.wasteproduct.reprocess")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.FOCUSING_CATALYST.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.focusingcatalyst")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.powerreagent")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.CATALYST_CORE.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.catalystcore")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.DEPLETED_CATALYST_CORE.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.catalystcore")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.catalystcore.damaged")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.CATALYST_CASING.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.catalystcasing")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.catalystcasing.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.WARPED_FOCUSING_CATALYST.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.wasteproduct")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.wasteproduct.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.IRIS_ARGENTI.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.irisargenti")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.irisargenti.ext")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.MAGIC_CIRCLE.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.magiccircle")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.ALCHEMICAL_WASTE.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.alchemicalwaste")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.alchemicalwaste.ext")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.RAREFIED_WASTE.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.rarefiedwaste")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.rarefiedwaste.ext")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.CLEANING_BRUSH.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.cleaningbrush")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.COG_COMPONENTS.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.cogcomponents")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.SUBLIME_COG_COMPONENTS.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.cogcomponents")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(Component.empty());
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.sublimecomponents.ext")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.TUBE_COMPONENTS.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.tubecomponents")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.SUBLIME_TUBE_COMPONENTS.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.tubecomponents")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(Component.empty());
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.sublimecomponents.ext")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.CATALYTIC_CARBON.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.catalyticcarbon")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.LABORATORY_CHARM.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.laboratorycharm")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.BRINDLE_GRIT_RED.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.brindlegrit_red")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.wasteproduct.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.BRINDLE_GRIT_YELLOW.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.brindlegrit_yellow")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.wasteproduct.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.BRINDLE_GRIT_GREEN.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.brindlegrit_green")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.wasteproduct.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.BRINDLE_GRIT_CYAN.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.brindlegrit_cyan")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.wasteproduct.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.BRINDLE_GRIT_BLUE.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.brindlegrit_blue")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.wasteproduct.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.BRINDLE_GRIT_MAGENTA.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.brindlegrit_magenta")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.wasteproduct.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.REFRACTIVE_CRYSTAL_GRIT.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.refractivecrystalgrit")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.wasteproduct.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.REFRACTIVE_CRYSTAL_SAND.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.refractivecrystalsand")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.wasteproduct.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.REFRACTIVE_CRYSTAL_GLASS.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.refractivecrystalglass")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.wasteproduct.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.AMPLIFYING_PRISM.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.amplifyingprism")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.powerreagent")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.MALFORMED_BRINDLE_GLASS.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.amplifyingprism")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.wasteproduct.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        if(itemStack.getItem() == ItemRegistry.CATALYTIC_CARBON.get()) {
            return 38400;
        }

        return super.getBurnTime(itemStack, recipeType);
    }
}
