package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageMultiTypeBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractMateriaStorageSingleTypeBlockEntity;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.util.render.ColorUtils;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MateriaManifestScreen extends AbstractContainerScreen<MateriaManifestMenu> {
    private static final ResourceLocation TEXTURE_COMPACT =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_materia_manifest_compact.png");
    private static final ResourceLocation TEXTURE_EXPANDED =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_materia_manifest_expanded.png");
    private final HashMap<String, ItemStack> materiaMap = new HashMap<>();
    private final List<Pair<MateriaItem, BlockEntity>> orderedMateriaStorage = new ArrayList<>();
    final List<Pair<MateriaItem, BlockEntity>> orderedMateriaStorageFiltered = new ArrayList<>();
    private ImageButton setCompactButton, setExpandedButton;
    private final ImageButton[]
            materiaSelectorButtonsCompact = new ImageButton[32],
            materiaSelectorButtonsExpanded = new ImageButton[16];
    private EditBox recipeFilterBox;
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
        menu.blockEntity.tetherType = null;
    }

    private void updateStorageScan() {
        menu.blockEntity.scanMateriaInZone();

        orderedMateriaStorage.clear();
        for(MateriaItem mi : menu.blockEntity.getMateriaTypesSorted()) {
            if(mi == null) continue;
            List<BlockEntity> listQuery = menu.blockEntity.getMateriaStorageInZone().get(mi);
            for(BlockEntity be : listQuery) {
                orderedMateriaStorage.add(new Pair<>(mi, be));
            }
        }

        updateMateriaOptionsByTextFilter();

        pageCount = (int)Math.ceil((float)orderedMateriaStorageFiltered.size() / 32f);
        if(pageCount <= 0)
            pageCount = 1;
    }

    @Override
    protected void init() {
        super.init();
        initializeButtons();
        initializeRecipeFilterBox();
    }

    private void initializeButtons() {
        for(int i=0; i<32; i++) {
            int buttonX = this.leftPos - 16 + ((i / 8) * 54);
            int buttonY = this.topPos - 6 + ((i % 8) * 23);

            int finalI = i;
            materiaSelectorButtonsCompact[i] = this.addRenderableWidget(new ImageButton(buttonX, buttonY, 18, 18, 24, 218, TEXTURE_COMPACT, button -> {
                setTetherTarget(finalI);
            }));
            materiaSelectorButtonsCompact[i].visible = menu.blockEntity.isCompactMode;
        }
        for(int i=0; i<16; i++) {
            int buttonX = this.leftPos - 16 + ((i / 8) * 108);
            int buttonY = this.topPos - 6 + ((i % 8) * 23);

            int finalI = i;
            materiaSelectorButtonsExpanded[i] = this.addRenderableWidget(new ImageButton(buttonX, buttonY, 18, 18, 24, 218, TEXTURE_COMPACT, button -> {
                setTetherTarget(finalI);
            }));
            materiaSelectorButtonsExpanded[i].visible = !menu.blockEntity.isCompactMode;
        }

        //next page button
        this.addRenderableWidget(new ImageButton(this.leftPos + 82, this.topPos + 177, 12, 7, 12, 242, TEXTURE_COMPACT, button -> {
            pageIndex = Math.min(pageIndex + 1, (menu.blockEntity.isCompactMode ? pageCount : pageCount * 2) - 1);
        }));

        //previous page button
        this.addRenderableWidget(new ImageButton(this.leftPos + 82, this.topPos - 17, 12, 7, 0, 242, TEXTURE_COMPACT, button -> {
            pageIndex = Math.max(pageIndex - 1, 0);
        }));

        //expanded / compacted button
        setExpandedButton = this.addRenderableWidget(new ImageButton(this.leftPos - 50, this.topPos + 22, 14, 14, 242, 228, TEXTURE_COMPACT, button -> {
            menu.blockEntity.isCompactMode = true;
            if(setCompactButton != null) setCompactButton.visible = true;
            if(setExpandedButton != null) setExpandedButton.visible = false;
            for(ImageButton ib : materiaSelectorButtonsCompact) {
                ib.visible = true;
            }
            for(ImageButton ib : materiaSelectorButtonsExpanded) {
                ib.visible = false;
            }
        }));

        //expanded / compacted button
        setCompactButton = this.addRenderableWidget(new ImageButton(this.leftPos - 50, this.topPos + 22, 14, 14, 228, 228, TEXTURE_COMPACT, button -> {
            menu.blockEntity.isCompactMode = false;
            if(setCompactButton != null) setCompactButton.visible = false;
            if(setExpandedButton != null) setExpandedButton.visible = true;
            for(ImageButton ib : materiaSelectorButtonsCompact) {
                ib.visible = false;
            }
            for(ImageButton ib : materiaSelectorButtonsExpanded) {
                ib.visible = true;
            }
        }));

        setCompactButton.visible = menu.blockEntity.isCompactMode;
        setExpandedButton.visible = !menu.blockEntity.isCompactMode;
    }

    private void initializeRecipeFilterBox() {
        int x = 0;//(width - 222) / 2;
        int y = 5;//(height - 213) / 2;

        this.recipeFilterBox = new EditBox(Minecraft.getInstance().font, x, y, 65, 16, Component.empty()) {
            @Override
            public boolean charTyped(char pCodePoint, int pModifiers) {
                final boolean b = super.charTyped(pCodePoint, pModifiers);
                updateMateriaOptionsByTextFilter();
                if(getValue().isEmpty())
                    setSuggestion("Filter...");
                else
                    setSuggestion("");
                return b;
            }

            @Override
            public void deleteChars(int pNum) {
                super.deleteChars(pNum);
                updateMateriaOptionsByTextFilter();
                if(getValue().isEmpty())
                    setSuggestion("Filter...");
                else
                    setSuggestion("");
            }

            @Override
            public void deleteWords(int pNum) {
                super.deleteWords(pNum);
                updateMateriaOptionsByTextFilter();
                if(getValue().isEmpty())
                    setSuggestion("Filter...");
                else
                    setSuggestion("");
            }
        };
        this.recipeFilterBox.setMaxLength(60);
        this.recipeFilterBox.setFocused(false);
        this.recipeFilterBox.setCanLoseFocus(false);
        this.setFocused(this.recipeFilterBox);

        renderFilterBox();
    }

    private void updateMateriaOptionsByTextFilter() {
        String filter = "";
        if(recipeFilterBox != null) filter = recipeFilterBox.getValue();
        orderedMateriaStorageFiltered.clear();
        for (Pair<MateriaItem, BlockEntity> pair : orderedMateriaStorage) {
            MateriaItem mi = pair.getFirst();
            String name = mi instanceof EssentiaItem ?
                    Component.translatable("item.magichem.essentia_" + mi.getMateriaName()).toString() :
                    Component.translatable("item.magichem.admixture_" + mi.getMateriaName()).toString();

            if(filter.equals("") || name.contains(filter)) {
                orderedMateriaStorageFiltered.add(pair);
            }
        }
    }

    private void renderFilterBox() {
        int xOrigin = (width - 222) / 2;
        int yOrigin = (height - 213) / 2;

        recipeFilterBox.setX(xOrigin - 76);
        recipeFilterBox.setY(yOrigin + 8);

        if(recipeFilterBox.getValue().isEmpty())
            recipeFilterBox.setSuggestion(Component.translatable("gui.magichem.typetofilter").getString());
        else
            recipeFilterBox.setSuggestion("");

        addRenderableWidget(recipeFilterBox);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == InputConstants.KEY_ESCAPE) {
            this.onClose();
            return true;
        } else if (this.recipeFilterBox.keyPressed(pKeyCode, pScanCode, pModifiers)) {
            return true;
        } else {
            return this.recipeFilterBox.isFocused() && this.recipeFilterBox.isVisible() || super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if(pDelta < 0)
            pageIndex = Math.min(pageIndex + 1, (menu.blockEntity.isCompactMode ? pageCount : pageCount * 2) - 1);
        else if(pDelta > 0)
            pageIndex = Math.max(pageIndex - 1, 0);

        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    private void setTetherTarget(int pButtonID) {
        int index = pButtonID + ((menu.blockEntity.isCompactMode ? 32 : 16) * pageIndex);
        if(index < orderedMateriaStorageFiltered.size()) {
            MateriaItem mi = orderedMateriaStorageFiltered.get(index).getFirst();
            menu.blockEntity.tetherTarget = orderedMateriaStorageFiltered.get(index).getSecond();
            menu.blockEntity.tetherType = orderedMateriaStorageFiltered.get(index).getFirst();

            Minecraft.getInstance().player.displayClientMessage(Component.empty()
                            .append(Component.translatable("feedback.block.materiamanifest.trackfrombottle").withStyle(ChatFormatting.DARK_GRAY))
                            .append(Component.translatable("item."+mi.getCreatorModId(new ItemStack(mi))+"."+mi.toString())),
                    true);
        }
    }

    private ResourceLocation getTexture() {
        return menu.blockEntity.isCompactMode ? TEXTURE_COMPACT : TEXTURE_EXPANDED;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, getTexture());

        int w = 222;
        int h = 213;

        int x = (width - w) / 2;
        int y = (height - h) / 2;

        //main panel
        pGuiGraphics.blit(getTexture(), x, y, 0, 0, w, h);

        //search bar
        pGuiGraphics.blit(getTexture(), x - 84, y, 116, 224, 80, 32);

        //button house
        pGuiGraphics.blit(getTexture(), x - 36, y + 36, 196, 224, 32, 32);

        int splitter = (menu.blockEntity.isCompactMode ? 32 : 16);

        int startIndex = pageIndex * splitter;
        int endIndex = (orderedMateriaStorageFiltered.size() - startIndex) > splitter ? startIndex + splitter : orderedMateriaStorageFiltered.size();

        for(int i=startIndex; i<endIndex; i++) {
            int itemX = x +  8 + (((i - startIndex) / 8) * (menu.blockEntity.isCompactMode ? 54 : 108));
            int itemY = y + 18 + (((i - startIndex) % 8) * 23);
            int barX = x + 29 + (((i - startIndex) / 8) * (menu.blockEntity.isCompactMode ? 54 : 108));
            int barY = y + 31 + (((i - startIndex) % 8) * 23);

            final Pair<MateriaItem, BlockEntity> entry = orderedMateriaStorageFiltered.get(i);
            MateriaItem mi = entry.getFirst();
            String id = (mi instanceof EssentiaItem ? "essentia_" : "admixture_") + mi.getMateriaName();
            ItemStack is = materiaMap.get(id);

            if(is != null) {
                pGuiGraphics.renderItem(is, itemX, itemY);

                int colorInt = mi.getMateriaColor();
                if(mi.getMateriaName().equals("color")) {
                    int period = 200;
                    int gt = (int)(menu.blockEntity.getLevel().getGameTime() % (period * 2));
                    float pScaledTime = ((float)((gt) % period)) / (float)period;

                    colorInt = ColorUtils.getLerpedRainbowColor(pScaledTime);
                }
                int intR = (colorInt & 0x00ff0000) >> 16;
                int intG = (colorInt & 0x0000ff00) >> 8;
                int intB = (colorInt & 0x000000ff);

                float r = (float)intR / 255f;
                float g = (float)intG / 255f;
                float b = (float)intB / 255f;
                pGuiGraphics.setColor(r, g, b, 1);

                int barW = 0;
                if(entry.getSecond() instanceof AbstractMateriaStorageSingleTypeBlockEntity amsstbe)
                    barW = Math.round(23 * amsstbe.getCurrentStockPercent());
                else if(entry.getSecond() instanceof AbstractMateriaStorageMultiTypeBlockEntity amsmtbe)
                    barW = Math.round(23 * amsmtbe.getCurrentStockPercent(entry.getFirst()));

                pGuiGraphics.blit(TEXTURE_COMPACT, barX, barY, 24, 254, barW, 2);
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
        int splitter = (menu.blockEntity.isCompactMode ? 32 : 16);

        int startIndex = pageIndex * splitter;
        int endIndex = (orderedMateriaStorageFiltered.size() - startIndex) > splitter ? startIndex + splitter : orderedMateriaStorageFiltered.size();

        for(int i=startIndex; i<endIndex; i++) {
            int counterX =  6 + (((i - startIndex) / 8) * (menu.blockEntity.isCompactMode ? 54 : 108));
            int counterY = -4 + (((i - startIndex) % 8) * 23);

            String type = "";
            int mLimit = 1;
            if(orderedMateriaStorageFiltered.get(i).getSecond() instanceof AbstractMateriaStorageSingleTypeBlockEntity single) {
                mLimit = single.getCurrentStock();
                type = single.getMateriaType() instanceof EssentiaItem ?
                                "item.magichem.essentia_" + single.getMateriaType().getMateriaName() + ".truncated" :
                                "item.magichem.admixture_" + single.getMateriaType().getMateriaName() + ".truncated";
            }
            else if(orderedMateriaStorageFiltered.get(i).getSecond() instanceof AbstractMateriaStorageMultiTypeBlockEntity multi) {
                mLimit = multi.getCurrentStock(orderedMateriaStorageFiltered.get(i).getFirst());
                type = orderedMateriaStorageFiltered.get(i).getFirst() instanceof EssentiaItem ?
                        "item.magichem.essentia_" + orderedMateriaStorageFiltered.get(i).getFirst().getMateriaName() + ".truncated" :
                        "item.magichem.admixture_" + orderedMateriaStorageFiltered.get(i).getFirst().getMateriaName() + ".truncated";
            }

            String mLimitFormatted = mLimit > 9999 ? ""+mLimit / 1000 + "K" : ""+mLimit;
            pGuiGraphics.drawString(font, mLimitFormatted, counterX, counterY, 0x00000000, false);
            if(!menu.blockEntity.isCompactMode) {

                pGuiGraphics.drawString(font, Component.translatable(type), counterX + 28, counterY, 0x00000000, false);
            }
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        super.renderTooltip(pGuiGraphics, pX, pY);
        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();

        int x = (width - 222) / 2;
        int y = (height - 213) / 2;

        int leftStart = x + 7;
        int topStart = y + 17;
        int buttonSize = 18;
        int paddingX = 36;
        int paddingY = 5;

        int columnID = (pX - leftStart) / (buttonSize + paddingX);
        int columnMod = (pX - leftStart) % (buttonSize + paddingX);
        int rowID = (pY - topStart) / (buttonSize + paddingY);
        int rowMod = (pY - topStart) % (buttonSize + paddingY);

        boolean xValidCompact = (columnID < 4);
        boolean xValidExpanded = (columnID == 0 || columnID == 2);
        boolean xValid = (columnID >= 0) && (menu.blockEntity.isCompactMode ? xValidCompact : xValidExpanded);
        boolean yValid = (rowID >= 0) && (rowID < 8);

        if(xValid && yValid) {
            if (pX >= leftStart && pY >= topStart && columnMod <= buttonSize && rowMod <= buttonSize) {
                int index = (menu.blockEntity.isCompactMode ? columnID : columnID / 2) * 8 + rowID + pageIndex * (menu.blockEntity.isCompactMode ? 32 : 16);

                if (index < orderedMateriaStorageFiltered.size())
                    tooltipContents.add(Component.empty()
                            .append(Component.translatable("item.magichem." + orderedMateriaStorageFiltered.get(index).getFirst().toString()))
                    );
            }
        }

        pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
    }
}
