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

package me.fzzyhmstrs.lootables.loot

import com.mojang.serialization.Codec
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

@JvmDefaultWithCompatibility
interface LootablePoolEntry {
    fun type(): LootablePoolEntryType
    fun apply(player: ServerPlayerEntity, origin: Vec3d)
    fun serverDescription(playerEntity: ServerPlayerEntity): Text? {
        return null
    }
    fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay
    fun maxUses(): Int? {
        return null
    }
    fun needsInvalidation(type: InvalidationType): Boolean {
        return false
    }

    enum class InvalidationType {
        INIT,
        PLAYER
    }

    companion object {
        val CODEC: Codec<LootablePoolEntry> = LootablePoolEntryType.CODEC.dispatch({ entry -> entry.type() }, { type -> type.codec() })
    }
}