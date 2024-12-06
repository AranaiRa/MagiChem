package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.mna.api.affinity.Affinity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StandingRetortSyncDataC2SPacket {
    private final BlockPos blockPos;
    private final int elementID;

    public StandingRetortSyncDataC2SPacket(BlockPos pBlockPos, int pElement) {
        this.blockPos = pBlockPos;
        this.elementID = pElement;
    }

    public StandingRetortSyncDataC2SPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.elementID = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeInt(elementID);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level().getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof StandingRetortBlockEntity srbe) {
                int elementOld = srbe.getElementID();
                srbe.setElementID(elementID);
                if(srbe.getElementID() != elementOld) srbe.syncAndSave();
            }
        });

        return true;
    }
}
