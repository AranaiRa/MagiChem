package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.MateriaVesselBlockEntity;
import com.aranaira.magichem.item.MateriaItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MateriaVesselSyncRequstC2SPacket {
    private final BlockPos blockPos;

    public MateriaVesselSyncRequstC2SPacket(BlockPos pBlockPos) {
        this.blockPos = pBlockPos;
    }

    public MateriaVesselSyncRequstC2SPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level.getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof MateriaVesselBlockEntity mvbe) {

            }
        });

        return true;
    }
}
