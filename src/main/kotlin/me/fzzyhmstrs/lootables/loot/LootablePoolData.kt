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

import me.fzzyhmstrs.fzzy_config.util.FcText
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import net.minecraft.util.Identifier
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class LootablePoolData private constructor(val id: Identifier, val description: Text?, val rarity: LootableRarity, val display: LootablePoolEntryDisplay) {

    fun provideDescription(): Text {
        return description ?: display.clientDescription() ?: FcText.translatable("lootables.entry.no.desc")
    }

    override fun toString(): String {
        return "LootablePoolData@${Integer.toHexString(System.identityHashCode(this))}(id=$id, description=${description}, rarity=$rarity, display=$display)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LootablePoolData

        if (id != other.id) return false
        if (description != other.description) return false
        if (rarity != other.rarity) return false
        if (display != other.display) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + rarity.hashCode()
        result = 31 * result + display.hashCode()
        return result
    }


    companion object {

        private val INSTANCES: ConcurrentMap<Identifier, LootablePoolData> = ConcurrentHashMap(24, 0.8f, 2)

        fun of(id: Identifier, description: Text?, rarity: LootableRarity, display: LootablePoolEntryDisplay): LootablePoolData {
            return INSTANCES.computeIfAbsent(id) { _ -> LootablePoolData(id, description, rarity, display) }
        }

        private fun of(id: Identifier, description: Optional<Text>, rarity: LootableRarity, display: LootablePoolEntryDisplay): LootablePoolData {
            return INSTANCES.computeIfAbsent(id) { _ -> LootablePoolData(id, description.orElse(null), rarity, display) }
        }

        val PACKET_CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC,
            LootablePoolData::id,
            PacketCodecs.optional(TextCodecs.PACKET_CODEC),
            { data -> Optional.ofNullable(data.description) },
            LootableRarity.PACKET_CODEC,
            LootablePoolData::rarity,
            LootablePoolEntryDisplay.PACKET_CODEC,
            LootablePoolData::display,
            ::of
        )

        val LIST_PACKET_CODEC = PACKET_CODEC.collect(PacketCodecs.toList())
    }
}