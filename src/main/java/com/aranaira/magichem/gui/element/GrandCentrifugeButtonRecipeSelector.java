package com.aranaira.magichem.gui.element;

import com.aranaira.magichem.gui.GrandCentrifugeMenu;
import com.aranaira.magichem.gui.GrandFuseryMenu;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;

public class GrandCentrifugeButtonRecipeSelector extends ImageButton {
    private final AbstractContainerScreen<GrandCentrifugeMenu> screen;
    private final int arrayIndex;

    public GrandCentrifugeButtonRecipeSelector(AbstractContainerScreen<GrandCentrifugeMenu> pScreen, int pIndex, int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation pResourceLocation, OnPress pOnPress) {
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
