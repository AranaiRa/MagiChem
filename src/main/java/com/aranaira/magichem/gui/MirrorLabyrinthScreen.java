package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.networking.MirrorLabyrinthSyncDataC2SPacket;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.PacketRegistry;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MirrorLabyrinthScreen extends AbstractContainerScreen<MirrorLabyrinthMenu> {
    private static final ResourceLocation TEXTURE_COMPACT =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_mirror_labyrinth_compact.png");
    private static final ResourceLocation TEXTURE_EXPANDED =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_mirror_labyrinth_expanded.png");
    private static final int
            PANEL_MAIN_W = 222, PANEL_MAIN_H = 121,
            PANEL_GRIME_X = 176, PANEL_GRIME_Y = 14, PANEL_GRIME_W = 64, PANEL_GRIME_H = 59, PANEL_GRIME_U = 176, PANEL_GRIME_V = 0,
            TOOLTIP_EFFICIENCY_X = 178, TOOLTIP_EFFICIENCY_Y = 18, TOOLTIP_EFFICIENCY_W = 57, TOOLTIP_EFFICIENCY_H = 15,
            TOOLTIP_OPERATIONTIME_X = 178, TOOLTIP_OPERATIONTIME_Y = 37, TOOLTIP_OPERATIONTIME_W = 57, TOOLTIP_OPERATIONTIME_H = 15,
            TOOLTIP_GRIME_X = 179, TOOLTIP_GRIME_Y = 53, TOOLTIP_GRIME_W = 56, TOOLTIP_GRIME_H = 14;
    private final HashMap<String, ItemStack> materiaMap = new HashMap<>();
    private final List<Pair<MateriaItem, Integer>> orderedMateriaStorage = new ArrayList<>();
    final List<Pair<MateriaItem, Integer>> orderedMateriaStorageFiltered = new ArrayList<>();
    private ImageButton setCompactButton, setExpandedButton;
    private final ImageButton[]
            materiaSelectorButtonsCompact = new ImageButton[16],
            materiaSelectorButtonsExpanded = new ImageButton[8];
    private EditBox recipeFilterBox;
    int pageIndex = 0;
    int pageCount = 1;

    public MirrorLabyrinthScreen(MirrorLabyrinthMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        if(materiaMap.size() == 0) {
            final HashMap<String, MateriaItem> baseMateriaMap = ItemRegistry.getMateriaMap(false, true);
            for(String key : baseMateriaMap.keySet()) {
                materiaMap.put(key, new ItemStack(baseMateriaMap.get(key)));
            }
        }
        updateStorageOrder();
    }

    @Override
    protected void init() {
        super.init();
        initializeButtons();
        initializeRecipeFilterBox();
    }

    private void initializeButtons() {
        for(int i=0; i<16; i++) {
            int buttonX = this.leftPos - 16 + ((i / 4) * 54);
            int buttonY = this.topPos - 20 + ((i % 4) * 23);

            int finalI = i;
            materiaSelectorButtonsCompact[i] = this.addRenderableWidget(new ImageButton(buttonX, buttonY, 18, 18, 24, 218, TEXTURE_COMPACT, button -> {
                MateriaItem mi = getMateriaTypeFromButtonID(finalI);
                if(mi != null) {
                    menu.blockEntity.setActiveMateriaType(mi);
                    PacketRegistry.sendToServer(new MirrorLabyrinthSyncDataC2SPacket(
                            menu.blockEntity.getBlockPos(),
                            mi,
                            menu.blockEntity.getPowerUsageSetting()
                    ));
                }
            }));
            materiaSelectorButtonsCompact[i].visible = menu.blockEntity.isCompactMode;
        }
        for(int i=0; i<8; i++) {
            int buttonX = this.leftPos - 16 + ((i / 4) * 108);
            int buttonY = this.topPos - 20 + ((i % 4) * 23);

            int finalI = i;
            materiaSelectorButtonsExpanded[i] = this.addRenderableWidget(new ImageButton(buttonX, buttonY, 18, 18, 24, 218, TEXTURE_COMPACT, button -> {
                MateriaItem mi = getMateriaTypeFromButtonID(finalI);
                if(mi != null) {
                    menu.blockEntity.setActiveMateriaType(mi);
                    PacketRegistry.sendToServer(new MirrorLabyrinthSyncDataC2SPacket(
                            menu.blockEntity.getBlockPos(),
                            mi,
                            menu.blockEntity.getPowerUsageSetting()
                    ));
                }
            }));
            materiaSelectorButtonsExpanded[i].visible = !menu.blockEntity.isCompactMode;
        }

        //next page button
        this.addRenderableWidget(new ImageButton(this.leftPos + 82, this.topPos + 71, 12, 7, 12, 242, TEXTURE_COMPACT, button -> {
            pageIndex = Math.min(pageIndex + 1, (menu.blockEntity.isCompactMode ? pageCount : pageCount * 2) - 1);
        }));

        //previous page button
        this.addRenderableWidget(new ImageButton(this.leftPos + 82, this.topPos - 31, 12, 7, 0, 242, TEXTURE_COMPACT, button -> {
            pageIndex = Math.max(pageIndex - 1, 0);
        }));

        //expanded / compacted button
        setExpandedButton = this.addRenderableWidget(new ImageButton(this.leftPos + 212, this.topPos - 28, 14, 14, 242, 228, TEXTURE_COMPACT, button -> {
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
        setCompactButton = this.addRenderableWidget(new ImageButton(this.leftPos + 212, this.topPos - 28, 14, 14, 228, 228, TEXTURE_COMPACT, button -> {
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

        //power level buttons

        this.addRenderableWidget(new ImageButton(this.leftPos - 100, this.topPos + 6, 12, 7, 0, 242, TEXTURE_COMPACT, button -> {
            menu.blockEntity.incrementPowerUsageSetting();
            MateriaItem fillTarget = null;
            if(menu.blockEntity.getActiveMateriaType() != null) {
                fillTarget = menu.blockEntity.getActiveMateriaType();
            }
            PacketRegistry.sendToServer(new MirrorLabyrinthSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    fillTarget,
                    menu.blockEntity.getPowerUsageSetting()
            ));
        }));
        this.addRenderableWidget(new ImageButton(this.leftPos - 100, this.topPos + 51, 12, 7, 12, 242, TEXTURE_COMPACT, button -> {
            menu.blockEntity.decrementPowerUsageSetting();
            MateriaItem fillTarget = null;
            if(menu.blockEntity.getActiveMateriaType() != null) {
                fillTarget = menu.blockEntity.getActiveMateriaType();
            }
            PacketRegistry.sendToServer(new MirrorLabyrinthSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    fillTarget,
                    menu.blockEntity.getPowerUsageSetting()
            ));
        }));
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
        for (Pair<MateriaItem, Integer> pair : orderedMateriaStorage) {
            MateriaItem mi = pair.getFirst();
            String name = mi instanceof EssentiaItem ?
                    Component.translatable("item.magichem.essentia_" + mi.getMateriaName()).toString() :
                    Component.translatable("item.magichem.admixture_" + mi.getMateriaName()).toString();

            if(filter.equals("") || name.contains(filter)) {
                orderedMateriaStorageFiltered.add(pair);
            }
        }
    }

    private MateriaItem getMateriaTypeFromButtonID(int pButtonID) {
        int index = pButtonID + ((menu.blockEntity.isCompactMode ? 16 : 8) * pageIndex);
        if(index < orderedMateriaStorageFiltered.size()) {
            return orderedMateriaStorageFiltered.get(index).getFirst();
        }

        return null;
    }

    private void renderFilterBox() {
        int xOrigin = (width - 222) / 2;
        int yOrigin = (height - 213) / 2;

        recipeFilterBox.setX(xOrigin - 76);
        recipeFilterBox.setY(yOrigin - 6);

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

    private ResourceLocation getTexture() {
        return menu.blockEntity.isCompactMode ? TEXTURE_COMPACT : TEXTURE_EXPANDED;
    }

    private void updateStorageOrder() {
        orderedMateriaStorage.clear();
        for(MateriaItem mi : menu.blockEntity.getMateriaTypesSorted()) {
            orderedMateriaStorage.add(new Pair<>(mi, menu.blockEntity.getCurrentStock(mi)));
        }

        updateMateriaOptionsByTextFilter();

        pageCount = (int)Math.ceil((float)orderedMateriaStorageFiltered.size() / 16f);
        if(pageCount <= 0)
            pageCount = 1;

        menu.blockEntity.needsGuiStorageUpdate = false;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //materia panel
        gui.blit(getTexture(), x, y - 60, 0, 0, 222, 121);

        //inventory panel
        gui.blit(TEXTURE_COMPACT, x - 7, y + 64, 0, 121, 176, 90);

        //insertion panel
        gui.blit(TEXTURE_COMPACT, x + 172, y + 64, 176, 121, 57, 90);

        //power panel
        gui.blit(TEXTURE_EXPANDED, x - 84, y - 24, 0, 121, 80, 66);

        //power bar
        int pH = (menu.blockEntity.getPowerUsageSetting() + 1) * 5;
        gui.blit(TEXTURE_EXPANDED, x - 75, y + 24 - pH, 0, 226 + (30 - pH), 8, pH);

        //search bar
        gui.blit(TEXTURE_COMPACT, x - 84, y - 60, 116, 224, 80, 32);

        //button house
        gui.blit(TEXTURE_COMPACT, x + 226, y - 60, 196, 224, 32, 32);

        //bottle ghosts for empty slots
        if(!menu.blockEntity.hasItemInInsertResultSlot())
            gui.blit(TEXTURE_COMPACT, x + 204, y + 71, 0, 224, 18, 18);
        if(!menu.blockEntity.hasItemInExtractResultSlot())
            gui.blit(TEXTURE_COMPACT, x + 179, y + 129, 0, 224, 18, 18);

        if(menu.blockEntity.needsGuiStorageUpdate) {
            updateStorageOrder();
        }

        int splitter = (menu.blockEntity.isCompactMode ? 16 : 8);

        int startIndex = pageIndex * splitter;
        int endIndex = (orderedMateriaStorageFiltered.size() - startIndex) > splitter ? startIndex + splitter : orderedMateriaStorageFiltered.size();

        for(int i=startIndex; i<endIndex; i++) {
            int itemX = x +  8 + (((i - startIndex) / 4) * (menu.blockEntity.isCompactMode ? 54 : 108));
            int itemY = y - 42 + (((i - startIndex) % 4) * 23);
            int barX = x + 29 + (((i - startIndex) / 4) * (menu.blockEntity.isCompactMode ? 54 : 108));
            int barY = y - 29 + (((i - startIndex) % 4) * 23);

            final Pair<MateriaItem, Integer> entry = orderedMateriaStorageFiltered.get(i);
            MateriaItem mi = entry.getFirst();
            String id = (mi instanceof EssentiaItem ? "essentia_" : "admixture_") + mi.getMateriaName();
            ItemStack is = materiaMap.get(id);

            if(is != null) {
                gui.renderItem(is, itemX, itemY);

                int colorInt = mi.getMateriaColor();
                int intR = (colorInt & 0x00ff0000) >> 16;
                int intG = (colorInt & 0x0000ff00) >> 8;
                int intB = (colorInt & 0x000000ff);

                float r = (float)intR / 255f;
                float g = (float)intG / 255f;
                float b = (float)intB / 255f;
                gui.setColor(r, g, b, 1);

                int barW = Math.round(23 * Math.min(1, (float)entry.getSecond() / (float)menu.blockEntity.getStorageLimit(mi)));

                gui.blit(TEXTURE_COMPACT, barX, barY, 24, 254, barW, 2);
                gui.setColor(1,1,1,1);
            }
        }

//        renderGrimePanel(gui, x + PANEL_GRIME_X, y + PANEL_GRIME_Y);
//
//        int sProg = DistilleryBlockEntity.getScaledProgress(menu.getProgress(), menu.getGrime(), menu.getBatchSize(), menu.getOperationTimeMod(), DistilleryBlockEntity::getVar, menu.blockEntity::getPoweredOperationTime);
//        if(sProg > 0)
//            gui.blit(TEXTURE, x+76, y+47, 0, 228, sProg, 28);
//
//        int sGrime = DistilleryBlockEntity.getScaledGrime(menu.getGrime());
//        if(sGrime > 0)
//            gui.blit(TEXTURE, x+182, y+57, 24, 248, sGrime, 8);
//
//        if(menu.getHeatDuration() > 0) {
//            int sHeat = DistilleryBlockEntity.getScaledHeat(menu.getHeat(), menu.getHeatDuration(), DistilleryBlockEntity::getVar);
//            int hHeat = DistilleryBlockEntity.getVar(AbstractDistillationBlockEntity.IDs.GUI_HEAT_GAUGE_HEIGHT) - sHeat;
//            gui.blit(TEXTURE, x + 79, y + 78 + hHeat, 24, 232 + hHeat, 18, sHeat);
//        }
    }

    private void renderGrimePanel(GuiGraphics gui, int x, int y) {
        gui.blit(TEXTURE_COMPACT, x, y, PANEL_GRIME_U, PANEL_GRIME_V, PANEL_GRIME_W, PANEL_GRIME_H);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        renderBackground(gui);
        super.render(gui, mouseX, mouseY, delta);
        renderTooltip(gui, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderTooltip(gui, mouseX, mouseY);
//
//        Font font = Minecraft.getInstance().font;
//        List<Component> tooltipContents = new ArrayList<>();
//        int x = (width - PANEL_MAIN_W) / 2;
//        int y = (height - PANEL_MAIN_H) / 2;
//
//        int g = 40;
//
//        //Efficiency
//        if(mouseX >= x+TOOLTIP_EFFICIENCY_X && mouseX <= x+TOOLTIP_EFFICIENCY_X+TOOLTIP_EFFICIENCY_W &&
//            mouseY >= y+TOOLTIP_EFFICIENCY_Y && mouseY <= y+TOOLTIP_EFFICIENCY_Y+TOOLTIP_EFFICIENCY_H) {
//
//            tooltipContents.add(Component.empty()
//                    .append(Component.translatable("tooltip.magichem.gui.efficiency").withStyle(ChatFormatting.GOLD))
//                    .append(": ")
//                    .append(Component.translatable("tooltip.magichem.gui.efficiency.line1")));
//            tooltipContents.add(Component.empty());
//            tooltipContents.add(Component.translatable("tooltip.magichem.gui.efficiency.line2"));
//            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
//        }
//
//        //Operation Time
//        if(mouseX >= x+TOOLTIP_OPERATIONTIME_X && mouseX <= x+TOOLTIP_OPERATIONTIME_X+TOOLTIP_OPERATIONTIME_W &&
//            mouseY >= y+TOOLTIP_OPERATIONTIME_Y && mouseY <= y+TOOLTIP_OPERATIONTIME_Y+TOOLTIP_OPERATIONTIME_H) {
//
//            tooltipContents.clear();
//            tooltipContents.add(Component.empty()
//                    .append(Component.translatable("tooltip.magichem.gui.operationtime").withStyle(ChatFormatting.GOLD))
//                    .append(": ")
//                    .append(Component.translatable("tooltip.magichem.gui.operationtime.line1")));
//            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
//        }
//
//        //Grime Bar
//        if(mouseX >= x+TOOLTIP_GRIME_X && mouseX <= x+TOOLTIP_GRIME_X+TOOLTIP_GRIME_W &&
//            mouseY >= y+TOOLTIP_GRIME_Y && mouseY <= y+TOOLTIP_GRIME_Y+TOOLTIP_GRIME_H) {
//
//            tooltipContents.clear();
//            tooltipContents.add(Component.empty()
//                    .append(Component.translatable("tooltip.magichem.gui.grime").withStyle(ChatFormatting.GOLD))
//                    .append(": ")
//                    .append(Component.translatable("tooltip.magichem.gui.grime.line1")));
//            tooltipContents.add(Component.empty());
//            tooltipContents.add(Component.translatable("tooltip.magichem.gui.grime.line2.1")
//                    .append(Component.literal(ServerConfig.grimePenaltyPoint+"%").withStyle(ChatFormatting.DARK_AQUA))
//                    .append(Component.translatable("tooltip.magichem.gui.grime.line2.2")));
//            tooltipContents.add(Component.empty());
//            tooltipContents.add(Component.empty()
//                    .append(Component.translatable("tooltip.magichem.gui.grime.line3").withStyle(ChatFormatting.DARK_GRAY))
//                    .append(" ")
//                    .append(Component.literal(String.format("%.1f", DistilleryBlockEntity.getGrimePercent(menu.getGrime(), DistilleryBlockEntity::getVar)*100.0f)+"%").withStyle(ChatFormatting.DARK_AQUA)));
//            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
//        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int pMouseX, int pMouseY) {
        Font font = Minecraft.getInstance().font;
        int splitter = (menu.blockEntity.isCompactMode ? 16 : 8);

        int startIndex = pageIndex * splitter;
        int endIndex = (orderedMateriaStorageFiltered.size() - startIndex) > splitter ? startIndex + splitter : orderedMateriaStorageFiltered.size();

        for(int i=startIndex; i<endIndex; i++) {
            int counterX =  6 + (((i - startIndex) / 4) * (menu.blockEntity.isCompactMode ? 54 : 108));
            int counterY = -18 + (((i - startIndex) % 4) * 23);

            int mLimit = orderedMateriaStorageFiltered.get(i).getSecond();
            String mLimitFormatted = mLimit > 9999 ? ""+mLimit / 1000 + "K" : ""+mLimit;
            String type = orderedMateriaStorageFiltered.get(i).getFirst() instanceof EssentiaItem ?
                    "item.magichem.essentia_" + orderedMateriaStorageFiltered.get(i).getFirst().getMateriaName() + ".truncated" :
                    "item.magichem.admixture_" + orderedMateriaStorageFiltered.get(i).getFirst().getMateriaName() + ".truncated";

            gui.drawString(font, mLimitFormatted, counterX, counterY, 0x00000000, false);
            if(!menu.blockEntity.isCompactMode) {

                gui.drawString(font, Component.translatable(type), counterX + 28, counterY, 0x00000000, false);
            }
        }

        gui.drawString(font, Component.literal(menu.blockEntity.getEnergyConsumptionRate()+"/t"), -68, 12, 0xff000000, false);

        int essentiaStorage = menu.blockEntity.getEssentiaStorageLimit();
        String essentiaStorageFormatted = essentiaStorage > 9999 ? "x"+essentiaStorage / 1000 + "K" : "x"+essentiaStorage;

        gui.drawString(font, Component.literal(essentiaStorageFormatted), -68, 27, 0xff000000, false);

        int admixtureStorage = menu.blockEntity.getAdmixtureStorageLimit();
        String admixtureStorageFormatted = admixtureStorage > 9999 ? "x"+admixtureStorage / 1000 + "K" : "x"+admixtureStorage;

        gui.drawString(font, Component.literal(admixtureStorageFormatted), -68, 44, 0xff000000, false);

//        int secWhole = DistilleryBlockEntity.getOperationTicks(menu.getGrime(), menu.getBatchSize(), menu.getOperationTimeMod(), DistilleryBlockEntity::getVar, menu.blockEntity::getPoweredOperationTime) / 20;
//        int secPartial = (DistilleryBlockEntity.getOperationTicks(menu.getGrime(), menu.getBatchSize(), menu.getOperationTimeMod(), DistilleryBlockEntity::getVar, menu.blockEntity::getPoweredOperationTime) % 20) * 5;
//        gui.drawString(font ,secWhole+"."+(secPartial < 10 ? "0"+secPartial : secPartial)+" s", PANEL_GRIME_X + 20, PANEL_GRIME_Y + 9, 0xff000000, false);
    }
}
