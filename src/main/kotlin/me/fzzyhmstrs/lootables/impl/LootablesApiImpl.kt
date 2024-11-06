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

package me.fzzyhmstrs.lootables.impl

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.api.IdKey
import me.fzzyhmstrs.lootables.loot.LootablesData
import me.fzzyhmstrs.lootables.loot.custom.CustomLootableEntry
import me.fzzyhmstrs.lootables.network.ChoicesS2CCustomPayload
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import java.util.*

internal object LootablesApiImpl {

    private val customEntries: MutableMap<Identifier, CustomLootableEntry> = mutableMapOf()

    internal fun supplyLootWithChoices(tableId: Identifier, playerEntity: ServerPlayerEntity, origin: Vec3d, onSuccess: BiConsumer<ServerPlayerEntity, Vec3d> = BiConsumer { _, _ -> }, onAbort: BiConsumer<ServerPlayerEntity, Vec3d> = BiConsumer { _, _ -> },  key: IdKey?, rolls: Int = 3, choices: Int = 1): Boolean {
        if (choices > rolls) throw IllegalArgumentException("Number of choices ($choices) greater than number of rolls ($rolls)")
        if (rolls < 1) throw IllegalArgumentException("Number of rolls can't be less than 1")
        if (key != null && !LootablesData.keyAvailable(key, playerEntity.uuid)) {
            return false
        }
        val table = LootablesData.getTable(tableId)
        if (table == null) {
            Lootables.LOGGER.error("Choices roll: Lootable table doesn't exist for ID $tableId")
            return false
        }
        val choiceKey = UUID.nameUUIDFromBytes((tableId.toString + playerEntity.uuid.toString + key.toString()).toByteArray())
        val pendingPoolChoices = LootablesData.getPendingPools(choiceKey)
        val poolChoices = if(pendingPoolChoices != null) {
            pendingPoolChoices
        } else {
            val storedPoolChoices = LootablesData.getStoredPools(choiceKey)
            if (storedPoolChoices != null) {
                LootablesData.setPending(choiceKey, playerEntity, origin, storedPoolChoices, key, onSuccess, onAbort)
                storedPoolChoices
            } else {
                val params = LootContextParameterSet.Builder(playerEntity.serverWorld).add(LootContextParameters.THIS_ENTITY, playerEntity).add(LootContextParameters.ORIGIN, origin)
                val context = LootContext.Builder(params.build(LootContextTypes.CHEST)).build(Optional.empty())
                val newPoolChoices = table.supplyPoolsById(context, rolls)
                LootablesData.setPending(choiceKey, playerEntity, origin, newPoolChoices, key, onSuccess, onAbort)
                newPoolChoices
            }
        }
        
        if (key != null)
            LootablesData.applyKey(key, playerEntity.uuid)
        val payload = ChoicesS2CCustomPayload(tableId, choiceKey, poolChoices, choices)
        ConfigApi.network().send(payload, playerEntity)
        return true
    }

    internal fun supplyLootRandomly(tableId: Identifier, playerEntity: ServerPlayerEntity, origin: Vec3d, key: IdKey?, rolls: Int = 1): Boolean {
        if (rolls < 1) throw IllegalArgumentException("Number of rolls can't be less than 1")
        if (key != null && !LootablesData.keyAvailable(key, playerEntity.uuid)) {
            return false
        }
        val table = LootablesData.getTable(tableId)
        if (table == null) {
            Lootables.LOGGER.error("Random roll: Lootable table doesn't exist for ID $tableId")
            return false
        }
        val params = LootContextParameterSet.Builder(playerEntity.serverWorld).add(LootContextParameters.THIS_ENTITY, playerEntity).add(LootContextParameters.ORIGIN, origin)
        val context = LootContext.Builder(params.build(LootContextTypes.CHEST)).build(Optional.empty())
        if (key != null)
            LootablesData.applyKey(key, playerEntity.uuid)
        table.applyPoolsRandomly(context, rolls)
        return true
    }

    internal fun registerCustomEntry(id: Identifier, entry: CustomLootableEntry) {
        if (customEntries.containsKey(id)) throw IllegalStateException("Custom lootable pool entry already registered at id: $id")
        customEntries[id] = entry
    }

    internal fun getCustomEntry(id: Identifier): CustomLootableEntry? {
        return customEntries[id]
    }

}
