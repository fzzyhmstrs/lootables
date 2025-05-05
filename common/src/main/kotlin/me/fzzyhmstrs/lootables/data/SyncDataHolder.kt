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

package me.fzzyhmstrs.lootables.data

import me.fzzyhmstrs.lootables.loot.LootablePoolData
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import java.util.function.BiConsumer

internal interface SyncDataHolder {

    fun forEachPlayer(players: List<ServerPlayerEntity>, action: BiConsumer<ServerPlayerEntity, Map<Identifier, List<LootablePoolData>>>)

    fun forPlayer(playerEntity: ServerPlayerEntity, action: BiConsumer<ServerPlayerEntity, Map<Identifier, List<LootablePoolData>>>)


    class Simple(private val d: Map<Identifier, List<LootablePoolData>>): SyncDataHolder {

        override fun forEachPlayer(players: List<ServerPlayerEntity>, action: BiConsumer<ServerPlayerEntity, Map<Identifier, List<LootablePoolData>>>) {
            players.forEach {
                action.accept(it, d)
            }
        }

        override fun forPlayer(playerEntity: ServerPlayerEntity, action: BiConsumer<ServerPlayerEntity, Map<Identifier, List<LootablePoolData>>>) {
            action.accept(playerEntity, d)
        }

    }

    class Complex(private val d: Map<ServerPlayerEntity, Map<Identifier, List<LootablePoolData>>>): SyncDataHolder {

        override fun forEachPlayer(players: List<ServerPlayerEntity>, action: BiConsumer<ServerPlayerEntity, Map<Identifier, List<LootablePoolData>>>) {
            players.forEach {
                action.accept(it, d.getOrDefault(it, mapOf()))
            }
        }

        override fun forPlayer(playerEntity: ServerPlayerEntity, action: BiConsumer<ServerPlayerEntity, Map<Identifier, List<LootablePoolData>>>) {
            action.accept(playerEntity, d.getOrDefault(playerEntity, mapOf()))
        }

    }

    object Empty: SyncDataHolder {

        override fun forEachPlayer(players: List<ServerPlayerEntity>, action: BiConsumer<ServerPlayerEntity, Map<Identifier, List<LootablePoolData>>>) {
            players.forEach {
                action.accept(it, mapOf())
            }
        }

        override fun forPlayer(playerEntity: ServerPlayerEntity, action: BiConsumer<ServerPlayerEntity, Map<Identifier, List<LootablePoolData>>>) {
            action.accept(playerEntity, mapOf())
        }

    }

    companion object {
        fun create(playersMatter: Boolean, result: Map<ServerPlayerEntity, Map<Identifier, List<LootablePoolData>>>): SyncDataHolder {
            return if (result.size == 1) {
                Simple(result.values.first())
            } else if (playersMatter && result.isNotEmpty()) {
                Complex(result)
            } else if (!playersMatter && result.isNotEmpty()) {
                Simple(result.values.first())
            } else {
                Empty
            }
        }
    }
}