package com.aranaira.magichem.item;

import com.aranaira.magichem.foundation.enums.EEssentiaHouse;
import com.aranaira.magichem.registry.CreativeModeTabs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class EssentiaItem extends MateriaItem {
    private static final String[] essentiaTypes = {
            "ender", "earth", "water", "air", "fire", "arcane",
            "conceptual", "verdant", "fleshy",  "nourishing", "rotten", "mineral", "wrought", "precious",
            "nigredo", "albedo", "citrinitas", "rubedo"
    };
    private static final String[] houseOfElements = {
            "ender", "earth", "water", "air", "fire", "arcane"
    };
    private static final String[] houseOfQualities = {
            "conceptual", "verdant", "fleshy",  "nourishing", "rotten", "mineral", "wrought", "precious"
    };
    private static final String[] houseOfAlchemy = {
            "nigredo", "albedo", "citrinitas", "rubedo"
    };

    private final String name;
    private final String abbreviation;
    private final EEssentiaHouse house;
    private final int wheel;

    public EssentiaItem(String essentiaName, String essentiaAbbreviation, String essentiaHouse, int essentiaWheel, String essentiaColor) {
        super(essentiaName, essentiaColor, new Item.Properties());
        this.name = essentiaName;
        this.abbreviation = essentiaAbbreviation;
        this.house = parseStringToHouse(essentiaHouse, essentiaName);
        this.wheel = essentiaWheel;
    }

    public static boolean isEssentia(String query) {
        boolean result = false;
        for(String test : essentiaTypes) {
            if(test == query) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static boolean isInHouse(String query, EEssentiaHouse houseQuery) {
        boolean result = false;

        switch(houseQuery) {
            case ELEMENTS: {
                for (String test : houseOfElements) {
                    if (test == query) {
                        result = true;
                        break;
                    }
                }
            }
            case QUALITIES: {
                for (String test : houseOfQualities) {
                    if (test == query) {
                        result = true;
                        break;
                    }
                }
            }
            case ALCHEMY: {
                for (String test : houseOfAlchemy) {
                    if (test == query) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private EEssentiaHouse parseStringToHouse(String input, String nameForErrorHandling) {
        switch(input) {
            case "elements": return EEssentiaHouse.ELEMENTS;
            case "qualities": return EEssentiaHouse.QUALITIES;
            case "alchemy": return EEssentiaHouse.ALCHEMY;
            case "none": return EEssentiaHouse.NONE;
            default: {
                System.out.println("Essentia entry \""+nameForErrorHandling+"\" has an invalid House, defaulting to NONE. Was something misspelled?");
                return EEssentiaHouse.NONE;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(MutableComponent.create(
                new TranslatableContents("tooltip.magichem."+getMateriaName(), "?", new Object[]{})).withStyle(ChatFormatting.DARK_GRAY)
        );
        tooltipComponents.add(MutableComponent.create(
                new TranslatableContents("tooltip.magichem.sign", "?", new Object[]{})).withStyle(ChatFormatting.DARK_GRAY)
                .append(" [ ")
                .append(Component.literal(getAbbreviation()).withStyle(ChatFormatting.DARK_AQUA))
                .append(" ]")
        );
    }

    public int getEssentiaWheel() {
        return this.wheel;
    }

    public EEssentiaHouse getEssentiaHouse() {
        return this.house;
    }

    public String getAbbreviation() {
        return this.abbreviation;
    }

    public String getDisplayFormula() { return getAbbreviation(); }
}
