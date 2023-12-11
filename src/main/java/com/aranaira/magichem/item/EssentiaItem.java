package com.aranaira.magichem.item;

import com.aranaira.magichem.foundation.Essentia;
import com.aranaira.magichem.foundation.enums.EMateriaHouse;
import com.aranaira.magichem.registry.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class EssentiaItem extends Item implements Essentia {

    private final String name;
    private final String abbreviation;
    private final int color;
    private final EMateriaHouse house;
    private final int wheel;

    public EssentiaItem(String essentiaName, String essentiaAbbreviation, String essentiaHouse, int essentiaWheel, String essentiaColor) {
        super(new Item.Properties().tab(CreativeModeTabs.MAGICHEM_TAB));
        this.name = essentiaName;
        this.abbreviation = essentiaAbbreviation;
        this.house = parseStringToHouse(essentiaHouse, essentiaName);
        this.wheel = essentiaWheel;
        this.color = Integer.parseInt(essentiaColor, 16) | 0xFF00000;
    }

    private EMateriaHouse parseStringToHouse(String input, String nameForErrorHandling) {
        switch(input) {
            case "elements": return EMateriaHouse.ELEMENTS;
            case "qualities": return EMateriaHouse.QUALITIES;
            case "alchemy": return EMateriaHouse.ALCHEMY;
            case "none": return EMateriaHouse.NONE;
            default: {
                System.out.println("Essentia entry \""+nameForErrorHandling+"\" has an invalid House, defaulting to NONE. Was something misspelled?");
                return EMateriaHouse.NONE;
            }
        }
    }

    @Override
    public int getWheel() {
        return this.wheel;
    }

    @Override
    public EMateriaHouse getHouse() {
        return this.house;
    }

    @Override
    public String getMateriaName() {
        return this.name;
    }

    @Override
    public String getAbbreviation() {
        return this.abbreviation;
    }

    @Override
    public int getColor() {
        return this.color;
    }
}
