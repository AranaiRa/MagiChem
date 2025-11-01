package com.aranaira.magichem.commands;

import com.aranaira.magichem.foundation.saveddata.EldrinOrreryLimiterSD;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Iterator;

public class MagiChemCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("magichem")
                .then(resetOrreryCommand())
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> resetOrreryCommand() {
        return (Commands.literal("resetOrreryLimit")
                .then(Commands.argument("player", EntityArgument.players()).executes((context) -> {
            return resetOrreryExecution((CommandSourceStack)context.getSource(), EntityArgument.getPlayers(context, "player"));
        })));
    }

    private static int resetOrreryExecution(CommandSourceStack source, Collection<ServerPlayer> players) {
        if (players != null && players.size() != 0) {

            for (ServerPlayer spe : players) {
                final EldrinOrreryLimiterSD eldrinOrreryData = spe.getServer().overworld().getDataStorage().computeIfAbsent(EldrinOrreryLimiterSD::load, EldrinOrreryLimiterSD::create, "eldrinOrreryData");
                if (eldrinOrreryData.playerHasOrrery(spe)) {
                    eldrinOrreryData.removeOrrery(spe.getStringUUID());
                    source.sendSuccess(() -> {
                        return Component.translatable("magichem.commands.reset_orrery.success", (players.iterator().next()).getDisplayName());
                    }, true);
                } else {
                    source.sendSuccess(() -> {
                        return Component.translatable("magichem.commands.reset_orrery.failure", (players.iterator().next()).getDisplayName());
                    }, true);
                }
            }

            return 1;
        } else {
            return 0;
        }
    }
}
