package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.ButtonData;
import com.aranaira.magichem.foundation.enums.DistillationSourceCategory;
import com.aranaira.magichem.foundation.options.RecipeDisplayOption;
import com.aranaira.magichem.gui.element.CodexMateriaButtonRecipeSelector;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.DistillationFabricationRecipe;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.recipe.FluidDistillationFabricationRecipe;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.tools.math.MathUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.*;

import static com.aranaira.magichem.foundation.enums.DistillationSourceCategory.*;

public class CodexMateriaScreen extends AbstractContainerScreen<CodexMateriaMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_codex_materia.png");
    private final ButtonData[] materiaSelectButtons = new ButtonData[24];
    private final ButtonData[] recipeSelectButtons = new ButtonData[8];
    private final ImageButton[] categoryToggleButtons = new ImageButton[6];
    private final ImageButton[] modeSwitchButtons = new ImageButton[2];
    private final ArrayList<DistillationSourceCategory> categories = new ArrayList<>();
    private MateriaItem selectedMateria;
    private RecipeDisplayOption selectedRecipe;
    private EditBox recipeFilterBox;
    private static final ArrayList<String> sortedMateriaKeys = new ArrayList<>();
    private static final HashMap<String, ItemStack> materiaMap = new HashMap<>();
    private static List<DistillationFabricationRecipe> allDistillationRecipes = new ArrayList<>();
    private static List<FluidDistillationFabricationRecipe> allFluidDistillationRecipes = new ArrayList<>();
    private static List<FixationSeparationRecipe> allFixationSeprationsRecipes = new ArrayList<>();
    private String lastUsedFilter = null;
    private boolean isDistillationMode = true;

    public static final int
        PANEL_MAIN_W = 256, PANEL_MAIN_H = 178;

    public CodexMateriaScreen(CodexMateriaMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        if(materiaMap.size() == 0) {
            HashMap<String, MateriaItem> materiaItemMap = ItemRegistry.getMateriaMap(false, false);
            for(String key : materiaItemMap.keySet()) {
                materiaMap.put(key, new ItemStack(materiaItemMap.get(key)));
            }
        }
        if(allDistillationRecipes.size() == 0)
            allDistillationRecipes = DistillationFabricationRecipe.getAllDistillingRecipes(pPlayerInventory.player.level());
        if(allFluidDistillationRecipes.size() == 0)
            allFluidDistillationRecipes = FluidDistillationFabricationRecipe.getAllDistillingRecipes(pPlayerInventory.player.level());
        if(allFixationSeprationsRecipes.size() == 0)
            allFixationSeprationsRecipes = FixationSeparationRecipe.getAllSeparatingRecipes(pPlayerInventory.player.level());
        categories.addAll(Arrays.asList(DistillationSourceCategory.values()));
        updateDisplayedMateria("");
    }

    private ArrayList<String> getSortedMateriaKeys() {
        if(sortedMateriaKeys.size() == 0) {
            String[] sortingArray = new String[materiaMap.size()];
            materiaMap.keySet().toArray(sortingArray);
            Arrays.sort(sortingArray, Comparator.comparing(o -> {
                return ((MateriaItem)materiaMap.get(o).getItem()).getMateriaSortingName();
            }));
            sortedMateriaKeys.addAll(Arrays.asList(sortingArray));
        }
        return sortedMateriaKeys;
    }

    @Override
    protected void init() {
        super.init();
        initializeRecipeButtons();
        initializeCategoryToggleButtons();
        initializeRecipeSelectButtons();
        initializeRecipeFilterBox();

        renderButtons();
    }

    private void initializeRecipeButtons() {
        int c = 0;
        for(int y=0; y<4; y++) {
            for(int x=0; x<6; x++) {
                materiaSelectButtons[c] = new ButtonData(this.addRenderableWidget(new CodexMateriaButtonRecipeSelector(
                        this, c, this.leftPos, this.topPos, 18, 18, 0, 206, TEXTURE, button -> {

                    CodexMateriaScreen query = (CodexMateriaScreen) ((CodexMateriaButtonRecipeSelector) button).getScreen();
                    query.setSelectedMateria(((CodexMateriaButtonRecipeSelector) button).getArrayIndex());
                })), x*18 + 15, y*18 + 75);
                c++;
            }
        }
    }

    private void initializeCategoryToggleButtons() {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        categoryToggleButtons[0] = this.addRenderableWidget(new ImageButton(x+174, y-10, 10, 10, 246, 236, TEXTURE, button -> {
            if (categories.contains(CRAFTABLE)) {
                if(categories.size() > 1) categories.remove(CRAFTABLE);
            }
            else categories.add(CRAFTABLE);
            updateDisplayedRecipes(selectedMateria);
            recipeFilterPage = 0;
            }));
        categoryToggleButtons[1] = this.addRenderableWidget(new ImageButton(x+185, y-10, 10, 10, 246, 236, TEXTURE, button -> {
            if (categories.contains(GATHERABLE)) {
                if(categories.size() > 1) categories.remove(GATHERABLE);
            }
            else categories.add(GATHERABLE);
            updateDisplayedRecipes(selectedMateria);
            recipeFilterPage = 0;
            }));
        categoryToggleButtons[2] = this.addRenderableWidget(new ImageButton(x+196, y-10, 10, 10, 246, 236, TEXTURE, button -> {
            if (categories.contains(FARMABLE)) {
                if(categories.size() > 1) categories.remove(FARMABLE);
            }
            else categories.add(FARMABLE);
            updateDisplayedRecipes(selectedMateria);
            recipeFilterPage = 0;
            }));
        categoryToggleButtons[3] = this.addRenderableWidget(new ImageButton(x+207, y-10, 10, 10, 246, 236, TEXTURE, button -> {
            if (categories.contains(RENEWABLE)) {
                if(categories.size() > 1) categories.remove(RENEWABLE);
            }
            else categories.add(RENEWABLE);
            updateDisplayedRecipes(selectedMateria);
            recipeFilterPage = 0;
            }));
        categoryToggleButtons[4] = this.addRenderableWidget(new ImageButton(x+218, y-10, 10, 10, 246, 236, TEXTURE, button -> {
            if (categories.contains(TROPHY)) {
                if(categories.size() > 1) categories.remove(TROPHY);
            }
            else categories.add(TROPHY);
            updateDisplayedRecipes(selectedMateria);
            recipeFilterPage = 0;
            }));
        categoryToggleButtons[5] = this.addRenderableWidget(new ImageButton(x+229, y-10, 10, 10, 246, 236, TEXTURE, button -> {
            if (categories.contains(RARE)) {
                if(categories.size() > 1) categories.remove(RARE);
            }
            else categories.add(RARE);
            updateDisplayedRecipes(selectedMateria);
            recipeFilterPage = 0;
            }));

        modeSwitchButtons[0] = this.addRenderableWidget(new ImageButton(x+146, y+153, 18, 28, 148, 178, TEXTURE, button -> {
            isDistillationMode = true;
            updateDisplayedRecipes(selectedMateria);
            recipeFilterPage = 0;
            modeSwitchButtons[0].visible = false;
            modeSwitchButtons[1].visible = true;
            for (ImageButton categoryToggleButton : categoryToggleButtons) {
                categoryToggleButton.visible = true;
            }

        }));
        modeSwitchButtons[1] = this.addRenderableWidget(new ImageButton(x+166, y+153, 18, 28, 166, 178, TEXTURE, button -> {
            isDistillationMode = false;
            updateDisplayedRecipes(selectedMateria);
            recipeFilterPage = 0;
            modeSwitchButtons[0].visible = true;
            modeSwitchButtons[1].visible = false;
            for (ImageButton categoryToggleButton : categoryToggleButtons) {
                categoryToggleButton.visible = false;
            }
            }));
        modeSwitchButtons[0].visible = false;
        modeSwitchButtons[1].visible = true;
    }

    private void initializeRecipeSelectButtons() {
        int c = 0;
        for(int y=0; y<8; y++) {
            recipeSelectButtons[c] = new ButtonData(this.addRenderableWidget(new CodexMateriaButtonRecipeSelector(
                    this, c, this.leftPos, this.topPos, 18, 18, 0, 206, TEXTURE, button -> {

                CodexMateriaScreen query = (CodexMateriaScreen) ((CodexMateriaButtonRecipeSelector) button).getScreen();
                query.setActiveRecipe(((CodexMateriaButtonRecipeSelector) button).getArrayIndex());
            })), 134, y*18 + 3);
            c++;
        }
    }

    private void renderButtons() {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        for (ButtonData bd : materiaSelectButtons) {
            bd.getButton().setPosition(x + bd.getXOffset(), y + bd.getYOffset());
            bd.getButton().active = true;
            bd.getButton().visible = true;
        }

        for (ButtonData bd : recipeSelectButtons) {
            bd.getButton().setPosition(x + bd.getXOffset(), y + bd.getYOffset());
            bd.getButton().active = true;
            bd.getButton().visible = true;
        }
    }

    private void initializeRecipeFilterBox() {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        this.recipeFilterBox = new EditBox(Minecraft.getInstance().font, x, y, 106, 16, Component.empty());
        this.recipeFilterBox.setMaxLength(60);

        renderFilterBox();
    }

    private void renderFilterBox() {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        recipeFilterBox.setX(xOrigin + 16);
        recipeFilterBox.setY(yOrigin + 51);

        if(recipeFilterBox.getValue().isEmpty())
            recipeFilterBox.setSuggestion(Component.translatable("gui.magichem.typetofilter").getString());
        else
            recipeFilterBox.setSuggestion("");

        addRenderableWidget(recipeFilterBox);
    }

    private void updateFilterBoxContents() {
        if(recipeFilterBox.getValue().isEmpty())
            recipeFilterBox.setSuggestion(Component.translatable("gui.magichem.typetofilter").getString());
        else
            recipeFilterBox.setSuggestion("");
    }

    public void setSelectedMateria(int index) {
        int trueIndex = materiaFilterRow *6 + index;
        if(trueIndex < filteredMateria.size()) {
            selectedMateria = (MateriaItem) filteredMateria.get(trueIndex).getItem();
            updateDisplayedRecipes(selectedMateria);
            selectedRecipe = null;
            recipeFilterPage = 0;
        }
    }

    public void setActiveRecipe(int index) {
        int trueIndex = recipeFilterPage*8 + index;
        if(trueIndex < filteredRecipes.size()) {
            selectedRecipe = filteredRecipes.get(trueIndex);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        if(pMouseX >= x-20 && pMouseX <= x+122 &&
                pMouseY >= y+75 && pMouseY <= y+146) {
            if (materiaFilterRowTotal > 4) {
                if (pDelta < 0)
                    materiaFilterRow = Math.min(materiaFilterRowTotal - 4, materiaFilterRow + 1);
                else
                    materiaFilterRow = Math.max(0, materiaFilterRow - 1);
            }
        }

        if(pMouseX >= x+134 && pMouseX <= x+275 &&
                pMouseY >= y+3 && pMouseY <= y+146) {
            if (recipeFilterPagesTotal > 0) {
                if (pDelta < 0)
                    recipeFilterPage = Math.min(recipeFilterPagesTotal, recipeFilterPage + 1);
                else
                    recipeFilterPage = Math.max(0, recipeFilterPage - 1);
            }
        }

        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if(pButton == 0) {
            int x = (width - PANEL_MAIN_W) / 2;
            int y = (height - PANEL_MAIN_H) / 2;
            if (materiaFilterRowTotal > 4) {

                if (pMouseX >= x-20 && pMouseX <= x+7 &&
                        pMouseY >= y+75 && pMouseY <= y+146) {
                    double point = pMouseY - (y + 75);
                    double percent = point / 54d;

                    materiaFilterRow = Math.max(0, Math.min(materiaFilterRowTotal - 4, (int) Math.round(percent * materiaFilterRowTotal)));
                }
            }
            if (recipeFilterPagesTotal > 0) {
                if (pMouseX >= x+248 && pMouseX <= x+275 &&
                        pMouseY >= y+3 && pMouseY <= y+146) {
                    double point = pMouseY - (y + 3);
                    double percent = point / 126d;

                    recipeFilterPage = Math.max(0, Math.min(recipeFilterPagesTotal, (int) Math.round(percent * recipeFilterPagesTotal)));
                }
            }
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if(pButton == 0) {
            int x = (width - PANEL_MAIN_W) / 2;
            int y = (height - PANEL_MAIN_H) / 2;
            if (materiaFilterRowTotal > 4) {

                if (pMouseX >= x-20 && pMouseX <= x+7 &&
                        pMouseY >= y+75 && pMouseY <= y+146) {
                    double point = pMouseY - (y + 75);
                    double percent = point / 54d;

                    materiaFilterRow = Math.max(0, Math.min(materiaFilterRowTotal - 4, (int) Math.round(percent * materiaFilterRowTotal)));
                }
            }
            if (recipeFilterPagesTotal > 0) {
                if (pMouseX >= x+248 && pMouseX <= x+275 &&
                        pMouseY >= y+3 && pMouseY <= y+146) {
                    double point = pMouseY - (y + 3);
                    double percent = point / 126d;

                    recipeFilterPage = Math.max(0, Math.min(recipeFilterPagesTotal, (int) Math.round(percent * recipeFilterPagesTotal)));
                }
            }
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    private final List<ItemStack> filteredMateria = new ArrayList<>();
    private int materiaFilterRow, materiaFilterRowTotal;
    private void updateDisplayedMateria(String filter) {
        if (Objects.equals(filter, lastUsedFilter)) return;
        lastUsedFilter = filter;

        filteredMateria.clear();

        for(int i=0; i<getSortedMateriaKeys().size(); i++) {
            String display = Language.getInstance().getOrDefault("item.magichem.admixture_" + getSortedMateriaKeys().get(i));
            if((Objects.equals(filter, "") || display.toLowerCase().contains(filter.toLowerCase()))) {
                filteredMateria.add(materiaMap.get(getSortedMateriaKeys().get(i)));
            }
        }

        materiaFilterRowTotal = (int)Math.ceil(filteredMateria.size() / 6d);
        materiaFilterRow = MathUtils.clamp(materiaFilterRow, 0, materiaFilterRowTotal - 4);
    }

    private final List<RecipeDisplayOption> filteredRecipes = new ArrayList<>();
    private final List<FixationSeparationRecipe> filteredSeparationRecipes = new ArrayList<>();
    private int recipeFilterPage, recipeFilterPagesTotal;
    private void updateDisplayedRecipes(MateriaItem pFilter) {
        filteredRecipes.clear();
        filteredSeparationRecipes.clear();

        if(isDistillationMode) {
            for (DistillationFabricationRecipe recipeQuery : allDistillationRecipes) {
                for (ItemStack materiaQuery : recipeQuery.getComponentMateria()) {
                    if (materiaQuery.getItem() == pFilter) {
                        boolean hasMatchingCategory = false;
                        for (DistillationSourceCategory category : categories) {
                            hasMatchingCategory = recipeQuery.hasSourceCategory(category);
                            if (hasMatchingCategory) break;
                        }
                        if (hasMatchingCategory) filteredRecipes.add(new RecipeDisplayOption(recipeQuery));
                        break;
                    }
                }
            }

            for (FluidDistillationFabricationRecipe recipeQuery : allFluidDistillationRecipes) {
                for (ItemStack materiaQuery : recipeQuery.getComponentMateria()) {
                    if (materiaQuery.getItem() == pFilter) {
                        boolean hasMatchingCategory = false;
                        for (DistillationSourceCategory category : categories) {
                            hasMatchingCategory = recipeQuery.hasSourceCategory(category);
                            if (hasMatchingCategory) break;
                        }
                        if (hasMatchingCategory) filteredRecipes.add(new RecipeDisplayOption(recipeQuery));
                        break;
                    }
                }
            }

            //sort recipes
            RecipeDisplayOption[] sortingArray = new RecipeDisplayOption[filteredRecipes.size()];
            filteredRecipes.toArray(sortingArray);
            Arrays.sort(sortingArray, Comparator.comparing(o -> {
                String sortingKey = "";

                if (o.getRecipe() instanceof DistillationFabricationRecipe item) {
                    for (ItemStack materiaQuery : item.getComponentMateria()) {
                        if (materiaQuery.getItem() == pFilter) {
                            sortingKey = String.format("%06.2f", o.getSortingFloat(pFilter));
                            sortingKey += o.getItemRecipe().getAlchemyObject().getDisplayName();
                            break;
                        }
                    }
                } else if (o.getRecipe() instanceof FluidDistillationFabricationRecipe fluid) {
                    for (ItemStack materiaQuery : fluid.getComponentMateria()) {
                        if (materiaQuery.getItem() == pFilter) {
                            sortingKey = String.format("%06.2f", o.getSortingFloat(pFilter));
                            sortingKey += fluid.getAlchemyFluid().getDisplayName();
                            break;
                        }
                    }
                }

                return sortingKey;
            }));
            filteredRecipes.clear();
            for (int i = sortingArray.length - 1; i >= 0; i--) {
                filteredRecipes.add(sortingArray[i]);
            }

        } else {
            for (FixationSeparationRecipe recipeQuery : allFixationSeprationsRecipes) {
                for (ItemStack materiaQuery : recipeQuery.getComponentMateria()) {
                    if (materiaQuery.getItem() == pFilter) {
                        filteredRecipes.add(new RecipeDisplayOption(recipeQuery));
                        break;
                    }
                }
            }

            //sort recipes
            RecipeDisplayOption[] sortingArray = new RecipeDisplayOption[filteredRecipes.size()];
            filteredRecipes.toArray(sortingArray);
            Arrays.sort(sortingArray, Comparator.comparing(o -> {
                String sortingKey = "";

                if (o.getRecipe() instanceof FixationSeparationRecipe separation) {
                    for (ItemStack materiaQuery : separation.getComponentMateria()) {
                        if (materiaQuery.getItem() == pFilter) {
                            sortingKey = String.format("%06.2f", o.getSortingFloat(pFilter));
                            sortingKey += o.getSeparationRecipe().getResultAdmixture().getDisplayName();
                            break;
                        }
                    }
                }

                return sortingKey;
            }));
            filteredRecipes.clear();
            for (int i = sortingArray.length - 1; i >= 0; i--) {
                filteredRecipes.add(sortingArray[i]);
            }

        }
        recipeFilterPagesTotal = filteredRecipes.size() / 8;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        pGuiGraphics.blit(TEXTURE, x, y-20, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);
        pGuiGraphics.blit(TEXTURE, x+89, y+206, 224, 220, 32, 36);

        renderMateriaSelections(pGuiGraphics);
        renderRecipeOptions(pGuiGraphics);

        if(isDistillationMode) {
            if (categories.contains(CRAFTABLE))
                pGuiGraphics.blit(TEXTURE, x + 174, y - 10, 0, 246, 10, 10);
            if (categories.contains(GATHERABLE))
                pGuiGraphics.blit(TEXTURE, x + 185, y - 10, 10, 246, 10, 10);
            if (categories.contains(FARMABLE))
                pGuiGraphics.blit(TEXTURE, x + 196, y - 10, 20, 246, 10, 10);
            if (categories.contains(RENEWABLE))
                pGuiGraphics.blit(TEXTURE, x + 207, y - 10, 30, 246, 10, 10);
            if (categories.contains(TROPHY))
                pGuiGraphics.blit(TEXTURE, x + 218, y - 10, 40, 246, 10, 10);
            if (categories.contains(RARE))
                pGuiGraphics.blit(TEXTURE, x + 229, y - 10, 50, 246, 10, 10);
        }

        if(selectedMateria != null)
            pGuiGraphics.renderFakeItem(materiaMap.get(selectedMateria.getMateriaName()), x+154, y-12);

        //Left Scroll Ribbon
        if(materiaFilterRowTotal > 4) {
            boolean top = materiaFilterRow == 0;
            boolean bottom = materiaFilterRow == materiaFilterRowTotal - 4;
            int u = top ? 84 : (bottom ? 28 : 56);
            int v = 196;
            float percent = (float)materiaFilterRow / (float)(materiaFilterRowTotal - 4);
            int nubbinShift = (int)Math.floor(percent * 54);
            pGuiGraphics.blit(TEXTURE, x - 20, y + 75 + nubbinShift, u, v, 28, 18);
        }

        //Right Scroll Ribbon
        if(recipeFilterPagesTotal > 0) {
            boolean top = recipeFilterPage == 0;
            boolean bottom = recipeFilterPage == recipeFilterPagesTotal;
            int u = top ? 84 : (bottom ? 28 : 56);
            int v = 178;
            float percent = (float)recipeFilterPage / (float)(recipeFilterPagesTotal);
            int nubbinShift = (int)Math.floor(percent * 126);
            pGuiGraphics.blit(TEXTURE, x + 248, y + 3 + nubbinShift, u, v, 28, 18);
        }

        //Distillation/Separation Mode
        pGuiGraphics.blit(TEXTURE, x + 146, y + 153, 112, isDistillationMode ? 178 : 206, 18, 28);
        pGuiGraphics.blit(TEXTURE, x + 166, y + 153, 130, isDistillationMode ? 206 : 178, 18, 28);

        if(selectedRecipe != null) {
            if(selectedRecipe.getOutputRate() < 1f){
                pGuiGraphics.drawString(font, Component.translatable("gui.magichem.reducedoutputrate"), x + 54, y - 9, 0xff5c3b14, false);
                MutableComponent compound = Component.empty()
                        .append(Math.max(1, Math.round((selectedRecipe.getOutputRate()) * 100)) + "%")
                        .append(Component.translatable("gui.magichem.reducedoutputrate.peritem")
                        );
                int shift = 70 - font.width(compound.getString());
                pGuiGraphics.drawString(font, compound, x + 54 + shift, y + 1, 0xff5c3b14, false);
            }

            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().scale(2,2,2);
            selectedRecipe.draw(pGuiGraphics, (x/2)+9, (y/2)-4);
            pGuiGraphics.pose().popPose();

            int componentShift = (5 - selectedRecipe.getComponentMateria().size()) * 18;
            for (int i=0; i<selectedRecipe.getComponentMateria().size(); i++) {
                pGuiGraphics.renderFakeItem(selectedRecipe.getComponentMateria().get(i), x+33 + i*18 + componentShift, y+26);
                pGuiGraphics.renderItemDecorations(font, selectedRecipe.getComponentMateria().get(i), x+33 + i*18 + componentShift, y+26);
            }

            if(isDistillationMode){
                if (selectedRecipe.hasSourceCategory(CRAFTABLE))
                    pGuiGraphics.blit(TEXTURE, x + 55, y + 12, 0, 246, 10, 10);
                if (selectedRecipe.hasSourceCategory(GATHERABLE))
                    pGuiGraphics.blit(TEXTURE, x + 66, y + 12, 10, 246, 10, 10);
                if (selectedRecipe.hasSourceCategory(FARMABLE))
                    pGuiGraphics.blit(TEXTURE, x + 77, y + 12, 20, 246, 10, 10);
                if (selectedRecipe.hasSourceCategory(RENEWABLE))
                    pGuiGraphics.blit(TEXTURE, x + 88, y + 12, 30, 246, 10, 10);
                if (selectedRecipe.hasSourceCategory(TROPHY))
                    pGuiGraphics.blit(TEXTURE, x + 99, y + 12, 40, 246, 10, 10);
                if (selectedRecipe.hasSourceCategory(RARE))
                    pGuiGraphics.blit(TEXTURE, x + 110, y + 12, 50, 246, 10, 10);
            }
        }
    }

    private void renderMateriaSelections(GuiGraphics gui) {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        List<ItemStack> snipped = new ArrayList<>();
        for(int i = materiaFilterRow *6; i<Math.min(filteredMateria.size(), materiaFilterRow *6 + 24); i++) {
            snipped.add(filteredMateria.get(i));
        }

        int c = 0;
        int cLimit = Math.min(24, snipped.size());
        while(c < cLimit) {

            for(int y=0; y<4; y++) {
                for (int x=0; x<6; x++) {
                    gui.renderItem(snipped.get(c), xOrigin+16 + x*18, yOrigin+76 + y*18);
                    c++;
                    if(c >= cLimit) break;
                }
                if(c >= cLimit) break;
            }
        }
    }

    private void renderRecipeOptions(GuiGraphics gui) {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        List<RecipeDisplayOption> snipped = new ArrayList<>();
        for(int i = recipeFilterPage*8; i<Math.min(filteredRecipes.size(), recipeFilterPage*8 + 8); i++) {
            snipped.add(filteredRecipes.get(i));
        }

        int c = 0;
        int cLimit = Math.min(8, snipped.size());
        while(c < cLimit) {

            for(int y=0; y<8; y++) {
                snipped.get(c).draw(gui, xOrigin+135, yOrigin+4 + y*18);
                for(ItemStack materiaQuery : snipped.get(c).getComponentMateria()) {
                    if(materiaQuery.getItem() == selectedMateria) {
                        float outputRate = snipped.get(c).getOutputRate();
//                        gui.renderItem(materiaQuery, xOrigin+154, yOrigin+4 + y*18);
                        if(outputRate == 1f) {
                            String amt = materiaQuery.getCount()+"";
                            int widthShift = font.width(amt) / 2;
                            gui.drawString(font, amt, xOrigin + 163 - widthShift, yOrigin + 9 + y * 18, 0xff5c3b14, false);
                        } else {
                            float reducedCount = materiaQuery.getCount() * outputRate;
                            String amt = reducedCount % 1f == 0 ? Math.round(reducedCount)+"" : String.format("%.1f", reducedCount);
                            int widthShift = font.width(amt) / 2;
                            gui.drawString(font, amt, xOrigin + 163 - widthShift, yOrigin + 9 + y * 18, 0xff5c3b14, false);
                        }
                        break;
                    }
                }

                if(snipped.get(c).hasSourceCategory(CRAFTABLE))
                    gui.blit(TEXTURE, xOrigin+175, yOrigin+9 + y*18, 18, 206, 8, 6);
                if(snipped.get(c).hasSourceCategory(DistillationSourceCategory.GATHERABLE))
                    gui.blit(TEXTURE, xOrigin+186, yOrigin+9 + y*18, 18, 206, 8, 6);
                if(snipped.get(c).hasSourceCategory(DistillationSourceCategory.FARMABLE))
                    gui.blit(TEXTURE, xOrigin+197, yOrigin+9 + y*18, 18, 206, 8, 6);
                if(snipped.get(c).hasSourceCategory(DistillationSourceCategory.RENEWABLE))
                    gui.blit(TEXTURE, xOrigin+208, yOrigin+9 + y*18, 18, 206, 8, 6);
                if(snipped.get(c).hasSourceCategory(DistillationSourceCategory.TROPHY))
                    gui.blit(TEXTURE, xOrigin+219, yOrigin+9 + y*18, 18, 206, 8, 6);
                if(snipped.get(c).hasSourceCategory(DistillationSourceCategory.RARE))
                    gui.blit(TEXTURE, xOrigin+230, yOrigin+9 + y*18, 18, 206, 8, 6);

                c++;
                if(c >= cLimit) break;
            }
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        super.render(gui, mouseX, mouseY, delta);
        renderTooltip(gui, mouseX, mouseY);
        updateDisplayedMateria(recipeFilterBox == null ? "" : recipeFilterBox.getValue());
        updateFilterBoxContents();
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        super.renderTooltip(pGuiGraphics, pX, pY);

        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Upper right category tooltips
        {
            if (pX >= x + 174 && pX <= x + 184 && pY >= y - 10 && pY <= y) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.crafted").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.crafted.desc"))
                );
            }

            if (pX >= x + 185 && pX <= x + 195 && pY >= y - 10 && pY <= y) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.gatherable").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.gatherable.desc"))
                );
            }

            if (pX >= x + 196 && pX <= x + 206 && pY >= y - 10 && pY <= y) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.farmable").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.farmable.desc"))
                );
            }

            if (pX >= x + 207 && pX <= x + 217 && pY >= y - 10 && pY <= y) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.renewable").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.renewable.desc"))
                );
            }

            if (pX >= x + 218 && pX <= x + 228 && pY >= y - 10 && pY <= y) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.trophy").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.trophy.desc"))
                );
            }

            if (pX >= x + 229 && pX <= x + 239 && pY >= y - 10 && pY <= y) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.rare").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.rare.desc"))
                );
            }
        }

        //Focused recipe area category tooltips
        if(selectedRecipe != null){
            if (selectedRecipe.hasSourceCategory(CRAFTABLE) && pX >= x + 55 && pX <= x + 65 && pY >= y + 12 && pY <= y + 22) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.crafted").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.crafted.desc"))
                );
            }

            if (selectedRecipe.hasSourceCategory(GATHERABLE) && pX >= x + 66 && pX <= x + 76 && pY >= y + 12 && pY <= y + 22) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.gatherable").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.gatherable.desc"))
                );
            }

            if (selectedRecipe.hasSourceCategory(FARMABLE) && pX >= x + 77 && pX <= x + 87 && pY >= y + 12 && pY <= y + 22) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.farmable").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.farmable.desc"))
                );
            }

            if (selectedRecipe.hasSourceCategory(RENEWABLE) && pX >= x + 88 && pX <= x + 98 && pY >= y + 12 && pY <= y + 22) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.renewable").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.renewable.desc"))
                );
            }

            if (selectedRecipe.hasSourceCategory(TROPHY) && pX >= x + 99 && pX <= x + 109 && pY >= y + 12 && pY <= y + 22) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.trophy").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.trophy.desc"))
                );
            }

            if (selectedRecipe.hasSourceCategory(RARE) && pX >= x + 110 && pX <= x + 120 && pY >= y + 12 && pY <= y + 22) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.rare").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.codex_materia.rare.desc"))
                );
            }
        }

        //Items in materia picker
        if(pX >= x+134 && pX <= x+151 &&
                pY >= y+3 && pY <= y+146) {
            int my = pY - (y+3);
            int id = (my / 18);

            if (id >= 0 && id < 8) {
                if(id + recipeFilterPage * 8 < filteredRecipes.size()) {
                    RecipeDisplayOption optionQuery = filteredRecipes.get(id + recipeFilterPage * 8);
                    tooltipContents.addAll(optionQuery.getTooltipLines());
                }
            }
        }

        //Items in recipe picker
        if(pX >= x+15 && pX <= x+122 &&
                pY >= y+75 && pY <= y+146) {
            int mx = pX - (x+15);
            int my = pY - (y+75);
            int id = ((my / 18) * 6) + ((mx / 18) % 6);

            if (id >= 0 && id < 24) {
                if(id + materiaFilterRow * 6 < filteredMateria.size()) {
                    ItemStack stackUnderMouse = filteredMateria.get(id + materiaFilterRow * 6);
                    tooltipContents.addAll(stackUnderMouse.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
                }
            }
        }

        //Active materia type on right page
        if(selectedMateria != null && pX >= x+154 && pX <= x+170 && pY >= y-12 && pY <= y+4) {
            ItemStack stackUnderMouse = materiaMap.get(selectedMateria.getMateriaName());
            tooltipContents.addAll(stackUnderMouse.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
        }

        if(selectedRecipe != null) {
            if(pX >= x+17 && pX <= x+50 && pY >= y-8 && pY <= y+24) {
                tooltipContents.addAll(selectedRecipe.getTooltipLines());
            }

            if(pX >= x+32 && pX <= x+122 && pY >= y+25 && pY <= y+43) {
                int index = ((pX-(x+33)) / 18) - (5 - selectedRecipe.getComponentMateria().size());
                if(index >= 0 && index < selectedRecipe.getComponentMateria().size())
                    tooltipContents.addAll(selectedRecipe.getComponentMateria().get(index).getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
            }
        }

        //Mode banners
        {
            if (pX >= x + 146 && pX <= x + 164 && pY >= y + 153 && pY <= y + 181) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("jei.magichem.distillation.short").withStyle(ChatFormatting.GOLD))
                );
            }
            if (pX >= x + 166 && pX <= x + 184 && pY >= y + 153 && pY <= y + 181) {
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("jei.magichem.separation.short").withStyle(ChatFormatting.GOLD))
                );
            }
        }

        pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == InputConstants.KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        if (Minecraft.getInstance().options.keyInventory.matches(pKeyCode, pScanCode)) {
            if (recipeFilterBox.canConsumeInput()) return recipeFilterBox.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
}
