package com.aranaira.magichem.foundation;

public class NameCountPair {
    private final String name;
    private final byte count;

    public NameCountPair(String pName, byte pCount) {
        name = pName;
        count = pCount;
    }

    public String getName() {
        return name;
    }

    public byte getCount() {
        return count;
    }
}
