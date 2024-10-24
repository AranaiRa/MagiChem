package com.aranaira.magichem.gui.radial;

import com.aranaira.magichem.item.TravellersCompassItem;
import com.google.common.collect.Lists;
import com.mna.KeybindInit;
import com.mna.events.ClientEventHandler;
import com.mna.gui.radial.GenericRadialMenu;
import com.mna.gui.radial.components.IRadialMenuHost;
import com.mna.gui.radial.components.ItemStackRadialMenuItem;
import com.mna.gui.radial.components.RadialMenuItem;
import com.mna.items.base.IRadialInventorySelect;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

import static com.aranaira.magichem.gui.TravellersCompassMenu.*;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class TravellersCompassRadialSelect extends Screen {
    private ItemStack stackEquipped;
    private IItemHandlerModifiable inventory;
    private boolean needsRecheckStacks = true;
    private final List<ItemStackRadialMenuItem> cachedMenuItems = Lists.newArrayList();
    private final GenericRadialMenu menu;
    private Minecraft mc = Minecraft.getInstance();
    private final boolean offhand;
    private int overrideStackCount = -1;
    private ItemStackHandler deserializedInventory = new ItemStackHandler();

    public TravellersCompassRadialSelect(boolean offhand) {
        super(Component.literal("RADIAL MENU"));
        this.offhand = offhand;
        this.stackEquipped = this.getHandItem();
        this.inventory = new ItemStackHandler(12);

        if(stackEquipped.hasTag()) {
            deserializedInventory.deserializeNBT(stackEquipped.getTag().getCompound("inventory"));

            for (int i = SLOT_RADIAL_START; i<SLOT_RADIAL_START+SLOT_RADIAL_COUNT; i++) {
                this.inventory.setStackInSlot(i-SLOT_RADIAL_START, deserializedInventory.getStackInSlot(i));
            }
        }

        this.menu = new GenericRadialMenu(Minecraft.getInstance(), new IRadialMenuHost() {
            public void renderTooltip(GuiGraphics pGuiGraphics, ItemStack stack, int mouseX, int mouseY) {
            }

            public void renderTooltip(GuiGraphics pGuiGraphics, Component text, int mouseX, int mouseY) {
            }

            public Screen getScreen() {
                return TravellersCompassRadialSelect.this;
            }

            public Font getFontRenderer() {
                return TravellersCompassRadialSelect.this.font;
            }
        }) {
            public void onClickOutside() {
                this.close();
            }
        };
        this.menu.radiusOffset = 35.0F;
    }

    public TravellersCompassRadialSelect setOverrideStackCount(int count) {
        this.overrideStackCount = count;
        return this;
    }

    private ItemStack getHandItem() {
        return this.offhand ? this.mc.player.getOffhandItem() : this.mc.player.getMainHandItem();
    }

    @SubscribeEvent
    public static void overlayEvent(RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof TravellersCompassRadialSelect) {
            event.setCanceled(true);
        }

    }

    public void removed() {
        super.removed();
        ClientEventHandler.wipeOpen();
    }

    public void tick() {
        super.tick();
        this.menu.tick();
        if (this.menu.isClosed()) {
            Minecraft.getInstance().setScreen((Screen)null);
            ClientEventHandler.wipeOpen();
        }

        if (this.menu.isReady() && this.inventory != null) {
            ItemStack inHand = this.getHandItem();
            if (!(inHand.getItem() instanceof TravellersCompassItem)) {
                this.inventory = null;
            } else if (inHand.getCount() <= 0) {
                this.inventory = null;
                this.stackEquipped = null;
            } else if (this.stackEquipped != inHand) {
                this.menu.close();
            }

            if (this.inventory == null) {
                Minecraft.getInstance().setScreen((Screen)null);
            } else if (!ClientEventHandler.isKeyDown((KeyMapping) KeybindInit.RadialMenuOpen.get())) {
                this.processClick(false);
            }

        }
    }

    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        this.processClick(true);
        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    protected void processClick(boolean triggeredByMouse) {
        this.menu.clickItem();
    }

    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        if (this.inventory != null) {
            final ItemStack inHand = this.getHandItem();
            if (inHand.getItem() instanceof IRadialInventorySelect) {
                if (this.needsRecheckStacks) {
                    this.cachedMenuItems.clear();
                    int slotCount = this.overrideStackCount > 0 ? Math.min(this.inventory.getSlots(), this.overrideStackCount) : this.inventory.getSlots();

                    for(int i = 0; i < slotCount; ++i) {
                        final int iFinal = i;
                        ItemStack inSlot = this.inventory.getStackInSlot(i);
                        ItemStackRadialMenuItem item = new ItemStackRadialMenuItem(this.menu, inSlot, Component.translatable("gui.mna.spellbook.empty")) {
                            public boolean onClick() {
                                ((IRadialInventorySelect)inHand.getItem()).setSlot(TravellersCompassRadialSelect.this.mc.player, TravellersCompassRadialSelect.this.offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, iFinal, TravellersCompassRadialSelect.this.offhand, true);
                                TravellersCompassRadialSelect.this.menu.close();
                                return true;
                            }
                        };
                        item.setVisible(true);
                        this.cachedMenuItems.add(item);
                    }

                    this.menu.clear();
                    this.menu.addAll(this.cachedMenuItems);
                    this.needsRecheckStacks = false;
                }

                if (this.cachedMenuItems.stream().noneMatch(RadialMenuItem::isVisible)) {
                    this.menu.setCentralText(Component.translatable("gui.mna.spellbook.empty"));
                } else {
                    this.menu.setCentralText((Component)null);
                }

                this.menu.draw(pGuiGraphics, partialTicks, mouseX, mouseY);
            }
        }
    }

    public boolean isPauseScreen() {
        return false;
    }
}
