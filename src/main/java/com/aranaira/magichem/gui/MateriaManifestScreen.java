package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;

public class MateriaManifestScreen extends AbstractContainerScreen<MateriaManifestMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_materia_manifest.png");
    private List<Triplet<MateriaItem, BlockPos, AbstractMateriaStorageBlockEntity>> materiaStorageInZone;
    private HashMap<String, ItemStack> materiaMap = new HashMap<>();
    int pageIndex = 0;
    int pageCount = 1;

    public MateriaManifestScreen(MateriaManifestMenu menu, Inventory inv, Component component) {
        super(menu, inv, component);
        updateStorageScan();
        if(materiaMap.size() == 0) {
            final HashMap<String, MateriaItem> baseMateriaMap = ItemRegistry.getMateriaMap(false, true);
            for(String key : baseMateriaMap.keySet()) {
                materiaMap.put(key, new ItemStack(baseMateriaMap.get(key)));
            }
        }
    }

    private void updateStorageScan() {
        menu.blockEntity.scanMateriaInZone();
        materiaStorageInZone = menu.blockEntity.getMateriaStorageInZone();
        pageCount = (int)Math.ceil((float)materiaStorageInZone.size() / 32f);
        if(pageCount <= 0)
            pageCount = 1;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int w = 222;
        int h = 213;

        int x = (width - w) / 2;
        int y = (height - h) / 2;

        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, w, h);

        for(int i=0; i<materiaStorageInZone.size(); i++) {
            int itemX = x +  8 + ((i / 8) * 54);
            int itemY = y + 18 + ((i % 8) * 23);
            int barX = x + 29 + ((i / 8) * 54);
            int barY = y + 31 + ((i % 8) * 23);

            final Triplet<MateriaItem, BlockPos, AbstractMateriaStorageBlockEntity> entry = materiaStorageInZone.get(i);
            MateriaItem mi = entry.getFirst();
            String id = (mi instanceof EssentiaItem ? "essentia_" : "admixture_") + mi.getMateriaName();
            ItemStack is = materiaMap.get(id);

            if(is != null) {
                pGuiGraphics.renderItem(is, itemX, itemY);

                int colorInt = mi.getMateriaColor();
                int intR = (colorInt & 0x00ff0000) >> 16;
                int intG = (colorInt & 0x0000ff00) >> 8;
                int intB = (colorInt & 0x000000ff) >> 0;

                float r = (float)intR / 255f;
                float g = (float)intG / 255f;
                float b = (float)intB / 255f;
                pGuiGraphics.setColor(r, g, b, 1);

                int barW = Math.round(23 * entry.getThird().getCurrentStockPercent());

                pGuiGraphics.blit(TEXTURE, barX, barY, 24, 254, barW, 2);
                pGuiGraphics.setColor(1,1,1,1);
            }
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        for(int i=0; i<materiaStorageInZone.size(); i++) {
            int counterX =  6 + ((i / 8) * 54);
            int counterY = -4 + ((i % 8) * 23);

            int mLimit = materiaStorageInZone.get(i).getThird().getCurrentStock();

            pGuiGraphics.drawString(font, ""+mLimit, counterX, counterY, 0x00000000, false);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        super.renderTooltip(pGuiGraphics, pX, pY);
    }
}
