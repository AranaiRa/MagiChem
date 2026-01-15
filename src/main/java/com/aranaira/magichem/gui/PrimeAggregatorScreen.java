package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.PrimeAggregatorBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.foundation.ButtonData;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.gui.element.PrimeAggregatorButtonRecipeSelector;
import com.aranaira.magichem.networking.DeviceRecipeClearC2SPacket;
import com.aranaira.magichem.networking.DeviceRecipeSyncDataC2SPacket;
import com.aranaira.magichem.recipe.ExaltationRecipe;
import com.aranaira.magichem.registry.PacketRegistry;
import com.mna.api.affinity.Affinity;
import com.mna.tools.math.Vector3;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.aranaira.magichem.block.entity.PrimeAggregatorBlockEntity.*;
import static com.mna.api.affinity.Affinity.*;

public class PrimeAggregatorScreen extends AbstractContainerScreen<PrimeAggregatorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_exaltation.png");
    private static final ResourceLocation TEXTURE_EXT =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_exaltation_ext.png");
    private static final ResourceLocation TEXTURE_SLURRY =
            new ResourceLocation(MagiChemMod.MODID, "textures/block/fluid/experience_still.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 176;
    private static final Affinity[] AFFINITIES_ORDERED = {
            ENDER, EARTH, WATER, WIND, FIRE, ARCANE
    };
    private static final HashMap<Affinity, Vector3> AFFINITIES = new HashMap<>();
    private final ButtonData[] recipeSelectButtons = new ButtonData[15];

    private EditBox recipeFilterBox;
    private ExaltationRecipe lastRecipe = null;
    private NonNullList<ItemStack> lastRecipeComponentMateria = NonNullList.create();
    private ItemStack lastRecipeResultAdmixture = ItemStack.EMPTY;
    private boolean recipesChanged = false;
    private IItemHandler itemHandler;
    private ItemStack displayItemStack = ItemStack.EMPTY, displayMateriaStack = ItemStack.EMPTY;

    public PrimeAggregatorScreen(PrimeAggregatorMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        updateDisplayedRecipes("");

        if(AFFINITIES.size() == 0) {
            AFFINITIES.put(ENDER, new Vector3(ENDER.getColor()[0]/255f, ENDER.getColor()[1]/255f, ENDER.getColor()[2]/255f));
            AFFINITIES.put(EARTH, new Vector3(EARTH.getColor()[0]/255f, EARTH.getColor()[1]/255f, EARTH.getColor()[2]/255f));
            AFFINITIES.put(WATER, new Vector3(WATER.getColor()[0]/255f, WATER.getColor()[1]/255f, WATER.getColor()[2]/255f));
            AFFINITIES.put(WIND, new Vector3(1f, 1f, 1f));
            AFFINITIES.put(FIRE, new Vector3(FIRE.getColor()[0]/255f, FIRE.getColor()[1]/255f, FIRE.getColor()[2]/255f));
            AFFINITIES.put(ARCANE, new Vector3(168/255f, 94/255f, 214/255f));
        }
    }

    @Override
    protected void init() {
        super.init();
        initializeRecipeSelectorButtons();
        initializeRecipeFilterBox();
        itemHandler = menu.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().get();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if(!this.recipeFilterBox.isFocused()) {
            boolean isNumber = (pKeyCode >= 48) && (pKeyCode <= 57);
            boolean isNumpadNumber = (pKeyCode >= 97) && (pKeyCode <= 105);

            if(isNumber || isNumpadNumber) return false;
        }

        if (pKeyCode == InputConstants.KEY_ESCAPE) {
            this.onClose();
            return true;
        } else if (this.recipeFilterBox.keyPressed(pKeyCode, pScanCode, pModifiers)) {
            return true;
        } else {
            return this.recipeFilterBox.isFocused() && this.recipeFilterBox.isVisible() || super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    private void initializeRecipeSelectorButtons(){
        int c = 0;
        for(int y=0; y<5; y++) {
            for(int x=0; x<3; x++) {
                recipeSelectButtons[c] = new ButtonData(this.addRenderableWidget(new PrimeAggregatorButtonRecipeSelector(
                        this, c, this.leftPos, this.topPos, 18, 18, 46, 220, TEXTURE, button -> {

                    PrimeAggregatorScreen query = (PrimeAggregatorScreen) ((PrimeAggregatorButtonRecipeSelector) button).getScreen();
                    query.setActiveRecipe(((PrimeAggregatorButtonRecipeSelector) button).getArrayIndex());
                })), x*18 - 106, y*18 + 19);
                c++;
            }
        }

        int x = this.leftPos + 68;
        int y = this.topPos + 62;
        new ButtonData(this.addRenderableWidget(new PrimeAggregatorButtonRecipeSelector(
                this, c, x, y, 9, 9, 37, 220, TEXTURE, button -> {

            PrimeAggregatorScreen query = (PrimeAggregatorScreen) ((PrimeAggregatorButtonRecipeSelector) button).getScreen();
            query.clearActiveRecipe();
        })), x, y);

        renderButtons();
    }

    private void initializeRecipeFilterBox() {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        this.recipeFilterBox = new EditBox(Minecraft.getInstance().font, x, y, 65, 16, Component.empty()) {
            @Override
            public boolean charTyped(char pCodePoint, int pModifiers) {
                recipesChanged = true;
                recipeFilterRow = 0;
                return super.charTyped(pCodePoint, pModifiers);
            }

            @Override
            public void deleteChars(int pNum) {
                recipesChanged = true;
                recipeFilterRow = 0;
                super.deleteChars(pNum);
            }

            @Override
            public void deleteWords(int pNum) {
                recipesChanged = true;
                recipeFilterRow = 0;
                super.deleteWords(pNum);
            }
        };
        this.recipeFilterBox.setMaxLength(60);
        this.recipeFilterBox.setFocused(false);
        this.recipeFilterBox.setCanLoseFocus(false);
        this.setFocused(this.recipeFilterBox);

        renderFilterBox();
    }

    public void setActiveRecipe(int index) {
        int trueIndex = recipeFilterRow*3 + index;
        if(trueIndex < filteredRecipes.size()) {
            PacketRegistry.sendToServer(new DeviceRecipeSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    filteredRecipes.get(trueIndex).getItem()
            ));
        }
    }

    public void clearActiveRecipe() {
        menu.blockEntity.clearRecipeAfterNextProcess = true;
        PacketRegistry.sendToServer(new DeviceRecipeClearC2SPacket(
                menu.blockEntity.getBlockPos()
        ));
    }

    private List<ItemStack> filteredRecipes = new ArrayList<>();
    private int recipeFilterRow, recipeFilterRowTotal;
    private void updateDisplayedRecipes(String filter) {
        List<ExaltationRecipe> exaltationRecipeList = getAllRecipes();
        filteredRecipes.clear();

        for(ExaltationRecipe fsr : exaltationRecipeList) {
            String display = fsr.getResultItem().getDisplayName().getString();
            if((Objects.equals(filter, "") || display.toLowerCase().contains(filter.toLowerCase()))) {
                filteredRecipes.add(fsr.getResultItem());
            }
        }

        recipeFilterRowTotal = (int)Math.ceil(filteredRecipes.size() / 3d);

        recipesChanged = false;
    }

    private List<ExaltationRecipe> allRecipes = new ArrayList<>();
    @NotNull
    private List<ExaltationRecipe> getAllRecipes() {
        if(allRecipes.size() == 0) {
            List<ExaltationRecipe> raw = menu.blockEntity.getLevel().getRecipeManager().getAllRecipesFor(ExaltationRecipe.Type.INSTANCE);
            Object[] sortable = raw.toArray();
            Arrays.sort(sortable, Comparator.comparing(o -> ((ExaltationRecipe)o).getResultItem().getDisplayName().getString()));
            for (Object o : sortable) {
                allRecipes.add((ExaltationRecipe) o);
            }
        }

        return allRecipes;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Inventory
        gui.blit(TEXTURE_EXT, x, y + 102, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        //Recipe Picker
        gui.blit(TEXTURE_EXT, x - 113, y - 10, 176, 0, 80, 126);

        //Main Panel
        gui.blit(TEXTURE, x + 38, y - 10, 0, 0, 102, 102);

        int animStage = menu.blockEntity.getAnimStage();
        int blinkLevel = (animStage / 2) + 1;
        int solidLevel = blinkLevel - 1;
        float rgb = 1f;
        //Items
        if(solidLevel >= 1) {
            gui.blit(TEXTURE, x + 45, y + 10, 184, 7, 12, 14);
        } else if(blinkLevel == 1) {
            if(menu.blockEntity.getLevel().getGameTime() % 40 < 20)
                gui.blit(TEXTURE, x + 45, y + 10, 184, 7, 12, 14);
        }
        rgb = (animStage == ANIM_STAGE_IDLE || animStage == ANIM_STAGE_GATHERING_ITEMS) ? 1.0f : 0.5f;
        gui.setColor(rgb, rgb, rgb, 1.0f);
        gui.blit(TEXTURE, x - 30, y - 31, 0, 155, 64, 53);
        gui.setColor(1f, 1f, 1f, 1f);
//        gui.blit(TEXTURE, x - 23, y - 3, 166, 21, 50, 18);

        //Materia
        if(solidLevel >= 2) {
            gui.blit(TEXTURE, x + 45, y + 26, 196, 7, 12, 14);
        } else if(blinkLevel == 2 && animStage % 2 != 0) {
            if(menu.blockEntity.getLevel().getGameTime() % 40 < 20)
                gui.blit(TEXTURE, x + 45, y + 26, 196, 7, 12, 14);
        }
        rgb = animStage == ANIM_STAGE_GATHERING_MATERIA ? 1.0f : 0.5f;
        gui.setColor(rgb, rgb, rgb, 1.0f);
        gui.blit(TEXTURE, x + 144, y - 31, 0, 102, 64, 53);
        if(itemHandler.getStackInSlot(SLOT_BOTTLES_OUTPUT).isEmpty()) {
            gui.blit(TEXTURE, x + 183, y - 24, 166, 0, 18, 18);
        }
        gui.setColor(1f, 1f, 1f, 1f);
//        gui.blit(TEXTURE, x + 151, y - 3, 166, 21, 50, 18);

        //Slurry
        if(solidLevel >= 3) {
            gui.blit(TEXTURE, x + 45, y + 42, 208, 7, 12, 14);
        } else if(blinkLevel == 3 && animStage % 2 != 0) {
            if(menu.blockEntity.getLevel().getGameTime() % 40 < 20)
                gui.blit(TEXTURE, x + 45, y + 42, 208, 7, 12, 14);
        }
        rgb = animStage == ANIM_STAGE_GATHERING_SLURRY ? 1.0f : 0.5f;
        gui.setColor(rgb, rgb, rgb, 1.0f);
        gui.blit(TEXTURE, x + 144, y + 26, 102, 73, 64, 73);
        gui.setColor(1f, 1f, 1f, 1f);
//        gui.blit(TEXTURE, x + 151, y + 74, 166, 21, 50, 18);

        //Eldrin
        if(solidLevel >= 4) {
            gui.blit(TEXTURE, x + 45, y + 59, 220, 7, 12, 14);
        } else if(blinkLevel == 4 && animStage % 2 != 0) {
            if(menu.blockEntity.getLevel().getGameTime() % 40 < 20)
                gui.blit(TEXTURE, x + 45, y + 59, 220, 7, 12, 14);
        }
        rgb = animStage == ANIM_STAGE_GATHERING_ELDRIN ? 1.0f : 0.5f;
        gui.setColor(rgb, rgb, rgb, 1.0f);
        gui.blit(TEXTURE, x - 30, y + 26, 102, 0, 64, 73);
        gui.setColor(1f, 1f, 1f, 1f);
//        gui.blit(TEXTURE, x - 23, y + 74, 166, 21, 50, 18);

        //Progress bar
        int sp = menu.blockEntity.getScaledProgress();
        if(sp > 0)
            gui.blit(TEXTURE, x+74, y+27, 0, 228, sp, 28);

        //Secondary progress bars
        gui.setColor(0.1686f, 0.4431f, 0.6863f, 1.0f);
        int si = menu.blockEntity.getScaledItems();
        if(si > 0) {
            gui.blit(TEXTURE, x - 21, y + 11, 210, 254, si, 2);
        }
        int sm = menu.blockEntity.getScaledMateria();
        if(sm > 0) {
            gui.blit(TEXTURE, x + 153, y + 11, 210, 254, sm, 2);
        }
        int ss = menu.blockEntity.getScaledSlurry();
        if(ss > 0) {
            gui.blit(TEXTURE, x + 153, y + 88, 210, 254, ss, 2);
        }
        int se = menu.blockEntity.getScaledEldrin();
        if(se > 0) {
            gui.blit(TEXTURE, x - 21, y + 88, 210, 254, se, 2);
        }
        int xShift = 0;
        if(menu.blockEntity.getCurrentRecipe() != null){
            for (Affinity affinity : AFFINITIES_ORDERED) {
                if (menu.blockEntity.getCurrentRecipe().usesEldrinType(affinity)) {
                    int ses = menu.blockEntity.getScaledEldrinSingle(affinity);
                    gui.setColor(AFFINITIES.get(affinity).x, AFFINITIES.get(affinity).y, AFFINITIES.get(affinity).z, 1f);

                    gui.blit(TEXTURE, x - 20 + xShift, y + 62 - ses, 253, 226, 3, ses);
                }
                xShift += 8;
            }
        }
        gui.setColor(1f, 1f, 1f, 1f);

        //slurry gauge
        int slurryH = PrimeAggregatorBlockEntity.getScaledTankSlurry(menu.blockEntity.getFluidInTank(0).getAmount());
        RenderSystem.setShaderTexture(1, TEXTURE_SLURRY);
        gui.blit(TEXTURE_SLURRY, x + 168, y + 70 - slurryH, 0, 0, 16, slurryH, 16, 16);

        //Scroll Nubbin
        if(recipeFilterRowTotal > 5) {
            float percent = (float)recipeFilterRow / (float)(recipeFilterRowTotal - 5);
            int nubbinShift = (int)Math.floor(percent * 80);
            gui.blit(TEXTURE, x - 48, y + 20 + nubbinShift, 64, 248, 8, 8);
        }

        //Recipe Indicator
        if(menu.blockEntity.getCurrentRecipe() == null) {
            gui.blit(TEXTURE, x + 79, y + 67, 28, 238, 16, 16);
        } else {
            float alpha = menu.blockEntity.clearRecipeAfterNextProcess ? 0.5f : 1.0f;
            gui.setColor(1,1,1, alpha);
            gui.renderItem(menu.blockEntity.getCurrentRecipe().getResultItem(), x + 80, y + 68);
            gui.renderItemDecorations(Minecraft.getInstance().font, menu.blockEntity.getCurrentRecipe().getResultItem(), x + 80, y + 68);
            gui.setColor(1,1,1, 1);
        }

        //Ghosts
        if(menu.blockEntity.getCurrentRecipe() != null) {
            if(displayItemStack.getItem() != menu.blockEntity.getCurrentRecipe().getItemType()) displayItemStack = new ItemStack(menu.blockEntity.getCurrentRecipe().getItemType());
            if(displayMateriaStack.getItem() != menu.blockEntity.getCurrentRecipe().getItemType()) displayMateriaStack = new ItemStack(menu.blockEntity.getCurrentRecipe().getMateriaType());

            gui.setColor(1f, 1f, 1f, 0.25f);
            gui.renderFakeItem(displayItemStack, x - 6, y - 23);
            if(displayItemStack.getItem() instanceof BlockItem) {
                gui.fill(RenderType.guiGhostRecipeOverlay(), x - 6, y - 23, x - 6 + 16, y - 23 + 16, 0xff8b8b8b);
                gui.fill(RenderType.guiGhostRecipeOverlay(), x - 6, y - 23, x - 6 + 16, y - 23 + 16, 0xff8b8b8b);
                gui.fill(RenderType.guiGhostRecipeOverlay(), x - 6, y - 23, x - 6 + 16, y - 23 + 16, 0xff8b8b8b);
                gui.fill(RenderType.guiGhostRecipeOverlay(), x - 6, y - 23, x - 6 + 16, y - 23 + 16, 0xff8b8b8b);
            }
            gui.renderFakeItem(displayMateriaStack, x + 152, y - 23);
            gui.setColor(1f, 1f, 1f, 1f);

            if(menu.blockEntity.getCurrentRecipe().usesEldrinType(ENDER)) gui.blit(TEXTURE, x - 22, y + 64, 184, 0, 7, 7);
            if(menu.blockEntity.getCurrentRecipe().usesEldrinType(EARTH)) gui.blit(TEXTURE, x - 14, y + 64, 191, 0, 7, 7);
            if(menu.blockEntity.getCurrentRecipe().usesEldrinType(WATER)) gui.blit(TEXTURE, x - 6, y + 64, 198, 0, 7, 7);
            if(menu.blockEntity.getCurrentRecipe().usesEldrinType(WIND)) gui.blit(TEXTURE, x + 2, y + 64, 205, 0, 7, 7);
            if(menu.blockEntity.getCurrentRecipe().usesEldrinType(FIRE)) gui.blit(TEXTURE, x + 10, y + 64, 212, 0, 7, 7);
            if(menu.blockEntity.getCurrentRecipe().usesEldrinType(ARCANE)) gui.blit(TEXTURE, x + 18, y + 64, 219, 0, 7, 7);
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        renderBackground(gui);
        super.render(gui, mouseX, mouseY, delta);
        renderTooltip(gui, mouseX, mouseY);
        if(recipesChanged)
            updateDisplayedRecipes(recipeFilterBox == null ? "" : recipeFilterBox.getValue());
        renderRecipeOptions(gui);
        updateFilterBoxContents();
    }

    private void renderSelectedRecipe(GuiGraphics gui, int x, int y) {
        if(menu.blockEntity.getCurrentRecipe() == null) {
            gui.blit(TEXTURE, x, y, 28, 238, 18, 18);
        }
        else {
            float alpha = menu.blockEntity.clearRecipeAfterNextProcess ? 0.5f : 1.0f;
            gui.setColor(1,1,1, alpha);
            gui.renderFakeItem(menu.blockEntity.getCurrentRecipe().getResultItem(), x+1, y+1);
            gui.setColor(1,1,1, 1);
        }
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

    private void renderFilterBox() {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        recipeFilterBox.setX(xOrigin - 105);
        recipeFilterBox.setY(yOrigin - 2);

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

    private void renderRecipeOptions(GuiGraphics gui) {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        List<ItemStack> snipped = new ArrayList<>();
        for(int i = recipeFilterRow*3; i<Math.min(filteredRecipes.size(), recipeFilterRow*3 + 15); i++) {
            snipped.add(filteredRecipes.get(i));
        }

        int c = 0;
        int cLimit = Math.min(15, snipped.size());
        while(c < cLimit) {

            for(int y=0; y<5; y++) {
                for (int x = 0; x < 3; x++) {

                    gui.renderItem(snipped.get(c), xOrigin - 105 + x*18, yOrigin + 20 + y*18);
                    gui.renderItemDecorations(Minecraft.getInstance().font, snipped.get(c), xOrigin - 105 + x*18, yOrigin + 20 + y*18);
                    c++;
                    if(c >= cLimit) break;
                }
                if(c >= cLimit) break;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        if(pMouseX >= x - 77 && pMouseX <= x - 11 &&
                pMouseY >= y + 21 && pMouseY <= y + 114) {
            if (recipeFilterRowTotal > 5) {
                if (pDelta < 0)
                    recipeFilterRow = Math.min(recipeFilterRowTotal - 5, recipeFilterRow + 1);
                else
                    recipeFilterRow = Math.max(0, recipeFilterRow - 1);
            }
        }

        return super.mouseScrolled(pMouseX, pMouseY, pDelta);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if(recipeFilterRowTotal > 5 && pButton == 0) {
            int x = (width - PANEL_MAIN_W) / 2;
            int y = (height - PANEL_MAIN_H) / 2;

            if (pMouseX >= x - 48 && pMouseX <= x - 39 &&
                    pMouseY >= y + 20 && pMouseY <= y + 110) {
                double point = pMouseY - (y + 42);
                double percent = point / 80d;

                recipeFilterRow = Math.max(0, Math.min(recipeFilterRowTotal - 5, (int) Math.round(percent * recipeFilterRowTotal)));
            }
        }

        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if(recipeFilterRowTotal > 5 && pButton == 0) {
            int x = (width - PANEL_MAIN_W) / 2;
            int y = (height - PANEL_MAIN_H) / 2;

            if (pMouseX >= x - 48 && pMouseX <= x - 39 &&
                    pMouseY >= y + 20 && pMouseY <= y + 110) {
                double point = pMouseY - (y + 42);
                double percent = point / 80d;

                recipeFilterRow = Math.max(0, Math.min(recipeFilterRowTotal - 5, (int) Math.round(percent * recipeFilterRowTotal)));
            }
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        final Font font = Minecraft.getInstance().font;

        if(menu.blockEntity.getCurrentRecipe() != null) {
            final Pair<Integer, Integer> items = menu.blockEntity.getItems();
            String itemCounter = ""+(items.getSecond() - items.getFirst());
            gui.drawString(font, itemCounter, 3 - font.width(itemCounter)/2, -6, 0xff000000, false);

            final Pair<Integer, Integer> eldrin = menu.blockEntity.getEldrin();
            String eldrinCounter = ""+(eldrin.getSecond() - eldrin.getFirst());
            gui.drawString(font, eldrinCounter, 3 - font.width(eldrinCounter)/2, 71, 0xff000000, false);

            final Pair<Integer, Integer> materia = menu.blockEntity.getMateria();
            String materiaCounter = ""+(materia.getSecond() - materia.getFirst());
            gui.drawString(font, materiaCounter, 177 - font.width(materiaCounter)/2, -6, 0xff000000, false);

            final Pair<Integer, Integer> slurry = menu.blockEntity.getSlurry();
            String slurryCounter = ""+(slurry.getSecond() - slurry.getFirst());
            gui.drawString(font, slurryCounter, 177 - font.width(slurryCounter)/2, 71, 0xff000000, false);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderTooltip(gui, mouseX, mouseY);

        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Selected recipe
//        if(mouseX >= x+TOOLTIP_SELECTED_RECIPE_X && mouseX <= x+TOOLTIP_SELECTED_RECIPE_X+TOOLTIP_SELECTED_RECIPE_S &&
//                mouseY >= y+TOOLTIP_SELECTED_RECIPE_Y && mouseY <= y+TOOLTIP_SELECTED_RECIPE_Y+TOOLTIP_SELECTED_RECIPE_S) {
//            ItemStack recipeItem = menu.getRecipeItem();
//            if(recipeItem == ItemStack.EMPTY) {
//                tooltipContents.add(Component.translatable("tooltip.magichem.gui.no_selected_recipe").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
//            } else {
//                int slurryCost = Math.round(menu.getCurrentRecipe().getSlurryCost() * ((100f - menu.getReductionRate()) / 100f));
//
//                tooltipContents.addAll(recipeItem.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
//                tooltipContents.add(Component.empty());
//                tooltipContents.add(Component.empty()
//                        .append(Component.translatable("tooltip.magichem.gui.fixation_cost.part1").withStyle(ChatFormatting.DARK_GRAY))
//                        .append(Component.literal(slurryCost+"mB").withStyle(ChatFormatting.DARK_AQUA))
//                        .append(Component.translatable("tooltip.magichem.gui.fixation_cost.part2").withStyle(ChatFormatting.DARK_GRAY))
//                );
//            }
//        }

        //Items in recipe picker
//        if(mouseX >= x+TOOLTIP_RECIPE_ZONE_X && mouseX <= x+TOOLTIP_RECIPE_ZONE_X+TOOLTIP_RECIPE_ZONE_W &&
//                mouseY >= y+TOOLTIP_RECIPE_ZONE_Y && mouseY <= y+TOOLTIP_RECIPE_ZONE_Y+TOOLTIP_RECIPE_ZONE_H) {
//            int mx = mouseX - (x+TOOLTIP_RECIPE_ZONE_X);
//            int my = mouseY - (y+TOOLTIP_RECIPE_ZONE_Y);
//            int id = ((my / 18) * 3) + ((mx / 18) % 3);
//
//            if (id >= 0 && id < 16) {
//                if(id + recipeFilterRow * 3 < filteredRecipes.size()) {
//                    ItemStack stackUnderMouse = filteredRecipes.get(id + recipeFilterRow * 3);
//                    tooltipContents.addAll(stackUnderMouse.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
//                }
//            }
//        }

        //Slurry Bar
//        if(mouseX >= x+TOOLTIP_SLURRY_X && mouseX <= x+TOOLTIP_SLURRY_X+TOOLTIP_SLURRY_W &&
//                mouseY >= y+TOOLTIP_SLURRY_Y && mouseY <= y+TOOLTIP_SLURRY_Y+TOOLTIP_SLURRY_H) {
//
//            tooltipContents.clear();
//            tooltipContents.add(Component.empty()
//                    .append(Component.translatable("tooltip.magichem.gui.slurry.tank").withStyle(ChatFormatting.GOLD))
//                    .append(": ")
//                    .append(Component.translatable("tooltip.magichem.gui.slurry.tank.line1"))
//                    .append(menu.blockEntity.getDisplayName())
//                    .append("."));
//            tooltipContents.add(Component.empty());
//            tooltipContents.add(Component.translatable("tooltip.magichem.gui.slurry.tank.line2a")
//                    .append(Component.literal(ServerConfig.fixationFailureRefund+"%").withStyle(ChatFormatting.DARK_AQUA))
//                    .append(Component.translatable("tooltip.magichem.gui.slurry.tank.line2b")));
//            tooltipContents.add(Component.empty());
//            tooltipContents.add(Component.empty()
//                    .append(Component.translatable("tooltip.magichem.gui.slurry.tank.line3").withStyle(ChatFormatting.DARK_GRAY))
//                    .append(Component.literal(menu.getSlurryInTank()+"mB").withStyle(ChatFormatting.DARK_AQUA)));
//            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
//        }

        gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
    }
}
