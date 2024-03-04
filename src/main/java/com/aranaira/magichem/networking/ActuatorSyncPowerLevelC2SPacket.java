package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.ActuatorWaterBlockEntity;
import com.aranaira.magichem.foundation.IBlockWithPowerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ActuatorSyncPowerLevelC2SPacket {
    private final BlockPos blockPos;
    private final boolean powerLevelUp;

    public ActuatorSyncPowerLevelC2SPacket(BlockPos pBlockPos, boolean pPowerLevelUp) {
        this.blockPos = pBlockPos;
        this.powerLevelUp = pPowerLevelUp;
    }

    public ActuatorSyncPowerLevelC2SPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.powerLevelUp = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeBoolean(powerLevelUp);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level().getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof IBlockWithPowerLevel bwpl) {
                if (powerLevelUp) {
                    bwpl.increasePowerLevel();
                } else {
                    bwpl.decreasePowerLevel();
                }
            }
        });

        return true;
    }
}
