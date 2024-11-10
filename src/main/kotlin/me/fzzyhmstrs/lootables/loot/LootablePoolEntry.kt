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

import com.mojang.serialization.MapCodec
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

interface LootablePoolEntry {
    fun type(): LootablePoolEntryType
    fun apply(player: PlayerEntity, origin: Vec3d)
    fun defaultDescription(playerEntity: ServerPlayerEntity): Text
    fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay

    companion object {
        val MAP_CODEC: MapCodec<LootablePoolEntry> = LootablePoolEntryType.CODEC.dispatchMap({ entry -> entry.type() }, { type -> type.codec() })
    }
}