package com.aranaira.magichem.networking;

import com.aranaira.magichem.capabilities.wisdom.IWisdomCapability;
import com.aranaira.magichem.capabilities.wisdom.WisdomProvider;
import com.aranaira.magichem.gui.WisdomMenu;
import com.aranaira.magichem.item.PhilosophersStoneItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class OpenWisdomWheelC2SPacket {
    private final short wisdom;

    public OpenWisdomWheelC2SPacket(short pWisdom) {
        this.wisdom = pWisdom;
    }

    public OpenWisdomWheelC2SPacket(FriendlyByteBuf buf) {
        this.wisdom = buf.readShort();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeShort(wisdom);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();

        context.enqueueWork(() -> {
            NetworkHooks.openScreen((ServerPlayer)player, new SimpleMenuProvider(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.empty();
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pInternalPlayer) {
                    ContainerData data = new SimpleContainerData(1);
                    data.set(0, wisdom);
                    return new WisdomMenu(pContainerId, pPlayerInventory, data);
                }
            }, Component.empty()));
        });

        return true;
    }
}
