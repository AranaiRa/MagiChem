package com.aranaira.magichem.foundation;

public class BlockRendererCoords {
    public float x, y, w, h, u, uw, v, vh;

    public BlockRendererCoords(float pX, float pY, float pW, float pH) {
        this.x = pX;
        this.y = pY;
        this.w = pW;
        this.h = pH;
    }

    public BlockRendererCoords(float pX, float pY, float pW, float pH, float pU, float pUW, float pV, float pVH) {
        this.x = pX;
        this.y = pY;
        this.w = pW;
        this.h = pH;
        this.u = pU;
        this.uw = pUW;
        this.v = pV;
        this.vh = pVH;
    }

    public void setUV(float pU, float pUW, float pV, float pVH) {
        this.u = pU;
        this.uw = pUW;
        this.v = pV;
        this.vh = pVH;
    }
}
