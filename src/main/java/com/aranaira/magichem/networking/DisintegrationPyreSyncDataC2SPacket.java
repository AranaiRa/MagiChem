package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
import com.aranaira.magichem.block.entity.DisintegrationPyreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DisintegrationPyreSyncDataC2SPacket {
    private final BlockPos blockPos;
    private final int percent;

    public DisintegrationPyreSyncDataC2SPacket(BlockPos pBlockPos, int pPercent) {
        this.blockPos = pBlockPos;
        this.percent = pPercent;
    }

    public DisintegrationPyreSyncDataC2SPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.percent = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeInt(percent);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level().getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof DisintegrationPyreBlockEntity pyre) {
                pyre.setPercent(percent);
                pyre.syncAndSave();
            }
        });

        return true;
    }
}
