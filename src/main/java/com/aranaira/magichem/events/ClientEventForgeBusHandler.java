package com.aranaira.magichem.events;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.gui.radial.*;
import com.aranaira.magichem.item.*;
import com.aranaira.magichem.networking.OpenWisdomWheelC2SPacket;
import com.aranaira.magichem.networking.WisdomSyncC2SPacket;
import com.aranaira.magichem.registry.KeybindRegistry;
import com.aranaira.magichem.registry.PacketRegistry;
import com.mna.KeybindInit;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(
        modid = MagiChemMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class ClientEventForgeBusHandler {
    private static final TagKey<Item>
            TAG_MAGICHEM_WISDOM_STONES = ItemTags.create(new ResourceLocation(MagiChemMod.MODID, "wisdom_stones"));

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (!mc.isPaused() && mc.screen == null) {
            handleRadialKeyDown();

            if (event.phase == TickEvent.Phase.END) {
                if (KeybindRegistry.OpenWisdomWheel.get().isDown()) {
                    CuriosApi.getCuriosInventory(mc.player).ifPresent(curiosInventory -> {
                        curiosInventory.getStacksHandler("wisdom").ifPresent(slotsInventory -> {
                            for (int i = 0; i < slotsInventory.getStacks().getSlots(); i++) {
                                ItemStack stack = slotsInventory.getStacks().getStackInSlot(i);
                                if (stack.is(TAG_MAGICHEM_WISDOM_STONES) && stack.getItem() instanceof PhilosophersStoneItem psi) {
                                    PacketRegistry.sendToServer(new OpenWisdomWheelC2SPacket(
                                            (short)psi.getWisdom()
                                    ));
                                }
                            }
                        });
                    });
                }
            }
        }
    }

    //Shamelessly stolen from MnA
    private static boolean toolMenuKeyWasDown;
    private static void handleRadialKeyDown() {
        Minecraft mc = Minecraft.getInstance();
        boolean toolMenuKeyIsDown = ((KeyMapping) KeybindInit.RadialMenuOpen.get()).isDown();
        if (toolMenuKeyIsDown && !toolMenuKeyWasDown) {
            if (mc.screen == null) {
                ItemStack inHand = mc.player.getMainHandItem();
                boolean checkOffhand = true;
                if (inHand.getItem() instanceof SublimationPrimerItem) {
                    mc.setScreen(new SublimationPrimerRadialSelect(false));
                    checkOffhand = false;
                } else if (inHand.getItem() instanceof TravellersCompassItem) {
                    mc.setScreen(new TravellersCompassRadialSelect(false));
                    checkOffhand = false;
                }

                if (checkOffhand) {
                    inHand = mc.player.getOffhandItem();
                    if (inHand.getItem() instanceof SublimationPrimerItem) {
                        mc.setScreen(new SublimationPrimerRadialSelect(true));
                    } else if (inHand.getItem() instanceof TravellersCompassItem) {
                        mc.setScreen(new TravellersCompassRadialSelect(true));
                    }
                }
            }
        }

        toolMenuKeyWasDown = toolMenuKeyIsDown;
    }
}
