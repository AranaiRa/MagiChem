package com.aranaira.magichem.item;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.NameCountPair;
import com.aranaira.magichem.foundation.enums.EEssentiaHouse;
import com.aranaira.magichem.registry.CreativeModeTabs;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public class AdmixtureItem extends MateriaItem {
    private static final String[] subscriptNumbers = {"\u2080","\u2081","\u2082","\u2083","\u2084","\u2085","\u2086","\u2087","\u2088","\u2089"};

    private final String name;
    private String displayFormula = "";
    private final List<NameCountPair> formulaEssentiaPortions;
    private final List<NameCountPair> formulaAdmixturePortions;

    public AdmixtureItem(String pName, String pColor, List<NameCountPair> pFormulaEssentia, List<NameCountPair> pFormulaAdmixtures) {
        super(pName, pColor, new Properties().tab(CreativeModeTabs.MAGICHEM_TAB));
        this.name = pName;
        this.formulaEssentiaPortions = pFormulaEssentia;
        this.formulaAdmixturePortions = pFormulaAdmixtures;
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
            return constructDisplayFormula();
        }
    }

    @NotNull
    private String constructDisplayFormula() {
        String output = "";

        HashMap<String, EssentiaItem> essentiaMap = ItemRegistry.getEssentiaMap();
        HashMap<String, AdmixtureItem> admixturesMap = ItemRegistry.getAdmixturesMap();

        //add elements
        for (NameCountPair formulaEssentiaPortion : formulaEssentiaPortions) {
            EssentiaItem e = essentiaMap.get(formulaEssentiaPortion.getName());
            if(e == null) {
                MagiChemMod.LOGGER.warn("WARNING: Essentia component \""+formulaEssentiaPortion.getName()+"\" does not exist. Was there a typo?");
                output += "?";
                continue;
            }
            if(e.getEssentiaHouse() == EEssentiaHouse.ELEMENTS) {
                output += e.getAbbreviation();
                if(formulaEssentiaPortion.getCount() > 1)
                    output += sub(formulaEssentiaPortion.getCount());
            }
        }

        //add admixtures
        for (NameCountPair formulaAdmixturePortion : formulaAdmixturePortions) {
            AdmixtureItem a = admixturesMap.get(formulaAdmixturePortion.getName());
            if(a == null) {
                MagiChemMod.LOGGER.warn("WARNING: Admixture component \""+formulaAdmixturePortion.getName()+"\" does not exist. Was there a typo?");
                output += "?";
                continue;
            }
            output += "(" + a.getDisplayFormula() + ")";
            if(formulaAdmixturePortion.getCount() > 1)
                output += sub(formulaAdmixturePortion.getCount());
        }

        //add qualities and alchemy
        for (NameCountPair formulaEssentiaPortion : formulaEssentiaPortions) {
            EssentiaItem e = essentiaMap.get(formulaEssentiaPortion.getName());
            if(e == null) {
                MagiChemMod.LOGGER.warn("WARNING: Essentia component \""+formulaEssentiaPortion.getName()+"\" does not exist. Was there a typo?");
                output += "?";
                continue;
            }
            if(e.getEssentiaHouse() != EEssentiaHouse.ELEMENTS) {
                output += e.getAbbreviation();
                if(formulaEssentiaPortion.getCount() > 1)
                    output += sub(formulaEssentiaPortion.getCount());
            }
        }
/*
        //add elements
        for (NameCountPair formulaEssentiaPortion : formulaEssentiaPortions) {
            String element = formulaEssentiaPortion.getName();
            if (EssentiaItem.isInHouse(element, EEssentiaHouse.ELEMENTS)) {
                EssentiaItem ref = null;
                for(int j=0; j<ItemRegistry.ESSENTIA.getEntries().size(); j++){
                    if(ItemRegistry.getEssentia().get(j).getMateriaName() == element) {
                        ref = ItemRegistry.getEssentia().get(j);
                        break;
                    }
                }
                if(ref == null) {
                    output += "UNKNOWN_ESSENTIA_\""+element+"\")";
                    continue;
                }

                byte count = formulaEssentiaPortion.getCount();
                if (count > 1)
                    output += ref.getAbbreviation() + sub(count);
                else
                    output += ref.getAbbreviation();
            }
        }

        //add admixtures
        for(int i=0; i<formulaEssentiaPortions.size(); i++) {
            String element = formulaAdmixturePortions.get(i).getName();
            byte count = formulaAdmixturePortions.get(i).getCount();

            AdmixtureItem ref = null;
            for(int j=0; j<ItemRegistry.ADMIXTURES.getEntries().size(); j++){
                if(ItemRegistry.getAdmixtures().get(j).getMateriaName() == element) {
                    ref = ItemRegistry.getAdmixtures().get(j);
                    break;
                }
            }
            if(ref == null) {
                output += "(UNKNOWN ADMIXTURE \""+element+"\")";
                continue;
            }

            output += "(" + ref.getDisplayFormula() + ")";
            if(count > 1)
                output += sub(count);


        }
        //add qualities and alchemy
        for (NameCountPair formulaEssentiaPortion : formulaEssentiaPortions) {
            String element = formulaEssentiaPortion.getName();
            if (!EssentiaItem.isInHouse(element, EEssentiaHouse.ELEMENTS)) {
                byte count = formulaEssentiaPortion.getCount();
                if (count > 1)
                    output += element + sub(count);
                else
                    output += element;
            }
        }
*/
        displayFormula = output;
        return output;
    }
}
