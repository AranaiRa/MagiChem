package com.aranaira.magichem.commands;

import com.aranaira.magichem.capabilities.enhancement.EnhancementProvider;
import com.aranaira.magichem.capabilities.enhancement.IEnhancementCapability;
import com.aranaira.magichem.foundation.saveddata.EldrinOrreryLimiterSD;
import com.aranaira.magichem.registry.ItemRegistry;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.server.command.EnumArgument;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public class MagiChemCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("magichem")
                .then(resetOrreryCommand())
                .then(resetChaliceCommand())
                .then(resetWisdomCommand())
                .then(setHeartCommand())
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> resetOrreryCommand() {
        return (Commands.literal("resetOrreryLimit")
                .then(Commands.argument("player", EntityArgument.players()).executes((context) -> {
            return resetOrreryExecution((CommandSourceStack)context.getSource(), EntityArgument.getPlayers(context, "player"));
        })));
    }

    private static int resetOrreryExecution(CommandSourceStack source, Collection<ServerPlayer> players) {
        if(source.hasPermission(2)) {
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
            }
        } else {
            source.sendFailure(Component.translatable("magichem.commands.permission_failure", (players.iterator().next()).getDisplayName()));
        }
        return 0;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> resetChaliceCommand() {
        return (Commands.literal("resetChaliceCooldown")
                .then(Commands.argument("player", EntityArgument.players()).executes((context) -> {
                    return resetChaliceExecution((CommandSourceStack)context.getSource(), EntityArgument.getPlayers(context, "player"));
                })));
    }

    private static int resetChaliceExecution(CommandSourceStack source, Collection<ServerPlayer> players) {
        if(source.hasPermission(2)) {
            if (players != null && players.size() != 0) {

                for (ServerPlayer spe : players) {
                    spe.getCooldowns().removeCooldown(ItemRegistry.CHALICE_OF_TEARS.get());
                    source.sendSuccess(() -> {
                        return Component.translatable("magichem.commands.reset_general.success", (players.iterator().next()).getDisplayName());
                    }, true);
                }

                return 1;
            }
        } else {
            source.sendFailure(Component.translatable("magichem.commands.permission_failure", (players.iterator().next()).getDisplayName()));
        }
        return 0;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> resetWisdomCommand() {
        return (Commands.literal("resetWisdomResurrection")
                .then(Commands.argument("player", EntityArgument.players()).executes((context) -> {
                    return resetWisdomExecution((CommandSourceStack)context.getSource(), EntityArgument.getPlayers(context, "player"));
                })));
    }

    private static int resetWisdomExecution(CommandSourceStack source, Collection<ServerPlayer> players) {
        if(source.hasPermission(2)) {
            if (players != null && players.size() != 0) {

                for (ServerPlayer spe : players) {
                    spe.getCooldowns().removeCooldown(ItemRegistry.FLUSHED_WISDOM_STONE.get());
                    spe.getCooldowns().removeCooldown(ItemRegistry.PHILOSOPHERS_STONE.get());
                    source.sendSuccess(() -> {
                        return Component.translatable("magichem.commands.reset_general.success", (players.iterator().next()).getDisplayName());
                    }, true);
                }

                return 1;
            }
        } else {
            source.sendFailure(Component.translatable("magichem.commands.permission_failure", (players.iterator().next()).getDisplayName()));
        }
        return 0;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> setHeartCommand() {
        return (Commands.literal("setEnhancedHeart")
                .then(Commands.argument("player", EntityArgument.players())
                .then(Commands.argument("type", EnumArgument.enumArgument(IEnhancementCapability.EnhancedHeartType.class))
                .executes((context) -> {
                    return setHeartExecution((CommandSourceStack)context.getSource(), EntityArgument.getPlayers(context, "player"), context.getArgument("type", IEnhancementCapability.EnhancedHeartType.class));
                })))
        );
    }

    private static int setHeartExecution(CommandSourceStack source, Collection<ServerPlayer> players, IEnhancementCapability.EnhancedHeartType type) {
        if(source.hasPermission(2)) {
            if (players != null && players.size() != 0) {
                for (ServerPlayer spe : players) {
                    final LazyOptional<IEnhancementCapability> capLazy = spe.getCapability(EnhancementProvider.ENHANCEMENT);
                    if (capLazy.isPresent()) {
                        final Optional<IEnhancementCapability> capQuery = capLazy.resolve();
                        if (capQuery.isPresent()) {
                            final IEnhancementCapability cap = capQuery.get();
                            cap.setHeart(type);
                            source.sendSuccess(() -> {
                                return Component.translatable("magichem.commands.set_heart.success", (players.iterator().next()).getDisplayName());
                            }, true);
                        }
                    }
                }

                return 1;
            }
        } else {
            source.sendFailure(Component.translatable("magichem.commands.permission_failure", (players.iterator().next()).getDisplayName()));
        }
        return 0;
    }
}
