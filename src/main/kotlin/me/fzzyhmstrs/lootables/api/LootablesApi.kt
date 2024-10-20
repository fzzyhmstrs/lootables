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

package me.fzzyhmstrs.lootables.api

import me.fzzyhmstrs.lootables.impl.LootablesApiImpl
import me.fzzyhmstrs.lootables.loot.custom.CustomLootableEntry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

object LootablesApi {

    @JvmStatic
    fun supplyLootWithChoices(tableId: Identifier, playerEntity: ServerPlayerEntity, origin: Vec3d, key: IdKey? = null, rolls: Int = 3, choices: Int = 1): Boolean {
        return LootablesApiImpl.supplyLootWithChoices(tableId, playerEntity, origin, key, rolls, choices)
    }

    @JvmStatic
    fun supplyLootRandomly(tableId: Identifier, playerEntity: ServerPlayerEntity, origin: Vec3d, key: IdKey? = null, rolls: Int = 1): Boolean {
        return LootablesApiImpl.supplyLootRandomly(tableId, playerEntity, origin, key, rolls)
    }

    @JvmStatic
    fun registerCustomEntry(id: Identifier, entry: CustomLootableEntry) {
        LootablesApiImpl.registerCustomEntry(id, entry)
    }
}