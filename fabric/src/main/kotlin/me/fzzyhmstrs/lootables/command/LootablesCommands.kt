/*
 *
 *  Copyright (c) 2024 Fzzyhmstrs
 *
 *  This file is part of Lootables API , a mod made for minecraft; as such it falls under the license of Lootables API.
 *
 *  Lootables API is free software provided under the terms of the Timefall Development License - Modified (TDL-M).
 *  You should have received a copy of the TDL-M with this software.
 *  If you did not, see <https://github.com/fzzyhmstrs/Timefall-Development-Licence-Modified>.
 *
 */

package me.fzzyhmstrs.lootables.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import me.fzzyhmstrs.fzzy_config.util.FcText
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.api.LootablesApi
import me.fzzyhmstrs.lootables.data.LootablesData
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

object LootablesCommands {

    fun init() {
        ArgumentTypeRegistry.registerArgumentType(
            Lootables.identity("lootable_table"),
            LootableTableArgumentType::class.java,
            ConstantArgumentSerializer.of { _ -> LootableTableArgumentType() }
        )

        CommandRegistrationCallback.EVENT.register { commandDispatcher, _, _ -> register(commandDispatcher) }
    }

    private fun register(commandDispatcher: CommandDispatcher<ServerCommandSource>) {
        commandDispatcher.register(
            CommandManager.literal("lootables")
                .requires { source -> source.hasPermissionLevel(2) }
                .then(CommandManager.literal("random")
                    .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .then(CommandManager.argument("table", LootableTableArgumentType())
                            .executes { context ->
                                random(context, EntityArgumentType.getPlayers(context, "targets"), LootableTableArgumentType.getId("table", context), 1)
                            }
                            .then(CommandManager.argument("rolls", IntegerArgumentType.integer(1))
                                .executes { context ->
                                    random(context, EntityArgumentType.getPlayers(context, "targets"), LootableTableArgumentType.getId("table", context), IntegerArgumentType.getInteger(context, "rolls"))
                                }
                            )
                        )
                    )
                )
                .then(CommandManager.literal("choose")
                    .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .then(CommandManager.argument("table", LootableTableArgumentType())
                            .executes { context ->
                                choices(context, EntityArgumentType.getPlayers(context, "targets"), LootableTableArgumentType.getId("table", context), 3, 1)
                            }
                            .then(CommandManager.argument("rolls", IntegerArgumentType.integer(1))
                                .executes { context ->
                                    choices(context, EntityArgumentType.getPlayers(context, "targets"), LootableTableArgumentType.getId("table", context), IntegerArgumentType.getInteger(context, "rolls"), 1)
                                }
                                .then(CommandManager.argument("choices", IntegerArgumentType.integer(1))
                                    .executes { context ->
                                        choices(context, EntityArgumentType.getPlayers(context, "targets"), LootableTableArgumentType.getId("table", context), IntegerArgumentType.getInteger(context, "rolls"), IntegerArgumentType.getInteger(context, "choices"))
                                    }
                                )
                            )
                            .then(CommandManager.argument("choices", IntegerArgumentType.integer(1))
                                .executes { context ->
                                    choices(context, EntityArgumentType.getPlayers(context, "targets"), LootableTableArgumentType.getId("table", context), 3, IntegerArgumentType.getInteger(context, "choices"))
                                }

                            )
                        )
                    )
                )
        )
    }

    private fun random(context: CommandContext<ServerCommandSource>, players: Collection<ServerPlayerEntity>, table: Identifier, rolls: Int): Int {
        if (!LootablesData.hasTable(table)) {
            context.source.sendError(FcText.translatable("lootables.command.error.no_table"))
            return 0
        }
        for (player in players) {
            LootablesApi.supplyLootRandomly(table, player, player.pos, null, rolls)
        }
        return 1
    }

    private fun choices(context: CommandContext<ServerCommandSource>, players: Collection<ServerPlayerEntity>, table: Identifier, rolls: Int, choices: Int): Int {
        if (!LootablesData.hasTable(table)) {
            context.source.sendError(FcText.translatable("lootables.command.error.no_table"))
            return 0
        }
        for (player in players) {
            LootablesApi.supplyLootWithChoices(table, player, player.pos, { _, _ -> }, { _, _ -> }, null, rolls, choices)
        }
        return 1
    }

}