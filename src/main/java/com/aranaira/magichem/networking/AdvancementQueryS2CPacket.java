package com.aranaira.magichem.networking;

import com.aranaira.magichem.util.AdvancementUtil;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AdvancementQueryS2CPacket {
    public final ResourceLocation key;
    public final Component title, description;
    public AdvancementQueryS2CPacket(Advancement source) {
        this.key = source.getId();
        this.title = source.getDisplay().getTitle();
        this.description = source.getDisplay().getDescription();
    }
    public AdvancementQueryS2CPacket(FriendlyByteBuf buf) {
        this.key = buf.readResourceLocation();
        this.title = buf.readComponent();
        this.description = buf.readComponent();
    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(key);
        buf.writeComponent(title);
        buf.writeComponent(description);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        AdvancementUtil.onReceiveFetchedAdvancement(this);
        return true;
    }
}
