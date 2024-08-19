package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
import com.aranaira.magichem.block.entity.VariegatorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class VariegatorSyncDataC2SPacket {
    private final BlockPos blockPos;
    private final int selectedColor;

    public VariegatorSyncDataC2SPacket(BlockPos pBlockPos, int pSelectedColor) {
        this.blockPos = pBlockPos;
        this.selectedColor = pSelectedColor;
    }

    public VariegatorSyncDataC2SPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.selectedColor = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeInt(selectedColor);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level().getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof VariegatorBlockEntity vbe) {
                vbe.selectedColor = selectedColor;
                vbe.syncAndSave();
            }
        });

        return true;
    }
}
