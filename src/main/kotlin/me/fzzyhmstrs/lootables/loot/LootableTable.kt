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

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.EitherCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.fzzy_config.util.ValidationResult.Companion.report
import me.fzzyhmstrs.lootables.Lootables
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.function.Consumer
import java.util.function.Function

class LootableTable private constructor(private val pools: List<LootablePool>, private val poolMap: Map<Identifier, LootablePool>, private val weightOverrides: Map<LootableRarity, Int>) {

    private fun supplyPools(context: LootContext, rolls: Int): List<LootablePool> {
        if (rolls <= 0) return listOf()
        val entryList: MutableList<PoolEntry> = mutableListOf()
        var totalWeight = 0
        val guaranteedChoices: MutableList<LootablePool> = mutableListOf()
        for (pool in pools) {
            if (!pool.canApply(context)) continue
            if (pool.guaranteed) {
                guaranteedChoices.add(pool)
                if (guaranteedChoices.size == rolls) {
                    return guaranteedChoices
                }
                continue
            }
            val previousWeight = totalWeight
            totalWeight += getWeight(pool)
            entryList.add(PoolEntry(previousWeight, totalWeight, pool))
        }
        if (entryList.size <= rolls) {
            return entryList.map { it.pool }
        }
        val choices: MutableList<IntRange> = mutableListOf()
        val results: MutableList<LootablePool> = guaranteedChoices
        for (i in results.size until rolls) {
            var rand = Lootables.random().nextInt(totalWeight) + 1
            while (choices.any { r -> r.contains(rand) }) {
                rand = Lootables.random().nextInt(totalWeight) + 1
            }
            val find = binary(entryList, 0, entryList.lastIndex, rand, -1)
            choices.add((find.previousIndex + 1)..find.weightIndex)
            results.add(find.pool)
        }
        return results
    }

    private fun getWeight(pool: LootablePool): Int {
        return pool.getWeight() ?: weightOverrides[pool.rarity] ?: pool.rarity.weight
    }

    fun supplyPoolsById(context: LootContext, rolls: Int): List<Identifier> {
        return supplyPools(context, rolls).map { it.id }
    }

    fun applyPoolsRandomly(context: LootContext, count: Int) {
        val playerEntity = context.get(LootContextParameters.THIS_ENTITY) as? ServerPlayerEntity
        val origin = context.get(LootContextParameters.ORIGIN)
        if (playerEntity == null || origin == null) {
            Lootables.LOGGER.error("Loot context not provided with player and origin! Lootables table can't apply loot.")
            return
        }
        supplyPools(context, count).forEach { it.apply(playerEntity, origin) }
    }

    fun applyPoolsById(ids: List<Identifier>, playerEntity: ServerPlayerEntity, origin: Vec3d) {
        for (id in ids) {
            poolMap[id]?.apply(playerEntity, origin)
        }
    }

    fun preSync(type: LootablePoolEntry.InvalidationType): Boolean {
        var b = false
        for (p in pools) {
            b = b || p.invalidateData(type)
        }
        return b
    }

    fun sync(playerEntity: ServerPlayerEntity): List<LootablePoolData> {
        return pools.map { it.createData(playerEntity) }
    }

    private tailrec fun binary(list: List<PoolEntry>, left: Int, right: Int, target: Int, previous: Int): PoolEntry {
        val mid = if(left == right) {
            right
        } else { //handling the case that left == x, right == x + 1, and left has already been checked, and right is the success. integer rounding will never succeed on its own
            val m = (left + right) / 2
            if (m == previous) {
                right
            } else {
                m
            }
        }

        val entry = list[mid]
        if (entry.previousIndex < target && entry.weightIndex >= target) return entry
        val l2: Int
        val r2: Int
        if (entry.previousIndex >= target) {
            l2 = left
            r2 = mid
        } else {
            l2 = mid
            r2 = right
        }
        return binary(list, l2, r2, target, mid)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LootableTable

        return poolMap == other.poolMap
    }

    override fun hashCode(): Int {
        return poolMap.hashCode()
    }

    override fun toString(): String {
        return "LootableTable$pools"
    }

    private data class PoolEntry(val previousIndex: Int, val weightIndex: Int, val pool: LootablePool)

    companion object {

        internal fun bake(loaders: Map<Identifier, TableLoader>, errorReporter: Consumer<String>): ConcurrentMap<Identifier, LootableTable> {
            val map: ConcurrentMap<Identifier, LootableTable> = ConcurrentHashMap((loaders.size/0.95f).toInt() + 2, 0.95f)
            val errors: MutableList<String> = mutableListOf()
            for ((id, l) in loaders) {
                val e: MutableList<String> = mutableListOf()
                val result = l.getPools().mapNotNull { it.map({ pl -> pl.create().report(e).get() }, Function.identity()) }
                if (e.isNotEmpty()) {
                    errors.addAll(e.map { "$id: $it" })
                    errors.add("Skipping table $id due to errors")
                }
                map[id] = of(result, l.weightOverrides)
            }
            errors.forEach(errorReporter)
            return map
        }

        internal fun of(pools: List<LootablePool>, weightOverrides: Map<LootableRarity, Int> = mapOf()): LootableTable {
            fun getWeight(pool: LootablePool): Int {
                return pool.getWeight() ?: weightOverrides[pool.rarity] ?: pool.rarity.weight
            }
            return LootableTable(pools.sortedWith { p1, p2 -> getWeight(p1).compareTo(getWeight(p2)) }, pools.associateBy { pool -> pool.id }, weightOverrides)
        }

        fun random(playerEntity: ServerPlayerEntity, meanPoolSize: Int): LootableTable {
            val overrides: MutableMap<LootableRarity, Int> = mutableMapOf()
            if (Lootables.random().nextFloat() < 0.05f) {
                overrides[LootableRarity.COMMON] = Lootables.random().nextInt(12) + 1
            }
            if (Lootables.random().nextFloat() < 0.05f) {
                overrides[LootableRarity.UNCOMMON] = Lootables.random().nextInt(12) + 1
            }
            if (Lootables.random().nextFloat() < 0.05f) {
                overrides[LootableRarity.RARE] = Lootables.random().nextInt(12) + 1
            }
            if (Lootables.random().nextFloat() < 0.05f) {
                overrides[LootableRarity.EPIC] = Lootables.random().nextInt(12) + 1
            }
            if (Lootables.random().nextFloat() < 0.05f) {
                overrides[LootableRarity.LEGENDARY] = Lootables.random().nextInt(12) + 1
            }
            if (Lootables.random().nextFloat() < 0.05f) {
                overrides[LootableRarity.RAINBOW] = Lootables.random().nextInt(12) + 1
            }
            return of(LootablePool.createRandomPools(playerEntity, meanPoolSize), overrides)
        }

        private val OVERRIDES_CODEC = Codec.simpleMap(LootableRarity.CODEC, Codec.intRange(1, Int.MAX_VALUE), LootableRarity.KEYS)

        private val POOL_CODEC = Codec.withAlternative(LootablePool.CODEC, LootablePool.REFERENCE_CODEC)

        private val EMPTY_OVERRIDES = mapOf<LootableRarity, Int>()

        val CODEC: Codec<LootableTable> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<LootableTable> ->
            instance.group(
                POOL_CODEC.listOf().fieldOf("pools").forGetter(LootableTable::pools),
                OVERRIDES_CODEC.codec().optionalFieldOf("weights", EMPTY_OVERRIDES).forGetter(LootableTable::weightOverrides)
            ).apply(instance) { p, o -> of(p, o) }
        }

        private val POOL_LOADER_EITHER_CODEC = EitherCodec(LootablePool.LOADER_CODEC, LootablePool.REFERENCE_CODEC)

        internal val LOADER_CODEC = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<TableLoader> ->
            instance.group(
                POOL_LOADER_EITHER_CODEC.listOf().fieldOf("pools").forGetter(TableLoader::getPools),
                OVERRIDES_CODEC.codec().optionalFieldOf("weights", EMPTY_OVERRIDES).forGetter(TableLoader::weightOverrides),
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(TableLoader::replace)
            ).apply(instance, ::TableLoader)
        }

        internal data class TableLoader(
            val pools: Map<Identifier, Either<LootablePool.Companion.PoolLoader, LootablePool>>,
            val weightOverrides: Map<LootableRarity, Int>,
            val replace: Boolean = false)
        {
            constructor(pools: List<Either<LootablePool.Companion.PoolLoader, LootablePool>>, weightOverrides: Map<LootableRarity, Int>, replace: Boolean = false): this(
                pools.associateBy{ e -> e.map(LootablePool.Companion.PoolLoader::id, LootablePool::id) },
                weightOverrides,
                replace
            )

            fun getPools(): List<Either<LootablePool.Companion.PoolLoader, LootablePool>> {
                return pools.values.toList()
            }

            fun composite(other: TableLoader): TableLoader {
                val newPools: MutableMap<Identifier, Either<LootablePool.Companion.PoolLoader, LootablePool>> = mutableMapOf()
                for ((id, e) in pools) {
                    val candidate = other.pools[id]
                    if (candidate != null) {
                        newPools[id] = e.flatMap { l -> Either.left(l.composite(candidate.left())) }
                    } else {
                        newPools[id] = e
                    }
                }
                for ((id, e2) in other.pools) {
                    if (!pools.containsKey(id)) {
                        newPools[id] = e2
                    }
                }
                val newWeightOverrides: MutableMap<LootableRarity, Int> = mutableMapOf()
                for ((rarity, w) in weightOverrides) {
                    val candidate = other.weightOverrides[rarity]
                    if (candidate == null) {
                        newWeightOverrides[rarity] = w
                    } else if ( candidate > 0) {
                        newWeightOverrides[rarity] = candidate
                    }
                }
                for ((rarity, w2) in other.weightOverrides) {
                    if (!weightOverrides.containsKey(rarity)) {
                        newWeightOverrides[rarity] = w2
                    }
                }
                return TableLoader(newPools, newWeightOverrides, false)
            }
        }
    }

}