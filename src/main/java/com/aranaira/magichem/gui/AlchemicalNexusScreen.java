package com.aranaira.magichem.gui;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
import com.aranaira.magichem.foundation.AlchemicalNexusAnimSpec;
import com.aranaira.magichem.foundation.ButtonData;
import com.aranaira.magichem.foundation.InfusionStage;
import com.aranaira.magichem.foundation.Triplet;
import com.aranaira.magichem.gui.element.AlchemicalNexusButtonRecipeSelector;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.networking.NexusSyncDataC2SPacket;
import com.aranaira.magichem.recipe.SublimationRecipe;
import com.aranaira.magichem.registry.PacketRegistry;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity.*;

public class AlchemicalNexusScreen extends AbstractContainerScreen<AlchemicalNexusMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_alchemicalnexus.png");
    private static final ResourceLocation TEXTURE_SLURRY =
            new ResourceLocation(MagiChemMod.MODID, "textures/block/fluid/experience_still.png");
    private static final ResourceLocation TEXTURE_EXT =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fabrication_ext.png");
    private ImageButton
            b_powerLevelUp, b_powerLevelDown;

    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 192,
            PANEL_STATS_X = 176, PANEL_STATS_Y = 37, PANEL_STATS_W = 80, PANEL_STATS_H = 66, PANEL_STATS_U = 176, PANEL_STATS_V = 126,
            PANEL_RECIPE_X = -84, PANEL_RECIPE_Y = -7, PANEL_RECIPE_U = 176, PANEL_RECIPE_W = 80, PANEL_RECIPE_H = 126,
            SLURRY_X = 8, SLURRY_Y = 23, SLURRY_W = 8, SLURRY_H = 73,
            STAGE_INDICATOR_U = 108, STAGE_INDICATOR_V = 238, STAGE_INDICATOR_W = 12, STAGE_INDICATOR_W_END = 6, STAGE_INDICATOR_H = 9,
            PROGRESS_BAR_WIDTH = 28,
            TOOLTIP_SLURRY_X = 7, TOOLTIP_SLURRY_Y = 7, TOOLTIP_SLURRY_W = 10, TOOLTIP_SLURRY_H = 90,
            TOOLTIP_SELECTED_RECIPE_X = 80, TOOLTIP_SELECTED_RECIPE_Y = 80, TOOLTIP_SELECTED_RECIPE_S = 16,
            TOOLTIP_MARK_X = 134, TOOLTIP_MARK_Y = 8, TOOLTIP_MARK_S = 16,
            TOOLTIP_STAGE_X = 115, TOOLTIP_STAGE_Y = 29, TOOLTIP_STAGE_W = 54, TOOLTIP_STAGE_H = 9,
            TOOLTIP_EXPERIENCE_X = 193, TOOLTIP_EXPERIENCE_Y = 54, TOOLTIP_EXPERIENCE_W = 59, TOOLTIP_EXPERIENCE_H = 13,
            TOOLTIP_OPTIME_X = 193, TOOLTIP_OPTIME_Y = 73, TOOLTIP_OPTIME_W = 59, TOOLTIP_OPTIME_H = 13,
            TOOLTIP_RECIPE_ZONE_X = -77, TOOLTIP_RECIPE_ZONE_Y = 22, TOOLTIP_RECIPE_ZONE_W = 54, TOOLTIP_RECIPE_ZONE_H = 90;
    private SublimationRecipe lastRecipe = null;
    private NonNullList<ItemStack> lastRecipeComponentMateria = NonNullList.create();
    private ItemStack lastRecipeResult = ItemStack.EMPTY;
    private int recipeTierCap = 1;

    private final ButtonData[] recipeSelectButtons = new ButtonData[15];
    private EditBox recipeFilterBox;

    public AlchemicalNexusScreen(AlchemicalNexusMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        pPlayerInventory.player.getCapability(PlayerProgressionProvider.PROGRESSION).ifPresent(cap -> recipeTierCap = cap.getTier());
        updateDisplayedRecipes("");
    }

    @Override
    protected void init() {
        super.init();
        initializeRecipeSelectorButtons();
        initializePowerLevelButtons();
        initializeRecipeFilterBox();
    }

    private List<ItemStack> filteredRecipeOutputs = new ArrayList<>();
    private int recipeFilterRow, recipeFilterRowTotal;
    private void updateDisplayedRecipes(String filter) {
        List<SublimationRecipe> sublimationRecipeOutputs = menu.blockEntity.getLevel().getRecipeManager().getAllRecipesFor(SublimationRecipe.Type.INSTANCE);
        filteredRecipeOutputs.clear();

        recipeFilterRowTotal = (int)Math.ceil(sublimationRecipeOutputs.size() / 3.0f);
        recipeFilterRow = 0;

        for(SublimationRecipe iar : sublimationRecipeOutputs) {
            String display = iar.getAlchemyObject().getDisplayName().getString();
            if((Objects.equals(filter, "") || display.toLowerCase().contains(filter.toLowerCase()))) {
                if(iar.getTier() <= recipeTierCap)
                    filteredRecipeOutputs.add(iar.getAlchemyObject().copy());
            }
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
        renderRecipeOptions(pGuiGraphics);
        updateFilterBoxContents();
    }

    private void initializePowerLevelButtons(){
        b_powerLevelUp = this.addRenderableWidget(new ImageButton(this.leftPos + 180, this.topPos + 31, 12, 7, 232, 242, TEXTURE, button -> {
            if(menu.blockEntity.getAnimStage() == AlchemicalNexusBlockEntity.ANIM_STAGE_IDLE || menu.blockEntity.getAnimStage() == AlchemicalNexusBlockEntity.ANIM_STAGE_CRAFTING_IDLE) {
                menu.blockEntity.incrementPowerUsageSetting();
                ItemStack output = menu.blockEntity.getCurrentRecipe() == null ? ItemStack.EMPTY : menu.blockEntity.getCurrentRecipe().getAlchemyObject();
                PacketRegistry.sendToServer(new NexusSyncDataC2SPacket(
                        menu.blockEntity.getBlockPos(),
                        menu.blockEntity.getPowerLevel(),
                        output
                ));
            }
        }));
        b_powerLevelDown = this.addRenderableWidget(new ImageButton(this.leftPos + 180, this.topPos + 76, 12, 7, 244, 242, TEXTURE, button -> {
            if(menu.blockEntity.getAnimStage() == AlchemicalNexusBlockEntity.ANIM_STAGE_IDLE || menu.blockEntity.getAnimStage() == AlchemicalNexusBlockEntity.ANIM_STAGE_CRAFTING_IDLE) {
                menu.blockEntity.decrementPowerUsageSetting();
                ItemStack output = menu.blockEntity.getCurrentRecipe() == null ? ItemStack.EMPTY : menu.blockEntity.getCurrentRecipe().getAlchemyObject();
                PacketRegistry.sendToServer(new NexusSyncDataC2SPacket(
                        menu.blockEntity.getBlockPos(),
                        menu.blockEntity.getPowerLevel(),
                        output
                ));
            }
        }));
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;
        Font font = Minecraft.getInstance().font;

        //bg and panel elements
        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);
        pGuiGraphics.blit(TEXTURE, x + PANEL_STATS_X, y + PANEL_STATS_Y, PANEL_STATS_U, PANEL_STATS_V, PANEL_STATS_W, PANEL_STATS_H);
        pGuiGraphics.blit(TEXTURE, x + PANEL_RECIPE_X, y + PANEL_RECIPE_Y, PANEL_RECIPE_U, 0, PANEL_RECIPE_W, PANEL_RECIPE_H);

        //slurry gauge
        int slurryH = AlchemicalNexusBlockEntity.getScaledSlurry(menu.getSlurryInTank());
        RenderSystem.setShaderTexture(1, TEXTURE_SLURRY);
        pGuiGraphics.blit(TEXTURE_SLURRY, x + SLURRY_X, y + SLURRY_Y + SLURRY_H - slurryH, 0, 0, SLURRY_W, slurryH, 16, 16);

        //stages
        renderStageGauge(pGuiGraphics, x, y);

        //recipe indicator
        menu.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            if(handler.getStackInSlot(AlchemicalNexusBlockEntity.SLOT_MARKS).isEmpty()) {
                pGuiGraphics.pose().scale(0.5f, 0.5f, 0.5f);
                pGuiGraphics.blit(TEXTURE, (x + 134) * 2, (y + 7) * 2, 66, 222, 32, 32);
                pGuiGraphics.pose().scale(2.0f, 2.0f, 2.0f);
            }

            if(handler.getStackInSlot(AlchemicalNexusBlockEntity.SLOT_RECIPE).isEmpty() || menu.getCurrentRecipe() == null) {
                pGuiGraphics.blit(TEXTURE, x + 79, y + 79, 28, 238, 18, 18);
            } else {
                pGuiGraphics.renderItem(menu.getCurrentRecipe().getAlchemyObject(), x + 80, y + 80);
                pGuiGraphics.renderItemDecorations(Minecraft.getInstance().font, menu.getCurrentRecipe().getAlchemyObject(), x + 80, y + 80);

                //ingredients
                {
                    int i=0;
                    for (ItemStack is : menu.getStage(menu.blockEntity.getCraftingStage()).componentItems) {
                        pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 0.25f);
                        pGuiGraphics.renderItem(is, x + 22, y + 8 + (i * 18));
                        pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

                        //correction for blockitems
                        if(is.getItem() instanceof BlockItem) {
                            pGuiGraphics.fill(RenderType.guiGhostRecipeOverlay(), x + 22, y + 8 + (i * 18), x + 40, y + 26 + (i * 18), 0x40ffffff);
                        }

                        i++;
                    }
                }

                //materia
                {
                    int i=0;
                    for (ItemStack is : menu.getStage(menu.blockEntity.getCraftingStage()).componentMateria) {
                        pGuiGraphics.renderItem(is, x + 44, y + 8 + (i * 18));
                        pGuiGraphics.renderItemDecorations(font, is, x + 44, y + 8 + (i * 18));
                        i++;
                    }
                }
            }
        });

        //power level gauge
        int powerLevel = menu.blockEntity.getPowerLevel() * 6;
        pGuiGraphics.blit(TEXTURE, x + 182, y + 55 + (30 - powerLevel), 100, 256 - powerLevel, 8, powerLevel);

        //progress gauge
        if(menu.blockEntity.getAnimStage() == ANIM_STAGE_CRAFTING) {
            int scaledProgress = menu.blockEntity.getScaledProgress(PROGRESS_BAR_WIDTH);
            pGuiGraphics.blit(TEXTURE, x + 74, y + 38, 0, 228, scaledProgress, 28);
        }

        if(menu.blockEntity.getAnimStage() == ANIM_STAGE_SHLORPS || menu.blockEntity.getAnimStage() == ANIM_STAGE_RAMP_CIRCLE || menu.blockEntity.getAnimStage() == ANIM_STAGE_RAMP_CRAFTING_CIRCLE) {
            renderMateriaWaitWarning(pGuiGraphics, x, y);
        }
    }

    private void initializeRecipeSelectorButtons(){
        int c = 0;
        for(int y=0; y<5; y++) {
            for(int x=0; x<3; x++) {
                recipeSelectButtons[c] = new ButtonData(this.addRenderableWidget(new AlchemicalNexusButtonRecipeSelector(
                        this, c, this.leftPos, this.topPos, 18, 18, 46, 220, TEXTURE, button -> {
                    if(menu.blockEntity.getAnimStage() == AlchemicalNexusBlockEntity.ANIM_STAGE_IDLE) {
                        AlchemicalNexusScreen query = (AlchemicalNexusScreen) ((AlchemicalNexusButtonRecipeSelector) button).getScreen();
                        query.setActiveRecipe(((AlchemicalNexusButtonRecipeSelector) button).getArrayIndex());
                    }
                })), x*18 - 77, y*18 + 22);
                c++;
            }
        }

        renderButtons();
    }

    public void setActiveRecipe(int index) {
        int trueIndex = recipeFilterRow*3 + index;
        if(trueIndex < filteredRecipeOutputs.size()) {
            PacketRegistry.sendToServer(new NexusSyncDataC2SPacket(
                    menu.blockEntity.getBlockPos(),
                    menu.blockEntity.getPowerLevel(),
                    filteredRecipeOutputs.get(trueIndex)
            ));
            menu.blockEntity.setRecipeFromOutput(menu.blockEntity.getLevel(), filteredRecipeOutputs.get(trueIndex));
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

    private void initializeRecipeFilterBox() {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        this.recipeFilterBox = new EditBox(Minecraft.getInstance().font, x, y, 65, 16, Component.empty()) {
            @Override
            public boolean charTyped(char pCodePoint, int pModifiers) {
                updateDisplayedRecipes(recipeFilterBox.getValue());
                return super.charTyped(pCodePoint, pModifiers);
            }

            @Override
            public void deleteChars(int pNum) {
                super.deleteChars(pNum);
                updateDisplayedRecipes(recipeFilterBox.getValue());
            }

            @Override
            public void deleteWords(int pNum) {
                super.deleteWords(pNum);
                updateDisplayedRecipes(recipeFilterBox.getValue());
            }
        };
        this.recipeFilterBox.setMaxLength(60);
        this.recipeFilterBox.setFocused(false);
        this.recipeFilterBox.setCanLoseFocus(false);
        this.setFocused(this.recipeFilterBox);

        renderFilterBox();
    }

    private void renderStageGauge(GuiGraphics pGuiGraphics, int pX, int pY) {
        int count = menu.getAllStages().size();

        if(count > 0) {
            int width = STAGE_INDICATOR_W_END + (count - 1) * STAGE_INDICATOR_W;
            int x = 115 + (27 - width / 2);
            int y = 29;

            for(int i=0; i<count; i++) {

                int activeShift = i <= menu.getCurrentStageID() ? STAGE_INDICATOR_H : 0;

                if(i == count - 1)
                    pGuiGraphics.blit(TEXTURE, pX + x + i * STAGE_INDICATOR_W, pY + y, STAGE_INDICATOR_U, STAGE_INDICATOR_V + activeShift, STAGE_INDICATOR_W_END, STAGE_INDICATOR_H, 256, 256);
                else
                    pGuiGraphics.blit(TEXTURE, pX + x + i * STAGE_INDICATOR_W, pY + y, STAGE_INDICATOR_U, STAGE_INDICATOR_V + activeShift, STAGE_INDICATOR_W, STAGE_INDICATOR_H, 256, 256);
            }
        }
    }

    protected void renderMateriaWaitWarning(GuiGraphics gui, int x, int y) {
        long cycle = Minecraft.getInstance().level.getGameTime() % 20;

        gui.blit(TEXTURE_EXT, x+10, y-30, 0, 230, 156, 26);
        if(cycle < 10) {
            gui.blit(TEXTURE_EXT, x + 17, y - 23, 156, 244, 12, 12);
            gui.blit(TEXTURE_EXT, x + 147, y - 23, 156, 244, 12, 12);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {

        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;
        boolean doOriginalTooltip = true;

        //Slurry Bar
        if(pX >= x+TOOLTIP_SLURRY_X && pX <= x+TOOLTIP_SLURRY_X+TOOLTIP_SLURRY_W &&
                pY >= y+TOOLTIP_SLURRY_Y && pY <= y+TOOLTIP_SLURRY_Y+TOOLTIP_SLURRY_H) {

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.slurry.tank").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.slurry.tank.line1"))
                    .append(menu.blockEntity.getDisplayName())
                    .append("."));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.slurry.tank.line3").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(menu.getSlurryInTank()+"mB").withStyle(ChatFormatting.DARK_AQUA)));
        }

        //Selected Recipe
        if(pX >= x+TOOLTIP_SELECTED_RECIPE_X && pX <= x+TOOLTIP_SELECTED_RECIPE_X+TOOLTIP_SELECTED_RECIPE_S &&
                pY >= y+TOOLTIP_SELECTED_RECIPE_Y && pY <= y+TOOLTIP_SELECTED_RECIPE_Y+TOOLTIP_SELECTED_RECIPE_S) {
            if(menu.getCurrentRecipe() == null) {

            } else {
                ItemStack recipeItem = menu.getCurrentRecipe().getAlchemyObject();
                if (recipeItem == ItemStack.EMPTY) {
                    tooltipContents.add(Component.translatable("tooltip.magichem.gui.noselectedrecipe").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                } else {
                    int totalCost = 0;
                    for (InfusionStage is : menu.getCurrentRecipe().getStages(false)) {
                        totalCost += is.experience * Config.fluidPerXPPoint;
                        totalCost += AlchemicalNexusBlockEntity.getBaseExperienceCostPerStage(menu.blockEntity.getPowerLevel());
                    }
                    totalCost = Math.round((float)totalCost * (1f - menu.getReductionRate()));

                    tooltipContents.addAll(recipeItem.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
                    tooltipContents.add(Component.empty());
                    tooltipContents.add(Component.empty()
                            .append(Component.translatable("tooltip.magichem.gui.sublimationcost.part1").withStyle(ChatFormatting.DARK_GRAY))
                            .append(Component.literal(totalCost + "mB").withStyle(ChatFormatting.DARK_AQUA))
                            .append(Component.translatable("tooltip.magichem.gui.sublimationcost.part2").withStyle(ChatFormatting.DARK_GRAY))
                            .append(Component.literal(menu.getCurrentRecipe().getStages(false).size() + "").withStyle(ChatFormatting.DARK_AQUA))
                            .append(Component.translatable("tooltip.magichem.gui.sublimationcost.part3").withStyle(ChatFormatting.DARK_GRAY))
                    );
                }
            }
        }

        //Items in recipe picker
        if(pX >= x+TOOLTIP_RECIPE_ZONE_X && pX <= x+TOOLTIP_RECIPE_ZONE_X+TOOLTIP_RECIPE_ZONE_W &&
                pY >= y+TOOLTIP_RECIPE_ZONE_Y && pY <= y+TOOLTIP_RECIPE_ZONE_Y+TOOLTIP_RECIPE_ZONE_H) {
            int mx = pX - (x+TOOLTIP_RECIPE_ZONE_X);
            int my = pY - (y+TOOLTIP_RECIPE_ZONE_Y);
            int id = ((my / 18) * 3) + ((mx / 18) % 3);

            if(id < filteredRecipeOutputs.size()) {
                if (id >= 0 && id < 16) {
                    ItemStack stackUnderMouse = filteredRecipeOutputs.get(id);
                    tooltipContents.addAll(stackUnderMouse.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
                }
            }
        }

        //Marks
        if(menu.blockEntity.getMarkItem().isEmpty()) {
            if (pX >= x + TOOLTIP_MARK_X && pX <= x + TOOLTIP_MARK_X + TOOLTIP_MARK_S &&
                    pY >= y + TOOLTIP_MARK_Y && pY <= y + TOOLTIP_MARK_Y + TOOLTIP_MARK_S) {

                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.marks").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.marks.line1")));
                tooltipContents.add(Component.empty());
                tooltipContents.add(Component.translatable("tooltip.magichem.gui.marks.line2"));
            }
        }

        //Stage
        if(menu.getCurrentRecipe() != null) {
            if (pX >= x + TOOLTIP_STAGE_X && pX <= x + TOOLTIP_STAGE_X + TOOLTIP_STAGE_W &&
                    pY >= y + TOOLTIP_STAGE_Y && pY <= y + TOOLTIP_STAGE_Y + TOOLTIP_STAGE_H) {

                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.stage").withStyle(ChatFormatting.GOLD))
                        .append(": ")
                        .append(Component.translatable("tooltip.magichem.gui.stage.line1")));
                tooltipContents.add(Component.empty());
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.stage.line2.part1"))
                        .append(Component.literal(menu.blockEntity.getCraftingStage() + 1 + "").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable("tooltip.magichem.gui.stage.line2.part2"))
                        .append(Component.literal(menu.getCurrentRecipe().getStages(false).size()+"").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable("tooltip.magichem.gui.stage.line2.part3")));
            }
        }

        //Experience Cost
        if(pX >= x+TOOLTIP_EXPERIENCE_X && pX <= x+TOOLTIP_EXPERIENCE_X+TOOLTIP_EXPERIENCE_W &&
                pY >= y+TOOLTIP_EXPERIENCE_Y && pY <= y+TOOLTIP_EXPERIENCE_Y+TOOLTIP_EXPERIENCE_H) {

            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.experience").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.experience.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.translatable("tooltip.magichem.gui.experience.line2"));
        }

        //Operation Time
        if(pX >= x+TOOLTIP_OPTIME_X && pX <= x+TOOLTIP_OPTIME_X+TOOLTIP_OPTIME_W &&
                pY >= y+TOOLTIP_OPTIME_Y && pY <= y+TOOLTIP_OPTIME_Y+TOOLTIP_OPTIME_H) {

            tooltipContents.clear();
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.operationtime").withStyle(ChatFormatting.GOLD))
                    .append(": ")
                    .append(Component.translatable("tooltip.magichem.gui.operationtime.line1")));
            tooltipContents.add(Component.empty());
            tooltipContents.add(Component.empty()
                    .append(Component.translatable("tooltip.magichem.gui.operationtime.nexus.line2")));
        }

        if(menu.getCurrentRecipe() != null) {
            //Ingredients
            if (pX >= x + 21 && pX <= x + 39 &&
                    pY >= y + 7 && pY <= y + 97) {
                int i = (pY - (y + 7)) / 18;
                NonNullList<ItemStack> componentItems = menu.getStage(menu.getCurrentStageID()).componentItems;

                if (i < componentItems.size()) {
                    ItemStack stackInSlot = menu.getItems().get(AlchemicalNexusBlockEntity.SLOT_INPUT_START + i + 35);

                    if (stackInSlot.isEmpty()) {
                        String name = componentItems.get(i).getDisplayName().getString();

                        tooltipContents.clear();
                        tooltipContents.add(Component.empty()
                                .append(Component.literal(name.substring(1, name.length() - 1)).withStyle(ChatFormatting.DARK_GRAY))
                        );
                    } else {
                        super.renderTooltip(pGuiGraphics, pX, pY);
                    }
                }
            }

            //Materia
            if (pX >= x + 43 && pX <= x + 60 &&
                    pY >= y + 7 && pY <= y + 97) {
                int i = (pY - (y + 7)) / 18;
                NonNullList<Triplet<MateriaItem, Integer, Boolean>> allMateriaDemands = menu.blockEntity.getAllMateriaDemands();
                NonNullList<ItemStack> componentMateria = menu.getCurrentRecipe().getStages(false).get(menu.getCurrentStageID()).componentMateria;

                int animStage = menu.blockEntity.getAnimStage();
                if (i < componentMateria.size()) {
                    String name = componentMateria.get(i).getDisplayName().getString();
                    int required = componentMateria.get(i).getCount();
                    int outstanding = required;
                    for (Triplet<MateriaItem, Integer, Boolean> triplet : allMateriaDemands) {
                        if (componentMateria.get(i).getItem() == triplet.getFirst()) {
                            outstanding = triplet.getSecond();
                        }
                    }

                    tooltipContents.clear();
                    tooltipContents.add(Component.empty()
                            .append(Component.literal(name.substring(1, name.length() - 1)))
                    );

                    if (animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_SHLORPS) {
                        tooltipContents.add(Component.empty()
                                .append(Component.literal("" + (required - outstanding)).withStyle(ChatFormatting.DARK_AQUA))
                                .append(" of ").withStyle(ChatFormatting.DARK_GRAY)
                                .append(Component.literal("" + required).withStyle(ChatFormatting.DARK_AQUA))
                                .append(" provided").withStyle(ChatFormatting.DARK_GRAY)
                        );
                    } else if (animStage == ANIM_STAGE_CRAFTING ||
                               animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_RAMP_CRAFTING) {
                        tooltipContents.add(Component.empty()
                                .append(Component.literal("" + required).withStyle(ChatFormatting.DARK_AQUA))
                                .append(" of ").withStyle(ChatFormatting.DARK_GRAY)
                                .append(Component.literal("" + required).withStyle(ChatFormatting.DARK_AQUA))
                                .append(" provided").withStyle(ChatFormatting.DARK_GRAY)
                        );
                    } else if (animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_IDLE ||
                               animStage == AlchemicalNexusBlockEntity.ANIM_STAGE_CRAFTING_IDLE) {
                        tooltipContents.add(Component.empty()
                                .append(Component.translatable("tooltip.magichem.gui.shlorps.idle")).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                        );
                    } else {
                        tooltipContents.add(Component.empty()
                                .append(Component.translatable("tooltip.magichem.gui.shlorps.waiting")).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                        );
                    }
                }
            }
        }

        //Sublimation In Progress
        if(pX >= x+79 && pX <= x+97 &&
                pY >= y+7 && pY <= y+24) {

            ItemStack stackInSlot = menu.getItems().get(AlchemicalNexusBlockEntity.SLOT_PROGRESS_HOLDER);
            doOriginalTooltip = false;

            if(!stackInSlot.isEmpty()) {
                tooltipContents.clear();
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.sublimation_in_progress.line1"))
                );
                tooltipContents.add(Component.empty());
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.sublimation_in_progress.line2.part1").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC))
                        .append(" ")
                        .append(Component.translatable("tooltip.magichem.gui.sublimation_in_progress.line2.part2"))
                );
            }
        }

        if(doOriginalTooltip)
            super.renderTooltip(pGuiGraphics, pX, pY);

        pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
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
        for(int i=recipeFilterRow*3; i<Math.min(filteredRecipeOutputs.size(), recipeFilterRow*3 + 15); i++) {
            snipped.add(filteredRecipeOutputs.get(i));
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
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        int experienceDraw = AlchemicalNexusBlockEntity.getBaseExperienceCostPerStage(menu.blockEntity.getPowerLevel());
        experienceDraw = Math.round((float)experienceDraw * (1f - menu.getReductionRate()));

        AlchemicalNexusAnimSpec animSpec = AlchemicalNexusBlockEntity.getAnimSpec(menu.blockEntity.getPowerLevel());
        int ticksToCraft = animSpec.ticksInRampSpeedup + animSpec.ticksInRampBeam + animSpec.ticksInRampCancel + animSpec.ticksToCraft;
        int secWhole = ticksToCraft / 20;
        int secPartial = (ticksToCraft % 20) * 5;

        Font font = Minecraft.getInstance().font;
        pGuiGraphics.drawString(font ,experienceDraw+" mB", 208, 44, 0xff000000, false);
        pGuiGraphics.drawString(font ,secWhole+"."+(secPartial < 10 ? "0"+secPartial : secPartial)+" s", 208, 63, 0xff000000, false);

        if(menu.blockEntity.getAnimStage() == ANIM_STAGE_RAMP_CIRCLE || menu.blockEntity.getAnimStage() == ANIM_STAGE_RAMP_CRAFTING_CIRCLE) {
            MutableComponent warningText = Component.translatable("gui.magichem.waitingforslurry");
            int width = Minecraft.getInstance().font.width(warningText.getString());
            pGuiGraphics.drawString(font, warningText, 89 - width / 2, -33, 0xff000000, false);
        }
        else if(menu.blockEntity.getAnimStage() == ANIM_STAGE_SHLORPS) {
            MutableComponent warningText = Component.translatable("gui.magichem.waitingformateria");
            int width = Minecraft.getInstance().font.width(warningText.getString());
            pGuiGraphics.drawString(font, warningText, 89 - width / 2, -33, 0xff000000, false);
        }
    }
}
