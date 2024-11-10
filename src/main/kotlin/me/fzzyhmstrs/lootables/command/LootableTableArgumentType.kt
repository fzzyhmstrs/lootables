/*
 * Copyright (c) 2024 Fzzyhmstrs
 *
 * This file is part of Modifier Core, a mod made for minecraft; as such it falls under the license of Modifier Core.
 *
 * Modifier Core is free software provided under the terms of the Timefall Development License - Modified (TDL-M).
 * You should have received a copy of the TDL-M with this software.
 * If you did not, see <https://github.com/fzzyhmstrs/Timefall-Development-Licence-Modified>.
 */

package me.fzzyhmstrs.lootables.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.fzzyhmstrs.lootables.loot.LootablesData
import net.minecraft.command.CommandSource
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

class LootableTableArgumentType: ArgumentType<Identifier> {

    private val invalidIdException = DynamicCommandExceptionType { id -> Text.of("Invalid lootable table id: [$id]")}

    override fun parse(reader: StringReader): Identifier {
        val i = reader.cursor
        val identifier = Identifier.fromCommandInput(reader)
        return if(LootablesData.getTableIds().contains(identifier)) {
            identifier
        } else {
            reader.cursor = i
            throw invalidIdException.create(identifier)
        }
    }

    override fun <S : Any?> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        return CommandSource.suggestIdentifiers(LootablesData.getTableIds(), builder)
    }

    companion object {

        fun getId(name: String, context: CommandContext<ServerCommandSource>): Identifier {
            return context.getArgument(name, Identifier::class.java)
        }
    }
}