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

package me.fzzyhmstrs.lootables.client

import me.fzzyhmstrs.lootables.loot.LootablePoolData
import net.minecraft.util.Identifier
import java.util.concurrent.ConcurrentHashMap

object LootablesClientData {

    private var tableData: ConcurrentHashMap<Identifier, ConcurrentHashMap<Identifier, LootablePoolData>> = ConcurrentHashMap()

    fun getData(table: Identifier, choices: List<Identifier>): List<LootablePoolData> {
        return choices.mapNotNull { tableData[table]?.get(it) }
    }

    fun receiveSync(data: Map<Identifier, List<LootablePoolData>>) {
        tableData = ConcurrentHashMap(data.mapValues { (_, data) -> ConcurrentHashMap(data.associateBy { it.id }) })
    }
}