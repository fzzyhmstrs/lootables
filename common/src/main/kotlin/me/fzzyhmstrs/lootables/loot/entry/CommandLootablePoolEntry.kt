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

package me.fzzyhmstrs.lootables.loot.entry

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.CommandLootablePoolEntryDisplay
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

/**
 * Executes a command on the server with permission level 2.
 * @param command the command string to run, without the '/' prefix.
 * @author fzzyhmstrs
 * @since 0.2.0
 */
class CommandLootablePoolEntry(private val command: String): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.COMMAND
    }

    override fun apply(player: ServerPlayerEntity, origin: Vec3d) {
        val server = player.server
        val commandSource = ServerCommandSource(
            player,
            origin,
            player.rotationClient,
            player.serverWorld,
            2,
            player.name.string,
            player.displayName,
            server,
            player).withSilent()
        server.commandManager.executeWithPrefix(commandSource, command)
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return CommandLootablePoolEntryDisplay
    }

    internal companion object {

        val CODEC: MapCodec<CommandLootablePoolEntry> = Codec.STRING.xmap(
            ::CommandLootablePoolEntry,
            CommandLootablePoolEntry::command
        ).fieldOf("command")
    }

}