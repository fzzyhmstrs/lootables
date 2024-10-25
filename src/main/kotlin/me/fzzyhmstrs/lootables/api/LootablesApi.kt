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

/**
 * API for interacting with Lootable Tables, as well as other miscellaneous methods
 * @author fzzyhmstrs
 * @since 0.1.0
 */
object LootablesApi {

    /**
     * Provide loot drops from a lootable table via loot choices, loot box style. A screen will pop up with the supplied number of rolls randomly selected as "tiles" for the user to choose from based on the number of choices supplied. 
     * @param tableId [Identifier] - the resource location of the Lootable Table you want to supply loot from
     * @param playerEntity [ServerPlayerEntity] the player to supply loot to
     * @param origin [Vec3d] the location you want loot supplied to. This may not be applicable if, for example, loot is added directly to the player inventory. However, some loot will interact with the location, providing an AOE buff perhaps, or scattering loot on the ground, or summoning a mount, and so on. 
     * @param key [IdKey], Nullable. A bounded key that indicates how many times this specific loot supply can be obtained by the player. Useful for containers that can only be looted once per player, for example. If null, no tracking will be done of how many times this supply is called for a specific player.
     * @param rolls Int, default 3. How many choice tiles will be rolled from the Lootable Tables available pools. If the Lootable Table doesn't have this many pools, it will supply all of its pools instead.
     * @param choices Int, default 1. How many tiles a user must pick before confirming their selection.
     * @throws IllegalArgumentException if choices is greater than rolls or rolls is less than 1.
     * @return true if the choices were successfully sent to the user, false otherwise. This is not confirming the loot is actually picked! The choices are made on the client and then sent back to the server for final supply. This means you will be able to send multiple requests even for a IdKey-bound call. Each request will override the one before it (reopen the choices screen), and user choices can still only be supplied a maximum if IdKey.count times. 
     * @author fzzyhmstrs
     * @since 0.1.0
     */
    @JvmStatic
    @JvmOverloads
    fun supplyLootWithChoices(tableId: Identifier, playerEntity: ServerPlayerEntity, origin: Vec3d, key: IdKey? = null, rolls: Int = 3, choices: Int = 1): Boolean {
        return LootablesApiImpl.supplyLootWithChoices(tableId, playerEntity, origin, key, rolls, choices)
    }

    /**
     * 
     * @author fzzyhmstrs
     * @since 0.1.0
     */
    @JvmStatic
    @JvmOverloads
    fun supplyLootRandomly(tableId: Identifier, playerEntity: ServerPlayerEntity, origin: Vec3d, key: IdKey? = null, rolls: Int = 1): Boolean {
        return LootablesApiImpl.supplyLootRandomly(tableId, playerEntity, origin, key, rolls)
    }

    @JvmStatic
    fun registerCustomEntry(id: Identifier, entry: CustomLootableEntry) {
        LootablesApiImpl.registerCustomEntry(id, entry)
    }
}
