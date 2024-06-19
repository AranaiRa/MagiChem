package com.aranaira.magichem.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ClientUtil {

    @Nullable
    public static Level tryGetClientLevel() {
        return Minecraft.getInstance().level;
    }
}
