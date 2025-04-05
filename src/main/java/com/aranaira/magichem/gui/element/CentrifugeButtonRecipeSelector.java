package com.aranaira.magichem.gui.element;

import com.aranaira.magichem.gui.CentrifugeMenu;
import com.aranaira.magichem.gui.GrandCentrifugeMenu;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;

public class CentrifugeButtonRecipeSelector extends ImageButton {
    private final AbstractContainerScreen<CentrifugeMenu> screen;
    private final int arrayIndex;

    public CentrifugeButtonRecipeSelector(AbstractContainerScreen<CentrifugeMenu> pScreen, int pIndex, int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation pResourceLocation, OnPress pOnPress) {
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
