package com.aranaira.magichem.foundation;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface IHasNonStandardTooltipLore {
    void addTooltipComponents(List<Component> pTooltipComponents);
}
