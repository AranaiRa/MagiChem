package com.aranaira.magichem.registry;

import com.aranaira.magichem.MagiChemMod;
import com.mna.api.cantrips.ICantrip;
import com.mna.api.tools.RLoc;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CantripRegistry {
    public static void register() {
        final com.mna.cantrips.CantripRegistry registry = com.mna.cantrips.CantripRegistry.INSTANCE;

        registry.registerCantrip(
                new ResourceLocation(MagiChemMod.MODID, "acid_ward"),
                new ResourceLocation(MagiChemMod.MODID, "textures/gui/cantrip/acid_ward.png"),
                2,
                CantripRegistry::applyAcidWard,
                ItemStack.EMPTY,
                RLoc.create("manaweave_patterns/knot2"),
                RLoc.create("manaweave_patterns/diamond")
                );
    }

    public static void applyAcidWard(Player player, ICantrip cantrip, InteractionHand hand) {
        player.getCapability(PlayerProgressionProvider.PROGRESSION).ifPresent((p) -> {
            int amp = p.getTier() - 2;
            MobEffectInstance mei = new MobEffectInstance(MobEffectsRegistry.ACID_WARD.get(), 6000, amp, false, false, true);
            player.addEffect(mei);
        });
    }
}
