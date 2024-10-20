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
import me.fzzyhmstrs.lootables.api.IdKey
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.network.packet.CustomPayload.Id
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.*

class ChosenC2SCustomPayload(val table: Identifier, val key: Optional<IdKey>, val chosen: List<Identifier>, val pos: Vec3d): CustomPayload {

    override fun getId(): Id<out CustomPayload> {
        return TYPE
    }

    companion object {
        val TYPE: Id<ChosenC2SCustomPayload> = Id(Lootables.identity("chosen_c2s"))

        val CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC,
            ChosenC2SCustomPayload::table,
            PacketCodecs.optional(IdKey.PACKET_CODEC),
            ChosenC2SCustomPayload::key,
            Identifier.PACKET_CODEC.collect(PacketCodecs.toList()),
            ChosenC2SCustomPayload::chosen,
            PacketCodec.tuple(PacketCodecs.DOUBLE, Vec3d::x, PacketCodecs.DOUBLE, Vec3d::y, PacketCodecs.DOUBLE, Vec3d::z, ::Vec3d),
            ChosenC2SCustomPayload::pos,
            ::ChosenC2SCustomPayload
        )
    }
}