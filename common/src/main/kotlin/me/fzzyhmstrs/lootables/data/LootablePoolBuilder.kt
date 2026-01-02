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

import me.fzzyhmstrs.lootables.loot.LootablePool
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootableRarity
import me.fzzyhmstrs.lootables.loot.entry.CommandLootablePoolEntry
import net.minecraft.loot.condition.AnyOfLootCondition
import net.minecraft.loot.condition.LootCondition
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.*

/**
 * Builder for a [LootablePool]. The pool will be an inline pool when used in [LootableTableBuilder.inlinePool], as the name implies. It will be a reference pool when defined in [LootablesDataProvider.pool], usable in tables via [LootableTableBuilder.referencePool]
 * @author fzzyhmstrs
 * @since 0.2.0
 */
class LootablePoolBuilder(private val entry: LootablePoolEntry) {

    private var rarity: LootableRarity = LootableRarity.COMMON
    private var guaranteed: Boolean = false
    private var desc: Text? = null
    private var weight: Int? = null
    private var maxUses: Int = -1
    private val conditions: MutableList<LootCondition.Builder> = mutableListOf()

    /**
     * Defines a [LootableRarity] for this pool; default is [LootableRarity.COMMON].
     *
     * If a [weight] is not defined, the weight of the table will be based on the default weight of the rarity. COMMON has a weight of 12, UNCOMMON 6, and so on.
     * @param rarity [LootableRarity] for this pool
     * @return this builder
     * @author fzzyhmstrs
     * @since 0.2.0
     */
    fun rarity(rarity: LootableRarity): LootablePoolBuilder {
        this.rarity = rarity
        return this
    }

    /**
     * Makes this pool a guaranteed roll. If used, [weight] isn't particularly useful to define, since it's guaranteed to be rolled.
     *
     * Very useful with [maxUses] if you want to guarantee a certain pool will appear, but only be available once/limited times. A legendary boss drop, for example, that you want to guarantee drops from the boss, but that will only drop once no matter how many times it is defeated. `guaranteed()` plus `maxUses(1)` enables that pattern
     * @return this builder
     * @author fzzyhmstrs
     * @since 0.2.0
     */
    fun guaranteed(): LootablePoolBuilder {
        this.guaranteed = true
        return this
    }

    /**
     * Adds a descriptive caption for this pool when using "choice" mode of loot generation. This is extremely useful for some types of pool entries, such as [CommandLootablePoolEntry], which otherwise has no way of communicating what the entry will actually *do* if the player selects it
     * @return this builder
     * @author fzzyhmstrs
     * @since 0.2.0
     */
    fun desc(desc: Text): LootablePoolBuilder {
        this.desc = desc
        return this
    }

    /**
     * Defines a custom roll weight for this pool. This weight determines the weighted chance that this pool will appear in a roll. With a defined weight of 15 and a total weight of 30 across all pools in a table, this pool will have a 50% chance of rolling.
     * @param weight
     * @author fzzyhmstrs
     * @since 0.2.0
     */
    fun weight(weight: Int): LootablePoolBuilder {
        if (weight < 1) throw IllegalStateException("Lootable pool weight can't be less than 1")
        this.weight = weight
        return this
    }

    /**
     * Sets the max number of uses that this pool can be rolled by a player. This max is common across all tables that use this pool.
     *
     * Very useful with [guaranteed] if you want to guarantee a certain pool will appear, but only be available once/limited times. A legendary boss drop, for example, that you want to guarantee drops from the boss, but that will only drop once no matter how many times it is defeated. `guaranteed()` plus `maxUses(1)` enables that pattern
     * @param maxUses how many times this pool can be chosen across all tables that use the pool
     * @return this builder
     * @author fzzyhmstrs
     * @since 0.2.0
     */
    fun maxUses(maxUses: Int): LootablePoolBuilder {
        if (maxUses < 1) throw IllegalStateException("Lootable pool weight can't be less than 1")
        this.maxUses = maxUses
        return this
    }

    /**
     * Adds a condition that must pass for this pool to be considered in a loot roll. All conditions supplied must pass; if you want "OR" functionality, use an [AnyOfLootCondition.Builder] and nest the optional conditions in it.
     * @param condition [LootCondition.Builder] to add to this pool's conditions.
     * @return this builder
     * @author fzzyhmstrs
     * @since 0.2.0
     */
    fun condition(condition: LootCondition.Builder): LootablePoolBuilder {
        this.conditions.add(condition)
        return this
    }

    internal fun buildFull(identifier: Identifier): LootablePool {
        return LootablePool(
            identifier,
            rarity,
            entry,
            guaranteed,
            Optional.ofNullable(desc),
            Optional.ofNullable(weight),
            maxUses,
            conditions.map { it.build() })
    }

    internal fun build(): LootablePool.Companion.PoolData {
        return LootablePool.Companion.PoolData(
            Optional.of(rarity),
            Optional.of(entry),
            Optional.of(guaranteed),
            Optional.ofNullable(desc),
            Optional.ofNullable(weight),
            Optional.of(maxUses),
            Optional.of(conditions.map { it.build() }))
    }

}