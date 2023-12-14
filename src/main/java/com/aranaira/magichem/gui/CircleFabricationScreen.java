package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.CircleFabricationBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CircleFabricationScreen extends AbstractContainerScreen<CircleFabricationMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_fabrication.png");
    private ImageButton
            b_powerLevelUp, b_powerLevelDown;
    private static final int
            PANEL_MAIN_W = 176, PANEL_MAIN_H = 192,
            PANEL_RECIPE_X = 176, PANEL_RECIPE_Y = 0, PANEL_RECIPE_W = 80, PANEL_RECIPE_H = 126,
            PANEL_POWER_X = 176, PANEL_POWER_Y = 126, PANEL_POWER_W = 80, PANEL_POWER_H = 66;


    public CircleFabricationScreen(CircleFabricationMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    protected void init() {
        super.init();
        initializeButtons();
    }

    private void initializeButtons(){
        b_powerLevelUp = (ImageButton)this.addRenderableWidget(new ImageButton(this.leftPos + 180, this.topPos + 126, 12, 7, 232, 242, TEXTURE, button -> {
            menu.blockEntity.incrementPowerLevel();
        }));
        b_powerLevelDown = (ImageButton)this.addRenderableWidget(new ImageButton(180, 126, 12, 7, 244, 242, TEXTURE, button -> {
            menu.blockEntity.decrementPowerLevel();
        }));
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
        renderSelectedRecipe(poseStack, x + 79, y + 79);
        renderPowerLevelBar(poseStack, x + 182, y + 37);
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
    }

    private void renderPowerLevelBar(PoseStack poseStack, int x, int y) {
        int powerLevel = menu.blockEntity.getPowerLevel();

        this.blit(poseStack, x, y + (30 - powerLevel), 84, 256 - powerLevel, 8, powerLevel);
    }

    private void renderSelectedRecipe(PoseStack poseStack, int x, int y) {
        if(menu.blockEntity.getCurrentRecipeID() == "") {
            this.blit(poseStack, x, y, 66, 238, 18, 18);
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
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int x, int y) {
        int powerDraw = menu.blockEntity.getPowerDraw();
        int secWhole = menu.blockEntity.getOperationTicks() / 20;
        int secPartial = (menu.blockEntity.getOperationTicks() % 20) * 5;

        Minecraft.getInstance().font.draw(poseStack, powerDraw+"/t", 208, 26, 0xff000000);
        Minecraft.getInstance().font.draw(poseStack, secWhole+"."+(secPartial < 10 ? "0"+secPartial : secPartial)+" s", 208, 45, 0xff000000);
    }
}
