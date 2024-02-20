package com.aranaira.magichem.capabilities.grime;

public class GrimeCapability implements IGrimeCapability {
    private int grime = 0;

    @Override
    public int getGrime() {
        return grime;
    }

    @Override
    public void setGrime(int value) {
        grime = value;
    }
}
