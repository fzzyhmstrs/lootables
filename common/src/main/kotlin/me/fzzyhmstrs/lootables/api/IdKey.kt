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

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.util.Identifier

/**
 * A key for restricting a certain instance of loot generation to a certain number of rolls per player.
 * @param id [Identifier] unique ID to make this key distinct from others.
 * @param count Int max number of times a particular player can roll the loot supplied with this key
 * @author fzzyhmstrs
 * @since 0.1.0
 */
data class IdKey @JvmOverloads constructor(val id: Identifier, val count: Int = 1) {

    fun pretty(): String {
        return "ID<$id x $count>"
    }

    internal companion object {
        val CODEC: Codec<IdKey> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<IdKey> ->
            instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(IdKey::id),
                Codec.INT.optionalFieldOf("count", 1).forGetter(IdKey::count)
            ).apply(instance, ::IdKey)
        }

        val PACKET_CODEC: PacketCodec<ByteBuf, IdKey> = PacketCodec.tuple(
            Identifier.PACKET_CODEC,
            IdKey::id,
            PacketCodecs.INTEGER,
            IdKey::count,
            ::IdKey
        )
    }
}