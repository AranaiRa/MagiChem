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
                    Component.translatable("tooltip.magichem.silver_grains")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.power_reagent")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.TARNISHED_SILVER_LUMP.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product.reprocess")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.FOCUSING_CATALYST.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.focusing_catalyst")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.power_reagent")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.CATALYST_CORE.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.catalyst_core")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.DEPLETED_CATALYST_CORE.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.catalyst_core")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.catalyst_core.damaged")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.CATALYST_CASING.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.catalyst_casing")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.catalyst_casing.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.WARPED_FOCUSING_CATALYST.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.IRIS_ARGENTI.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.iris_argenti")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.iris_argenti.ext")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.ALCHEMICAL_WASTE.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.alchemical_waste")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.alchemical_waste.ext")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.RAREFIED_WASTE.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.rarefied_waste")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.rarefied_waste.ext")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.COG_COMPONENTS.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.cog_components")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.SUBLIME_COG_COMPONENTS.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.cog_components")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(Component.empty());
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.sublime_components.ext")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.TUBE_COMPONENTS.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.tube_components")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.SUBLIME_TUBE_COMPONENTS.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.tube_components")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(Component.empty());
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.sublime_components.ext")
                    .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.BRINDLE_GRIT_RED.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.brindle_grit_red")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.BRINDLE_GRIT_YELLOW.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.brindle_grit_yellow")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.BRINDLE_GRIT_GREEN.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.brindle_grit_green")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.BRINDLE_GRIT_CYAN.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.brindle_grit_cyan")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.BRINDLE_GRIT_BLUE.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.brindle_grit_blue")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.BRINDLE_GRIT_MAGENTA.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.brindle_grit_magenta")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.REFRACTIVE_CRYSTAL_GRIT.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.refractive_crystal_grit")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.REFRACTIVE_CRYSTAL_SAND.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.refractive_crystal_sand")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.REFRACTIVE_CRYSTAL_GLASS.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.refractive_crystal_glass")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.AMPLIFYING_PRISM.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.amplifying_prism")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.power_reagent")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.MALFORMED_BRINDLE_GLASS.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.AUXILIARY_CIRCLE_ARRAY.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.aca")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.power_reagent")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.RUINED_PROJECTION_APPARATUS.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.aca.single.damaged")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.CIRCLE_PROJECTION_APPARATUS.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.aca.single")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.LIGHTWRACKED_PROJECTION_GEM.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.aca.gem.damaged")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.DISSONANT_CRYSTAL_CORE.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.aca.core.damaged")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.BEFOULED_PROJECTION_CASING.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.aca.casing.damaged")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.CONTORTED_PENNON.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.aca.pennon.damaged")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.waste_product.reprocess")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.CORONAL_PROJECTION_GEM.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.aca.gem")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.aca.part")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.CONCORDANT_CRYSTAL_CORE.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.aca.core")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.aca.part")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.PRISTINE_PROJECTION_CASING.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.aca.casing")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.aca.part")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        else if(pStack.getItem() == ItemRegistry.GUIDANCE_PENNON.get()) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.aca.pennon")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
            pTooltipComponents.add(
                    Component.translatable("tooltip.magichem.aca.part")
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
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
        if(itemStack.getItem() == ItemRegistry.CATALYTIC_CARBON.get()) {
            return 38400;
        }

        return super.getBurnTime(itemStack, recipeType);
    }
}
