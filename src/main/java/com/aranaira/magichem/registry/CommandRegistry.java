package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.commands.MagiChemCommand;
import com.mna.commands.CommandStructureDiff;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommandRegistry {
    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        MagiChemCommand.register(event.getDispatcher());
        if (MagiChemMod.INSTANCE.isDebug) {
            CommandStructureDiff.register(event.getDispatcher());
        }
    }
}
