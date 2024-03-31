package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.networking.ActuatorSyncPowerLevelC2SPacket;
import com.aranaira.magichem.networking.FuserySyncDataC2SPacket;
import com.aranaira.magichem.networking.FabricationSyncDataC2SPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketRegistry {

    private static SimpleChannel INSTANCE;

    private static int packetID = 0;
    private static int ID() {
        return packetID++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(MagiChemMod.MODID, "packets"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(FabricationSyncDataC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(FabricationSyncDataC2SPacket::new)
                .encoder(FabricationSyncDataC2SPacket::toBytes)
                .consumerMainThread(FabricationSyncDataC2SPacket::handle)
                .add();

        net.messageBuilder(FuserySyncDataC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(FuserySyncDataC2SPacket::new)
                .encoder(FuserySyncDataC2SPacket::toBytes)
                .consumerMainThread(FuserySyncDataC2SPacket::handle)
                .add();

        net.messageBuilder(ActuatorSyncPowerLevelC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ActuatorSyncPowerLevelC2SPacket::new)
                .encoder(ActuatorSyncPowerLevelC2SPacket::toBytes)
                .consumerMainThread(ActuatorSyncPowerLevelC2SPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
