package com.aranaira.magichem.gui;

import com.aranaira.magichem.block.entity.GrandFuseryBlockEntity;
import com.aranaira.magichem.config.ServerConfig;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.GrandCentrifugeBlockEntity;
import com.aranaira.magichem.foundation.ButtonData;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.gui.element.GrandCentrifugeButtonRecipeSelector;
import com.aranaira.magichem.gui.element.GrandFuseryButtonRecipeSelector;
import com.aranaira.magichem.networking.DeviceRecipeClearC2SPacket;
import com.aranaira.magichem.networking.DeviceRecipeSyncDataC2SPacket;
import com.aranaira.magichem.networking.GrandDeviceSyncDataC2SPacket;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.aranaira.magichem.registry.PacketRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GrandCentrifugeScreen extends AbstractContainerScreen<GrandCentrifugeMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_grand_centrifuge.png");
    private static final ResourceLocation TEXTURE_GDIST =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_grand_distillery.png");
    private static final ResourceLocation TEXTURE_EXT =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fabrication_ext.png");
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 192,
            PANEL_GRIME_X = 176, PANEL_GRIME_Y = 14, PANEL_GRIME_W = 80, PANEL_GRIME_H = 80, PANEL_GRIME_U = 176, PANEL_GRIME_V = 0,
            PANEL_RECIPE_X = -84, PANEL_RECIPE_Y = -7, PANEL_RECIPE_U = 176, PANEL_RECIPE_W = 80, PANEL_RECIPE_H = 126,
            TOOLTIP_SELECTED_RECIPE_X = 79, TOOLTIP_SELECTED_RECIPE_Y = 79, TOOLTIP_SELECTED_RECIPE_S = 18,
            TOOLTIP_EFFICIENCY_X = 193, TOOLTIP_EFFICIENCY_Y = 22, TOOLTIP_EFFICIENCY_W = 59, TOOLTIP_EFFICIENCY_H = 15,
            TOOLTIP_POWERUSAGE_X = 193, TOOLTIP_POWERUSAGE_Y = 39, TOOLTIP_POWERUSAGE_W = 59, TOOLTIP_POWERUSAGE_H = 15,
            TOOLTIP_OPERATIONTIME_X = 193, TOOLTIP_OPERATIONTIME_Y = 56, TOOLTIP_OPERATIONTIME_W = 59, TOOLTIP_OPERATIONTIME_H = 15,
            TOOLTIP_GRIME_X = 180, TOOLTIP_GRIME_Y = 77, TOOLTIP_GRIME_W = 69, TOOLTIP_GRIME_H = 10,
            TOOLTIP_RECIPE_ZONE_X = -77, TOOLTIP_RECIPE_ZONE_Y = 22, TOOLTIP_RECIPE_ZONE_W = 54, TOOLTIP_RECIPE_ZONE_H = 90;
    private final ButtonData[] recipeSelectButtons = new ButtonData[15];
    private EditBox recipeFilterBox;
    private ImageButton
            b_powerLevelUp, b_powerLevelDown;
    private FixationSeparationRecipe lastRecipe = null;
    private NonNullList<ItemStack> lastRecipeComponentMateria = NonNullList.create();
    private ItemStack lastRecipeResultAdmixture = ItemStack.EMPTY;
    private String lastUsedFilter = null;

    public GrandCentrifugeScreen(GrandCentrifugeMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        updateDisplayedRecipes("");
    }

    private Triplet<FixationSeparationRecipe, NonNullList<ItemStack>, ItemStack> getOrUpdateRecipe(){
        ItemStack recipeItemQuery = menu.blockEntity.getRecipeItem();
        if(lastRecipeResultAdmixture.getItem() != recipeItemQuery.getItem()) {
            if(recipeItemQuery.isEmpty()) {
                lastRecipe = null;
                lastRecipeResultAdmixture = ItemStack.EMPTY;
                lastRecipeComponentMateria = NonNullList.create();
            } else {
                lastRecipe = menu.getCurrentRecipe();
                if(lastRecipe != null) {
                    lastRecipeResultAdmixture = menu.getCurrentRecipe().getResultAdmixture().copy();
                    lastRecipeComponentMateria = NonNullList.create();
                    for (ItemStack is : menu.getCurrentRecipe().getComponentMateria()) {
                        lastRecipeComponentMateria.add(is.copy());
                    }
                }
            }
        }
        return new Triplet<>(lastRecipe, lastRecipeComponentMateria, lastRecipeResultAdmixture);
    }

    @Override
    protected void init() {
        super.init();
        initializePowerLevelButtons();
        initializeRecipeSelectorButtons();
        initializeRecipeFilterBox();
    }

    private void initializePowerLevelButtons(){
        b_powerLevelUp = this.addRenderableWidget(new ImageButton(this.leftPos + 180, this.topPos + 8, 12, 7, 232, 242, TEXTURE_GDIST, button -> {
            menu.blockEntity.incrementPowerUsageSetting();
            PacketRegistry.sendToServer(new GrandDeviceSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    menu.blockEntity.getPowerUsageSetting()
            ));
        }));
        b_powerLevelDown = this.addRenderableWidget(new ImageButton(this.leftPos + 180, this.topPos + 53, 12, 7, 244, 242, TEXTURE_GDIST, button -> {
            menu.blockEntity.decrementPowerUsageSetting();
            PacketRegistry.sendToServer(new GrandDeviceSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    menu.blockEntity.getPowerUsageSetting()
            ));
        }));
    }

    private void initializeRecipeSelectorButtons(){
        int c = 0;
        for(int y=0; y<5; y++) {
            for(int x=0; x<3; x++) {
                recipeSelectButtons[c] = new ButtonData(this.addRenderableWidget(new GrandCentrifugeButtonRecipeSelector(
                        this, c, this.leftPos, this.topPos, 18, 18, 42, 220, TEXTURE, button -> {

                    GrandCentrifugeScreen query = (GrandCentrifugeScreen) ((GrandCentrifugeButtonRecipeSelector) button).getScreen();
                    query.setActiveRecipe(((GrandCentrifugeButtonRecipeSelector) button).getArrayIndex());
                })), x*18 - 77, y*18 + 22);
                c++;
            }
        }

        int x = this.leftPos + 68;
        int y = this.topPos + 66;
        new ButtonData(this.addRenderableWidget(new GrandCentrifugeButtonRecipeSelector(
                this, c, x, y, 9, 9, 33, 220, TEXTURE, button -> {

            GrandCentrifugeScreen query = (GrandCentrifugeScreen) ((GrandCentrifugeButtonRecipeSelector) button).getScreen();
            query.clearActiveRecipe();
        })), x, y);

        renderButtons();
    }

    private void initializeRecipeFilterBox() {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        this.recipeFilterBox = new EditBox(Minecraft.getInstance().font, x, y, 65, 16, Component.empty());
        this.recipeFilterBox.setMaxLength(60);

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
        if (Objects.equals(filter, lastUsedFilter)) return;
        lastUsedFilter = filter;

        List<FixationSeparationRecipe> fixationRecipeOutputs = getAllRecipes();
        filteredRecipes.clear();

        for(FixationSeparationRecipe fsr : fixationRecipeOutputs) {
            String display = fsr.getResultAdmixture().getDisplayName().getString();
            if((Objects.equals(filter, "") || display.toLowerCase().contains(filter.toLowerCase()))) {
                filteredRecipes.add(fsr.getResultAdmixture());
            }
        }

        recipeFilterRowTotal = (int)Math.ceil(filteredRecipes.size() / 3d);
    }

    private List<FixationSeparationRecipe> allRecipes = new ArrayList<>();
    @NotNull
    private List<FixationSeparationRecipe> getAllRecipes() {
        if(allRecipes.size() == 0) {
            List<FixationSeparationRecipe> raw = menu.blockEntity.getLevel().getRecipeManager().getAllRecipesFor(FixationSeparationRecipe.Type.INSTANCE);
            Object[] sortable = raw.toArray();
            Arrays.sort(sortable, Comparator.comparing(o -> ((FixationSeparationRecipe)o).getResultAdmixture().getDisplayName().getString()));
            for (Object o : sortable) {
                allRecipes.add((FixationSeparationRecipe) o);
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

        gui.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        renderGrimePanel(gui, x + PANEL_GRIME_X, y + PANEL_GRIME_Y);

        gui.blit(TEXTURE, x + PANEL_RECIPE_X, y + PANEL_RECIPE_Y, PANEL_RECIPE_U, 0, PANEL_RECIPE_W, PANEL_RECIPE_H);

        renderSelectedRecipe(gui, x + 79, y + 79);

        int sProg = GrandCentrifugeBlockEntity.getScaledProgress(menu.getProgress(), menu.getGrime(), menu.getBatchSize(), menu.getOperationTimeMod(), GrandCentrifugeBlockEntity::getVar, menu.blockEntity::getPoweredOperationTime);
        if(sProg > 0)
            gui.blit(TEXTURE, x+76, y+38, 0, 228, sProg, 28);

        int powerLevel = menu.blockEntity.getPowerUsageSetting();
        gui.blit(TEXTURE_GDIST, x+182, y + (62 - powerLevel), 24, 248 - powerLevel, 8, powerLevel);

        int sGrime = GrandCentrifugeBlockEntity.getScaledGrime(menu.getGrime());
        if(sGrime > 0)
            gui.blit(TEXTURE_GDIST, x+181, y+78, 24, 248, sGrime, 8);

//        renderSlotGhosts(gui);

        //Scroll Nubbin
        if(recipeFilterRowTotal > 5) {
            float percent = (float)recipeFilterRow / (float)(recipeFilterRowTotal - 5);
            int nubbinShift = (int)Math.floor(percent * 80);
            gui.blit(TEXTURE, x - 19, y + 23 + nubbinShift, 60, 240, 8, 8);
        }

        if(!menu.blockEntity.getPowerSufficiency()) {
            renderPowerWarning(gui, x, y);
        }


    }

    private void renderGrimePanel(GuiGraphics gui, int x, int y) {
        gui.blit(TEXTURE_GDIST, x, y, PANEL_GRIME_U, PANEL_GRIME_V, PANEL_GRIME_W, PANEL_GRIME_H);
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
    public void render(GuiGraphics gui, int mouseX, int mouseY, float delta) {
        renderBackground(gui);
        super.render(gui, mouseX, mouseY, delta);
        renderTooltip(gui, mouseX, mouseY);
        updateDisplayedRecipes(recipeFilterBox == null ? "" : recipeFilterBox.getValue());
        renderRecipeOptions(gui);
        updateFilterBoxContents();
    }

    private void renderSlotGhosts(GuiGraphics gui) {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        Triplet<FixationSeparationRecipe, NonNullList<ItemStack>, ItemStack> recipeCompound = getOrUpdateRecipe();

        if(recipeCompound.getFirst() == null)
            return;

        gui.setColor(1f, 1f, 1f, 0.25f);
        int slotGroup = 0;
        for(ItemStack stack : recipeCompound.getSecond()) {
            gui.renderItem(stack, xOrigin+26, yOrigin+23 + (18*slotGroup));
            gui.renderItem(stack, xOrigin+44, yOrigin+23 + (18*slotGroup));
            slotGroup++;
        }
        gui.setColor(1f, 1f, 1f, 1f);
    }

    private void renderSelectedRecipe(GuiGraphics gui, int x, int y) {
        Triplet<FixationSeparationRecipe, NonNullList<ItemStack>, ItemStack> recipeCompound = getOrUpdateRecipe();

        if(recipeCompound.getThird() == ItemStack.EMPTY) {
            gui.blit(TEXTURE, x, y, 24, 238, 18, 18);
        }
        else {
            float alpha = menu.blockEntity.clearRecipeAfterNextProcess ? 0.5f : 1.0f;
            gui.setColor(1,1,1, alpha);
            gui.renderFakeItem(recipeCompound.getThird(), x+1, y+1);
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

        recipeFilterBox.setX(xOrigin - 76);
        recipeFilterBox.setY(yOrigin + 1);

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

                    gui.renderItem(snipped.get(c), xOrigin - 76 + x*18, yOrigin + 23 + y*18);
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

            if (pMouseX >= x - 20 && pMouseX <= x - 11 &&
                    pMouseY >= y + 25 && pMouseY <= y + 115) {
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

            if (pMouseX >= x - 20 && pMouseX <= x - 11 &&
                    pMouseY >= y + 25 && pMouseY <= y + 115) {
                double point = pMouseY - (y + 42);
                double percent = point / 80d;

                recipeFilterRow = Math.max(0, Math.min(recipeFilterRowTotal - 5, (int) Math.round(percent * recipeFilterRowTotal)));
            }
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    protected void renderTooltip(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderTooltip(gui, mouseX, mouseY);

        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Selected recipe
        if(mouseX >= x+TOOLTIP_SELECTED_RECIPE_X && mouseX <= x+TOOLTIP_SELECTED_RECIPE_X+TOOLTIP_SELECTED_RECIPE_S &&
                mouseY >= y+TOOLTIP_SELECTED_RECIPE_Y && mouseY <= y+TOOLTIP_SELECTED_RECIPE_Y+TOOLTIP_SELECTED_RECIPE_S) {
            ItemStack recipeItem = menu.getRecipeItem();
            if(recipeItem == ItemStack.EMPTY) {
                tooltipContents.add(Component.translatable("tooltip.magichem.gui.no_selected_recipe").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            } else {
                tooltipContents.addAll(recipeItem.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
            }
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Items in recipe picker
        if(mouseX >= x+TOOLTIP_RECIPE_ZONE_X && mouseX <= x+TOOLTIP_RECIPE_ZONE_X+TOOLTIP_RECIPE_ZONE_W &&
                mouseY >= y+TOOLTIP_RECIPE_ZONE_Y && mouseY <= y+TOOLTIP_RECIPE_ZONE_Y+TOOLTIP_RECIPE_ZONE_H) {
            int mx = mouseX - (x+TOOLTIP_RECIPE_ZONE_X);
            int my = mouseY - (y+TOOLTIP_RECIPE_ZONE_Y);
            int id = ((my / 18) * 3) + ((mx / 18) % 3);

            if (id >= 0 && id < 16) {
                if(id + recipeFilterRow * 3 < filteredRecipes.size()) {
                    ItemStack stackUnderMouse = filteredRecipes.get(id + recipeFilterRow * 3);
                    tooltipContents.addAll(stackUnderMouse.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
                    gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
                }
            }
        }

        //Efficiency
        if(mouseX >= x+TOOLTIP_EFFICIENCY_X && mouseX <= x+TOOLTIP_EFFICIENCY_X+TOOLTIP_EFFICIENCY_W &&
                mouseY >= y+TOOLTIP_EFFICIENCY_Y && mouseY <= y+TOOLTIP_EFFICIENCY_Y+TOOLTIP_EFFICIENCY_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.efficiency").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.efficiency.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.translatable("tooltip.magichem.gui.efficiency.line2"));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Efficiency
        if(mouseX >= x+TOOLTIP_POWERUSAGE_X && mouseX <= x+TOOLTIP_POWERUSAGE_X+TOOLTIP_POWERUSAGE_W &&
                mouseY >= y+TOOLTIP_POWERUSAGE_Y && mouseY <= y+TOOLTIP_POWERUSAGE_Y+TOOLTIP_POWERUSAGE_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.power_usage").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.power_usage.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.translatable("tooltip.magichem.gui.power_usage.line2"));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Operation Time
        if(mouseX >= x+TOOLTIP_OPERATIONTIME_X && mouseX <= x+TOOLTIP_OPERATIONTIME_X+TOOLTIP_OPERATIONTIME_W &&
                mouseY >= y+TOOLTIP_OPERATIONTIME_Y && mouseY <= y+TOOLTIP_OPERATIONTIME_Y+TOOLTIP_OPERATIONTIME_H) {

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.operation_time").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.operation_time.line1")));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }

        //Grime Bar
        if(mouseX >= x+TOOLTIP_GRIME_X && mouseX <= x+TOOLTIP_GRIME_X+TOOLTIP_GRIME_W &&
                mouseY >= y+TOOLTIP_GRIME_Y && mouseY <= y+TOOLTIP_GRIME_Y+TOOLTIP_GRIME_H) {

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.grime").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.grime.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.translatable("tooltip.magichem.gui.grime.line2.1")
                    .append(Component.literal(ServerConfig.grimePenaltyPoint+"%").withStyle(ChatFormatting.DARK_AQUA))
                    .append(Component.translatable("tooltip.magichem.gui.grime.line2.2")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.grime.line3").withStyle(ChatFormatting.DARK_GRAY))
                    .append(" ")
                    .append(Component.literal(String.format("%.1f", GrandCentrifugeBlockEntity.getGrimePercent(menu.getGrime(), GrandCentrifugeBlockEntity::getVar)*100.0f)+"%").withStyle(ChatFormatting.DARK_AQUA)));
            gui.renderTooltip(font, tooltipContents, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gui, int pMouseX, int pMouseY) {
        Font font = Minecraft.getInstance().font;

        gui.drawString(font, Component.literal(GrandCentrifugeBlockEntity.getActualEfficiency(menu.getEfficiencyMod(), menu.getGrime(), GrandCentrifugeBlockEntity::getVar)+"%"), PANEL_GRIME_X + 32, PANEL_GRIME_Y - 1, 0xff000000, false);

        float fireActuatorReduction = 1 - (menu.getOperationTimeMod() / 10000f);
        int powerDraw = Math.round((float)menu.blockEntity.getPowerDraw() * fireActuatorReduction);
        gui.drawString(font, Component.literal(powerDraw + "/t"), PANEL_GRIME_X + 32, PANEL_GRIME_Y + 16, 0xff000000, false);

        int opTicks = GrandCentrifugeBlockEntity.getOperationTicks(menu.getGrime(), menu.getBatchSize(), menu.getOperationTimeMod(), GrandCentrifugeBlockEntity::getVar, menu.blockEntity::getPoweredOperationTime);
        int secWhole = opTicks / 20;
        int secPartial = (opTicks % 20) * 5;
        gui.drawString(font ,secWhole+"."+(secPartial < 10 ? "0"+secPartial : secPartial)+" s", PANEL_GRIME_X + 32, PANEL_GRIME_Y + 33, 0xff000000, false);

        if(!menu.blockEntity.getPowerSufficiency()) {
            MutableComponent warningText = Component.translatable("gui.magichem.insufficientpower");
            int width = Minecraft.getInstance().font.width(warningText.getString());
            gui.drawString(font, warningText, 89 - width/2, -34, 0xff000000, false);
        }
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

    public static List<Rect2i> getGuiExtraAreas(GrandCentrifugeScreen screen) {
        int xOrigin = (screen.width - PANEL_MAIN_W) / 2;
        int yOrigin = (screen.height - PANEL_MAIN_H) / 2;
        return List.of(
                new Rect2i(xOrigin + PANEL_RECIPE_X, yOrigin + PANEL_RECIPE_Y, PANEL_RECIPE_W, PANEL_RECIPE_H),
                new Rect2i(xOrigin + PANEL_GRIME_X, yOrigin + PANEL_GRIME_Y, PANEL_GRIME_W, PANEL_GRIME_H)
        );
    }
}
