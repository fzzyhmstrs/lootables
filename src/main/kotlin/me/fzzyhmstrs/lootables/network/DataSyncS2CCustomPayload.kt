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

import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.loot.LootablePoolData
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.network.packet.CustomPayload.Id
import net.minecraft.util.Identifier

class DataSyncS2CCustomPayload(val tables: Map<Identifier, List<LootablePoolData>>): CustomPayload {

    override fun getId(): Id<out CustomPayload> {
        return TYPE
    }

    companion object {
        val TYPE: Id<DataSyncS2CCustomPayload> = Id(Lootables.identity("sync_s2c"))

        val CODEC = PacketCodecs.map({ mutableMapOf() }, Identifier.PACKET_CODEC, LootablePoolData.LIST_PACKET_CODEC)
            .xmap(::DataSyncS2CCustomPayload) { payload -> payload.tables.toMutableMap() }
    }
}