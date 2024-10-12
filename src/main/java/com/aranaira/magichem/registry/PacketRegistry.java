package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.aranaira.magichem.networking.*;
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

        net.messageBuilder(NexusSyncDataC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(NexusSyncDataC2SPacket::new)
                .encoder(NexusSyncDataC2SPacket::toBytes)
                .consumerMainThread(NexusSyncDataC2SPacket::handle)
                .add();

        net.messageBuilder(SublimationPrimerSyncRecipeC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SublimationPrimerSyncRecipeC2SPacket::new)
                .encoder(SublimationPrimerSyncRecipeC2SPacket::toBytes)
                .consumerMainThread(SublimationPrimerSyncRecipeC2SPacket::handle)
                .add();

        net.messageBuilder(GrandDeviceSyncDataC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(GrandDeviceSyncDataC2SPacket::new)
                .encoder(GrandDeviceSyncDataC2SPacket::toBytes)
                .consumerMainThread(GrandDeviceSyncDataC2SPacket::handle)
                .add();

        net.messageBuilder(VariegatorSyncDataC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(VariegatorSyncDataC2SPacket::new)
                .encoder(VariegatorSyncDataC2SPacket::toBytes)
                .consumerMainThread(VariegatorSyncDataC2SPacket::handle)
                .add();

        net.messageBuilder(TravellersCompassSyncC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(TravellersCompassSyncC2SPacket::new)
                .encoder(TravellersCompassSyncC2SPacket::toBytes)
                .consumerMainThread(TravellersCompassSyncC2SPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
