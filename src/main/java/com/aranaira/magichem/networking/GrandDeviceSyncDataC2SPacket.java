package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.GrandCentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.GrandDistilleryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GrandDeviceSyncDataC2SPacket {
    private final BlockPos blockPos;
    private final byte powerUsageSetting;

    public GrandDeviceSyncDataC2SPacket(BlockPos pBlockPos, int pPowerLevel) {
        this.blockPos = pBlockPos;
        this.powerUsageSetting = (byte)pPowerLevel;
    }

    public GrandDeviceSyncDataC2SPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.powerUsageSetting = buf.readByte();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeByte(powerUsageSetting);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level().getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof GrandDistilleryBlockEntity gdbe) {
                gdbe.setPowerUsageSetting(powerUsageSetting);
                gdbe.syncAndSave();
            }
            else if(entity instanceof GrandCentrifugeBlockEntity gcbe) {
                gcbe.setPowerUsageSetting(powerUsageSetting);
                gcbe.syncAndSave();
            }
        });

        return true;
    }
}
