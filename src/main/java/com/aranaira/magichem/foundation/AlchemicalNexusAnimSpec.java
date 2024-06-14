package com.aranaira.magichem.foundation;

public class AlchemicalNexusAnimSpec {
    public final int
            ticksInRampSpeedup,
            ticksInRampCancel,
            ticksInRampBeam,
            ticksInRampCircle,
            ticksBetweenShlorpPulls,
            ticksToCraft;
    public final float
            shlorpSpeed;

    public AlchemicalNexusAnimSpec(int pRampSpeedup, int pRampCancel, int pRampBeam, int pRampCircle, int pShlorpPull, float pShlorpSpeed, int pCraft) {
        this.ticksInRampSpeedup = pRampSpeedup;
        this.ticksInRampCancel = pRampCancel;
        this.ticksInRampBeam = pRampBeam;
        this.ticksInRampCircle = pRampCircle;
        this.ticksBetweenShlorpPulls = pShlorpPull;
        this.shlorpSpeed = pShlorpSpeed;
        this.ticksToCraft = pCraft;
    }
}
