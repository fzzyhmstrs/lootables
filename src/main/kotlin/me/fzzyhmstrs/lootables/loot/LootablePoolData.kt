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

import net.minecraft.network.codec.PacketCodec
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import net.minecraft.util.Identifier

data class LootablePoolData(val id: Identifier, val description: Text, val display: LootablePoolEntryDisplay) {

    companion object {
        val PACKET_CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC,
            LootablePoolData::id,
            TextCodecs.PACKET_CODEC,
            LootablePoolData::description,
            LootablePoolEntryDisplay.PACKET_CODEC,
            LootablePoolData::display,
            ::LootablePoolData
        )
    }
}