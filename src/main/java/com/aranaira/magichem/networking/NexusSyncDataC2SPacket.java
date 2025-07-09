package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NexusSyncDataC2SPacket {
    private final BlockPos blockPos;
    private final byte powerUsageSetting;
    private final ItemStack recipeOutput;
    private final boolean preventDrawingLastMateria;

    public NexusSyncDataC2SPacket(BlockPos pBlockPos, int pPowerLevel, ItemStack pStack, boolean pPreventDrawingLastMateria) {
        this.blockPos = pBlockPos;
        this.powerUsageSetting = (byte)pPowerLevel;
        this.recipeOutput = pStack.copy();
        this.preventDrawingLastMateria = pPreventDrawingLastMateria;
    }

    public NexusSyncDataC2SPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.powerUsageSetting = buf.readByte();
        this.recipeOutput = buf.readItem();
        this.preventDrawingLastMateria = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeByte(powerUsageSetting);
        buf.writeItemStack(recipeOutput, true);
        buf.writeBoolean(preventDrawingLastMateria);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level().getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof AlchemicalNexusBlockEntity anbe) {
                anbe.setPowerUsageSetting(powerUsageSetting);
                anbe.setRecipeFromOutput(anbe.getLevel(), recipeOutput);
                anbe.preventDrawingLastMateria = preventDrawingLastMateria;
                anbe.setInitiatingPlayer(player.getUUID());
                anbe.syncAndSave();
            }
        });

        return true;
    }
}
