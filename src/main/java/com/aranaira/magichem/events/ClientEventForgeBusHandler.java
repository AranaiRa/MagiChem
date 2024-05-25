package com.aranaira.magichem.events;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.gui.radial.SublimationPrimerRadialSelect;
import com.aranaira.magichem.item.AdmixtureItem;
import com.aranaira.magichem.item.EssentiaItem;
import com.aranaira.magichem.item.MateriaItem;
import com.aranaira.magichem.item.SublimationPrimerItem;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.KeybindInit;
import com.mna.gui.radial.SpellRadialSelect;
import com.mna.items.sorcery.ItemSpellBook;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
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
            while(((KeyMapping)KeybindInit.RadialMenuOpen.get()).consumeClick()) {
                if (mc.screen == null) {
                    ItemStack inHand = mc.player.getMainHandItem();
                    boolean checkOffhand = true;
                    if (inHand.getItem() instanceof SublimationPrimerItem) {
                        mc.setScreen(new SublimationPrimerRadialSelect(false));
                        checkOffhand = false;
                    }

                    if (checkOffhand) {
                        inHand = mc.player.getOffhandItem();
                        if (inHand.getItem() instanceof SublimationPrimerItem) {
                            mc.setScreen(new SublimationPrimerRadialSelect(true));
                        }
                    }
                }
            }
        }

        toolMenuKeyWasDown = toolMenuKeyIsDown;
    }
}
