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
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import me.fzzyhmstrs.fzzy_config.util.FcText
import me.fzzyhmstrs.fzzy_config.util.FcText.lit
import me.fzzyhmstrs.lootables.api.IdKey
import me.fzzyhmstrs.lootables.api.LootablesApi
import me.fzzyhmstrs.lootables.command.LootableCommandsCommon.buildKey
import me.fzzyhmstrs.lootables.data.LootablesData
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.command.argument.IdentifierArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.function.Supplier

object LootableCommandsCommon {

    fun register(commandDispatcher: CommandDispatcher<ServerCommandSource>) {
        commandDispatcher.register(
            CommandManager.literal("lootables")
                .requires { source -> source.hasPermissionLevel(2) }
                .then(CommandManager.literal("random")
                    .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .then(CommandManager.argument("table", LootableTableArgumentType())
                            .finishRandomCommand()
                            .then(CommandManager.literal("keyed")
                                .finishIdKeys { arg, id, max ->
                                    arg.finishRandomCommand(id, max)
                                }
                            )
                        )
                    )
                )
                .then(CommandManager.literal("choose")
                    .then(CommandManager.argument("targets", EntityArgumentType.players())
                        .then(CommandManager.argument("table", LootableTableArgumentType())
                            .finishChoiceCommand()
                            .then(CommandManager.literal("keyed")
                                .finishIdKeys { arg, id, max ->
                                    arg.finishChoiceCommand(id, max)
                                }
                            )
                        )
                    )
                )
                .then(CommandManager.literal("reset_key")
                    .finishIdKeys { arg, id, max ->
                        arg.executes { context ->
                            reset(context, context.buildKey(id, max))
                        }
                        .then(CommandManager.argument("targets", EntityArgumentType.players())
                            .executes { context ->
                                reset(context, context.buildKey(id, max), EntityArgumentType.getPlayers(context, "targets"))
                            }
                        )
                    }
                )
        )
    }

    private fun <T: ArgumentBuilder<ServerCommandSource, T>> T.finishIdKeys(executor: (RequiredArgumentBuilder<ServerCommandSource, Identifier>, String?, String?) -> ArgumentBuilder<ServerCommandSource, *>): T {
        return this.then(CommandManager.argument("max_uses", IntegerArgumentType.integer(1))
            .then(CommandManager.argument("id_key", IdentifierArgumentType.identifier())
                .also {
                    executor(it, "id_key", "max_uses")
                }
            )
        )
        .then(CommandManager.argument("id_key", IdentifierArgumentType.identifier())
            .also {
                executor(it, "id_key", null)
            }
        )
    }

    private fun <T: ArgumentBuilder<ServerCommandSource, T>> T.finishChoiceCommand(id: String? = null, maxCount: String? = null): T {
        return this
            .executes { context ->
                choices(context, EntityArgumentType.getPlayers(context, "targets"), LootableTableArgumentType.getId("table", context), context.buildKey(id, maxCount), 3, 1)
            }
            .then(CommandManager.argument("rolls", IntegerArgumentType.integer(1))
                .executes { context ->
                    choices(context, EntityArgumentType.getPlayers(context, "targets"), LootableTableArgumentType.getId("table", context), context.buildKey(id, maxCount), IntegerArgumentType.getInteger(context, "rolls"), 1)
                }
                .then(CommandManager.argument("choices", IntegerArgumentType.integer(1))
                    .executes { context ->
                        choices(context, EntityArgumentType.getPlayers(context, "targets"), LootableTableArgumentType.getId("table", context), context.buildKey(id, maxCount), IntegerArgumentType.getInteger(context, "rolls"), IntegerArgumentType.getInteger(context, "choices"))
                    }
                )
            )
            .then(CommandManager.argument("choices", IntegerArgumentType.integer(1))
                .executes { context ->
                    choices(context, EntityArgumentType.getPlayers(context, "targets"), LootableTableArgumentType.getId("table", context), context.buildKey(id, maxCount), 3, IntegerArgumentType.getInteger(context, "choices"))
                }
            )
    }

    private fun <T: ArgumentBuilder<ServerCommandSource, T>> T.finishRandomCommand(id: String? = null, maxCount: String? = null): T {
        return this
            .executes { context ->
                random(context, EntityArgumentType.getPlayers(context, "targets"), LootableTableArgumentType.getId("table", context), context.buildKey(id, maxCount), 1)
            }
            .then(
                CommandManager.argument("rolls", IntegerArgumentType.integer(1))
                    .executes { context ->
                        random(context, EntityArgumentType.getPlayers(context, "targets"), LootableTableArgumentType.getId("table", context), context.buildKey(id, maxCount), IntegerArgumentType.getInteger(context, "rolls"))
                    }
            )
    }

    private fun <T: ArgumentBuilder<ServerCommandSource, T>> T.finishResetCommand(id: String? = null, maxCount: String? = null): T {
        return this


    }

    private fun CommandContext<ServerCommandSource>.buildKey(id: String?, maxCount: String?): IdKey? {
        if (id == null) return null
        val count = if (maxCount == null) 1 else IntegerArgumentType.getInteger(this, maxCount)
        return IdKey(IdentifierArgumentType.getIdentifier(this, id), count)
    }

    private fun random(context: CommandContext<ServerCommandSource>, players: Collection<ServerPlayerEntity>, table: Identifier, idKey: IdKey?, rolls: Int): Int {
        if (!LootablesData.hasTable(table)) {
            context.source.sendError(FcText.translatable("lootables.command.error.no_table"))
            return 0
        }
        for (player in players) {
            LootablesApi.supplyLootRandomly(table, player, player.pos, idKey, rolls)
        }
        return 1
    }

    private fun choices(context: CommandContext<ServerCommandSource>, players: Collection<ServerPlayerEntity>, table: Identifier, idKey: IdKey?, rolls: Int, choices: Int): Int {
        if (!LootablesData.hasTable(table)) {
            context.source.sendError(FcText.translatable("lootables.command.error.no_table"))
            return 0
        }
        var success = true
        for (player in players) {
            success = success && LootablesApi.supplyLootWithChoices(table, player, player.pos, { _, _ -> }, { _, _ -> }, idKey, rolls, choices)
        }
        return if (success) {
            1
        } else {
            0
        }
    }

    private fun reset(context: CommandContext<ServerCommandSource>, idKey: IdKey?, players: Collection<ServerPlayerEntity>? = null): Int {
        if (idKey == null) {
            context.source.sendError(FcText.translatable("lootables.command.error.no_key"))
            return 0
        }
        if (players != null) {
            for (player in players) {
                LootablesApi.resetKey(idKey, player)
            }
            if (players.size == 1) {
                context.source.sendFeedback( {
                    FcText.translatable("lootables.command.feedback.reset_players", idKey.pretty(), players.toList()[0])
                }, true)
            } else {
                val playerTexts = joinToText(players.map { it.name }, ", ".lit())
                context.source.sendFeedback( {
                    FcText.translatable("lootables.command.feedback.reset_players", idKey.pretty(), playerTexts)
                }, true)
            }
        } else {
            LootablesApi.resetKey(idKey, context.source.server)
            context.source.sendFeedback( {
                FcText.translatable("lootables.command.feedback.reset_all_players", idKey.pretty())
            }, true)
        }
        return 1
    }

    private fun joinToText(texts: List<Text>, separator: Text): Text {
        if (texts.isEmpty()) return FcText.empty()
        if (texts.size == 1) return texts[0]
        var t = texts[0].copy()
        for (i in 1..texts.lastIndex) {
            t = t.append(separator)
            t = t.append(texts[i])
        }
        return t
    }
}