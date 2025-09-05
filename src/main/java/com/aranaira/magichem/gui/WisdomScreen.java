package com.aranaira.magichem.gui;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.capabilities.wisdom.IWisdomCapability;
import com.aranaira.magichem.capabilities.wisdom.WisdomProvider;
import com.aranaira.magichem.networking.NexusSyncDataC2SPacket;
import com.aranaira.magichem.networking.WisdomSyncC2SPacket;
import com.aranaira.magichem.registry.ItemRegistry;
import com.aranaira.magichem.registry.PacketRegistry;
import com.mna.api.spells.attributes.Attribute;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WisdomScreen extends AbstractContainerScreen<WisdomMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(MagiChemMod.MODID, "textures/gui/gui_wisdom.png");
    public static final ItemStack[] WISDOM_STONES = new ItemStack[]{
            new ItemStack(ItemRegistry.INERT_WISDOM_STONE.get()),
            new ItemStack(ItemRegistry.ASHEN_WISDOM_STONE.get()),
            new ItemStack(ItemRegistry.BLEACHED_WISDOM_STONE.get()),
            new ItemStack(ItemRegistry.YELLOWED_WISDOM_STONE.get()),
            new ItemStack(ItemRegistry.FLUSHED_WISDOM_STONE.get()),
            new ItemStack(ItemRegistry.PHILOSOPHERS_STONE.get())
    };
    private boolean buttonsReadyForWisdomShift = false, buttonsShiftedToFinalPosition = false;
    private ImageButton
        bRadiusUp, bRadiusDown, bRangeUp, bRangeDown, bDurationUp, bDurationDown, bMagnitudeUp, bMagnitudeDown,
        bDamageUp, bDamageDown, bLesserMagnitudeUp, bLesserMagnitudeDown, bDelayUp, bDelayDown, bSpeedUp, bSpeedDown;

    public static final int
        PANEL_MAIN_W = 210, PANEL_MAIN_H = 228;

    public WisdomScreen(WisdomMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        initializeSettingButtons();
    }

    private void initializeSettingButtons() {
        //Radius
        {
            bRadiusUp = this.addRenderableWidget(new ImageButton(0, 0, 6, 4, 220, 0, TEXTURE, button -> {
                menu.capability.incrementValue(Attribute.RADIUS, menu.getWisdom());
            }));

            bRadiusDown = this.addRenderableWidget(new ImageButton(0, 0, 6, 4, 226, 0, TEXTURE, button -> {
                menu.capability.decrementValue(Attribute.RADIUS, menu.getWisdom());
            }));

            bRadiusUp.visible = false;
            bRadiusDown.visible = false;
        }

        //Range
        {
            bRangeUp = this.addRenderableWidget(new ImageButton(0, 0, 5, 5, 227, 20, TEXTURE, button -> {
                menu.capability.incrementValue(Attribute.RANGE, menu.getWisdom());
            }));

            bRangeDown = this.addRenderableWidget(new ImageButton(0, 0, 5, 5, 222, 20, TEXTURE, button -> {
                menu.capability.decrementValue(Attribute.RANGE, menu.getWisdom());
            }));

            bRangeUp.visible = false;
            bRangeDown.visible = false;
        }

        //Duration
        {
            bDurationUp = this.addRenderableWidget(new ImageButton(0, 0, 4, 6, 228, 8, TEXTURE, button -> {
                menu.capability.incrementValue(Attribute.DURATION, menu.getWisdom());
            }));

            bDurationDown = this.addRenderableWidget(new ImageButton(0, 0, 4, 6, 224, 8, TEXTURE, button -> {
                menu.capability.decrementValue(Attribute.DURATION, menu.getWisdom());
            }));

            bDurationUp.visible = false;
            bDurationDown.visible = false;
        }

        //Magnitude
        {
            bMagnitudeUp = this.addRenderableWidget(new ImageButton(0, 0, 5, 5, 227, 30, TEXTURE, button -> {
                menu.capability.incrementValue(Attribute.MAGNITUDE, menu.getWisdom());
            }));

            bMagnitudeDown = this.addRenderableWidget(new ImageButton(0, 0, 5, 5, 222, 30, TEXTURE, button -> {
                menu.capability.decrementValue(Attribute.MAGNITUDE, menu.getWisdom());
            }));

            bMagnitudeUp.visible = false;
            bMagnitudeDown.visible = false;
        }

        //Damage
        {
            bDamageUp = this.addRenderableWidget(new ImageButton(0, 0, 6, 4, 226, 0, TEXTURE, button -> {
                menu.capability.incrementValue(Attribute.DAMAGE, menu.getWisdom());
            }));

            bDamageDown = this.addRenderableWidget(new ImageButton(0, 0, 6, 4, 220, 0, TEXTURE, button -> {
                menu.capability.decrementValue(Attribute.DAMAGE, menu.getWisdom());
            }));

            bDamageUp.visible = false;
            bDamageDown.visible = false;
        }

        //Lesser Magnitude
        {
            bLesserMagnitudeUp = this.addRenderableWidget(new ImageButton(0, 0, 5, 5, 222, 20, TEXTURE, button -> {
                menu.capability.incrementValue(Attribute.LESSER_MAGNITUDE, menu.getWisdom());
            }));

            bLesserMagnitudeDown = this.addRenderableWidget(new ImageButton(0, 0, 5, 5, 227, 20, TEXTURE, button -> {
                menu.capability.decrementValue(Attribute.LESSER_MAGNITUDE, menu.getWisdom());
            }));

            bLesserMagnitudeUp.visible = false;
            bLesserMagnitudeDown.visible = false;
        }

        //Delay
        {
            bDelayUp = this.addRenderableWidget(new ImageButton(0, 0, 4, 6, 224, 8, TEXTURE, button -> {
                menu.capability.incrementValue(Attribute.DELAY, menu.getWisdom());
            }));

            bDelayDown = this.addRenderableWidget(new ImageButton(0, 0, 4, 6, 228, 8, TEXTURE, button -> {
                menu.capability.decrementValue(Attribute.DELAY, menu.getWisdom());
            }));

            bDelayUp.visible = false;
            bDelayDown.visible = false;
        }

        //Speed
        {
            bSpeedUp = this.addRenderableWidget(new ImageButton(0, 0, 5, 5, 222, 30, TEXTURE, button -> {
                menu.capability.incrementValue(Attribute.SPEED, menu.getWisdom());
            }));

            bSpeedDown = this.addRenderableWidget(new ImageButton(0, 0, 5, 5, 227, 30, TEXTURE, button -> {
                menu.capability.decrementValue(Attribute.SPEED, menu.getWisdom());
            }));

            bSpeedUp.visible = false;
            bSpeedDown.visible = false;
        }
    }

    private void setButtonPositionsForWisdomLimits() {
        //Radius
        {
            int startX = 85;
            int startY = -5;
            int padding = 8;
            int perStep = 5;

            int limit = menu.capability.getLimit(Attribute.RADIUS, menu.getWisdom());

            if(limit > 0) {
                bRadiusUp.setPosition(this.leftPos + startX, this.topPos + startY - padding - (perStep * limit));
                bRadiusDown.setPosition(this.leftPos + startX, this.topPos + startY);
                bRadiusUp.visible = true;
                bRadiusDown.visible = true;
            }
        }

        //Range
        {
            int startX = 128;
            int startY = 10;
            int padding = 6;
            int perStep = 3;

            int limit = menu.capability.getLimit(Attribute.RANGE, menu.getWisdom());

            if(limit > 0) {
                bRangeUp.setPosition(this.leftPos + startX + padding + (perStep * limit), this.topPos + startY - padding - (perStep * limit));
                bRangeDown.setPosition(this.leftPos + startX, this.topPos + startY);
                bRangeUp.visible = true;
                bRangeDown.visible = true;
            }
        }

        //Duration
        {
            int startX = 144;
            int startY = 52;
            int padding = 7;
            int perStep = 5;

            int limit = menu.capability.getLimit(Attribute.DURATION, menu.getWisdom());

            if(limit > 0) {
                bDurationUp.setPosition(this.leftPos + startX + padding + (perStep * limit), this.topPos + startY);
                bDurationDown.setPosition(this.leftPos + startX, this.topPos + startY);
                bDurationUp.visible = true;
                bDurationDown.visible = true;
            }
        }

        //Magnitude
        {
            int startX = 128;
            int startY = 95;
            int padding = 6;
            int perStep = 3;

            int limit = menu.capability.getLimit(Attribute.MAGNITUDE, menu.getWisdom());

            if(limit > 0) {
                bMagnitudeUp.setPosition(this.leftPos + startX + padding + (perStep * limit), this.topPos + startY + padding + (perStep * limit));
                bMagnitudeDown.setPosition(this.leftPos + startX, this.topPos + startY);
                bMagnitudeUp.visible = true;
                bMagnitudeDown.visible = true;
            }
        }

        //Damage
        {
            int startX = 85;
            int startY = 111;
            int padding = 7;
            int perStep = 5;

            int limit = menu.capability.getLimit(Attribute.DAMAGE, menu.getWisdom());

            if(limit > 0) {
                bDamageUp.setPosition(this.leftPos + startX, this.topPos + startY + padding + (perStep * limit));
                bDamageDown.setPosition(this.leftPos + startX, this.topPos + startY);
                bDamageUp.visible = true;
                bDamageDown.visible = true;
            }
        }

        //Lesser Magnitude
        {
            int startX = 43;
            int startY = 95;
            int padding = 6;
            int perStep = 3;

            int limit = menu.capability.getLimit(Attribute.LESSER_MAGNITUDE, menu.getWisdom());

            if(limit > 0) {
                bLesserMagnitudeUp.setPosition(this.leftPos + startX - padding - (perStep * limit), this.topPos + startY + padding + (perStep * limit));
                bLesserMagnitudeDown.setPosition(this.leftPos + startX, this.topPos + startY);
                bLesserMagnitudeUp.visible = true;
                bLesserMagnitudeDown.visible = true;
            }
        }

        //Delay
        {
            int startX = 28;
            int startY = 52;
            int padding = 7;
            int perStep = 5;

            int limit = menu.capability.getLimit(Attribute.DELAY, menu.getWisdom());

            if(limit > 0) {
                bDelayUp.setPosition(this.leftPos + startX - padding - (perStep * limit), this.topPos + startY);
                bDelayDown.setPosition(this.leftPos + startX, this.topPos + startY);
                bDelayUp.visible = true;
                bDelayDown.visible = true;
            }
        }

        //Speed
        {
            int startX = 43;
            int startY = 10;
            int padding = 6;
            int perStep = 3;

            int limit = menu.capability.getLimit(Attribute.SPEED, menu.getWisdom());

            if(limit > 0) {
                bSpeedUp.setPosition(this.leftPos + startX - padding - (perStep * limit), this.topPos + startY - padding - (perStep * limit));
                bSpeedDown.setPosition(this.leftPos + startX, this.topPos + startY);
                bSpeedUp.visible = true;
                bSpeedDown.visible = true;
            }
        }
    }

    @Override
    public void onClose() {
        final Pair<Short, Short> cardinalIntercardinalPair = WisdomProvider.serializeShorts(menu.capability);

        PacketRegistry.sendToServer(new WisdomSyncC2SPacket(
                cardinalIntercardinalPair.getFirst(),
                cardinalIntercardinalPair.getSecond()
        ));

        super.onClose();
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
        pGuiGraphics.renderFakeItem(WISDOM_STONES[menu.getWisdom()], (x/2)+45, (y/2)+35);
        pGuiGraphics.pose().popPose();

        //Modifier Icons
        pGuiGraphics.blit(TEXTURE, x+97, y+36, 240, 128, 16, 16); //Radius
        pGuiGraphics.blit(TEXTURE, x+67, y+48, 240, 112, 16, 16); //Speed
        pGuiGraphics.blit(TEXTURE, x+127, y+48, 240, 144, 16, 16); //Range
        pGuiGraphics.blit(TEXTURE, x+55, y+78, 240, 96, 16, 16); //Delay
        pGuiGraphics.blit(TEXTURE, x+139, y+78, 240, 160, 16, 16); //Duration
        pGuiGraphics.blit(TEXTURE, x+67, y+108, 240, 80, 16, 16); //Lesser Magnitude
        pGuiGraphics.blit(TEXTURE, x+127, y+108, 240, 176, 16, 16); //Magnitude
        pGuiGraphics.blit(TEXTURE, x+97, y+120, 240, 64, 16, 16); //Damage

        //Upgrade pips
        {
            //Radius
            {
                int boost = menu.capability.getValue(Attribute.RADIUS);
                int limit = menu.capability.getLimit(Attribute.RADIUS, menu.getWisdom());

                for(int i=0; i<limit; i++) {
                    int ux = i < boost ? 244 : 232;
                    pGuiGraphics.blit(TEXTURE, x+99, y+19 - i*5, ux, 0, 12, 6);
                }
            }

            //Range
            {
                int boost = menu.capability.getValue(Attribute.RANGE);
                int limit = menu.capability.getLimit(Attribute.RANGE, menu.getWisdom());

                for(int i=0; i<limit; i++) {
                    int ux = i < boost ? 246 : 236;
                    pGuiGraphics.blit(TEXTURE, x+146 + i*3, y+35 - i*3, ux, 24, 10, 10);
                }
            }

            //Duration
            {
                int boost = menu.capability.getValue(Attribute.DURATION);
                int limit = menu.capability.getLimit(Attribute.DURATION, menu.getWisdom());

                for(int i=0; i<limit; i++) {
                    int ux = i < boost ? 250 : 244;
                    pGuiGraphics.blit(TEXTURE, x+166 + i*5, y+80, ux, 12, 6, 12);
                }
            }

            //Magnitude
            {
                int boost = menu.capability.getValue(Attribute.MAGNITUDE);
                int limit = menu.capability.getLimit(Attribute.MAGNITUDE, menu.getWisdom());

                for(int i=0; i<limit; i++) {
                    int ux = i < boost ? 246 : 236;
                    pGuiGraphics.blit(TEXTURE, x+146 + i*3, y+127 + i*3, ux, 54, 10, 10);
                }
            }

            //Damage
            {
                int boost = menu.capability.getValue(Attribute.DAMAGE);
                int limit = menu.capability.getLimit(Attribute.DAMAGE, menu.getWisdom());

                for(int i=0; i<limit; i++) {
                    int ux = i < boost ? 244 : 232;
                    pGuiGraphics.blit(TEXTURE, x+99, y+147 + i*5, ux, 6, 12, 6);
                }
            }

            //Lesser Magnitude
            {
                int boost = menu.capability.getValue(Attribute.LESSER_MAGNITUDE);
                int limit = menu.capability.getLimit(Attribute.LESSER_MAGNITUDE, menu.getWisdom());

                for(int i=0; i<limit; i++) {
                    int ux = i < boost ? 246 : 236;
                    pGuiGraphics.blit(TEXTURE, x+54 - i*3, y+127 + i*3, ux, 44, 10, 10);
                }
            }

            //Delay
            {
                int boost = menu.capability.getValue(Attribute.DELAY);
                int limit = menu.capability.getLimit(Attribute.DELAY, menu.getWisdom());

                for(int i=0; i<limit; i++) {
                    int ux = i < boost ? 238 : 232;
                    pGuiGraphics.blit(TEXTURE, x+38 - i*5, y+80, ux, 12, 6, 12);
                }
            }

            //Speed
            {
                int boost = menu.capability.getValue(Attribute.SPEED);
                int limit = menu.capability.getLimit(Attribute.SPEED, menu.getWisdom());

                for(int i=0; i<limit; i++) {
                    int ux = i < boost ? 246 : 236;
                    pGuiGraphics.blit(TEXTURE, x+54 - i*3, y+35 - i*3, ux, 34, 10, 10);
                }
            }
        }

        if(!buttonsShiftedToFinalPosition) {
            buttonsReadyForWisdomShift = menu.getWisdom() > 0;
            if(buttonsReadyForWisdomShift) {
                setButtonPositionsForWisdomLimits();
                buttonsShiftedToFinalPosition = true;
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

//        if(pX >= x + 93 && pX < x + 93 + 18 && pY >= y + 22 && pY < y + 22 + 18) {
//            if(menu.itemHandler.getStackInSlot(ChargingTalismanMenu.SLOT_SPIKE) == ItemStack.EMPTY) {
//                tooltipContents.add(Component.empty()
//                        .append(Component.translatable("tooltip.magichem.gui.charging_talisman.spike.line1"))
//                );
//                tooltipContents.add(Component.empty());
//                tooltipContents.add(Component.empty()
//                        .append(Component.translatable("tooltip.magichem.gui.charging_talisman.spike.line2"))
//                );
//                pGuiGraphics.renderTooltip(font, tooltipContents, Optional.empty(), pX, pY);
//            }
//        }
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

    }
}
