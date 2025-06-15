package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.CentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.FuseryBlockEntity;
import com.aranaira.magichem.block.entity.GrandCentrifugeBlockEntity;
import com.aranaira.magichem.block.entity.GrandFuseryBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractFabricationBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractFixationBlockEntity;
import com.aranaira.magichem.block.entity.ext.AbstractSeparationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DeviceRecipeClearC2SPacket {
    private final BlockPos blockPos;

    public DeviceRecipeClearC2SPacket(BlockPos pBlockPos) {
        this.blockPos = pBlockPos;
    }

    public DeviceRecipeClearC2SPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level().getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof AbstractFixationBlockEntity fixation) {
                fixation.clearRecipeAfterNextProcess = true;
            }
            else if(entity instanceof AbstractSeparationBlockEntity separation) {
                separation.clearRecipeAfterNextProcess = true;
            }
            else if(entity instanceof AbstractFabricationBlockEntity fabrication) {
                fabrication.clearRecipeAfterNextProcess = true;
            }
        });

        return true;
    }
}
