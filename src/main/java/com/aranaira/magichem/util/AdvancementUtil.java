package com.aranaira.magichem.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

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
}
