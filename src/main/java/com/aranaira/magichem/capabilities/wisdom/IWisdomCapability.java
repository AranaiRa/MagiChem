package com.aranaira.magichem.capabilities.wisdom;

import com.aranaira.magichem.MagiChemMod;
import com.mna.api.spells.attributes.Attribute;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

public interface IWisdomCapability {
    ResourceLocation WISDOM = new ResourceLocation(MagiChemMod.MODID, "wisdom");

    int getValue(Attribute pSpellAttribute);

    HashMap<Attribute, Integer> getValues();

    void setValue(Attribute pSpellAttribute, int pLevel);

    boolean incrementValue(Attribute pSpellAttribute, int pWisdom);

    boolean decrementValue(Attribute pSpellAttribute, int pWisdom);

    int getLimit(Attribute pSpellAttribute, int pWisdom);

    boolean getIsDisabled();

    void setIsDisabled(boolean pDisabled);

    void copyFrom(IWisdomCapability pCapability);
}
