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

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.mojang.serialization.JsonOps
import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.api.IdKey
import me.fzzyhmstrs.lootables.network.DataSyncS2CCustomPayload
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import java.io.File

import java.util.UUID
import kotlin.math.max
import kotlin.math.min

object LootablesData: SimpleSynchronousResourceReloadListener {

    private val usageData: UsageData by lazy {
        loadUsageData()
    }

    private var lootableTables: Map<Identifier, LootableTable> = mapOf()
    private var dataInvalid = true
    private var lootableSyncData: Map<Identifier, List<LootablePoolData>> = mapOf()

    fun getSyncData(): Map<Identifier, List<LootablePoolData>> {
        if (dataInvalid) {
            lootableSyncData = lootableTables.mapValues { (_, table) -> table.prepareForSync() }
        }
        return lootableSyncData
    }

    fun getTable(id: Identifier): LootableTable? {
        return lootableTables[id]
    }

    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun getUses(id: Identifier, uuid: UUID): Int {
        return usageData.usesMap[id]?.get(uuid) ?: 0
    }

    fun use(id: Identifier, uuid: UUID) {
        val map = usageData.usesMap.computeIfAbsent(id) { _ -> mutableMapOf() }
        val uses = map[uuid] ?: 0
        map[uuid] = uses + 1
    }

    fun keyAvailable(key: IdKey, uuid: UUID): Boolean {
        return (usageData.keyMap[key.id]?.get(uuid) ?: 0) < key.count
    }

    fun applyKey(key: IdKey, uuid: UUID) {
        val map = usageData.keyMap.computeIfAbsent(key.id) { _ -> mutableMapOf() }
        val uses = map[uuid] ?: 0
        map[uuid] = min(uses + 1, key.count)
    }

    fun retractKey(key: IdKey, uuid: UUID) {
        val map = usageData.keyMap.computeIfAbsent(key.id) { _ -> mutableMapOf() }
        val uses = map[uuid] ?: 0
        map[uuid] = max(uses - 1, 0)
    }

    private fun loadUsageData(): UsageData {
        try {
            val dir = ConfigApi.platform().gameDir()
            val f = File(dir, "lootables_uses.json")
            if (!f.exists()) return UsageData(mutableMapOf(), mutableMapOf())
            val str = f.readLines().joinToString("\n")
            if (str.isEmpty()) {
                return UsageData(mutableMapOf(), mutableMapOf())
            }
            val json = JsonParser.parseString(str)
            if (!json.isJsonObject) {
                Lootables.LOGGER.error("Lootables usage data file was malformed. Using empty map.")
                return UsageData(mutableMapOf(), mutableMapOf())
            }
            val keyMapJson = json.asJsonObject["key_map"]
            val keyMap = if (!keyMapJson.isJsonObject) {
                Lootables.LOGGER.error("Lootables key map was malformed. Using empty map.")
                mutableMapOf()
            } else {
                val keyMapTemp: MutableMap<Identifier, MutableMap<UUID, Int>> = mutableMapOf()
                for ((idStr, element) in keyMapJson.asJsonObject.entrySet()) {
                    if (!element.isJsonObject) {
                        Lootables.LOGGER.error("Lootables key map encountered read error for element $idStr. Not an object")
                        continue
                    }
                    val id = Identifier.tryParse(idStr)
                    if (id == null) {
                        Lootables.LOGGER.error("Lootables key map encountered read error for element $idStr. Not an object")
                        continue
                    }
                    val elementMap: MutableMap<UUID, Int> = mutableMapOf()
                    for ((uuidStr, elementElement) in element.asJsonObject.entrySet()) {
                        val uuid = UUID.fromString(uuidStr)
                        val usages = elementElement.asInt
                        elementMap[uuid] = usages
                    }
                    keyMapTemp[id] = elementMap
                }
                keyMapTemp
            }
            val usesMapJson = json.asJsonObject["uses_map"]
            val usesMap = if (!usesMapJson.isJsonObject) {
                Lootables.LOGGER.error("Lootables usage map was malformed. Using empty map.")
                mutableMapOf()
            } else {
                val usesMapTemp: MutableMap<Identifier, MutableMap<UUID, Int>> = mutableMapOf()
                for ((idStr, element) in usesMapJson.asJsonObject.entrySet()) {
                    if (!element.isJsonObject) {
                        Lootables.LOGGER.error("Lootables usage map encountered read error for element $idStr. Not an object")
                        continue
                    }
                    val id = Identifier.tryParse(idStr)
                    if (id == null) {
                        Lootables.LOGGER.error("Lootables usage map encountered read error for element $idStr. Not an object")
                        continue
                    }
                    val elementMap: MutableMap<UUID, Int> = mutableMapOf()
                    for ((uuidStr, elementElement) in element.asJsonObject.entrySet()) {
                        val uuid = UUID.fromString(uuidStr)
                        val usages = elementElement.asInt
                        elementMap[uuid] = usages
                    }
                    usesMapTemp[id] = elementMap
                }
                usesMapTemp
            }
            return UsageData(usesMap, keyMap)
        } catch (e: Throwable) {
            Lootables.LOGGER.error("Critical exception encountered while reading lootables usage map. Using empty map.")
            e.printStackTrace()
            return UsageData(mutableMapOf(), mutableMapOf())
        }
    }

    private fun saveUsageData() {
        val dir = ConfigApi.platform().gameDir()
        val f = File(dir, "lootables_uses.json")
        val json = JsonObject()
        val keyJson = JsonObject()
        for ((id, map) in usageData.keyMap) {
            val mapJson = JsonObject()
            for ((uuid, uses) in map) {
                mapJson.add(uuid.toString(), JsonPrimitive(uses))
            }
            keyJson.add(id.toString(), mapJson)
        }
        json.add("key_map", keyJson)
        val usesJson = JsonObject()
        for ((id, map) in usageData.usesMap) {
            val mapJson = JsonObject()
            for ((uuid, uses) in map) {
                mapJson.add(uuid.toString(), JsonPrimitive(uses))
            }
            usesJson.add(id.toString(), mapJson)
        }
        json.add("uses_map", usesJson)
        if (f.exists()) {
            f.writeText(gson.toJson(json))
        } else if (f.createNewFile()) {
            f.writeText(gson.toJson(json))
        } else {
            Lootables.LOGGER.error("Couldn't create new lootables_uses json; data wasn't saved.")
        }
    }

    override fun reload(manager: ResourceManager) {
        dataInvalid = true
        lootableSyncData = mapOf()
        val map: MutableMap<Identifier, LootableTable> = mutableMapOf()
        manager.findResources("lootable_tables") { path -> path.path.endsWith(".json") }
            .forEach { (id, resource) ->
                try {
                    val reader = resource.reader
                    val json = JsonParser.parseReader(reader)
                    val result = LootableTable.CODEC.parse(JsonOps.INSTANCE, json)
                    val tableId = Identifier.of(id.namespace, id.path.removePrefix("lootable_tables/").removeSuffix(".json"))
                    result.ifError { error -> Lootables.LOGGER.error(error.messageSupplier.get()) }.ifSuccess { table -> map[tableId] = table }
                } catch (e: Throwable) {
                    Lootables.LOGGER.error("Error parsing lootable table $id")
                    e.printStackTrace()
                }
            }
        lootableTables = map
    }

    override fun getFabricId(): Identifier {
        return Lootables.identity("data_reloader")
    }

    fun init() {
        ServerLifecycleEvents.BEFORE_SAVE.register { _, _, _ ->
            saveUsageData()
        }

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(this)

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { server, _, _ ->
            for (player in server.playerManager.playerList) {
                ConfigApi.network().send(DataSyncS2CCustomPayload(getSyncData()), player)
            }
        }

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            ConfigApi.network().send(DataSyncS2CCustomPayload(getSyncData()), handler.getPlayer())
        }
    }

    private class UsageData(val usesMap: MutableMap<Identifier, MutableMap<UUID, Int>>, val keyMap: MutableMap<Identifier, MutableMap<UUID, Int>>)

}