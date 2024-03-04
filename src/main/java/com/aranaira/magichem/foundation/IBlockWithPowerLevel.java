package com.aranaira.magichem.foundation;

public interface IBlockWithPowerLevel {
    void increasePowerLevel();

    void decreasePowerLevel();

    int getPowerLevel();

    void setPowerLevel(int pPowerLevel);
}
