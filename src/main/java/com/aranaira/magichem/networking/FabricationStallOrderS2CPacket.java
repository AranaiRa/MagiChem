package com.aranaira.magichem.networking;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.CircleFabricationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FabricationStallOrderS2CPacket {
    private final BlockPos blockPos;
    private final boolean newStallStatus;

    public FabricationStallOrderS2CPacket(BlockPos pBlockPos, boolean pNewStallStatus) {
        this.blockPos = pBlockPos;
        this.newStallStatus = pNewStallStatus;
    }

    public FabricationStallOrderS2CPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.newStallStatus = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeBoolean(newStallStatus);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level.getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof CircleFabricationBlockEntity cfbe) {
                MagiChemMod.LOGGER.debug("&&&& CLIENTSIDE: Setting stalled state to "+newStallStatus);

                cfbe.setStallState(newStallStatus);
            }
        });

        return true;
    }
}
