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
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import java.util.concurrent.ConcurrentHashMap

object LootablesClientData {

    private var tableData: ConcurrentHashMap<Identifier, ConcurrentHashMap<Identifier, LootablePoolData>> = ConcurrentHashMap()

    fun getData(table: Identifier, choices: List<Identifier>): List<LootablePoolData> {
        return choices.mapNotNull { tableData[table]?.get(it) }
    }

    fun receiveSync(data: Map<Identifier, List<LootablePoolData>>) {
        val start = System.currentTimeMillis()
        if (FabricLoader.getInstance().isDevelopmentEnvironment)
            println("Starting client data build")
        tableData = ConcurrentHashMap(data.mapValues { (_, data) -> ConcurrentHashMap(data.associateBy { it.id }) })
        if (FabricLoader.getInstance().isDevelopmentEnvironment)
            println("Finishing client data build in: ${System.currentTimeMillis()-start}ms")
        /*if (FabricLoader.getInstance().isDevelopmentEnvironment) {
            println("Receiving Sync for player ${MinecraftClient.getInstance().player}")
            for ((id, d) in tableData) {
                println(id)
                for ((id2, d2) in d) {
                    println(" >$id2")
                    println("  $d2")
                }
            }
        }*/
    }
}