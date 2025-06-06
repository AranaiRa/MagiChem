package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.block.entity.ext.AbstractFabricationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FabricationBatchSizeC2SPacket {
    private final BlockPos blockPos;
    private final byte newBatchSize;

    public FabricationBatchSizeC2SPacket(BlockPos pBlockPos, int pNewBatchSize) {
        this.blockPos = pBlockPos;
        this.newBatchSize = (byte)pNewBatchSize;
    }

    public FabricationBatchSizeC2SPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.newBatchSize = buf.readByte();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeByte(newBatchSize);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level().getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof AbstractFabricationBlockEntity afbe) {
                afbe.setBatchSize(newBatchSize);
            }
        });

        return true;
    }
}
