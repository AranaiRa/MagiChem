package com.aranaira.magichem.networking;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.block.entity.CircleFabricationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FabricationSyncDataC2SPacket {
    private final BlockPos blockPos;
    private final Item recipeItem;
    private final byte powerLevel;

    public FabricationSyncDataC2SPacket(BlockPos pBlockPos, Item pRecipeItem, int pPowerLevel) {
        this.blockPos = pBlockPos;
        this.recipeItem = pRecipeItem;
        this.powerLevel = (byte)pPowerLevel;
    }

    public FabricationSyncDataC2SPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.recipeItem = buf.readItem().getItem();
        this.powerLevel = buf.readByte();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeItem(new ItemStack(recipeItem, 1));
        buf.writeByte(powerLevel);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level.getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof CircleFabricationBlockEntity cfbe) {
                MagiChemMod.LOGGER.debug("&&&& SERVERSIDE: Item is "+recipeItem+", powerLevel is "+powerLevel);

                cfbe.setCurrentRecipeTarget(recipeItem, true);
                cfbe.setPowerLevel(powerLevel);
            }
        });

        return true;
    }
}
