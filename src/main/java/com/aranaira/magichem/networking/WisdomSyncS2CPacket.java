package com.aranaira.magichem.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
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
        supplier.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PacketHelper.handleWisdomSyncPacket(cardinal, intercardinal));
        });
        supplier.get().setPacketHandled(true);
        return true;
    }
}
