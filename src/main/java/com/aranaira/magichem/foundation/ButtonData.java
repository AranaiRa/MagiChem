package com.aranaira.magichem.foundation;

import net.minecraft.client.gui.components.ImageButton;

public class ButtonData {
    ImageButton button;
    int x, y;

    public ButtonData(ImageButton pButton, int pXOffset, int pYOffset) {
        this.button = pButton;
        this.x = pXOffset;
        this.y = pYOffset;
    }

    public ImageButton getButton() {
        return button;
    }

    public int getXOffset() {
        return x;
    }

    public int getYOffset() {
        return y;
    }
}
