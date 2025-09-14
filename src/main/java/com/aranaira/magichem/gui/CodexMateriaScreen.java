package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.foundation.ButtonData;
import com.aranaira.magichem.foundation.enums.DistillationSourceCategory;
import com.aranaira.magichem.gui.element.CodexMateriaButtonRecipeSelector;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.recipe.DistillationFabricationRecipe;
import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public class CodexMateriaScreen extends AbstractContainerScreen<CodexMateriaMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_codex_materia.png");
    private final ButtonData[] recipeSelectButtons = new ButtonData[20];
    private MateriaItem recipe;
    private static final ArrayList<String> sortedMateriaKeys = new ArrayList<>();
    private static final HashMap<String, ItemStack> materiaMap = new HashMap<>();
    private static List<DistillationFabricationRecipe> allDistillationRecipes = new ArrayList<>();

    public static final int
        PANEL_MAIN_W = 256, PANEL_MAIN_H = 178;

    public CodexMateriaScreen(CodexMateriaMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        if(materiaMap.size() == 0) {
            HashMap<String, MateriaItem> materiaItemMap = ItemRegistry.getMateriaMap(false, false);
            String[] sortingArray = new String[materiaItemMap.size()];
            materiaItemMap.keySet().toArray(sortingArray);
            Arrays.sort(sortingArray, Comparator.comparing(o -> {
                return materiaItemMap.get(o).getMateriaSortingName();
            }));
            for(String key : sortedMateriaKeys) {
                sortedMateriaKeys.add(key);
                materiaMap.put(key, new ItemStack(materiaItemMap.get(key)));
            }
        }
        if(allDistillationRecipes.size() == 0)
            allDistillationRecipes = DistillationFabricationRecipe.getAllDistillingRecipes(pPlayerInventory.player.level());
        updateDisplayedMateria("");
    }

    @Override
    protected void init() {
        super.init();
        initializeRecipeButtons();
    }

    private void initializeRecipeButtons() {
        int c = 0;
        for(int y=0; y<4; y++) {
            for(int x=0; x<5; x++) {
                recipeSelectButtons[c] = new ButtonData(this.addRenderableWidget(new CodexMateriaButtonRecipeSelector(
                        this, c, this.leftPos, this.topPos, 18, 18, 0, 206, TEXTURE, button -> {

                    CodexMateriaScreen query = (CodexMateriaScreen) ((CodexMateriaButtonRecipeSelector) button).getScreen();
                    query.setActiveRecipe(((CodexMateriaButtonRecipeSelector) button).getArrayIndex());
                })), x*18 + 16, y*18 + 75);
                c++;
            }
        }

        renderButtons();
    }

    private void renderButtons() {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        for (ButtonData bd : recipeSelectButtons) {
            bd.getButton().setPosition(x + bd.getXOffset(), y + bd.getYOffset());
            bd.getButton().active = true;
            bd.getButton().visible = true;
        }
    }

    public void setActiveRecipe(int index) {
        int trueIndex = materiaFilterRow *3 + index;
        if(trueIndex < filteredMateria.size()) {
            recipe = (MateriaItem) filteredMateria.get(trueIndex).getItem();
            updateDisplayedRecipes(recipe);
        }
    }

    private List<ItemStack> filteredMateria = new ArrayList<>();
    private int materiaFilterRow, materiaFilterRowTotal;
    private void updateDisplayedMateria(String filter) {
        filteredMateria.clear();

        for(String key : sortedMateriaKeys) {
            String display = Component.translatable("item.magichem.admixture_"+key).toString();
            if((Objects.equals(filter, "") || display.toLowerCase().contains(filter.toLowerCase()))) {
                filteredMateria.add(materiaMap.get(key));
            }
        }

        materiaFilterRowTotal = (int)Math.ceil(filteredMateria.size() / 3d);

//        recipesChanged = false;
    }

    private List<DistillationFabricationRecipe> filteredRecipes = new ArrayList<>();
    private int recipeFilterPage, recipeFilterPagesTotal;
    private void updateDisplayedRecipes(MateriaItem pFilter) {
        filteredRecipes.clear();

        for(DistillationFabricationRecipe recipeQuery : allDistillationRecipes) {
            for(ItemStack materiaQuery : recipeQuery.getComponentMateria()) {
                if (materiaQuery.getItem() == pFilter) {
                    filteredRecipes.add(recipeQuery);
                    break;
                }
            }
        }

        //sort recipes
        DistillationFabricationRecipe[] sortingArray = new DistillationFabricationRecipe[filteredRecipes.size()];
        filteredRecipes.toArray(sortingArray);
        Arrays.sort(sortingArray, Comparator.comparing(o -> {
            String sortingKey = "";

            for(ItemStack materiaQuery : o.getComponentMateria()) {
                if (materiaQuery.getItem() == pFilter) {
                    sortingKey = String.format("%06.2f", o.getOutputRate() * materiaQuery.getCount());
                    sortingKey += o.getAlchemyObject().getDisplayName();
                    break;
                }
            }

            return sortingKey;
        }));
        filteredRecipes.clear();
        for(int i=sortingArray.length-1; i>=0; i--) {
            filteredRecipes.add(sortingArray[i]);
        }

        recipeFilterPagesTotal = (int)Math.ceil(filteredRecipes.size() / 8d);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        pGuiGraphics.blit(TEXTURE, x, y-20, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);
        pGuiGraphics.blit(TEXTURE, x+89, y+206, 224, 220, 32, 36);

        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().scale(2.0f, 2.0f, 2.0f);
        pGuiGraphics.pose().translate(0.0f, 0.5f, 0.0f);
//        pGuiGraphics.renderFakeItem(blah, (x/2)+45, (y/2)+35);
        pGuiGraphics.pose().popPose();

        renderMateriaSelections(pGuiGraphics);
        renderRecipeOptions(pGuiGraphics);
    }

    private void renderMateriaSelections(GuiGraphics gui) {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        List<ItemStack> snipped = new ArrayList<>();
        for(int i = materiaFilterRow *4; i<Math.min(filteredMateria.size(), materiaFilterRow *4 + 20); i++) {
            snipped.add(filteredMateria.get(i));
        }

        int c = 0;
        int cLimit = Math.min(20, snipped.size());
        while(c < cLimit) {

            for(int y=0; y<4; y++) {
                for (int x=0; x<5; x++) {
                    gui.renderItem(snipped.get(c), xOrigin+17 + x*18, yOrigin+76 + y*18);
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

        List<DistillationFabricationRecipe> snipped = new ArrayList<>();
        for(int i = recipeFilterPage*8; i<Math.min(filteredRecipes.size(), recipeFilterPage*8 + 8); i++) {
            snipped.add(filteredRecipes.get(i));
        }

        int c = 0;
        int cLimit = Math.min(8, snipped.size());
        while(c < cLimit) {

            for(int y=0; y<8; y++) {
                gui.renderItem(snipped.get(c).getAlchemyObject(), xOrigin+135, yOrigin+4 + y*18);
                for(ItemStack materiaQuery : snipped.get(c).getComponentMateria()) {
                    if(materiaQuery.getItem() == recipe) {
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

                if(snipped.get(c).hasSourceCategory(DistillationSourceCategory.CRAFTABLE))
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

        pGuiGraphics.drawString(font, "-x:"+x, 10, 10, 0xffffffff, true);
        pGuiGraphics.drawString(font, "pX:"+pX, 10, 22, 0xffffffff, true);
        pGuiGraphics.drawString(font, "dX:"+(pX-x), 10, 34, 0xffffffff, true);

        pGuiGraphics.drawString(font, "-y:"+y, 10, 50, 0xffffffff, true);
        pGuiGraphics.drawString(font, "py:"+pY, 10, 62, 0xffffffff, true);
        pGuiGraphics.drawString(font, "dy:"+(pY-y), 10, 74, 0xffffffff, true);

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
//
//        if(pX >= x + 65 && pX < x + 65 + 18 && pY >= y + 50 && pY < y + 50 + 18) {
//            if(menu.itemHandler.getStackInSlot(ChargingTalismanMenu.SLOT_CHARGEABLE_ITEM) == ItemStack.EMPTY) {
//                tooltipContents.add(Component.empty()
//                        .append(Component.translatable("tooltip.magichem.gui.charging_talisman.chargee.line1"))
//                );
//                tooltipContents.add(Component.empty());
//                tooltipContents.add(Component.empty()
//                        .append(Component.translatable("tooltip.magichem.gui.charging_talisman.chargee.line2"))
//                );
//                pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
//            }
//        }

        pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
    }
}
