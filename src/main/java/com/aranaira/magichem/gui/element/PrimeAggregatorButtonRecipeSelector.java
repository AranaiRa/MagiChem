package com.aranaira.magichem.gui.element;

import com.aranaira.magichem.gui.FuseryMenu;
import com.aranaira.magichem.gui.PrimeAggregatorMenu;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;

public class PrimeAggregatorButtonRecipeSelector extends ImageButton {
    private final AbstractContainerScreen<PrimeAggregatorMenu> screen;
    private final int arrayIndex;

    public PrimeAggregatorButtonRecipeSelector(AbstractContainerScreen<PrimeAggregatorMenu> pScreen, int pIndex, int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation pResourceLocation, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pResourceLocation, pOnPress);
        this.screen = pScreen;
        arrayIndex = pIndex;
    }

    public AbstractContainerScreen<?> getScreen() {
        return screen;
    }

    public int getArrayIndex() {
        return arrayIndex;
    }
}
