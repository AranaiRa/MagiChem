package com.aranaira.magichem.networking;

import com.aranaira.magichem.capabilities.wisdom.IWisdomCapability;
import com.aranaira.magichem.capabilities.wisdom.WisdomProvider;
import com.aranaira.magichem.gui.WisdomMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class ToggleWisdomC2SPacket {
    public ToggleWisdomC2SPacket() {

    }

    public ToggleWisdomC2SPacket(FriendlyByteBuf buf) {

    }

    public void toBytes(FriendlyByteBuf buf) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();

        context.enqueueWork(() -> {
            if(player != null) {
                final Optional<IWisdomCapability> capQuery = WisdomProvider.getCapability(player);
                capQuery.ifPresent(iWisdomCapability -> iWisdomCapability.setIsDisabled(!iWisdomCapability.getIsDisabled()));
            }
        });

        return true;
    }
}
