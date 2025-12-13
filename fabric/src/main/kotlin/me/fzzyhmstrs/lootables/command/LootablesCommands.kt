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
import me.fzzyhmstrs.lootables.Lootables
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer
import net.minecraft.server.command.ServerCommandSource

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
        LootableCommandsCommon.register(commandDispatcher)
    }
}