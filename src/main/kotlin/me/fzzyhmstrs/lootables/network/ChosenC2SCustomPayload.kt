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

class ChosenC2SCustomPayload(val table: Identifier, val choiceKey: UUID, val chosen: List<Identifier>): CustomPayload {

    override fun getId(): Id<out CustomPayload> {
        return TYPE
    }

    companion object {
        val TYPE: Id<ChosenC2SCustomPayload> = Id(Lootables.identity("chosen_c2s"))

        val CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC,
            ChosenC2SCustomPayload::table,
            PacketCodecs.STRING.xmap(UUID::fromString, UUID::toString),
            ChosenC2SCustomPayload::choiceKey,
            Identifier.PACKET_CODEC.collect(PacketCodecs.toList()),
            ChosenC2SCustomPayload::chosen,
            ::ChosenC2SCustomPayload
        )
    }
}
