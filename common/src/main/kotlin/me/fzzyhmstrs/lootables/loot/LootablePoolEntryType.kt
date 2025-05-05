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

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import me.fzzyhmstrs.lootables.loot.number.BinomialLootableNumber
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import java.util.*
import java.util.function.Function

class LootablePoolEntryType private constructor(private val codec: MapCodec<out LootablePoolEntry>, private val s2cCodec: PacketCodec<RegistryByteBuf, out LootablePoolEntryDisplay>, private val randomGenerator: Function<ServerPlayerEntity, LootablePoolEntry?>) {

    fun codec(): MapCodec<out LootablePoolEntry> {
        return this.codec
    }

    fun s2c(): PacketCodec<RegistryByteBuf, out LootablePoolEntryDisplay> {
        return s2cCodec
    }

    companion object {
        fun create(id: Identifier, codec: MapCodec<out LootablePoolEntry>, s2cCodec: PacketCodec<RegistryByteBuf, out LootablePoolEntryDisplay>, randomGenerator: Function<ServerPlayerEntity, LootablePoolEntry?> = Function { _ -> null }): LootablePoolEntryType {
            val type = LootablePoolEntryType(codec, s2cCodec, randomGenerator)
            if (idToType.containsKey(id)) throw IllegalStateException("Duplicate type registered: $id")
            idToType[id] = type
            typeToId[type] = id
            return type
        }

        fun random(playerEntity: ServerPlayerEntity): LootablePoolEntry? {
            return idToType.values.random().randomGenerator.apply(playerEntity)
        }

        fun randomList(playerEntity: ServerPlayerEntity, meanSize: Int): List<LootablePoolEntry> {
            val number = BinomialLootableNumber(meanSize, 0.5f)
            val size = number.nextInt()
            val list: MutableList<LootablePoolEntry> = mutableListOf()
            while (list.size < size) {
                random(playerEntity)?.let { list.add(it) }
            }
            return list
        }

        private val idToType: MutableMap<Identifier, LootablePoolEntryType> = mutableMapOf()
        private val typeToId: IdentityHashMap<LootablePoolEntryType, Identifier> = IdentityHashMap()

        val CODEC: Codec<LootablePoolEntryType> = Identifier.CODEC.flatXmap(
            { id -> idToType[id]?.let{ DataResult.success(it) } ?: DataResult.error{ "Not a valid Lootable Pool Entry Type: $id" } },
            { type -> typeToId[type]?.let{ DataResult.success(it) } ?: DataResult.error{ "Lootable Poo Entry Type not registered with a key" } }
        )

        val PACKET_CODEC: PacketCodec<RegistryByteBuf, LootablePoolEntryType> = PacketCodecs.codec(CODEC).cast()
    }

}