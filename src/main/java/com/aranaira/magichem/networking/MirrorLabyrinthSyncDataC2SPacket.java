package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.CircleFabricationBlockEntity;
import com.aranaira.magichem.block.entity.GrandCircleFabricationBlockEntity;
import com.aranaira.magichem.block.entity.MirrorLabyrinthBlockEntity;
import com.aranaira.magichem.item.MateriaItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MirrorLabyrinthSyncDataC2SPacket {
    private final BlockPos blockPos;
    private final Item recipeItem;
    private final byte powerUsageSetting;

    public MirrorLabyrinthSyncDataC2SPacket(BlockPos pBlockPos, Item pRecipeItem, int pPowerLevel) {
        this.blockPos = pBlockPos;
        this.recipeItem = pRecipeItem;
        this.powerUsageSetting = (byte)pPowerLevel;
    }

    public MirrorLabyrinthSyncDataC2SPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.recipeItem = buf.readItem().getItem();
        this.powerUsageSetting = buf.readByte();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        if(recipeItem == null)
            buf.writeItem(ItemStack.EMPTY);
        else
            buf.writeItem(new ItemStack(recipeItem, 1));
        buf.writeByte(powerUsageSetting);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level().getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof MirrorLabyrinthBlockEntity mlbe) {
                mlbe.setPowerUsageSetting(powerUsageSetting);
                if(recipeItem instanceof MateriaItem mi)
                    mlbe.setActiveMateriaType(mi);
            }
        });

        return true;
    }
}
