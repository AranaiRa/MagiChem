package com.aranaira.magichem.networking;

import com.aranaira.magichem.capabilities.wisdom.IWisdomCapability;
import com.aranaira.magichem.capabilities.wisdom.WisdomProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class ResetWisdomToggleS2CPacket {
    public ResetWisdomToggleS2CPacket() {

    }

    public ResetWisdomToggleS2CPacket(FriendlyByteBuf buf) {

    }

    public void toBytes(FriendlyByteBuf buf) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = Minecraft.getInstance().player;

        context.enqueueWork(() -> {
            if(player != null && WisdomProvider.getCapability(player).isPresent()) {
                WisdomProvider.getCapability(player).get().setIsDisabled(false);
            }
        });

        return true;
    }
}
