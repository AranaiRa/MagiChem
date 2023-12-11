package com.aranaira.magichem.foundation;

import net.minecraft.world.level.ItemLike;

public interface Materia extends ItemLike {
    String getMateriaName();

    String getAbbreviation();

    int getColor();
}
