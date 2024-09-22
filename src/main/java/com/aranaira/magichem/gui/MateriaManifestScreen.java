package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageBlockEntity;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
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
        menu.blockEntity.tetherTarget = null;
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
        initializeButtons();
    }

    private void initializeButtons() {
        for(int i=0; i<32; i++) {
            int buttonX = this.leftPos - 16 + ((i / 8) * 54);
            int buttonY = this.topPos - 6 + ((i % 8) * 23);

            int finalI = i;
            this.addRenderableWidget(new ImageButton(buttonX, buttonY, 18, 18, 24, 218, TEXTURE, button -> {
                setTetherTarget(finalI);
            }));
        }

        //next page button
        this.addRenderableWidget(new ImageButton(this.leftPos + 82, this.topPos + 177, 12, 7, 12, 242, TEXTURE, button -> {
            pageIndex = Math.min(pageIndex + 1, pageCount - 1);
        }));

        //previous page button
        this.addRenderableWidget(new ImageButton(this.leftPos + 82, this.topPos - 17, 12, 7, 0, 242, TEXTURE, button -> {
            pageIndex = Math.max(pageIndex - 1, 0);
        }));
    }

    private void setTetherTarget(int pButtonID) {
        menu.blockEntity.tetherTarget = materiaStorageInZone.get(pButtonID + (32 * pageIndex)).getThird();
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

        int startIndex = pageIndex * 32;
        int endIndex = (materiaStorageInZone.size() - startIndex) > 32 ? startIndex + 32 : materiaStorageInZone.size();

        for(int i=startIndex; i<endIndex; i++) {
            int itemX = x +  8 + (((i - startIndex) / 8) * 54);
            int itemY = y + 18 + (((i - startIndex) % 8) * 23);
            int barX = x + 29 + (((i - startIndex) / 8) * 54);
            int barY = y + 31 + (((i - startIndex) % 8) * 23);

            final Triplet<MateriaItem, BlockPos, AbstractMateriaStorageBlockEntity> entry = materiaStorageInZone.get(i);
            MateriaItem mi = entry.getFirst();
            String id = (mi instanceof EssentiaItem ? "essentia_" : "admixture_") + mi.getMateriaName();
            ItemStack is = materiaMap.get(id);

            if(is != null) {
                pGuiGraphics.renderItem(is, itemX, itemY);

                int colorInt = mi.getMateriaColor();
                int intR = (colorInt & 0x00ff0000) >> 16;
                int intG = (colorInt & 0x0000ff00) >> 8;
                int intB = (colorInt & 0x000000ff);

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
        int startIndex = pageIndex * 32;
        int endIndex = (materiaStorageInZone.size() - startIndex) > 32 ? startIndex + 32 : materiaStorageInZone.size();

        for(int i=startIndex; i<endIndex; i++) {
            int counterX =  6 + (((i - startIndex) / 8) * 54);
            int counterY = -4 + (((i - startIndex) % 8) * 23);

            int mLimit = materiaStorageInZone.get(i).getThird().getCurrentStock();

            pGuiGraphics.drawString(font, ""+mLimit, counterX, counterY, 0x00000000, false);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        super.renderTooltip(pGuiGraphics, pX, pY);
    }
}
