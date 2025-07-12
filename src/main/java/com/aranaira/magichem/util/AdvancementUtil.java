package com.aranaira.magichem.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

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
}
