package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.aranaira.magichem.item.MateriaItem;
import com.mna.api.affinity.Affinity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ParticleSpawnAnointingS2CPacket {
    private final int x, y, z, color;
    private final boolean isSuccess;

    public ParticleSpawnAnointingS2CPacket(int pX, int pY, int pZ, int pColor, boolean pIsSuccess) {
        this.x = pX;
        this.y = pY;
        this.z = pZ;
        this.color = pColor;
        this.isSuccess = pIsSuccess;
    }

    public ParticleSpawnAnointingS2CPacket(FriendlyByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.color = buf.readInt();
        this.isSuccess = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(color);
        buf.writeBoolean(isSuccess);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            if(isSuccess)
                MateriaItem.generateSuccessParticles(x, y, z, color);
            else
                MateriaItem.generateFailureParticles(x, y, z, color);
        });

        return true;
    }
}
