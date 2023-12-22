package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.CircleFabricationBlockEntity;
import com.aranaira.magichem.block.entity.container.OnlyMateriaInputSlot;
import com.aranaira.magichem.foundation.ButtonData;
import com.aranaira.magichem.gui.element.ImageButtonFabricationRecipeSelector;
import com.aranaira.magichem.networking.FabricationSyncDataC2SPacket;
import com.aranaira.magichem.recipe.AlchemicalCompositionRecipe;
import com.aranaira.magichem.registry.PacketRegistry;
import com.aranaira.magichem.util.RenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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

    private void initializeRecipeFilterBox() {
        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        recipeFilterBox = new EditBox(Minecraft.getInstance().font, x, y, 67, 18, Component.empty());
        this.addWidget(recipeFilterBox);
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
                recipeSelectButtons[c] = new ButtonData(this.addRenderableWidget(new ImageButtonFabricationRecipeSelector(
                        this, c, this.leftPos, this.topPos, 18, 18, 92, 220, TEXTURE, button -> {

                            CircleFabricationScreen query = (CircleFabricationScreen) ((ImageButtonFabricationRecipeSelector) button).getScreen();
                            query.setActiveRecipe(((ImageButtonFabricationRecipeSelector) button).getArrayIndex());
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
            if((Objects.equals(filter, "") || acr.getAlchemyObject().toString().contains(filter)) && !acr.getIsDistillOnly()) {
                filteredRecipes.add(acr);
            }
        }

        recipeFilterRowTotal = filteredRecipes.size() / 3;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        //Panels
        this.blit(poseStack, x - 78, y + 13, PANEL_RECIPE_X, PANEL_RECIPE_Y, PANEL_RECIPE_W, PANEL_RECIPE_H);
        this.blit(poseStack, x + 176, y + 19, PANEL_POWER_X, PANEL_POWER_Y, PANEL_POWER_W, PANEL_POWER_H);
        this.blit(poseStack, x, y, 0, 0, PANEL_MAIN_W, PANEL_MAIN_H);

        renderProgressBar(poseStack, x + 55, y + 12);

        //RenderSystem.setShader(GameRenderer::getBlockShader);
        renderSelectedRecipe(poseStack, x + 79, y + 79);

        //RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        renderPowerLevelBar(poseStack, x + 182, y + 37);
        renderIngredientPanel(poseStack, x + PANEL_INGREDIENTS_X, y + PANEL_INGREDIENTS_Y);

        renderSlotGhosts(poseStack);
    }

    private void renderIngredientPanel(PoseStack poseStack, int x, int y) {
        RenderSystem.setShaderTexture(0, TEXTURE_EXT);

        if(menu.blockEntity.getCurrentRecipe() != null) {
            AlchemicalCompositionRecipe recipe = menu.blockEntity.getCurrentRecipe();

            //A switch statement doesn't work here and I have no idea why.
            if(recipe.getComponentMateria().size() == 1) {
                this.blit(poseStack, x, y, PANEL_INGREDIENTS_U1, PANEL_INGREDIENTS_V1, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H1);
            }
            else if(recipe.getComponentMateria().size() == 2) {
                this.blit(poseStack, x, y, PANEL_INGREDIENTS_U2, PANEL_INGREDIENTS_V2, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H2);
            }
            else if(recipe.getComponentMateria().size() == 3) {
                this.blit(poseStack, x, y, PANEL_INGREDIENTS_U3, PANEL_INGREDIENTS_V3, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H3);
            }
            else if(recipe.getComponentMateria().size() == 4) {
                this.blit(poseStack, x, y, PANEL_INGREDIENTS_U4, PANEL_INGREDIENTS_V4, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H4);
            }
            else if(recipe.getComponentMateria().size() == 5) {
                this.blit(poseStack, x, y, PANEL_INGREDIENTS_U5, PANEL_INGREDIENTS_V5, PANEL_INGREDIENTS_W, PANEL_INGREDIENTS_H5);
            }

            for(int i=0; i<recipe.getComponentMateria().size(); i++) {
                Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(recipe.getComponentMateria().get(i), x + 4, y + 7 + i * 18);
            }
        }

        RenderSystem.setShaderTexture(0, TEXTURE);
    }

    private void renderButtons(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {

        int x = (width - PANEL_MAIN_W) / 2;
        int y = (height - PANEL_MAIN_H) / 2;

        b_powerLevelUp.setPosition(x+180, y+26);
        b_powerLevelUp.renderButton(poseStack, mouseX, mouseY, partialTick);
        b_powerLevelUp.active = true;
        b_powerLevelUp.visible = true;

        b_powerLevelDown.setPosition(x+180, y+71);
        b_powerLevelDown.renderButton(poseStack, mouseX, mouseY, partialTick);
        b_powerLevelDown.active = true;
        b_powerLevelDown.visible = true;

        for(ButtonData bd : recipeSelectButtons) {
            bd.getButton().setPosition(x+bd.getXOffset(), y+bd.getYOffset());
            bd.getButton().renderButton(poseStack, mouseX, mouseY, partialTick);
            bd.getButton().active = true;
            bd.getButton().visible = true;
        }
    }

    private void renderPowerLevelBar(PoseStack poseStack, int x, int y) {
        int powerLevel = menu.blockEntity.getPowerUsageSetting();

        this.blit(poseStack, x, y + (30 - powerLevel), 84, 256 - powerLevel, 8, powerLevel);
    }

    private void renderSelectedRecipe(PoseStack poseStack, int x, int y) {
        if(menu.blockEntity.getCurrentRecipe() == null) {
            this.blit(poseStack, x, y, 66, 238, 18, 18);
        }
        else {
            Minecraft.getInstance().getItemRenderer().renderAndDecorateFakeItem(menu.blockEntity.getCurrentRecipe().getAlchemyObject(), x + 1, y + 1);
        }
    }

    private void renderProgressBar(PoseStack poseStack, int x, int y) {
        int sp = CircleFabricationBlockEntity.getScaledProgress(menu.blockEntity);
        if(sp > 0)
            this.blit(poseStack, x, y , 0, 199, sp, CircleFabricationBlockEntity.PROGRESS_BAR_HEIGHT);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
        renderButtons(poseStack, delta, mouseX, mouseY);
        renderFilterBox();
        updateDisplayedRecipes(recipeFilterBox.getValue());
        renderRecipeOptions();

        //Check to see if slots need to be re-locked
        if(menu.dataSlot.get() == 1) {
            menu.setInputSlotFilters(lastClickedRecipe);
            menu.dataSlot.set(0);
        }
    }

    private void renderFilterBox() {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        recipeFilterBox.x = xOrigin - 70;
        recipeFilterBox.y = yOrigin + 21;

        if(recipeFilterBox.getValue().isEmpty())
            recipeFilterBox.setSuggestion(Component.translatable("gui.magichem.typetofilter").getString());
        else
            recipeFilterBox.setSuggestion("");

        addRenderableWidget(recipeFilterBox);
    }

    private void renderSlotGhosts(PoseStack poseStack) {
        int xOrigin = (width - PANEL_MAIN_W) / 2;
        int yOrigin = (height - PANEL_MAIN_H) / 2;

        if(menu.blockEntity.getCurrentRecipe() == null)
            return;

        AlchemicalCompositionRecipe acr = menu.blockEntity.getCurrentRecipe();

        int slotGroup = 0;
        for(ItemStack stack : acr.getComponentMateria()) {
            RenderUtils.RenderGhostedItemStack(stack, xOrigin + 8, yOrigin + 8 + (18 * slotGroup), 0.25f);
            RenderUtils.RenderGhostedItemStack(stack, xOrigin + 26, yOrigin + 8 + (18 * slotGroup), 0.25f);
            slotGroup++;
        }
    }

    private void renderRecipeOptions() {
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

                    Minecraft.getInstance().getItemRenderer().renderAndDecorateFakeItem(
                            new ItemStack(snipped.get(c).getAlchemyObject().getItem()),
                            xOrigin - 70 + x*18, yOrigin + 43 + y*18
                            );
                    c++;
                    if(c >= cLimit) break;
                }
                if(c >= cLimit) break;
            }
        }
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int x, int y) {
        int powerDraw = menu.blockEntity.getPowerDraw();
        int secWhole = menu.blockEntity.getOperationTicks() / 20;
        int secPartial = (menu.blockEntity.getOperationTicks() % 20) * 5;

        Minecraft.getInstance().font.draw(poseStack, powerDraw+"/t", 208, 26, 0xff000000);
        Minecraft.getInstance().font.draw(poseStack, secWhole+"."+(secPartial < 10 ? "0"+secPartial : secPartial)+" s", 208, 45, 0xff000000);

        AlchemicalCompositionRecipe recipe = menu.blockEntity.getCurrentRecipe();
        if(recipe != null) {
            for (int i = 0; i < recipe.getComponentMateria().size(); i++) {
                Component text = Component.literal(recipe.getComponentMateria().get(i).getCount() + " x ")
                        .append(Component.translatable("item."+MagiChemMod.MODID+"."+recipe.getComponentMateria().get(i).getItem()+".short"));
                poseStack.scale(0.5f, 0.5f, 0.5f);
                Minecraft.getInstance().font.draw(poseStack, text, 400, 169 + i * 36, 0xff000000);
                poseStack.scale(2.0f, 2.0f, 2.0f);
            }
        }
    }
}
