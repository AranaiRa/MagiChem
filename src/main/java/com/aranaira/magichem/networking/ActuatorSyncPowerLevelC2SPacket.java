package com.aranaira.magichem.networking;

import com.aranaira.magichem.block.entity.*;
import com.aranaira.magichem.block.entity.ext.AbstractDirectionalPluginBlockEntity;
import com.mna.api.affinity.Affinity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ActuatorSyncPowerLevelC2SPacket {
    private final BlockPos blockPos;
    private final boolean powerLevelUp;
    private final int elementID;

    private static final Affinity[] elementMap = {
            Affinity.ENDER, Affinity.EARTH, Affinity.WATER, Affinity.WIND, Affinity.FIRE, Affinity.ARCANE
    };

    public ActuatorSyncPowerLevelC2SPacket(BlockPos pBlockPos, boolean pPowerLevelUp, Affinity pAffinity) {
        this.blockPos = pBlockPos;
        this.powerLevelUp = pPowerLevelUp;
        this.elementID = affinityToID(pAffinity);
    }

    public ActuatorSyncPowerLevelC2SPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.powerLevelUp = buf.readBoolean();
        this.elementID = buf.readInt();
    }

    public static int affinityToID(Affinity pAffinity) {
        return switch (pAffinity) {
            case ENDER -> 0;
            case EARTH -> 1;
            case WATER -> 2;
            case WIND -> 3;
            case FIRE -> 4;
            case ARCANE -> 5;
            default -> -1;
        };
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeBoolean(powerLevelUp);
        buf.writeInt(elementID);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        Player player = context.getSender();
        BlockEntity entity = player.level().getBlockEntity(blockPos);

        context.enqueueWork(() -> {
            if(entity instanceof AbstractDirectionalPluginBlockEntity adpbe) {
                int pl = adpbe.getPowerLevel();
                if (powerLevelUp) {
                    if(elementMap[elementID] == Affinity.EARTH)
                        adpbe.increasePowerLevel(ActuatorEarthBlockEntity::getValue);
                    else if(elementMap[elementID] == Affinity.WATER)
                        adpbe.increasePowerLevel(ActuatorWaterBlockEntity::getValue);
                    else if(elementMap[elementID] == Affinity.WIND)
                        adpbe.increasePowerLevel(ActuatorAirBlockEntity::getValue);
                    else if(elementMap[elementID] == Affinity.FIRE)
                        adpbe.increasePowerLevel(ActuatorFireBlockEntity::getValue);
                    else if(elementMap[elementID] == Affinity.ARCANE)
                        adpbe.increasePowerLevel(ActuatorArcaneBlockEntity::getValue);
                } else {
                    adpbe.decreasePowerLevel();
                }
                if(adpbe.getPowerLevel() != pl) adpbe.syncAndSave();
            }
        });

        return true;
    }
}
