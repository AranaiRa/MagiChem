package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.AlchemicalNexusBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class NexusSyncDataC2SPacket {
    private final BlockPos blockPos;
    private final byte powerUsageSetting;
    private final @Nullable ResourceLocation recipeId;
    private final boolean preventDrawingLastMateria;

    public NexusSyncDataC2SPacket(BlockPos pBlockPos, int pPowerLevel, @Nullable ResourceLocation pRecipeId, boolean pPreventDrawingLastMateria) {
        this.blockPos = pBlockPos;
        this.powerUsageSetting = (byte)pPowerLevel;
        this.recipeId = pRecipeId;
        this.preventDrawingLastMateria = pPreventDrawingLastMateria;
    }

    public NexusSyncDataC2SPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.powerUsageSetting = buf.readByte();
        this.recipeId = buf.readBoolean() ? buf.readResourceLocation() : null;
        this.preventDrawingLastMateria = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeByte(powerUsageSetting);
        buf.writeBoolean(recipeId != null);
        if(recipeId != null)
            buf.writeResourceLocation(recipeId);
        buf.writeBoolean(preventDrawingLastMateria);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level().getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof AlchemicalNexusBlockEntity anbe) {
                anbe.setPowerUsageSetting(powerUsageSetting);
                if(recipeId != null)
                    anbe.setRecipeFromId(anbe.getLevel(), recipeId);
                anbe.preventDrawingLastMateria = preventDrawingLastMateria;
                anbe.setInitiatingPlayer(player.getUUID());
                anbe.syncAndSave();
            }
        });

        return true;
    }
}
