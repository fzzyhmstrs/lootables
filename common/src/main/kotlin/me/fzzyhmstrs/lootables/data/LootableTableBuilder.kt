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

package me.fzzyhmstrs.lootables.data

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.EitherCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.lootables.loot.LootablePool
import me.fzzyhmstrs.lootables.loot.LootableRarity
import net.minecraft.util.Identifier
import java.util.function.UnaryOperator

class LootableTableBuilder {
    private val pools: MutableList<Pair<Identifier, LootablePoolBuilder>> = mutableListOf()
    private val poolReferences: MutableList<Identifier> = mutableListOf()
    private var weightOverrides: MutableMap<LootableRarity, Int> = mutableMapOf()

    private fun eitherList(): List<Either<LootablePool, Identifier>> {
        return poolReferences.map { Either.right<LootablePool, Identifier>(it) } + pools.map { (id, builder) -> Either.left(builder.buildFull(id)) }
    }

    fun pool(id: Identifier, builder: UnaryOperator<LootablePoolBuilder>): LootableTableBuilder {
        pools.add(id to builder.apply(LootablePoolBuilder()))
        return this
    }

    fun pool(id: Identifier): LootableTableBuilder {
        poolReferences.add(id)
        return this
    }

    fun override(rarity: LootableRarity, weight: Int): LootableTableBuilder {
        this.weightOverrides[rarity] = weight
        return this
    }

    fun overrides(overrides: Map<LootableRarity, Int>): LootableTableBuilder {
        this.weightOverrides = overrides.toMutableMap()
        return this
    }

    internal companion object {
        private val EITHER_CODEC: Codec<Either<LootablePool, Identifier>> = EitherCodec(LootablePool.CODEC, Identifier.CODEC)
        private val OVERRIDES_CODEC = Codec.simpleMap(LootableRarity.CODEC, Codec.intRange(1, Int.MAX_VALUE), LootableRarity.KEYS)

        val TABLE_GEN_CODEC = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<LootableTableBuilder> ->
            instance.group(
                EITHER_CODEC.listOf().fieldOf("pools").forGetter(LootableTableBuilder::eitherList),
                OVERRIDES_CODEC.codec().optionalFieldOf("weights", mapOf<LootableRarity, Int>()).forGetter(LootableTableBuilder::weightOverrides)
            ).apply(instance) { _, _ -> LootableTableBuilder() }
        }
    }
}