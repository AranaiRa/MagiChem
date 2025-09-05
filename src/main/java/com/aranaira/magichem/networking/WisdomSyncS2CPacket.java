package com.aranaira.magichem.networking;

import com.aranaira.magichem.capabilities.wisdom.IWisdomCapability;
import com.aranaira.magichem.capabilities.wisdom.WisdomProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WisdomSyncS2CPacket {
    private final short cardinal, intercardinal;

    public WisdomSyncS2CPacket(short pCardinal, short pIntercardinal) {
        this.cardinal = pCardinal;
        this.intercardinal = pIntercardinal;
    }

    public WisdomSyncS2CPacket(FriendlyByteBuf buf) {
        this.cardinal = buf.readShort();
        this.intercardinal = buf.readShort();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeShort(cardinal);
        buf.writeShort(intercardinal);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player != null) {
            final IWisdomCapability capability = WisdomProvider.getCapability(player);
            WisdomProvider.deserializeShorts(capability, cardinal, intercardinal);
        }

        return true;
    }
}
