package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.CircleFabricationBlockEntity;
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

public class MateriaVesselSyncDataS2CPacket {
    private final BlockPos blockPos;
    private final Item materiaType;
    private final int materiaCount;

    public MateriaVesselSyncDataS2CPacket(BlockPos pBlockPos, Item pMateriaType, int pMateriaCount) {
        this.blockPos = pBlockPos;
        this.materiaType = pMateriaType;
        this.materiaCount = pMateriaCount;
    }

    public MateriaVesselSyncDataS2CPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.materiaType = buf.readItem().getItem();
        this.materiaCount = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        if(materiaType == null)
            buf.writeItem(ItemStack.EMPTY);
        else
            buf.writeItem(new ItemStack(materiaType, 1));
        buf.writeInt(materiaCount);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level.getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof MateriaVesselBlockEntity mvbe) {
                mvbe.setContents((MateriaItem) materiaType, materiaCount);
            }
        });

        return true;
    }
}
