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

import net.minecraft.data.DataOutput
import net.minecraft.data.DataProvider
import net.minecraft.data.DataWriter
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

abstract class LootableTableDataProvider(output: DataOutput, registryFuture: CompletableFuture<WrapperLookup>): DataProvider {

    private val tableResolver = output.getResolver(DataOutput.OutputType.DATA_PACK, "lootable_table")
    private val poolResolver = output.getResolver(DataOutput.OutputType.DATA_PACK, "lootable_pool")

    private val tables: MutableMap<Identifier, LootableTableBuilder> = mutableMapOf()

    abstract fun configure(lookup: WrapperLookup)

    override fun run(writer: DataWriter): CompletableFuture<*> {
        TODO("Not yet implemented")
    }

    override fun getName(): String {
        return "Lootable Tables"
    }
}