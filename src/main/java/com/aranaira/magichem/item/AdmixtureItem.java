package com.aranaira.magichem.item;

import com.aranaira.magichem.foundation.NameCountPair;
import com.aranaira.magichem.registry.CreativeModeTabs;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.MateriaRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class AdmixtureItem extends MateriaItem {
    private static final String[] subscriptNumbers = {"\u2080","\u2081","\u2082","\u2083","\u2084","\u2085","\u2086","\u2087","\u2088","\u2089"};

    private final String name;
    private String displayFormula = "";
    private final NonNullList<NameCountPair> formula;

    public AdmixtureItem(String pName, String pColor, NonNullList<NameCountPair> pFormula) {
        super(pName, pColor, new Properties().tab(CreativeModeTabs.MAGICHEM_TAB));
        this.name = pName;
        this.formula = pFormula;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(MutableComponent.create(
                new TranslatableContents("tooltip.magichem.admixturegeneral")).withStyle(ChatFormatting.DARK_GRAY)
        );
        tooltipComponents.add(MutableComponent.create(
                new TranslatableContents("tooltip.magichem.admixtureformula")).withStyle(ChatFormatting.DARK_GRAY)
                .append(" [ ")
                .append(Component.literal(getDisplayFormula()).withStyle(ChatFormatting.DARK_AQUA))
                .append(" ]")
        );
    }

    private String sub(int num) {
        return subscriptNumbers[num];
    }

    private String getDisplayFormula() {
        if(displayFormula != "") {
            return displayFormula;
        } else {
            String output = "T"+sub(1)+"B"+sub(2)+"D"+sub(3);

            formula.forEach(ncp -> {
            });

            displayFormula = output;
            return output;
        }
    }
}
