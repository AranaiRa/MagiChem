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

public class ActuatorToggleEldrinC2SPacket {
    private final BlockPos blockPos;
    private final boolean eldrinFlag;

    public ActuatorToggleEldrinC2SPacket(BlockPos pBlockPos, boolean pEldrinFlag) {
        this.blockPos = pBlockPos;
        this.eldrinFlag = pEldrinFlag;
    }

    public ActuatorToggleEldrinC2SPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.eldrinFlag = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeBoolean(eldrinFlag);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level().getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof AbstractDirectionalPluginBlockEntity adpbe) {
                adpbe.doEldrinPowerConsumption = eldrinFlag;
            }
        });

        return true;
    }
}
