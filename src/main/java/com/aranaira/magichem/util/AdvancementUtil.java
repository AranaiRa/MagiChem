package com.aranaira.magichem.util;

import com.aranaira.magichem.networking.*;
import com.aranaira.magichem.registry.PacketRegistry;
import net.minecraft.advancements.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AdvancementUtil {
    public static boolean clientHasAdvancement(LocalPlayer player, ResourceLocation key) {
        ClientAdvancements advancements = Minecraft.getInstance().getConnection().getAdvancements();
        final Advancement advancement = advancements.getAdvancements().get(key);
        if (advancement != null) {
                AdvancementProgress progress = advancements.progress.get(advancement);
                return progress != null && progress.isDone();
            }
            return false;
    }

    public static boolean serverPlayerHasAdvancement(Level pLevel, Player pPlayer, ResourceLocation pKey) {
        if(pLevel instanceof ServerLevel serverLevel && pPlayer instanceof ServerPlayer player) {
            ServerAdvancementManager manager = serverLevel.getServer().getAdvancements();
            final Advancement advancementQuery = manager.getAdvancement(pKey);
            if(advancementQuery != null) {
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancementQuery);
                return progress.isDone();
            }
        }
        return false;
    }

    private static final Map<ResourceLocation, Advancement> cachedAdvancements = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, Boolean> pendingAdvancements = new ConcurrentHashMap<>();

    public static Advancement getAdvancementForDisplay(ResourceLocation key) {
        if (!cachedAdvancements.containsKey(key) && !pendingAdvancements.containsKey(key)) {
            pendingAdvancements.put(key, true);
            PacketRegistry.sendToServer(new AdvancementQueryC2SPacket(key));
        }
        return cachedAdvancements.get(key);
    }

    public static void onReceiveFetchedAdvancement(AdvancementQueryS2CPacket packet) {
        DisplayInfo fetchedDisplay = new DisplayInfo(ItemStack.EMPTY, packet.title, packet.description, null, FrameType.GOAL, false, false, false);
        Advancement fetched = new Advancement(packet.key, null, fetchedDisplay, AdvancementRewards.EMPTY, Map.of(), new String[0][], false);
        cachedAdvancements.put(packet.key, fetched);
        pendingAdvancements.remove(packet.key);
    }
}
