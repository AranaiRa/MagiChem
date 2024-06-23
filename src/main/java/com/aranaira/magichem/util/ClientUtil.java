package com.aranaira.magichem.util;

import com.aranaira.magichem.recipe.AlchemicalInfusionRitualRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ClientUtil {
    @Nullable
    public static Level tryGetClientLevel() {
        return Minecraft.getInstance().level;
    }

    public static int getInfusionRitualRecipeCount() {
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null) {
            return 3;
        }
        else return AlchemicalInfusionRitualRecipe.getAllOutputs(level).size();
    }
}
