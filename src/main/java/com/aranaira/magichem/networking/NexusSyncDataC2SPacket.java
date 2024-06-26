package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
import com.aranaira.magichem.block.entity.CircleFabricationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NexusSyncDataC2SPacket {
    private final BlockPos blockPos;
    private final byte powerUsageSetting;

    public NexusSyncDataC2SPacket(BlockPos pBlockPos, int pPowerLevel) {
        this.blockPos = pBlockPos;
        this.powerUsageSetting = (byte)pPowerLevel;
    }

    public NexusSyncDataC2SPacket(FriendlyByteBuf buf) {
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
            if(entity instanceof AlchemicalNexusBlockEntity anbe) {
                anbe.setPowerUsageSetting(powerUsageSetting);
                anbe.syncAndSave();
            }
        });

        return true;
    }
}
