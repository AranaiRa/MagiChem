package com.aranaira.magichem.gui.element;

import com.aranaira.magichem.gui.AlchemicalNexusMenu;
import com.aranaira.magichem.gui.FuseryMenu;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;

public class AlchemicalNexusButtonRecipeSelector extends ImageButton {
    private final AbstractContainerScreen<AlchemicalNexusMenu> screen;
    private final int arrayIndex;

    public AlchemicalNexusButtonRecipeSelector(AbstractContainerScreen<AlchemicalNexusMenu> pScreen, int pIndex, int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation pResourceLocation, OnPress pOnPress) {
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
