package com.aranaira.magichem.gui.element;

import com.aranaira.magichem.gui.AdmixerMenu;
import com.aranaira.magichem.gui.CircleFabricationMenu;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;

public class AdmixerButtonRecipeSelector extends ImageButton {
    private final AbstractContainerScreen<AdmixerMenu> screen;
    private final int arrayIndex;

    public AdmixerButtonRecipeSelector(AbstractContainerScreen<AdmixerMenu> pScreen, int pIndex, int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation pResourceLocation, OnPress pOnPress) {
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
