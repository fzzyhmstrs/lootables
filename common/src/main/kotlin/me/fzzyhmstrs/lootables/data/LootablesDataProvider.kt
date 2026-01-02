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

import com.mojang.serialization.JsonOps
import me.fzzyhmstrs.lootables.loot.LootablePool
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootableTable
import net.minecraft.data.DataGenerator
import net.minecraft.data.DataOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.DataWriter
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture
import java.util.function.UnaryOperator

/**
 * A data provider for lootable pools and tables. Register with your data generator in a [DataGenerator.Pack] in the typical way
 * - Fabric: `pack.addProvider(::LootableTableDataProviderSubClass)` (kotlin) or `pack.addProvider(LootableTableDataProviderSubClass::new);` (java)
 * - Neoforge: `event.getGenerator().addProvider(event.includeServer(), LootableTableDataProviderSubClass(event.getGenerator().getPackOutput(), event.getLookupProvider()))` (kotlin) or `pack.addProvider(LootableTableDataProviderSubClass::new);` (java)
 * @see configure
 * @author fzzyhmstrs
 * @since 0.2.0
 */
abstract class LootablesDataProvider(output: DataOutput, private val registryFuture: CompletableFuture<WrapperLookup>): DataProvider {

    private val poolResolver = output.getResolver(DataOutput.OutputType.DATA_PACK, "lootable_pool")
    private val tableResolver = output.getResolver(DataOutput.OutputType.DATA_PACK, "lootable_table")

    private val pools: MutableMap<Identifier, LootablePoolBuilder> = mutableMapOf()
    private val tables: MutableMap<Identifier, LootableTableBuilder> = mutableMapOf()

    /**
     * Add pools and tables in the configure method of your subclass using the [pool] and [table] methods
     * @param lookup [WrapperLookup] used for getting registry object information
     * @see pool
     * @see table
     * @author fzzyhmstrs
     * @since 0.2.0
     */
    abstract fun configure(lookup: WrapperLookup)

    /**
     * Adds a [LootablePool] to this provider. This pool will be separate from any particular table; add it to a table using its Identifier as a reference pool as seen below with `"mod_id:reference_pool"`
     *
     * In a [LootableTableBuilder], this reference is added using [LootableTableBuilder.referencePool]
     * ```
     * "pools": [
     *   "mod_id:reference_pool", <- A reference pool is utilized in a table like so
     *   {
     *     "id": "mod_id:inline_pool",
     *     "entry": {
     *       "type": "lootables:item",
     *       "item": "minecraft:stick"
     *     }
     *   }
     * ]
     * ```
     * @see LootableTableBuilder
     * @author fzzyhmstrs
     * @since 0.2.0
     */
    fun pool(id: Identifier, entry: LootablePoolEntry, builder: UnaryOperator<LootablePoolBuilder>) {
        if (pools.put(id, builder.apply(LootablePoolBuilder(entry))) != null) throw IllegalStateException("Lootable Pool was already provided for id [$id]")
    }

    /**
     * Adds a [LootableTable] to this provider, via a [LootableTableBuilder].
     *
     * If you want to define reference pools for use across multiple tables, use [pool] and then add the reference pool where desired with [LootableTableBuilder.referencePool]. You may reference pools outside of those built in this provider, even pools in completely separate mods, data packs, etc. as long as they are available at runtime. If they are not, the table will be skipped with an error.
     * @author fzzyhmstrs
     * @since 0.2.0
     */
    fun table(id: Identifier, builder: UnaryOperator<LootableTableBuilder>) {
        if (tables.put(id, builder.apply(LootableTableBuilder())) != null) throw IllegalStateException("Lootable Table was already provided for id [$id]")
    }

    override fun run(writer: DataWriter): CompletableFuture<*> {
        return getRegistryLookupFuture().thenCompose { lookup ->
            val ops = lookup.getOps(JsonOps.INSTANCE)
            val poolsFuture = CompletableFuture.allOf( *pools.map { (id, builder) ->
                val json = LootablePool.DATA_CODEC.encodeStart(ops, builder.build()).orThrow
                DataProvider.writeToPath(writer, json, poolResolver.resolveJson(id))
            }.toTypedArray())
            val tablesFuture = CompletableFuture.allOf( *tables.map { (id, builder) ->
                val json = LootableTableBuilder.TABLE_GEN_CODEC.encodeStart(ops, builder).orThrow
                DataProvider.writeToPath(writer, json, tableResolver.resolveJson(id))
            }.toTypedArray())
            CompletableFuture.allOf(poolsFuture, tablesFuture)
        }
    }

    private fun getRegistryLookupFuture(): CompletableFuture<WrapperLookup> {
        return registryFuture.thenApply { lookup ->
            tables.clear()
            configure(lookup)
            lookup
        }
    }

    override fun getName(): String {
        return "Lootable Tables and Pools"
    }
}