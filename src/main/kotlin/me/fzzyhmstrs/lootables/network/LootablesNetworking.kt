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

package me.fzzyhmstrs.lootables.network

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.fzzyhmstrs.fzzy_config.networking.api.ServerPlayNetworkContext
import me.fzzyhmstrs.lootables.client.LootablesClientNetworking
import me.fzzyhmstrs.lootables.loot.LootablesData

object LootablesNetworking {

    fun init() {
        ConfigApi.network().registerS2C(ChoicesS2CCustomPayload.TYPE, ChoicesS2CCustomPayload.CODEC, LootablesClientNetworking::handleChoicesS2C)
        ConfigApi.network().registerS2C(DataSyncS2CCustomPayload.TYPE, DataSyncS2CCustomPayload.CODEC, LootablesClientNetworking::handleDataSyncS2C)
        ConfigApi.network().registerC2S(ChosenC2SCustomPayload.TYPE, ChosenC2SCustomPayload.CODEC, this::handleChosenS2C)
    }

    private fun handleChosenS2C(payload: ChosenC2SCustomPayload, context: ServerPlayNetworkContext) {
        val table = LootablesData.getTable(payload.table) ?: return
        val key = payload.key
        if (key.isPresent) {
            if (!LootablesData.keyAvailable(key.get(), context.player().uuid)) return
            LootablesData.applyKey(key.get(), context.player().uuid)
        }
        table.applyPoolsById(payload.chosen, context.player(), payload.pos)
    }

}