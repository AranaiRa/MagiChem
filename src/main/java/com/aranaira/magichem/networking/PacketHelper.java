package com.aranaira.magichem.networking;

import com.aranaira.magichem.capabilities.wisdom.IWisdomCapability;
import com.aranaira.magichem.capabilities.wisdom.WisdomProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public class PacketHelper {
    public static void handleWisdomSyncPacket(short cardinal, short intercardinal) {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player != null) {
            final Optional<IWisdomCapability> capability = WisdomProvider.getCapability(player);
            capability.ifPresent(iWisdomCapability -> WisdomProvider.deserializeShorts(iWisdomCapability, cardinal, intercardinal));
        }
    }

    public static void handleWisdomToggleResetPacket() {
        Player player = Minecraft.getInstance().player;

        if(player != null && WisdomProvider.getCapability(player).isPresent()) {
            WisdomProvider.getCapability(player).get().setIsDisabled(false);
        }
    }
}
