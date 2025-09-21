package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.mojang.blaze3d.platform.InputConstants;
import cpw.mods.util.Lazy;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
    modid = MagiChemMod.MODID,
    bus = Mod.EventBusSubscriber.Bus.MOD,
    value = Dist.CLIENT
)
public class KeybindRegistry {
    public static final Lazy<KeyMapping> OpenWisdomWheel = Lazy.of(() -> {
        return new KeyMapping(
                "key.openwisdomwheel",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                "key.categories.mna.magichem");
    });
    public static final Lazy<KeyMapping> ToggleWisdomEffects = Lazy.of(() -> {
        return new KeyMapping(
                "key.togglewisdomeffects",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                InputConstants.UNKNOWN.getValue(),
                "key.categories.mna.magichem");
    });

    public KeybindRegistry() {

    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register((KeyMapping) OpenWisdomWheel.get());
        event.register((KeyMapping) ToggleWisdomEffects.get());
    }
}
