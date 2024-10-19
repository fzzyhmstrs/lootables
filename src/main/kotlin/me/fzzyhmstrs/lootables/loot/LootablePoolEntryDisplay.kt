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

import io.netty.buffer.ByteBuf
import me.fzzyhmstrs.lootables.client.render.TileIcon
import net.minecraft.network.codec.PacketCodec

interface LootablePoolEntryDisplay {
    fun type(): LootablePoolEntryType
    fun provideIcons(): List<TileIcon>

    companion object {
        val PACKET_CODEC: PacketCodec<ByteBuf, LootablePoolEntryDisplay> = LootablePoolEntryType.PACKET_CODEC.dispatch({ display -> display.type() }, {type -> type.s2c()})
    }
}