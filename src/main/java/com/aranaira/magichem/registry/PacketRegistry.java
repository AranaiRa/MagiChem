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

        //Client to Server
        net.messageBuilder(FabricationSyncDataC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(FabricationSyncDataC2SPacket::new)
                .encoder(FabricationSyncDataC2SPacket::toBytes)
                .consumerMainThread(FabricationSyncDataC2SPacket::handle)
                .add();

        net.messageBuilder(DeviceRecipeSyncDataC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(DeviceRecipeSyncDataC2SPacket::new)
                .encoder(DeviceRecipeSyncDataC2SPacket::toBytes)
                .consumerMainThread(DeviceRecipeSyncDataC2SPacket::handle)
                .add();

        net.messageBuilder(DeviceRecipeClearC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(DeviceRecipeClearC2SPacket::new)
                .encoder(DeviceRecipeClearC2SPacket::toBytes)
                .consumerMainThread(DeviceRecipeClearC2SPacket::handle)
                .add();

        net.messageBuilder(ActuatorSyncPowerLevelC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ActuatorSyncPowerLevelC2SPacket::new)
                .encoder(ActuatorSyncPowerLevelC2SPacket::toBytes)
                .consumerMainThread(ActuatorSyncPowerLevelC2SPacket::handle)
                .add();

        net.messageBuilder(ActuatorToggleEldrinC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ActuatorToggleEldrinC2SPacket::new)
                .encoder(ActuatorToggleEldrinC2SPacket::toBytes)
                .consumerMainThread(ActuatorToggleEldrinC2SPacket::handle)
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

        net.messageBuilder(StandingRetortSyncDataC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(StandingRetortSyncDataC2SPacket::new)
                .encoder(StandingRetortSyncDataC2SPacket::toBytes)
                .consumerMainThread(StandingRetortSyncDataC2SPacket::handle)
                .add();

        net.messageBuilder(MirrorLabyrinthSyncDataC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(MirrorLabyrinthSyncDataC2SPacket::new)
                .encoder(MirrorLabyrinthSyncDataC2SPacket::toBytes)
                .consumerMainThread(MirrorLabyrinthSyncDataC2SPacket::handle)
                .add();

        net.messageBuilder(FabricationBatchSizeC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(FabricationBatchSizeC2SPacket::new)
                .encoder(FabricationBatchSizeC2SPacket::toBytes)
                .consumerMainThread(FabricationBatchSizeC2SPacket::handle)
                .add();

        net.messageBuilder(AdvancementQueryC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(AdvancementQueryC2SPacket::new)
                .encoder(AdvancementQueryC2SPacket::toBytes)
                .consumerMainThread(AdvancementQueryC2SPacket::handle)
                .add();

        net.messageBuilder(WisdomSyncC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(WisdomSyncC2SPacket::new)
                .encoder(WisdomSyncC2SPacket::toBytes)
                .consumerMainThread(WisdomSyncC2SPacket::handle)
                .add();

        net.messageBuilder(OpenWisdomWheelC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(OpenWisdomWheelC2SPacket::new)
                .encoder(OpenWisdomWheelC2SPacket::toBytes)
                .consumerMainThread(OpenWisdomWheelC2SPacket::handle)
                .add();

        net.messageBuilder(ToggleWisdomC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ToggleWisdomC2SPacket::new)
                .encoder(ToggleWisdomC2SPacket::toBytes)
                .consumerMainThread(ToggleWisdomC2SPacket::handle)
                .add();

        net.messageBuilder(DisintegrationPyreSyncDataC2SPacket.class, ID(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(DisintegrationPyreSyncDataC2SPacket::new)
                .encoder(DisintegrationPyreSyncDataC2SPacket::toBytes)
                .consumerMainThread(DisintegrationPyreSyncDataC2SPacket::handle)
                .add();

        //Server to Clients

        MagiChemMod.CHANNEL.registerMessage(ID(), ParticleSpawnAnointingS2CPacket.class,
                ParticleSpawnAnointingS2CPacket::toBytes,
                ParticleSpawnAnointingS2CPacket::new,
                ParticleSpawnAnointingS2CPacket::handle);

        MagiChemMod.CHANNEL.registerMessage(ID(), AdvancementQueryS2CPacket.class,
                AdvancementQueryS2CPacket::toBytes,
                AdvancementQueryS2CPacket::new,
                AdvancementQueryS2CPacket::handle);

        MagiChemMod.CHANNEL.registerMessage(ID(), WisdomSyncS2CPacket.class,
                WisdomSyncS2CPacket::toBytes,
                WisdomSyncS2CPacket::new,
                WisdomSyncS2CPacket::handle);

        MagiChemMod.CHANNEL.registerMessage(ID(), ResetWisdomToggleS2CPacket.class,
                ResetWisdomToggleS2CPacket::toBytes,
                ResetWisdomToggleS2CPacket::new,
                ResetWisdomToggleS2CPacket::handle);
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
