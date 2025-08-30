package com.aranaira.magichem.networking;

import com.aranaira.magichem.MagiChemMod;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class AdvancementQueryC2SPacket {
    public final ResourceLocation key;
    public AdvancementQueryC2SPacket(ResourceLocation key) {
        this.key = key;
    }
    public AdvancementQueryC2SPacket(FriendlyByteBuf buf) {
        this.key = buf.readResourceLocation();
    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(key);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        ServerAdvancementManager serverManager = supplier.get().getSender().server.getAdvancements();
        Advancement query = serverManager.getAdvancement(key);
        if (query == null) {
            // send an error message maybe?
            return true;
        }
        MagiChemMod.CHANNEL.send(PacketDistributor.PLAYER.with(supplier.get()::getSender), new AdvancementQueryS2CPacket(query));
        return true;
    }
}
