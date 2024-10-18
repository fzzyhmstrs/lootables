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
import net.minecraft.util.Identifier

import java.util.IdentityHashMap

class LootablePoolEntryType private constructor(private val codec: MapCodec<out LootablePoolEntry>) { 
    fun codec(): MapCodec<out LootablePoolEntry> {
        return this.codec
    }

    companion object {        
        fun create(id: Identifier, codec: MapCodec<out LootablePoolEntry>): LootablePoolEntryType {
            val type = LootablePoolEntryType(codec)
            if (idToType.containsKey(id)) throw IllegalStateException("Duplicate type registered: $id")
            idToType[id] = id
            typeToId[type] = type
        }

        private val idToType: MutableMap<Identifier, LootablePoolEntryType> = mutableMapOf()
        private val typeToId: IdentityHashMap<LootablePoolEntryType, Identifier> = IdentityHashMap()

        val CODEC: Codec<LootablePoolEntryType> = Identifier.CODEC.flatXMap(
            { id -> idToType[id]?.let{ DataResult.success(it) } ?: DataResult.error{ "Not a valid Lootable Pool Entry Type: $id" } },
            { type -> typeToId[type]?.let{ DataResult.success(it) } ?: DataResult.error{ "Lootable Poo Entry Type not registered with a key" } } }
        )
    }
    
}
