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
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.PoolLootablePoolEntryDisplay
import net.minecraft.item.ItemStack
import net.minecraft.loot.LootPool
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.Vec3d
import java.util.*
import java.util.function.Consumer

/**
 * Executes a command on the server with permission level 2.
 * @param command the command string to run, without the '/' prefix.
 * @author fzzyhmstrs
 * @since 0.2.0
 */
class CommandLootablePoolEntry @JvmOverloads constructor(private val command: String): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.COMMAND
    }

    override fun apply(player: ServerPlayerEntity, origin: Vec3d) {
        val server = player.server
        val realCommand = command.removePrefix("/")
        val commandSource = ServerCommandSource(
            player, 
            origin, 
            playerEntity.rotationClient, 
            player.serverWorld, 
            2, 
            playerEntity.name.string, 
            playerEntity.displayName, 
            server, 
            player)
        server.commandManager.execute(realCommand, command)
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return CommandLootablePoolEntryDisplay
    }

    internal companion object {

        val CODEC: MapCodec<PoolLootablePoolEntry> = Codec.STRING.xmap(
            ::CommandLootablePoolEntry,
            CommandLootablePoolEntry::command
        ).fieldOf("command")
    }

}
