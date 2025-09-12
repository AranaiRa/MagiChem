package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.StandingRetortBlockEntity;
import com.aranaira.magichem.capabilities.wisdom.IWisdomCapability;
import com.aranaira.magichem.capabilities.wisdom.WisdomProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class WisdomSyncC2SPacket {
    private final short cardinal, intercardinal;

    public WisdomSyncC2SPacket(short pCardinal, short pIntercardinal) {
        this.cardinal = pCardinal;
        this.intercardinal = pIntercardinal;
    }

    public WisdomSyncC2SPacket(FriendlyByteBuf buf) {
        this.cardinal = buf.readShort();
        this.intercardinal = buf.readShort();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeShort(cardinal);
        buf.writeShort(intercardinal);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();

        context.enqueueWork(() -> {
            final Optional<IWisdomCapability> capability = WisdomProvider.getCapability(player);
            capability.ifPresent(iWisdomCapability -> WisdomProvider.deserializeShorts(iWisdomCapability, cardinal, intercardinal));
        });

        return true;
    }
}
