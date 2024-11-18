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
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.EitherMapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.fzzy_config.util.ValidationResult
import me.fzzyhmstrs.fzzy_config.util.ValidationResult.Companion.report
import me.fzzyhmstrs.lootables.Lootables
import net.minecraft.loot.condition.LootCondition
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.function.Consumer
import java.util.function.Function

internal class LootablePool private constructor(
    internal val id: Identifier,
    internal val rarity: LootableRarity,
    internal val entry: LootablePoolEntry,
    internal val guaranteed: Boolean,
    private val description: Optional<Text>,
    private val weight: Optional<Int>,
    private val maxUses: Int = -1,
    private val conditions: List<LootCondition> = listOf())
{

    private var data: LootablePoolData? = null

    fun canApply(context: LootContext): Boolean {
        val entity = context.get(LootContextParameters.THIS_ENTITY) ?: return false
        val uses = LootablesData.getUses(id, entity.uuid)
        if (maxUses in 1..uses) return false
        for (condition in conditions) {
            if (!condition.test(context)) return false
        }
        return true
    }

    fun apply(player: ServerPlayerEntity, origin: Vec3d) {
        LootablesData.use(id, player.uuid)
        this.entry.apply(player, origin)
    }

    fun getWeight(): Int? {
        return weight.orElse(null)
    }

    private fun initData(playerEntity: ServerPlayerEntity): LootablePoolData {
        val d = LootablePoolData.of(id, description.orElse(entry.serverDescription(playerEntity)), rarity, entry.createDisplay(playerEntity)).also { data = it }
        data = d
        return d
    }

    fun createData(playerEntity: ServerPlayerEntity): LootablePoolData {
        return data ?: initData(playerEntity)
    }

    override fun toString(): String {
        return "${Objects.toIdentityString(this)}(id=$id, rarity=$rarity, entry=$entry, guaranteed=$guaranteed, description=$description, weight=$weight, maxUses=$maxUses, conditions=$conditions, data=$data)"
    }

    internal companion object {

        private var seenIds: MutableSet<Identifier> = mutableSetOf()

        fun reset() {
            seenIds = mutableSetOf()
        }

        fun bake(loaders: Map<Identifier, PoolData>, errorReporter: Consumer<String>): ConcurrentMap<Identifier, LootablePool> {
            val map: ConcurrentMap<Identifier, LootablePool> = ConcurrentHashMap((loaders.size/0.95f).toInt() + 2, 0.95f)
            val errors: MutableList<String> = mutableListOf()
            for ((id, d) in loaders) {
                val result = create(id, d).report(errors)
                if (result.isValid()) {
                    map[id] = result.get()
                }
            }
            errors.forEach(errorReporter)
            return map
        }

        fun create(id: Identifier, d: PoolData): ValidationResult<LootablePool?> {
            if (seenIds.contains(id)) return ValidationResult.error(null, "LootablePool with ID $id already created!")
            seenIds.add(id)
            if (d.entry.isEmpty) return ValidationResult.error(null, "Entry required for pool $id")
            val rarity = d.rarity.orElse(LootableRarity.COMMON)
            val entry = d.entry.get()
            val guaranteed = d.guaranteed.orElse(false)
            val maxUses = d.maxUses.orElse(-1)
            val conditions = d.conditions.orElse(listOf())
            return ValidationResult.success(LootablePool(id, rarity, entry, guaranteed, d.description, d.weight, maxUses, conditions))
        }

        private fun createRandomPool(entry: LootablePoolEntry): LootablePool {
            return LootablePool(
                Lootables.identity(UUID.randomUUID().toString().lowercase()),
                LootableRarity.entries[Lootables.random().nextInt(LootableRarity.entries.size)],
                entry,
                Lootables.random().nextBoolean(),
                Optional.empty(),
                if (Lootables.random().nextBoolean()) Optional.empty() else Optional.of(Lootables.random().nextInt(12) + 1),
                if (Lootables.random().nextBoolean()) -1 else Lootables.random().nextInt(100) + 1
            )
        }

        fun createRandomPools(playerEntity: ServerPlayerEntity, meanSize: Int): List<LootablePool> {
            return LootablePoolEntryType.randomList(playerEntity, meanSize).map { createRandomPool(it) }
        }

        fun <T> Optional<T>.otherwise(other: Optional<T>): Optional<T> {
            return if (this.isPresent) this else other
        }

        private val CONDITION_CODEC: MapCodec<List<LootCondition>> = EitherMapCodec(LootCondition.CODEC.listOf().optionalFieldOf("conditions", listOf()), LootCondition.CODEC.optionalFieldOf("condition")).xmap(
            { e -> e.map(Function.identity()) { c -> c.map { listOf(it) }.orElse(listOf()) } },
            { l -> if(l.size != 1) Either.left(l) else Either.right(Optional.of(l[0])) }
        )

        private val OPTIONAL_CONDITION_CODEC: MapCodec<Optional<List<LootCondition>>> = EitherMapCodec(LootCondition.CODEC.listOf().optionalFieldOf("conditions"), LootCondition.CODEC.optionalFieldOf("condition")).xmap(
            { e -> e.map(Function.identity()) { c -> c.map { listOf(it) } } },
            { l -> if(l.isEmpty || l.get().size != 1) Either.left(l) else Either.right(l.map { it[0] }) }
        )

        private val checker: Function<Int, DataResult<Int>> = Function { i -> if (i == -1 || i > 0) DataResult.success(i) else DataResult.error { "Integer $i out of bounds [-1],[1,)" } }

        private val USES_CODEC: Codec<Int> = Codec.INT.flatXmap(checker, checker)

        val CODEC: Codec<LootablePool> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<LootablePool> ->
            instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(LootablePool::id),
                LootableRarity.CODEC.optionalFieldOf("rarity", LootableRarity.COMMON).forGetter(LootablePool::rarity),
                LootablePoolEntry.CODEC.fieldOf("entry").forGetter(LootablePool::entry),
                Codec.BOOL.optionalFieldOf("guaranteed", false).forGetter(LootablePool::guaranteed),
                TextCodecs.CODEC.optionalFieldOf("desc").forGetter(LootablePool::description),
                Codec.intRange(1, Int.MAX_VALUE).optionalFieldOf("weight").orElse(Optional.empty()).forGetter(LootablePool::weight),
                USES_CODEC.optionalFieldOf("max_uses", -1).forGetter(LootablePool::maxUses),
                CONDITION_CODEC.forGetter(LootablePool::conditions)
            ).apply(instance, ::LootablePool)
        }

        val REFERENCE_CODEC: Codec<LootablePool> = Identifier.CODEC.comapFlatMap(
            { id -> LootablesData.getPool(id)?.let { DataResult.success(it) } ?: DataResult.error { "Reference Lootable Pool $id not found." } },
            { lp -> lp.id }
        )

        val DATA_MAP_CODEC: MapCodec<PoolData> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<PoolData> ->
            instance.group(
                LootableRarity.CODEC.optionalFieldOf("rarity").forGetter(PoolData::rarity), //COMMON
                LootablePoolEntry.CODEC.optionalFieldOf("entry").forGetter(PoolData::entry),
                Codec.BOOL.optionalFieldOf("guaranteed").forGetter(PoolData::guaranteed), //false
                TextCodecs.CODEC.optionalFieldOf("desc").forGetter(PoolData::description), //null
                Codec.intRange(1, Int.MAX_VALUE).optionalFieldOf("weight").orElse(Optional.empty()).forGetter(PoolData::weight),
                Codec.INT.optionalFieldOf("max_uses").forGetter(PoolData::maxUses),
                OPTIONAL_CONDITION_CODEC.forGetter(PoolData::conditions),
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(PoolData::replace)
            ).apply(instance, ::PoolData)
        }

        val DATA_CODEC: Codec<PoolData> = DATA_MAP_CODEC.codec()

        val LOADER_CODEC: Codec<PoolLoader> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<PoolLoader> ->
            instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(PoolLoader::id),
                DATA_MAP_CODEC.forGetter(PoolLoader::data)
            ).apply(instance, ::PoolLoader)
        }

        data class PoolData(
            val rarity: Optional<LootableRarity>,
            val entry: Optional<LootablePoolEntry>,
            val guaranteed: Optional<Boolean>,
            val description: Optional<Text>,
            val weight: Optional<Int>,
            val maxUses: Optional<Int>,
            val conditions: Optional<List<LootCondition>>,
            val replace: Boolean = false)
        {
            fun composite(other: PoolData): PoolData {
                val r = other.rarity.otherwise(this.rarity)
                val e = other.entry.otherwise(this.entry)
                val g = other.guaranteed.otherwise(this.guaranteed)
                val d = other.description.otherwise(this.description)
                val w = other.weight.otherwise(this.weight)
                val m = other.maxUses.otherwise(this.maxUses)
                val c = other.conditions.otherwise(this.conditions)
                return PoolData(r, e, g, d, w, m, c, false)
            }
        }

        data class PoolLoader(
            val id: Identifier,
            val data: PoolData)
        {
            fun composite(o: Optional<PoolLoader>): PoolLoader {
                if (o.isEmpty) return this
                val other = o.get()
                val d = data.composite(other.data)
                return PoolLoader(this.id, d)
            }

            fun create(): ValidationResult<LootablePool?> {
                return create(this.id, this.data)
            }
        }
    }


}