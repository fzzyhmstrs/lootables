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
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootableRarity
import me.fzzyhmstrs.lootables.loot.LootableTable
import net.minecraft.util.Identifier
import java.util.function.UnaryOperator

/**
 * Builds a [LootableTable] for a data generator.
 * @author fzzyhmstrs
 * @since 0.2.0
 */
class LootableTableBuilder {
    private val pools: MutableList<Pair<Identifier, LootablePoolBuilder>> = mutableListOf()
    private val poolReferences: MutableList<Identifier> = mutableListOf()
    private var weightOverrides: MutableMap<LootableRarity, Int> = mutableMapOf()

    private fun eitherList(): List<Either<LootablePool, Identifier>> {
        return poolReferences.map { Either.right<LootablePool, Identifier>(it) } + pools.map { (id, builder) -> Either.left(builder.buildFull(id)) }
    }

    /**
     * Adds a [LootablePool] inline to this table. A pool added this way cannot be shared amongst other tables.
     * @param id [Identifier] the unique id for this pool. It must be unique between all inline and reference pools.
     * @param entry [LootablePoolEntry] the entry this pool will provide
     * @param builder [UnaryOperator]&lt;[LootablePoolBuilder]&gt; operator for creating the pool builder
     * @return this builder
     * @author fzzyhmstrs
     * @since 0.2.0
     */
    fun inlinePool(id: Identifier, entry: LootablePoolEntry, builder: UnaryOperator<LootablePoolBuilder>): LootableTableBuilder {
        pools.add(id to builder.apply(LootablePoolBuilder(entry)))
        return this
    }

    /**
     * Adds a [LootablePool] via reference to this table. Any reference pool, either added in this generator or provided elsewhere, can be referenced here.
     * @param id [Identifier] the resource id of the reference pool
     * @return this builder
     * @author fzzyhmstrs
     * @since 0.2.0
     */
    fun referencePool(id: Identifier): LootableTableBuilder {
        poolReferences.add(id)
        return this
    }

    /**
     * Creates an override for a default weight provided by a [LootableRarity]
     * @param rarity [LootableRarity] the rarity to override
     * @param weight the integer weight to override the default weight with. Must be greater than 0.
     * @return this builder
     * @author fzzyhmstrs
     * @since 0.2.0
     */
    fun override(rarity: LootableRarity, weight: Int): LootableTableBuilder {
        if (weight < 1) throw IllegalStateException("LootableTable weight override can't be less than 1")
        this.weightOverrides[rarity] = weight
        return this
    }

    /**
     * Sets the rarity weight overrides for this table.
     * @param overrides [Map]&lt;[LootableRarity], Int&gt; overrides map to apply.
     * @return this builder
     * @author fzzyhmstrs
     * @since 0.2.0
     */
    fun overrides(overrides: Map<LootableRarity, Int>): LootableTableBuilder {
        if (overrides.values.any { it < 1 }) throw IllegalStateException("LootableTable weight overrides map can't contain a weight less than 1")
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