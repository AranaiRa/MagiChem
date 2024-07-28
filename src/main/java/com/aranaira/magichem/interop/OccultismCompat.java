package com.aranaira.magichem.interop;

import com.aranaira.magichem.events.compat.OccultismEventHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public class OccultismCompat {
    public static void handleRegistration(IEventBus modBus) {
        MinecraftForge.EVENT_BUS.addListener(OccultismEventHelper::onBlockActivated);
    }
}
