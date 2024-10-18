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

import net.minecraft.util.Identifier

import java.util.UUID

class LootableData {

    private val usesMap: MutableMap<Identifier, MutableMap<UUID, Int>> by lazy {
        loadUsesMap()
    }

    private fun loadUsesMap(): MutableMap<Identifier, MutableMap<UUID, Int>> {
        TODO()
    }
  
    fun getUses(id: Identifier, uuid: UUID): Int {
        return usesMap.get(id)?.get(uuid) ?: 0
    }

    fun use(id: Identifier, uuid: UUID) {
        val map = usesMap.computeIfAbsent(id) { _ -> mutableMapOf() }
        val uses = map[uuid] ?: 0
        map[uuid] = uses + 1
    }

    
}
