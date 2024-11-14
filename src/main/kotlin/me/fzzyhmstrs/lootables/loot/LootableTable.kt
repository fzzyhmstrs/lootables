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
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.lootables.Lootables
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

class LootableTable private constructor(private val pools: List<LootablePool>, private val poolMap: Map<Identifier, LootablePool>, private val overrides: Map<LootableRarity, Int>) {

    fun supplyPools(context: LootContext, rolls: Int): List<LootablePool> {
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
        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            println(entryList)
        }
        val choices: MutableList<Int> = mutableListOf()
        val results: MutableList<LootablePool> = guaranteedChoices
        for (i in results.size until rolls) {
            var rand = Lootables.random().nextInt(totalWeight) + 1
            while (choices.contains(rand)) {
                rand = Lootables.random().nextInt(totalWeight) + 1
            }
            results.add(binary(entryList, 0, entryList.lastIndex, rand).pool)
        }
        return results
    }

    private fun getWeight(pool: LootablePool): Int {
        return pool.getWeight() ?: overrides[pool.rarity] ?: pool.rarity.weight
    }

    fun supplyPoolsById(context: LootContext, rolls: Int): List<Identifier> {
        return supplyPools(context, rolls).map { it.id }
    }

    fun applyPoolsRandomly(context: LootContext, count: Int) {
        val playerEntity = context.get(LootContextParameters.THIS_ENTITY) as? PlayerEntity
        val origin = context.get(LootContextParameters.ORIGIN)
        if (playerEntity == null || origin == null) {
            Lootables.LOGGER.error("Loot context not provided with player and origin! Lootables table can't apply loot.")
            return
        }
        supplyPools(context, count).forEach { it.apply(playerEntity, origin) }
    }

    fun applyPoolsById(ids: List<Identifier>, playerEntity: PlayerEntity, origin: Vec3d) {
        for (id in ids) {
            poolMap[id]?.apply(playerEntity, origin)
        }
    }

    fun prepareForSync(playerEntity: ServerPlayerEntity): List<LootablePoolData> {
        return pools.map { it.createData(playerEntity) }
    }

    private tailrec fun binary(list: List<PoolEntry>, left: Int, right: Int, target: Int): PoolEntry {
        if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            println("left: $left")
            println("right: $right")
            println("target: $target")
        }
        val mid = (left + right) / 2
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
        return binary(list, l2, r2, target)
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

    private data class PoolEntry(val previousIndex: Int, val weightIndex: Int, val pool: LootablePool)

    companion object {
        fun of(pools: List<LootablePool>, overrides: Map<LootableRarity, Int> = mapOf()): LootableTable {
            fun getWeight(pool: LootablePool): Int {
                return pool.getWeight() ?: overrides[pool.rarity] ?: pool.rarity.weight
            }
            return LootableTable(pools.sortedWith { p1, p2 -> getWeight(p1).compareTo(getWeight(p2)) }, pools.associateBy { pool -> pool.id }, overrides)
        }

        private val OVERRIDES_CODEC = Codec.simpleMap(LootableRarity.CODEC, Codec.intRange(1, Int.MAX_VALUE), LootableRarity.KEYS)

        private val POOL_CODEC = Codec.withAlternative(LootablePool.CODEC, LootablePool.REFERENCE_CODEC)

        val CODEC = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<LootableTable> ->
            instance.group(
                POOL_CODEC.listOf().fieldOf("pools").forGetter(LootableTable::pools),
                OVERRIDES_CODEC.codec().optionalFieldOf("weights", mapOf()).forGetter(LootableTable::overrides)
            ).apply(instance) { p, o -> of(p, o) }
        }
    }

}