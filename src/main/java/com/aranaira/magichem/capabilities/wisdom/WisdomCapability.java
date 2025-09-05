package com.aranaira.magichem.capabilities.wisdom;

import com.aranaira.magichem.foundation.Triplet;
import com.mna.api.spells.attributes.Attribute;

import java.util.HashMap;

public class WisdomCapability implements IWisdomCapability {
    private final HashMap<Attribute, Integer> values = new HashMap<>();
    private final HashMap<Attribute, int[]> limits = new HashMap<>();

    @Override
    public int getValue(Attribute pSpellAttribute) {
        if(values.containsKey(pSpellAttribute))
            return values.get(pSpellAttribute);
        return 0;
    }

    @Override
    public HashMap<Attribute, Integer> getValues() {
        return values;
    }

    @Override
    public void setValue(Attribute pSpellAttribute, int pLevel) {
        values.put(pSpellAttribute, pLevel);
    }

    @Override
    public boolean incrementValue(Attribute pSpellAttribute, int pWisdom) {
        int pre = getValue(pSpellAttribute);
        int post = Math.min(getLimit(pSpellAttribute, pWisdom), getValue(pSpellAttribute) + 1);

        if(pre != post) {
            setValue(pSpellAttribute, post);
            return true;
        }
        return false;
    }

    @Override
    public boolean decrementValue(Attribute pSpellAttribute, int pWisdom) {
        int pre = getValue(pSpellAttribute);
        int post = Math.max(0, getValue(pSpellAttribute) - 1);

        if(pre != post) {
            setValue(pSpellAttribute, post);
            return true;
        }
        return false;
    }

    @Override
    public int getLimit(Attribute pSpellAttribute, int pWisdom) {
        return getOrDefineLimits().get(pSpellAttribute)[Math.max(0,Math.min(5,pWisdom))];
    }

    private HashMap<Attribute, int[]> getOrDefineLimits() {
        if(limits.size() == 0) {
            limits.put(Attribute.DAMAGE,           new int[]{0, 1, 2, 3, 5, 8});
            limits.put(Attribute.DELAY,            new int[]{0, 1, 2, 3, 4, 5});
            limits.put(Attribute.DURATION,         new int[]{0, 0, 1, 2, 3, 5});
            limits.put(Attribute.LESSER_MAGNITUDE, new int[]{0, 0, 1, 2, 3, 5});
            limits.put(Attribute.MAGNITUDE,        new int[]{0, 0, 0, 1, 3, 5});
            limits.put(Attribute.SPEED,            new int[]{0, 1, 2, 3, 4, 5});
            limits.put(Attribute.RADIUS,           new int[]{0, 0, 1, 1, 2, 3});
            limits.put(Attribute.RANGE,            new int[]{0, 1, 2, 3, 4, 5});
        }
        return limits;
    }
}
