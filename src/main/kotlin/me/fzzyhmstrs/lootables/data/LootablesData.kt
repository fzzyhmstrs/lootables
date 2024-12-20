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

import com.google.gson.*
import com.mojang.serialization.JsonOps
import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.api.IdKey
import me.fzzyhmstrs.lootables.loot.LootablePool
import me.fzzyhmstrs.lootables.loot.LootablePoolData
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootableTable
import me.fzzyhmstrs.lootables.network.AbortChoicesC2SCustomPayload
import me.fzzyhmstrs.lootables.network.ChosenC2SCustomPayload
import me.fzzyhmstrs.lootables.network.DataSyncS2CCustomPayload
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.registry.RegistryWrapper
import net.minecraft.resource.ResourceManager
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec3d
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ForkJoinPool
import java.util.function.BiConsumer
import java.util.stream.Stream
import kotlin.math.max
import kotlin.math.min

internal object LootablesData {

    private var lootablePools: Map<Identifier, LootablePool> = mapOf()
    private var lootableTables: Map<Identifier, LootableTable> = mapOf()
    private var dataInvalid = true
    private var lootableSyncData: SyncDataHolder = SyncDataHolder.Empty
    private val pendingChoicesMap: MutableMap<UUID, PendingChoices> = mutableMapOf()
    private val abortedChoices: MutableSet<UUID> = mutableSetOf()
    private val storedChoices: MutableMap<UUID, List<Identifier>> by lazy {
        loadStoredChoices()
    }

    private val usageData: UsageData by lazy {
        loadUsageData()
    }

    private fun getSyncData(players: List<ServerPlayerEntity>): SyncDataHolder {
        if (dataInvalid) {
            lootableTables.forEach { it.value.preSync(LootablePoolEntry.InvalidationType.INIT) }

            var b = false

            val m: Map<ServerPlayerEntity, Map<Identifier, List<LootablePoolData>>> = players.associateByTo(
                ConcurrentHashMap((players.size/0.95f).toInt(), 1f),
                { p -> p },
                { p ->
                    lootableTables.forEach { b = b || it.value.preSync(LootablePoolEntry.InvalidationType.PLAYER) }
                    lootableTables.mapValuesTo(ConcurrentHashMap((lootableTables.size/0.95f).toInt(), 1f)) { (_, tables) -> tables.sync(p) }
                }
            )
            lootableSyncData = SyncDataHolder.create(b, m)
            dataInvalid = false
        }
        return lootableSyncData
    }

    fun getPool(id: Identifier): LootablePool? {
        return lootablePools[id]
    }

    fun hasTable(id: Identifier): Boolean {
        return lootableTables.containsKey(id)
    }

    fun getTable(id: Identifier): LootableTable? {
        return lootableTables[id]
    }

    fun getTableIds(): Set<Identifier> {
        return lootableTables.keys
    }

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

    fun applyChosen(payload: ChosenC2SCustomPayload, playerEntity: ServerPlayerEntity) {
        val pending = pendingChoicesMap[payload.choiceKey]
        pending?.succeed(playerEntity)
        getTable(payload.table)?.applyPoolsById(payload.chosen, playerEntity, pending?.pos ?: Vec3d.ZERO)
        pendingChoicesMap.remove(payload.choiceKey)
    }

    fun applyAbort(payload: AbortChoicesC2SCustomPayload, playerEntity: ServerPlayerEntity) {
        if (abortedChoices.contains(payload.choiceKey)) return
        val pending = pendingChoicesMap[payload.choiceKey] ?: return
        pending.abort(playerEntity)
        if (pending.key != null) {
            retractKey(pending.key, playerEntity.uuid)
        }
        abortedChoices.add(payload.choiceKey)
    }

    fun setPending(choiceKey: UUID, playerEntity: ServerPlayerEntity, pos: Vec3d, poolChoices: List<Identifier>, key: IdKey?, onSuccess: BiConsumer<ServerPlayerEntity, Vec3d>, onAbort: BiConsumer<ServerPlayerEntity, Vec3d>) {
        pendingChoicesMap[choiceKey] = PendingChoices(playerEntity.uuid, pos, poolChoices, key, onSuccess, onAbort)
    }

    fun getPendingPools(choiceKey: UUID): List<Identifier>? {
        val pending = pendingChoicesMap[choiceKey] ?: return null
        abortedChoices.remove(choiceKey)
        return pending.poolChoices
    }

    fun getStoredPools(choiceKey: UUID): List<Identifier>? {
        val stored = storedChoices[choiceKey] ?: return null
        storedChoices.remove(choiceKey)
        return stored
    }

    //region LOADING & SAVING

    private val gson = GsonBuilder().setPrettyPrinting().create()

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
                Lootables.LOGGER.error("Lootables usage map was malformed. Using empty data.")
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
            Lootables.LOGGER.error("Critical exception encountered while reading lootables usage data. Using empty data.")
            e.printStackTrace()
            return UsageData(mutableMapOf(), mutableMapOf())
        }
    }

    private fun saveUsageData() {
        if (usageData.isEmpty()) return
        try {
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
        } catch(e: Throwable) {
            Lootables.LOGGER.error("Critical exception encountered while saving lootables usage data. Not saved.")
            e.printStackTrace()
        }
    }

    private fun loadStoredChoices(): MutableMap<UUID, List<Identifier>> {
        try {
            val dir = ConfigApi.platform().gameDir()
            val f = File(dir, "lootables_choices.json")
            if (!f.exists()) return mutableMapOf()
            val str = f.readLines().joinToString("\n")
            if (str.isEmpty()) {
                return mutableMapOf()
            }
            val json = JsonParser.parseString(str)
            if (!json.isJsonObject) {
                Lootables.LOGGER.error("Lootables stored choices file was malformed. Using empty map.")
                return mutableMapOf()
            }
            val map: MutableMap<UUID, List<Identifier>> = mutableMapOf()
            for ((uuidStr, element) in json.asJsonObject.entrySet()) {
                if (!element.isJsonArray) {
                    Lootables.LOGGER.error("Lootables stored choices encountered read error for element $uuidStr. Not an array")
                    continue
                }
                val uuid = UUID.fromString(uuidStr)
                val list: MutableList<Identifier> = mutableListOf()
                for (idElement in element.asJsonArray) {
                    if (!idElement.isJsonPrimitive) {
                        Lootables.LOGGER.error("Lootables stored choices encountered read error for list in element $uuidStr. Not a string")
                        continue
                    }
                    val id = Identifier.tryParse(idElement.asString)
                    if (id == null) {
                        Lootables.LOGGER.error("Lootables stored choices encountered read error for list in element $uuidStr. Not a valid identifier")
                        continue
                    }
                    list.add(id)
                }
                map[uuid] = list
            }
            return map
        } catch(e: Exception) {
            Lootables.LOGGER.error("Critical exception encountered while reading lootables stored choices.")
            e.printStackTrace()
            return mutableMapOf()
        }
    }

    private fun saveStoredChoices() {
        if (storedChoices.isEmpty()) return
        try {
            val dir = ConfigApi.platform().gameDir()
            val f = File(dir, "lootables_choices.json")
            val json = JsonObject()
            for ((uuid, pendingChoices) in pendingChoicesMap) {
                val jsonArray = JsonArray()
                for (choice in pendingChoices.poolChoices) {
                    jsonArray.add(choice.toString())
                }
                json.add(uuid.toString(), jsonArray)
            }
            if (f.exists()) {
                f.writeText(gson.toJson(json))
            } else if (f.createNewFile()) {
                f.writeText(gson.toJson(json))
            } else {
                Lootables.LOGGER.error("Couldn't create new lootables_choices json; data wasn't saved.")
            }
        } catch(e: Throwable) {
            Lootables.LOGGER.error("Critical exception encountered while saving lootables stored choice data. Not saved.")
            e.printStackTrace()
        }
    }

    //endregion

    private const val PARALLEL_BREAKPOINT = 512

    private fun<T> Map<Identifier, T>.ifStream(): Stream<Map.Entry<Identifier, T>> {
        return if (this.size < PARALLEL_BREAKPOINT) {
            this.entries.stream()
        } else {
            this.entries.parallelStream()
        }
    }

    fun reload(manager: ResourceManager, dynamicRegistries: RegistryWrapper.WrapperLookup) {

        dataInvalid = true
        lootableSyncData = SyncDataHolder.Empty
        LootablePool.reset()

        Lootables.LOGGER.info("Starting lootables table load")
        val start = System.currentTimeMillis()
        val ops = dynamicRegistries.getOps(JsonOps.INSTANCE)

        val poolMap: MutableMap<Identifier, LootablePool.Companion.PoolData>
        manager.findResources("lootable_pool") {
                path -> path.path.endsWith(".json")
        }.also {
            poolMap = if (it.size < PARALLEL_BREAKPOINT) {
                ConcurrentHashMap((it.size / 0.95f + 2).toInt(), 0.95f)
            } else {
                ConcurrentHashMap((it.size / 0.95f + 2).toInt(), 0.95f, ForkJoinPool.getCommonPoolParallelism())
            }
        }.ifStream().forEach { (id, resource) ->
                try {
                    val reader = resource.reader
                    val json = JsonParser.parseReader(reader)
                    val result = LootablePool.DATA_CODEC.parse(ops, json)
                    result.ifSuccess { data ->
                        val poolId = Identifier.of(id.namespace, id.path.substring(14, id.path.length - 5))
                        if(data.replace) {
                            poolMap[poolId] = data
                        } else {
                            poolMap.compute(poolId) { _, d ->
                                d?.composite(data) ?: data
                            }
                        }
                    }.ifError { error ->
                        Lootables.LOGGER.error("Error parsing Lootables pool file: $id")
                        Lootables.LOGGER.error(error.messageSupplier.get())
                    }
                } catch (e: Throwable) {
                    Lootables.LOGGER.error("Critical error encountered while parsing lootable pool $id", e)
                }
            }
        lootablePools = LootablePool.bake(poolMap, Lootables.LOGGER::error)

        val tableMap: MutableMap<Identifier, LootableTable.Companion.TableLoader>
        manager.findResources("lootable_table") {
            path -> path.path.endsWith(".json")
        }.also {
            tableMap = if (it.size < PARALLEL_BREAKPOINT) {
                ConcurrentHashMap((it.size / 0.95f + 2).toInt(), 0.95f)
            } else {
                ConcurrentHashMap((it.size / 0.95f + 2).toInt(), 0.95f, ForkJoinPool.getCommonPoolParallelism())
            }
        }.ifStream().forEach { (id, resource) ->
                try {
                    val reader = resource.reader
                    val json = JsonParser.parseReader(reader)
                    val result = LootableTable.LOADER_CODEC.parse(ops, json)
                    result.ifSuccess { table ->
                        val tableId = Identifier.of(id.namespace, id.path.substring(15, id.path.length - 5))
                        if (table.replace) {
                            tableMap[tableId] = table
                        } else {
                            tableMap.compute(tableId) { _, loader ->
                                loader?.composite(table) ?: table
                            }
                        }
                    }.ifError { error ->
                        Lootables.LOGGER.error("Error while parsing Lootables table file: $id")
                        Lootables.LOGGER.error(error.messageSupplier.get())
                    }
                } catch (e: Throwable) {
                    Lootables.LOGGER.error("Critical error encountered while parsing Lootables table $id", e)
                }
            }
        lootableTables = LootableTable.bake(tableMap, Lootables.LOGGER::error)
        Lootables.LOGGER.info("Finished lootable table load in ${System.currentTimeMillis() - start}ms")
        LootablePool.reset()
    }

    fun init() {

        ServerLifecycleEvents.BEFORE_SAVE.register { _, _, _ ->
            saveUsageData()
        }

        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            //performed syncronously to avoid race condition with join or something
            reload(server.resourceManager, server.reloadableRegistries.registryManager)
            server.isDedicated
        }

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { server, resourceManager, _ ->
            if (server.playerManager.playerList.isEmpty()) return@register
            CompletableFuture.supplyAsync {
                reload(resourceManager, server.reloadableRegistries.registryManager)
            }.thenApplyAsync {
                getSyncData(server.playerManager.playerList)
            }.thenAccept { sd ->
                sd.forEachPlayer(server.playerManager.playerList) { p, m ->
                    ConfigApi.network().send(DataSyncS2CCustomPayload(m), p)
                }
            }
        }

        ServerPlayConnectionEvents.JOIN.register { handler, _, server ->
            CompletableFuture.supplyAsync {
                dataInvalid = true
                val list: MutableList<ServerPlayerEntity> = server.playerManager.playerList.toMutableList()
                list.add(handler.getPlayer())
                getSyncData(list)
            }.thenAccept { sd ->
                sd.forPlayer(handler.getPlayer()) {p, m ->
                    ConfigApi.network().send(DataSyncS2CCustomPayload(m), p)
                }
            }
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            for ((uuid, pendingChoices) in pendingChoicesMap) {
                if (pendingChoices.playerUuid == handler.getPlayer().uuid) {
                    pendingChoices.abort(handler.getPlayer())
                    storedChoices[uuid] = pendingChoices.poolChoices
                }
            }
        }

        ServerLifecycleEvents.SERVER_STOPPING.register { server ->
            for ((uuid, pendingChoices) in pendingChoicesMap) {
                val player = server.playerManager.getPlayer(pendingChoices.playerUuid) ?: continue
                pendingChoices.abort(player)
                storedChoices[uuid] = pendingChoices.poolChoices
            }
            saveStoredChoices()
        }
    }

    private data class UsageData(val usesMap: MutableMap<Identifier, MutableMap<UUID, Int>>, val keyMap: MutableMap<Identifier, MutableMap<UUID, Int>>) {
        fun isEmpty(): Boolean {
            return usesMap.isEmpty() && keyMap.isEmpty()
        }
    }

    private data class PendingChoices(val playerUuid: UUID, val pos: Vec3d, val poolChoices: List<Identifier>, val key: IdKey?, val onSuccess: BiConsumer<ServerPlayerEntity, Vec3d>, val onAbort: BiConsumer<ServerPlayerEntity, Vec3d>) {
        fun succeed(playerEntity: ServerPlayerEntity) {
            onSuccess.accept(playerEntity, pos)
        }

        fun abort(playerEntity: ServerPlayerEntity) {
            onAbort.accept(playerEntity, pos)
        }
    }
}