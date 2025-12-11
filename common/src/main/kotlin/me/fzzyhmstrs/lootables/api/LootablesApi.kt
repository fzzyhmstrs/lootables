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
import me.fzzyhmstrs.lootables.loot.custom.CustomLootableEntryDisplay
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d

import java.util.function.BiConsumer

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
     * @param onSuccess [BiConsumer]&lt;[ServerPlayerEntity], [Vec3d]&gt; action to take when choices are made successfully. This isn't run when this method returns true. That indicates that the choices were _generated_ successfully. This will fire when the player makes choices on the client side and the C2S confirmation packet is received on the server.
     * @param onAbort [BiConsumer]&lt;[ServerPlayerEntity], [Vec3d]&gt; action to take when choices are aborted by the player (not made). This isn't run when this method returns false. That indicates that the choices caouldn't be _generated_. This will fire when the player closes the screen for some reason before they make their loot choices. The choices generated will be stored, and if this method is called with the same tableId, player, and idkey, those aborted choices will be resent. This prevents players from, for example, continuously rerolling a loot bag item by escaping the choices screen until they get a roll they like.
     * @param key [IdKey], Nullable. A bounded key that indicates how many times this specific loot supply can be obtained by the player. Useful for containers that can only be looted once per player, for example. If null, no tracking will be done of how many times this supply is called for a specific player.
     * @param rolls Int, default 3. How many choice tiles will be rolled from the Lootable Tables available pools. If the Lootable Table doesn't have this many pools, it will supply all of its pools instead.
     * @param choices Int, default 1. How many tiles a user must pick before confirming their selection.
     * @throws IllegalArgumentException if choices is greater than rolls or rolls is less than 1.
     * @return true if the choices were successfully sent to the user, false otherwise. This is not confirming the loot is actually picked! The choices are made on the client and then sent back to the server for final supply. This means you will be able to send multiple requests even for a IdKey-bound call. Each request will override the one before it (reopen the choices screen), and user choices can still only be supplied a maximum if IdKey.count times.
     * @author fzzyhmstrs
     * @since 0.1.0, new origin overload 0.1.3
     */
    @JvmStatic
    @JvmOverloads
    fun supplyLootWithChoices(
        tableId: Identifier,
        playerEntity: ServerPlayerEntity,
        origin: Vec3d = playerEntity.pos,
        onSuccess: BiConsumer<ServerPlayerEntity, Vec3d> = BiConsumer { _, _ -> },
        onAbort: BiConsumer<ServerPlayerEntity?, Vec3d> = BiConsumer { _, _ -> },
        key: IdKey? = null,
        rolls: Int = 3,
        choices: Int = 1)
    : Boolean {
        return LootablesApiImpl.supplyLootWithChoices(tableId, playerEntity, origin, onSuccess, onAbort, key, rolls, choices)
    }

    /**
     * Provide loot drops from a lootable table via loot choices, randomly. Will roll pools and supply the chosen loot automatically. Basically an advanced vanilla loot table roll.
     * @param tableId [Identifier] - the resource location of the Lootable Table you want to supply loot from
     * @param playerEntity [ServerPlayerEntity] the player to supply loot to
     * @param origin [Vec3d] the location you want loot supplied to. This may not be applicable if, for example, loot is added directly to the player inventory. However, some loot will interact with the location, providing an AOE buff perhaps, or scattering loot on the ground, or summoning a mount, and so on.
     * @param key [IdKey], Nullable. A bounded key that indicates how many times this specific loot supply can be obtained by the player. Useful for containers that can only be looted once per player, for example. If null, no tracking will be done of how many times this supply is called for a specific player.
     * @param rolls Int, default 1. How many pools will be rolled from the Lootable Tables available pools. If the Lootable Table doesn't have this many pools, it will supply all of its pools instead.
     * @author fzzyhmstrs
     * @since 0.1.0, new origin overload 0.1.3
     */
    @JvmStatic
    @JvmOverloads
    fun supplyLootRandomly(
        tableId: Identifier,
        playerEntity: ServerPlayerEntity,
        origin: Vec3d = playerEntity.pos,
        key: IdKey? = null,
        rolls: Int = 1)
    : Boolean {
        return LootablesApiImpl.supplyLootRandomly(tableId, playerEntity, origin, key, rolls)
    }

    /**
     * Supplies the loot from a single loot pool. This is by nature only as random as the pool is. A RANDOM pool will somewhat mimic rolling 1 pool from a lootable table, otherwise the pool will always supply what it will.
     *
     * This only works for standalone pools. Pools created inline with a table aren't mapped for use by this method.
     * @param poolId [Identifier] standalone pool resource location id.
     * @param playerEntity [ServerPlayerEntity] the player to apply the loot to.
     * @param origin [Vec3d] the location of loot supply. This doesn't necessarily have to be at the players position, but often is.
     * @return true if the poolId is valid, false if not. The loot is only supplied on a success.
     * @author fzzyhmstrs
     * @since 0.1.3
     */
    @JvmStatic
    @JvmOverloads
    fun supplySinglePool(
        poolId: Identifier,
        playerEntity: ServerPlayerEntity,
        origin: Vec3d = playerEntity.pos)
    : Boolean {
        return LootablesApiImpl.supplySinglePool(poolId, playerEntity, origin)
    }

    /**
     * Registers a [CustomLootableEntry] for use in the `"custom"` lootable pool type.
     * @param id [Identifier] unique id to register this entry to.
     * @param entry [CustomLootableEntry] the entry to register.
     * @param entryDisplay [CustomLootableEntryDisplay] the display corresponding to the entry, registered alongside its entry.
     * @author fzzyhmstrs
     * @since 0.1.0
     */
    @JvmStatic
    fun registerCustomEntry(id: Identifier, entry: CustomLootableEntry, entryDisplay: CustomLootableEntryDisplay) {
        LootablesApiImpl.registerCustomEntry(id, entry, entryDisplay)
    }

    /**
     * Determines if the player can apply the provided IdKey. This is typically used to determine if a player can roll a Lootable Table before trying.
     * @param key [IdKey] the key to test
     * @param playerEntity [ServerPlayerEntity] the server player checked against.
     * @return True if the key can still be applied, false otherwise. NOTE: This information is only on the server. If you need this result on the client for some reason, you will need to sync it as needed.
     * @author fzzyhmstrs
     * @since 0.1.3
     */
    @JvmStatic
    fun canApplyKey(key: IdKey, playerEntity: ServerPlayerEntity): Boolean {
        return LootablesApiImpl.canApplyKey(key, playerEntity)
    }

    /**
     * Resets the progress of a particular [IdKey] for a particular player
     * @param key [IdKey] the key to reset
     * @param playerEntity [ServerPlayerEntity] the server player to reset for
     * @author fzzyhmstrs
     * @since 0.2.0
     */
    @JvmStatic
    fun resetKey(key: IdKey, playerEntity: ServerPlayerEntity) {
        LootablesApiImpl.resetKey(key, playerEntity)
    }

    /**
     * Resets the progress of a particular [IdKey] for all players
     * @param key [IdKey] the key to reset
     * @param server [MinecraftServer] the server the lootables data is attached to
     * @author fzzyhmstrs
     * @since 0.2.0
     */
    @JvmStatic
    fun resetKey(key: IdKey, server: MinecraftServer) {
       LootablesApiImpl.resetKey(key, server)
    }
}
