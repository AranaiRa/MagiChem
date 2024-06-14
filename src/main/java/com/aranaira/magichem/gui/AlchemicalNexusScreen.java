package com.aranaira.magichem.gui;

import com.aranaira.magichem.Config;
import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
import com.aranaira.magichem.block.entity.CirclePowerBlockEntity;
import com.aranaira.magichem.block.entity.FuseryBlockEntity;
import com.aranaira.magichem.foundation.ButtonData;
import com.aranaira.magichem.foundation.InfusionStage;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import com.aranaira.magichem.recipe.AlchemicalInfusionRecipe;
import com.aranaira.magichem.recipe.FixationSeparationRecipe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlchemicalNexusScreen extends AbstractContainerScreen<AlchemicalNexusMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_alchemicalnexus.png");
    private static final ResourceLocation TEXTURE_SLURRY =
            new ResourceLocation(MagiChemMod.MODID, "textures/block/fluid/experience_still.png");
    private static final ResourceLocation TEXTURE_INGREDIENTS =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fabrication_ext.png");

    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 192,
            PANEL_STATS_X = 176, PANEL_STATS_Y = 37, PANEL_STATS_W = 80, PANEL_STATS_H = 66, PANEL_STATS_U = 176, PANEL_STATS_V = 126,
            PANEL_RECIPE_X = -84, PANEL_RECIPE_Y = -7, PANEL_RECIPE_U = 176, PANEL_RECIPE_W = 80, PANEL_RECIPE_H = 126,
            PANEL_INGREDIENTS_X = 176, PANEL_INGREDIENTS_Y = 98, PANEL_INGREDIENTS_W = 80,
            PANEL_INGREDIENTS_U1 = 160, PANEL_INGREDIENTS_U2 = 80, PANEL_INGREDIENTS_U3 = 160, PANEL_INGREDIENTS_U4 = 80, PANEL_INGREDIENTS_U5 = 0,
            PANEL_INGREDIENTS_V1 =  66, PANEL_INGREDIENTS_V2 = 84, PANEL_INGREDIENTS_V3 =   0, PANEL_INGREDIENTS_V4 =  0, PANEL_INGREDIENTS_V5 = 0,
            PANEL_INGREDIENTS_H1 =  30, PANEL_INGREDIENTS_H2 = 48, PANEL_INGREDIENTS_H3 =  66, PANEL_INGREDIENTS_H4 = 84, PANEL_INGREDIENTS_H5 = 102,
            SLURRY_X = 8, SLURRY_Y = 23, SLURRY_W = 8, SLURRY_H = 73,
            STAGE_INDICATOR_U = 108, STAGE_INDICATOR_V = 238, STAGE_INDICATOR_W = 12, STAGE_INDICATOR_W_END = 6, STAGE_INDICATOR_H = 9,
            TOOLTIP_SLURRY_X = 7, TOOLTIP_SLURRY_Y = 7, TOOLTIP_SLURRY_W = 10, TOOLTIP_SLURRY_H = 90,
            TOOLTIP_INPROGRESS_X = 80, TOOLTIP_INPROGRESS_Y = 8, TOOLTIP_INPROGRESS_S = 16,
            TOOLTIP_SELECTED_RECIPE_X = 80, TOOLTIP_SELECTED_RECIPE_Y = 80, TOOLTIP_SELECTED_RECIPE_S = 16,
            TOOLTIP_MARK_X = 134, TOOLTIP_MARK_Y = 8, TOOLTIP_MARK_S = 16,
            TOOLTIP_STAGE_X = 115, TOOLTIP_STAGE_Y = 29, TOOLTIP_STAGE_W = 54, TOOLTIP_STAGE_H = 9,
            TOOLTIP_POWERLEVEL_X = 180, TOOLTIP_POWERLEVEL_Y = 44, TOOLTIP_POWERLEVEL_W = 12, TOOLTIP_POWERLEVEL_H = 52,
            TOOLTIP_EXPERIENCE_X = 193, TOOLTIP_EXPERIENCE_Y = 54, TOOLTIP_EXPERIENCE_W = 59, TOOLTIP_EXPERIENCE_H = 13,
            TOOLTIP_OPTIME_X = 193, TOOLTIP_OPTIME_Y = 73, TOOLTIP_OPTIME_W = 59, TOOLTIP_OPTIME_H = 13,
            TOOLTIP_RECIPE_ZONE_X = -77, TOOLTIP_RECIPE_ZONE_Y = 22, TOOLTIP_RECIPE_ZONE_W = 54, TOOLTIP_RECIPE_ZONE_H = 90;

    private final ButtonData[] recipeSelectButtons = new ButtonData[15];
    private EditBox recipeFilterBox;

    public AlchemicalNexusScreen(AlchemicalNexusMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
//        renderButtons(pGuiGraphics, pPartialTick, pMouseX, pMouseY);
//        renderFilterBox();
//        renderRecipeOptions(pGuiGraphics);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;
        Font font = Minecraft.getInstance().font;

        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);
        pGuiGraphics.blit(TEXTURE, x + PANEL_STATS_X, y + PANEL_STATS_Y, PANEL_STATS_U, PANEL_STATS_V, PANEL_STATS_W, PANEL_STATS_H);
        pGuiGraphics.blit(TEXTURE, x + PANEL_RECIPE_X, y + PANEL_RECIPE_Y, PANEL_RECIPE_U, 0, PANEL_RECIPE_W, PANEL_RECIPE_H);

        //slurry gauge
        int slurryH = AlchemicalNexusBlockEntity.getScaledSlurry(menu.getSlurryInTank());
        RenderSystem.setShaderTexture(1, TEXTURE_SLURRY);
        pGuiGraphics.blit(TEXTURE_SLURRY, x + SLURRY_X, y + SLURRY_Y + SLURRY_H - slurryH, 0, 0, SLURRY_W, slurryH, 16, 16);

        renderStageGauge(pGuiGraphics, x, y);

        menu.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            if(handler.getStackInSlot(AlchemicalNexusBlockEntity.SLOT_MARKS).isEmpty()) {
                pGuiGraphics.pose().scale(0.5f, 0.5f, 0.5f);
                pGuiGraphics.blit(TEXTURE, (x + 134) * 2, (y + 7) * 2, 66, 222, 32, 32);
                pGuiGraphics.pose().scale(2.0f, 2.0f, 2.0f);
            }

            if(handler.getStackInSlot(AlchemicalNexusBlockEntity.SLOT_RECIPE).isEmpty() || menu.getCurrentRecipe() == null) {
                pGuiGraphics.blit(TEXTURE, x + 79, y + 79, 28, 238, 18, 18);
            } else {
                pGuiGraphics.renderItem(handler.getStackInSlot(AlchemicalNexusBlockEntity.SLOT_RECIPE), x + 80, y + 80);

                pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 0.25f);
                //ingredients
                {
                    int i=0;
                    for (ItemStack is : menu.getStage(menu.blockEntity.getCraftingStage()).componentItems) {
                        pGuiGraphics.renderItem(is, x + 22, y + 8 + (i * 18));
                        i++;
                    }
                }
                pGuiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

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

    }

//    private void initializeRecipeFilterBox() {
//        int x = (width - PANEL_MAIN_W) / 2;
//        int y = (height - PANEL_MAIN_H) / 2;
//
//        this.recipeFilterBox = new EditBox(Minecraft.getInstance().font, x, y, 65, 16, Component.empty()) {
//            @Override
//            public boolean charTyped(char pCodePoint, int pModifiers) {
//                updateDisplayedRecipes(recipeFilterBox.getValue());
//                return super.charTyped(pCodePoint, pModifiers);
//            }
//
//            @Override
//            public void deleteChars(int pNum) {
//                super.deleteChars(pNum);
//                updateDisplayedRecipes(recipeFilterBox.getValue());
//            }
//
//            @Override
//            public void deleteWords(int pNum) {
//                super.deleteWords(pNum);
//                updateDisplayedRecipes(recipeFilterBox.getValue());
//            }
//        };
//        this.recipeFilterBox.setMaxLength(60);
//        this.recipeFilterBox.setFocused(false);
//        this.recipeFilterBox.setCanLoseFocus(false);
//        this.setFocused(this.recipeFilterBox);
//    }

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

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int pX, int pY) {
        super.renderTooltip(pGuiGraphics, pX, pY);

        Font font = Minecraft.getInstance().font;
        List<Component> tooltipContents = new ArrayList<>();
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

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
            ItemStack recipeItem = menu.getCurrentRecipe().getAlchemyObject();
            if(recipeItem == ItemStack.EMPTY) {
                tooltipContents.add(Component.translatable("tooltip.magichem.gui.noselectedrecipe").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            } else {
                int totalCost = 0;
                for(InfusionStage is : menu.getCurrentRecipe().getStages(false)){
                    totalCost += is.experience;
                    totalCost += AlchemicalNexusBlockEntity.getBaseExperienceCostPerStage(menu.blockEntity.getPowerLevel());
                }
                totalCost *= Config.fluidPerXPPoint;

                tooltipContents.addAll(recipeItem.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
                tooltipContents.add(Component.empty());
                tooltipContents.add(Component.empty()
                        .append(Component.translatable("tooltip.magichem.gui.sublimationcost.part1").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(totalCost+"mB").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable("tooltip.magichem.gui.sublimationcost.part2").withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal(menu.getCurrentRecipe().getStages(false).size()+"").withStyle(ChatFormatting.DARK_AQUA))
                        .append(Component.translatable("tooltip.magichem.gui.sublimationcost.part3").withStyle(ChatFormatting.DARK_GRAY))
                );
            }
        }

        //Items in recipe picker
//        if(mouseX >= x+TOOLTIP_RECIPE_ZONE_X && mouseX <= x+TOOLTIP_RECIPE_ZONE_X+TOOLTIP_RECIPE_ZONE_W &&
//                mouseY >= y+TOOLTIP_RECIPE_ZONE_Y && mouseY <= y+TOOLTIP_RECIPE_ZONE_Y+TOOLTIP_RECIPE_ZONE_H) {
//            int mx = mouseX - (x+TOOLTIP_RECIPE_ZONE_X);
//            int my = mouseY - (y+TOOLTIP_RECIPE_ZONE_Y);
//            int id = ((my / 18) * 3) + ((mx / 18) % 3);
//
//            if(id < filteredRecipeOutputs.size()) {
//                if (id >= 0 && id < 16) {
//                    ItemStack stackUnderMouse = filteredRecipeOutputs.get(id);
//                    tooltipContents.addAll(stackUnderMouse.getTooltipLines(getMinecraft().player, TooltipFlag.NORMAL));
//                }
//            }
//        }

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
        }

        pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        int powerDraw = 10;
        int ticksToCraft = AlchemicalNexusBlockEntity.getAnimSpec(menu.blockEntity.getPowerLevel()).ticksToCraft;
        int secWhole = ticksToCraft / 20;
        int secPartial = (ticksToCraft % 20) * 5;

        Font font = Minecraft.getInstance().font;
        pGuiGraphics.drawString(font ,powerDraw+"", 208, 44, 0xff000000, false);
        pGuiGraphics.drawString(font ,secWhole+"."+(secPartial < 10 ? "0"+secPartial : secPartial)+" s", 208, 63, 0xff000000, false);

//        if(!menu.getHasSufficientPower()) {
//            MutableComponent warningText = Component.translatable("gui.magichem.insufficientpower");
//            int width = Minecraft.getInstance().font.width(warningText.getString());
//            pGuiGraphics.drawString(font, warningText, 89 - width/2, -33, 0xff000000, false);
//        }
    }
}
