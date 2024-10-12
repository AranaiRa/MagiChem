package com.aranaira.magichem.events;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.gui.radial.*;
import com.aranaira.magichem.item.*;
import com.mna.KeybindInit;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = MagiChemMod.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class ClientEventForgeBusHandler {
    @SubscribeEvent
    public static void handleKeys(ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            handleRadialKeyDown();
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
