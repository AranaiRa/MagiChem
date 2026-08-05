package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class DeviceRecipeSyncDataC2SPacket {
    private final BlockPos blockPos;
    private final ItemStack recipeOutput;

    public DeviceRecipeSyncDataC2SPacket(BlockPos pBlockPos, Item pRecipeItem) {
        this(pBlockPos, pRecipeItem == null ? ItemStack.EMPTY : new ItemStack(pRecipeItem));
    }

    public DeviceRecipeSyncDataC2SPacket(BlockPos pBlockPos, ItemStack pRecipeOutput) {
        this.blockPos = pBlockPos;
        this.recipeOutput = pRecipeOutput.copy();
    }

    public DeviceRecipeSyncDataC2SPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.recipeOutput = buf.readItem();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeItemStack(recipeOutput, true);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level().getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof FuseryBlockEntity fusery) {
                fusery.setRecipeByOutput(recipeOutput.copy());
            }
            else if(entity instanceof GrandFuseryBlockEntity fusery) {
                fusery.setRecipeByOutput(recipeOutput.copy());
            }
            else if(entity instanceof CentrifugeBlockEntity centrifuge) {
                centrifuge.setRecipeByOutput(recipeOutput.copy());
            }
            else if(entity instanceof GrandCentrifugeBlockEntity centrifuge) {
                centrifuge.setRecipeByOutput(recipeOutput.copy());
            }
            else if(entity instanceof PrimeAggregatorBlockEntity aggregator) {
                aggregator.setRecipeByOutput(recipeOutput.copy());
            }
        });

        return true;
    }
}
