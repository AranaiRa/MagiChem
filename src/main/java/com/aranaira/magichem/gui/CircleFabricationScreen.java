package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.CircleFabricationBlockEntity;
import com.aranaira.magichem.foundation.ButtonData;
import com.aranaira.magichem.gui.element.FabricationButtonRecipeSelector;
import com.aranaira.magichem.networking.FabricationSyncDataC2SPacket;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import com.aranaira.magichem.registry.PacketRegistry;
import com.aranaira.magichem.util.render.RenderUtils;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CircleFabricationScreen extends AbstractContainerScreen<CircleFabricationMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fabrication.png");
    private static final ResourceLocation TEXTURE_EXT =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fabrication_ext.png");
    private ImageButton
            b_powerLevelUp, b_powerLevelDown;
    private ButtonData[] recipeSelectButtons = new ButtonData[15];
    private EditBox recipeFilterBox;
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 192,
            PANEL_RECIPE_X = 176, PANEL_RECIPE_Y = 0, PANEL_RECIPE_W = 80, PANEL_RECIPE_H = 126,
            PANEL_POWER_X = 176, PANEL_POWER_Y = 126, PANEL_POWER_W = 80, PANEL_POWER_H = 66,
            PANEL_INGREDIENTS_X = 176, PANEL_INGREDIENTS_Y = 85, PANEL_INGREDIENTS_W = 80,
            PANEL_INGREDIENTS_U1 = 160, PANEL_INGREDIENTS_U2 = 80, PANEL_INGREDIENTS_U3 = 160, PANEL_INGREDIENTS_U4 = 80, PANEL_INGREDIENTS_U5 = 0,
            PANEL_INGREDIENTS_V1 =  66, PANEL_INGREDIENTS_V2 = 84, PANEL_INGREDIENTS_V3 =   0, PANEL_INGREDIENTS_V4 =  0, PANEL_INGREDIENTS_V5 = 0,
            PANEL_INGREDIENTS_H1 =  30, PANEL_INGREDIENTS_H2 = 48, PANEL_INGREDIENTS_H3 =  66, PANEL_INGREDIENTS_H4 = 84, PANEL_INGREDIENTS_H5 = 102;
    private AlchemicalCompositionRecipe lastClickedRecipe = null;

    public CircleFabricationScreen(CircleFabricationMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        initializePowerLevelButtons();
        initializeRecipeSelectorButtons();
        updateDisplayedRecipes("");
        initializeRecipeFilterBox();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if(pKeyCode == InputConstants.KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return this.getFocused() != null && this.getFocused().keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    private void initializeRecipeFilterBox() {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        this.recipeFilterBox = new EditBox(Minecraft.getInstance().font, x, y, 67, 18, Component.empty());
        this.recipeFilterBox.setMaxLength(60);
        this.recipeFilterBox.setFocused(false);
        this.recipeFilterBox.setCanLoseFocus(false);
        this.setFocused(this.recipeFilterBox);
    }

    private void initializePowerLevelButtons(){
        b_powerLevelUp = this.addRenderableWidget(new ImageButton(this.leftPos + 180, this.topPos + 126, 12, 7, 232, 242, TEXTURE, button -> {
            menu.blockEntity.incrementPowerUsageSetting();
            Item recipeTarget = null;
            if(menu.blockEntity.getCurrentRecipe() != null) {
                recipeTarget = menu.blockEntity.getCurrentRecipe().getAlchemyObject().getItem();
            }
            PacketRegistry.sendToServer(new FabricationSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    recipeTarget,
                    menu.blockEntity.getPowerUsageSetting()
            ));
        }));
        b_powerLevelDown = this.addRenderableWidget(new ImageButton(180, 126, 12, 7, 244, 242, TEXTURE, button -> {
            menu.blockEntity.decrementPowerUsageSetting();
            Item recipeTarget = null;
            if(menu.blockEntity.getCurrentRecipe() != null) {
                recipeTarget = menu.blockEntity.getCurrentRecipe().getAlchemyObject().getItem();
            }
            PacketRegistry.sendToServer(new FabricationSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    recipeTarget,
                    menu.blockEntity.getPowerUsageSetting()
            ));
        }));
    }

    private void initializeRecipeSelectorButtons(){
        int c = 0;
        for(int y=0; y<5; y++) {
            for(int x=0; x<3; x++) {
                recipeSelectButtons[c] = new ButtonData(this.addRenderableWidget(new FabricationButtonRecipeSelector(
                        this, c, this.leftPos, this.topPos, 18, 18, 92, 220, TEXTURE, button -> {

                            CircleFabricationScreen query = (CircleFabricationScreen) ((FabricationButtonRecipeSelector) button).getScreen();
                            query.setActiveRecipe(((FabricationButtonRecipeSelector) button).getArrayIndex());
                })), x*18 - 71, y*18 + 42);
                c++;
            }
        }
    }

    public void setActiveRecipe(int index) {
        int trueIndex = recipeFilterRow*3 + index;
        if(trueIndex < filteredRecipes.size()) {
            PacketRegistry.sendToServer(new FabricationSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    filteredRecipes.get(trueIndex).getAlchemyObject().getItem(),
                    menu.blockEntity.getPowerUsageSetting()
            ));
            lastClickedRecipe = filteredRecipes.get(trueIndex);
            menu.setInputSlotFilters(lastClickedRecipe);
            menu.blockEntity.setCurrentRecipeByOutput(lastClickedRecipe.getAlchemyObject().getItem());
        }
    }

    private static List<AlchemicalCompositionRecipe> filteredRecipes = new ArrayList<>();
    private int recipeFilterRow, recipeFilterRowTotal;
    private void updateDisplayedRecipes(String filter) {
        List<AlchemicalCompositionRecipe> fabricationRecipeOutputs = menu.blockEntity.getLevel().getRecipeManager().getAllRecipesFor(AlchemicalCompositionRecipe.Type.INSTANCE);
        filteredRecipes.clear();

        for(AlchemicalCompositionRecipe acr : fabricationRecipeOutputs) {
            String display = acr.getAlchemyObject().getDisplayName().getString();
            if((Objects.equals(filter, "") || display.toLowerCase().contains(filter.toLowerCase())) && !acr.getIsDistillOnly()) {
                filteredRecipes.add(acr);
            }
        }

        recipeFilterRowTotal = filteredRecipes.size() / 3;
    }

    @Override
    protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Panels
        gui.blit(TEXTURE, x - 78, y + 13, PANEL_RECIPE_X, PANEL_RECIPE_Y, PANEL_RECIPE_W, PANEL_RECIPE_H);
        gui.blit(TEXTURE, x + 176, y + 19, PANEL_POWER_X, PANEL_POWER_Y, PANEL_POWER_W, PANEL_POWER_H);
        gui.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        renderProgressBar(gui, x + 55, y + 12);

        //RenderSystem.setShader(GameRenderer::getBlockShader);
        renderSelectedRecipe(gui, x + 79, y + 79);

        //RenderSystem.setShader(GameRenderer::getPositionTexShader);
        renderPowerLevelBar(gui, x + 182, y + 37);
        renderIngredientPanel(gui, x + PANEL_INGREDIENTS_X, y + PANEL_INGREDIENTS_Y);

        renderSlotGhosts(gui);

        if(!menu.getHasSufficientPower()) {
            RenderSystem.setShaderTexture(0, TEXTURE_EXT);
            renderPowerWarning(gui, x, y);
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        renderBackground(gui);
        super.render(gui, mouseX, mouseY, delta);
        renderTooltip(gui, mouseX, mouseY);
        renderButtons(gui, delta, mouseX, mouseY);
        renderFilterBox();
        updateDisplayedRecipes(recipeFilterBox.getValue());
        renderRecipeOptions(gui);
    }

    private void renderIngredientPanel(GuiGraphics gui, int x, int y) {
        if(menu.blockEntity.getCurrentRecipe() != null) {
            AlchemicalCompositionRecipe recipe = menu.blockEntity.getCurrentRecipe();

            //A switch statement doesn't work here and I have no idea why.
            if(recipe.getComponentMateria().size() == 1) {
                gui.blit(TEXTURE_EXT, x, y, PANEL_INGREDIENTS_U1, PANEL_INGREDIENTS_V1, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H1);
            }
            else if(recipe.getComponentMateria().size() == 2) {
                gui.blit(TEXTURE_EXT, x, y, PANEL_INGREDIENTS_U2, PANEL_INGREDIENTS_V2, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H2);
            }
            else if(recipe.getComponentMateria().size() == 3) {
                gui.blit(TEXTURE_EXT, x, y, PANEL_INGREDIENTS_U3, PANEL_INGREDIENTS_V3, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H3);
            }
            else if(recipe.getComponentMateria().size() == 4) {
                gui.blit(TEXTURE_EXT, x, y, PANEL_INGREDIENTS_U4, PANEL_INGREDIENTS_V4, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H4);
            }
            else if(recipe.getComponentMateria().size() == 5) {
                gui.blit(TEXTURE_EXT, x, y, PANEL_INGREDIENTS_U5, PANEL_INGREDIENTS_V5, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H5);
            }

            for(int i=0; i<recipe.getComponentMateria().size(); i++) {
                gui.renderItem(recipe.getComponentMateria().get(i), x+4, y+7 + i*18);
            }
        }

        RenderSystem.setShaderTexture(0, TEXTURE);
    }

    private void renderButtons(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        b_powerLevelUp.setPosition(x+180, y+26);
        b_powerLevelUp.renderWidget(gui, mouseX, mouseY, partialTick);
        b_powerLevelUp.active = true;
        b_powerLevelUp.visible = true;

        b_powerLevelDown.setPosition(x+180, y+71);
        b_powerLevelDown.renderWidget(gui, mouseX, mouseY, partialTick);
        b_powerLevelDown.active = true;
        b_powerLevelDown.visible = true;

        for(ButtonData bd : recipeSelectButtons) {
            bd.getButton().setPosition(x+bd.getXOffset(), y+bd.getYOffset());
            bd.getButton().renderWidget(gui, mouseX, mouseY, partialTick);
            bd.getButton().active = true;
            bd.getButton().visible = true;
        }
    }

    private void renderPowerLevelBar(GuiGraphics gui, int x, int y) {
        int powerLevel = menu.blockEntity.getPowerUsageSetting();

        gui.blit(TEXTURE, x, y + (30 - powerLevel), 84, 256 - powerLevel, 8, powerLevel);
    }

    private void renderSelectedRecipe(GuiGraphics gui, int x, int y) {
        if(menu.blockEntity.getCurrentRecipe() == null) {
            gui.blit(TEXTURE, x, y, 66, 238, 18, 18);
        }
        else {
            gui.renderItem(menu.blockEntity.getCurrentRecipe().getAlchemyObject(), x+1, y+1);
        }
    }

    private void renderProgressBar(GuiGraphics gui, int x, int y) {
        int sp = CircleFabricationBlockEntity.getScaledProgress(menu.blockEntity);
        if(sp > 0)
            gui.blit(TEXTURE, x, y , 0, 199, sp, CircleFabricationBlockEntity.PROGRESS_BAR_HEIGHT);
    }

    private void renderFilterBox() {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        recipeFilterBox.setX(xOrigin - 70);
        recipeFilterBox.setY(yOrigin + 21);

        if(recipeFilterBox.getValue().isEmpty())
            recipeFilterBox.setSuggestion(Component.translatable("gui.magichem.typetofilter").getString());
        else
            recipeFilterBox.setSuggestion("");

        addRenderableWidget(recipeFilterBox);
    }

    private void renderSlotGhosts(GuiGraphics gui) {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        if(menu.blockEntity.getCurrentRecipe() == null)
            return;

        AlchemicalCompositionRecipe acr = menu.blockEntity.getCurrentRecipe();

        gui.setColor(1f, 1f, 1f, 0.25f);
        int slotGroup = 0;
        for(ItemStack stack : acr.getComponentMateria()) {
            gui.renderItem(stack, xOrigin + 8, yOrigin+8 + (18*slotGroup));
            gui.renderItem(stack, xOrigin + 26, yOrigin+8 + (18*slotGroup));
            slotGroup++;
        }
        gui.setColor(1f, 1f, 1f, 1f);
    }

    private void renderRecipeOptions(GuiGraphics gui) {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        List<AlchemicalCompositionRecipe> snipped = new ArrayList<>();
        for(int i=recipeFilterRow*3; i<Math.min(filteredRecipes.size(), recipeFilterRow*3 + 15); i++) {
            snipped.add(filteredRecipes.get(i));
        }

        int c = 0;
        int cLimit = Math.min(15, snipped.size());
        while(c < cLimit) {

            for(int y=0; y<5; y++) {
                for (int x = 0; x < 3; x++) {
                    gui.renderItem(snipped.get(c).getAlchemyObject(), xOrigin-70 + x*18, yOrigin+43 + y*18);
                    c++;
                    if(c >= cLimit) break;
                }
                if(c >= cLimit) break;
            }
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        super.renderTooltip(pGuiGraphics, pX, pY);

        //do special tooltips here
    }

    protected void renderPowerWarning(GuiGraphics gui, int x, int y) {
        long cycle = Minecraft.getInstance().level.getGameTime() % 20;

        gui.blit(TEXTURE_EXT, x+10, y-30, 0, 230, 156, 26);
        if(cycle < 10) {
            gui.blit(TEXTURE_EXT, x + 17, y - 23, 156, 244, 12, 12);
            gui.blit(TEXTURE_EXT, x + 147, y - 23, 156, 244, 12, 12);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int x, int y) {
        int powerDraw = menu.blockEntity.getPowerDraw();
        int secWhole = menu.blockEntity.getOperationTicks() / 20;
        int secPartial = (menu.blockEntity.getOperationTicks() % 20) * 5;

        Font font = Minecraft.getInstance().font;
        gui.drawString(font ,powerDraw+"/t", 208, 26, 0xff000000, false);
        gui.drawString(font ,secWhole+"."+(secPartial < 10 ? "0"+secPartial : secPartial)+" s", 208, 45, 0xff000000, false);

        AlchemicalCompositionRecipe recipe = menu.blockEntity.getCurrentRecipe();
        if(recipe != null) {
            for (int i = 0; i < recipe.getComponentMateria().size(); i++) {
                Component text = Component.literal(recipe.getComponentMateria().get(i).getCount() + " x ")
                        .append(Component.translatable("item."+MagiChemMod.MODID+"."+recipe.getComponentMateria().get(i).getItem()+".short"));
                gui.pose().scale(0.5f, 0.5f, 0.5f);
                gui.drawString(font, text, 400, 169 + i * 36, 0xff000000, false);
                gui.pose().scale(2.0f, 2.0f, 2.0f);
            }
        }

        if(!menu.getHasSufficientPower()) {
            MutableComponent warningText = Component.translatable("gui.magichem.insufficientpower");
            int width = Minecraft.getInstance().font.width(warningText.getString());
            gui.drawString(font, warningText, 89 - width/2, -33, 0xff000000, false);
        }
    }
}
