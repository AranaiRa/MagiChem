package com.aranaira.magichem.networking;

import com.aranaira.magichem.registry.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TravellersCompassSyncC2SPacket {
    private final int index;

    public TravellersCompassSyncC2SPacket(int pIndex) {
        this.index = pIndex;
    }

    public TravellersCompassSyncC2SPacket(FriendlyByteBuf buf) {
        this.index = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(index);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        ItemStack itemToModify = player.getInventory().getSelected();

        if(itemToModify.getItem() == ItemRegistry.TRAVELLERS_COMPASS.get()) {
            CompoundTag nbt = itemToModify.getOrCreateTag();
            nbt.putInt("index", index);
        }

        player.sendSystemMessage(Component.literal("received index = "+index));

        return true;
    }
}
