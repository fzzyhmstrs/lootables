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

package me.fzzyhmstrs.lootables.api

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.util.Identifier

class IdKey @JvmOverloads constructor(val id: Identifier, val count: Int = 1) {

    companion object {
        val PACKET_CODEC: PacketCodec<ByteBuf, IdKey> = PacketCodec.tuple(
            Identifier.PACKET_CODEC,
            IdKey::id,
            PacketCodecs.INTEGER,
            IdKey::count,
            ::IdKey
        )
    }
}