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

import com.google.gson.JsonParser
import com.mojang.serialization.Codec
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
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.RegistryWrapper
import net.minecraft.resource.ResourceManager
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.Uuids
import net.minecraft.util.math.Vec3d
import net.minecraft.world.PersistentState
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.BiConsumer
import java.util.function.Function
import java.util.stream.Stream
import kotlin.math.max
import kotlin.math.min

internal object LootablesData {

    private var lootablePools: Map<Identifier, LootablePool> = mapOf()
    private var lootableTables: Map<Identifier, LootableTable> = mapOf()
    private var dataInvalid: AtomicBoolean = AtomicBoolean(true)
    private var lootableSyncData: SyncDataHolder = SyncDataHolder.Empty
    private val abortedChoices: MutableSet<UUID> = mutableSetOf()

    private fun getUsageData(server: MinecraftServer): UsageData {
        return server.overworld?.persistentStateManager?.getOrCreate(UsageData.TYPE, Lootables.ID + "_usage_data") ?: UsageData.EMPTY
    }

    private fun getChoicesData(server: MinecraftServer): ChoicesData {
        return server.overworld?.persistentStateManager?.getOrCreate(ChoicesData.TYPE, Lootables.ID + "_choices_data") ?: ChoicesData.EMPTY
    }

    private fun getSyncData(players: List<ServerPlayerEntity>): SyncDataHolder {
        if (dataInvalid.get()) {
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
            dataInvalid.set(false)
        }
        return lootableSyncData
    }

    fun getPool(id: Identifier): LootablePool? {
        return lootablePools[id]
    }

    fun getPoolIds(): Set<Identifier> {
        return lootableTables.keys
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

    fun getUses(id: Identifier, playerEntity: ServerPlayerEntity): Int {
        return getUsageData(playerEntity.server).getUses(id, playerEntity.uuid)
    }

    fun use(id: Identifier, playerEntity: ServerPlayerEntity) {
        getUsageData(playerEntity.server).use(id, playerEntity.uuid)
    }

    fun keyAvailable(key: IdKey, playerEntity: ServerPlayerEntity): Boolean {
        return getUsageData(playerEntity.server).keyAvailable(key, playerEntity.uuid)
    }

    fun applyKey(key: IdKey, playerEntity: ServerPlayerEntity) {
        getUsageData(playerEntity.server).applyKey(key, playerEntity.uuid)
    }

    private fun retractKey(key: IdKey, playerEntity: ServerPlayerEntity) {
        getUsageData(playerEntity.server).retractKey(key, playerEntity.uuid)
    }

    internal fun resetKey(key: IdKey, playerEntity: ServerPlayerEntity) {
        getUsageData(playerEntity.server).resetKey(key, playerEntity.uuid)
    }

    internal fun resetKey(key: IdKey, server: MinecraftServer) {
        getUsageData(server).resetKey(key)
    }

    fun applyChosen(payload: ChosenC2SCustomPayload, playerEntity: ServerPlayerEntity) {
        val choices = getChoicesData(playerEntity.server)
        val pending = choices.getPending(payload.choiceKey)
        pending?.succeed(playerEntity)
        getTable(payload.table)?.applyPoolsById(payload.chosen, playerEntity, pending?.pos ?: Vec3d.ZERO)
        choices.removePending(payload.choiceKey)
    }

    fun applyAbort(payload: AbortChoicesC2SCustomPayload, playerEntity: ServerPlayerEntity) {
        if (abortedChoices.contains(payload.choiceKey)) return
        val choices = getChoicesData(playerEntity.server)
        val pending = choices.getPending(payload.choiceKey) ?: return
        pending.abort(playerEntity)
        if (pending.key != null) {
            retractKey(pending.key, playerEntity)
        }
        abortedChoices.add(payload.choiceKey)
    }

    fun setPending(choiceKey: UUID, playerEntity: ServerPlayerEntity, pos: Vec3d, poolChoices: List<Identifier>, key: IdKey?, onSuccess: BiConsumer<ServerPlayerEntity, Vec3d>, onAbort: BiConsumer<ServerPlayerEntity?, Vec3d>) {
        getChoicesData(playerEntity.server).setPending(choiceKey, PendingChoices(playerEntity.uuid, pos, poolChoices, key, onSuccess, onAbort))
    }

    fun getPendingPools(choiceKey: UUID, playerEntity: ServerPlayerEntity): List<Identifier>? {
        return getChoicesData(playerEntity.server).getPendingPools(choiceKey)
    }

    fun getStoredPools(choiceKey: UUID, playerEntity: ServerPlayerEntity): List<Identifier>? {
        return getChoicesData(playerEntity.server).getStoredPools(choiceKey)
    }

    private const val PARALLEL_BREAKPOINT = 512

    private fun<T> Map<Identifier, T>.ifStream(): Stream<Map.Entry<Identifier, T>> {
        return if (this.size < PARALLEL_BREAKPOINT) {
            this.entries.stream()
        } else {
            this.entries.parallelStream()
        }
    }

    private fun reload(manager: ResourceManager, dynamicRegistries: RegistryWrapper.WrapperLookup) {
        dataInvalid.set(true)
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
        Lootables.LOGGER.info("Finished lootables table load in ${System.currentTimeMillis() - start}ms")
        LootablePool.reset()
    }

    internal fun runServerStart(server: MinecraftServer) {
        //performed synchronously to avoid race condition with join or something
        reload(server.resourceManager, server.reloadableRegistries.registryManager)
    }

    internal fun runEndReload(server: MinecraftServer, resourceManager: ResourceManager) {
        if (server.playerManager.playerList.isEmpty()) return
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

    internal fun runPlayerJoin(playerEntity: ServerPlayerEntity, server: MinecraftServer) {
        CompletableFuture.supplyAsync {
            dataInvalid.set(true)
            val list: MutableList<ServerPlayerEntity> = server.playerManager.playerList.toMutableList()
            list.add(playerEntity)
            getSyncData(list)
        }.thenAccept { sd ->
            sd.forPlayer(playerEntity) { p, m ->
                ConfigApi.network().send(DataSyncS2CCustomPayload(m), p)
            }
        }
    }

    internal fun runPlayerDisconnect(playerEntity: ServerPlayerEntity) {
        getChoicesData(playerEntity.server).onDisconnect(playerEntity)
    }

    fun init() {}

    private data class UsageData(private val usesMap: MutableMap<Identifier, MutableMap<UUID, Int>>, private val keyMap: MutableMap<Identifier, MutableMap<UUID, Int>>): PersistentState() {
        fun getUses(id: Identifier, uuid: UUID): Int {
            return usesMap[id]?.get(uuid) ?: 0
        }

        fun use(id: Identifier, uuid: UUID) {
            Lootables.DEVLOG.info("Using {} for {}", id, uuid)
            markDirty()
            val map = usesMap.computeIfAbsent(id) { _ -> mutableMapOf() }
            val uses = map[uuid] ?: 0
            map[uuid] = uses + 1
        }

        fun keyAvailable(key: IdKey, uuid: UUID): Boolean {
            return (keyMap[key.id]?.get(uuid) ?: 0) < key.count
        }

        fun applyKey(key: IdKey, uuid: UUID) {
            markDirty()
            val map = keyMap.computeIfAbsent(key.id) { _ -> mutableMapOf() }
            val uses = map[uuid] ?: 0
            map[uuid] = min(uses + 1, key.count)
        }

        fun retractKey(key: IdKey, uuid: UUID) {
            markDirty()
            val map = keyMap.computeIfAbsent(key.id) { _ -> mutableMapOf() }
            val uses = map[uuid] ?: 0
            map[uuid] = max(uses - 1, 0)
        }

        fun resetKey(key: IdKey, uuid: UUID) {
            markDirty()
            keyMap[key.id]?.remove(uuid)
        }

        fun resetKey(key: IdKey) {
            markDirty()
            keyMap.remove(key.id)
        }

        override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup): NbtCompound {
            Lootables.DEVLOG.info("Saving Usage Data")
            val ops = registryLookup.getOps(NbtOps.INSTANCE)
            val useResult = MAP_CODEC.encodeStart(ops, usesMap)
            val keyResult = MAP_CODEC.encodeStart(ops, keyMap)
            nbt.put(USES_KEY, useResult.mapOrElse(Function.identity()) { err -> Lootables.LOGGER.error(err.message()); NbtCompound() })
            nbt.put(KEY_KEY, keyResult.mapOrElse(Function.identity()) { err -> Lootables.LOGGER.error(err.message()); NbtCompound() })
            return nbt
        }

        companion object {
            fun create(): UsageData {
                return UsageData(mutableMapOf(), mutableMapOf())
            }
            fun load(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup): UsageData {
                val ops = registryLookup.getOps(NbtOps.INSTANCE)
                val useNbt = nbt.getCompound(USES_KEY)
                val keyNbt = nbt.getCompound(KEY_KEY)
                val useResult = MAP_CODEC.parse(ops, useNbt)
                val keyResult = MAP_CODEC.parse(ops, keyNbt)
                val uses = useResult.mapOrElse(Function.identity()) { err ->
                    Lootables.LOGGER.error("Error loading Lootables Usage Data")
                    Lootables.LOGGER.error(err.message())
                    mutableMapOf()
                }
                val keys = keyResult.mapOrElse(Function.identity()) { err ->
                    Lootables.LOGGER.error("Error loading Lootables Key Data")
                    Lootables.LOGGER.error(err.message())
                    mutableMapOf()
                }
                return UsageData(uses, keys)
            }

            val EMPTY: UsageData = UsageData(mutableMapOf(), mutableMapOf())
            internal val TYPE: Type<UsageData> = Type(::create, ::load, null)

            private const val USES_KEY = "uses_map"
            private const val KEY_KEY = "key_map"

            private val UUID_CODEC = Codec.unboundedMap(Uuids.STRING_CODEC, Codec.INT).xmap({ m -> m.toMutableMap() }, Function.identity())
            private val MAP_CODEC = Codec.unboundedMap(Identifier.CODEC, UUID_CODEC).xmap({ m -> m.toMutableMap() }, Function.identity())

        }
    }

    private data class ChoicesData(private val storedChoices: MutableMap<UUID, List<Identifier>>, private val pendingChoices: MutableMap<UUID, PendingChoices>): PersistentState() {

        fun getPending(choiceKey: UUID): PendingChoices? {
            return pendingChoices[choiceKey]
        }

        fun setPending(choiceKey: UUID, choices: PendingChoices) {
            markDirty()
            pendingChoices[choiceKey] = choices
        }

        fun removePending(choiceKey: UUID) {
            markDirty()
            pendingChoices.remove(choiceKey)
        }

        fun getPendingPools(choiceKey: UUID): List<Identifier>? {
            val pending = pendingChoices[choiceKey] ?: return null
            abortedChoices.remove(choiceKey)
            return pending.poolChoices
        }

        fun getStoredPools(choiceKey: UUID): List<Identifier>? {
            val stored = storedChoices[choiceKey] ?: return null
            storedChoices.remove(choiceKey)
            return stored
        }

        fun onDisconnect(playerEntity: ServerPlayerEntity) {
            markDirty()
            for ((uuid, pendingChoices) in pendingChoices) {
                if (pendingChoices.playerUuid == playerEntity.uuid) {
                    pendingChoices.abort(playerEntity)
                    storedChoices[uuid] = pendingChoices.poolChoices
                }
            }
        }

        override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup): NbtCompound {
            Lootables.DEVLOG.info("Saving Choice Data")
            for ((uuid, pendingChoices) in pendingChoices) {
                pendingChoices.abort(null)
                storedChoices[uuid] = pendingChoices.poolChoices
            }
            val ops = registryLookup.getOps(NbtOps.INSTANCE)
            val mapResult = MAP_CODEC.encodeStart(ops, storedChoices)
            nbt.put(MAP_KEY, mapResult.mapOrElse(Function.identity()) { err ->
                Lootables.LOGGER.error("Error writing Lootables stored choices")
                Lootables.LOGGER.error(err.message())
                NbtCompound() })
            return nbt
        }

        companion object {
            fun create(): ChoicesData {
                return ChoicesData(mutableMapOf(), mutableMapOf())
            }

            fun load(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup): ChoicesData {
                val ops = registryLookup.getOps(NbtOps.INSTANCE)
                val mapNbt = nbt.getCompound(MAP_KEY)
                val mapResult = MAP_CODEC.parse(ops, mapNbt)
                val map = mapResult.mapOrElse(Function.identity()) { err ->
                    Lootables.LOGGER.error("Error loading Lootables stored choices")
                    Lootables.LOGGER.error(err.message())
                    mutableMapOf()
                }
                return ChoicesData(map, mutableMapOf())
            }

            val EMPTY: ChoicesData = ChoicesData(mutableMapOf(), mutableMapOf())

            internal val TYPE: Type<ChoicesData> = Type(::create, ::load, null)

            private const val MAP_KEY = "stored_choices"

            private val MAP_CODEC = Codec.unboundedMap(Uuids.STRING_CODEC, Identifier.CODEC.listOf()).xmap({ m -> m.toMutableMap() }, Function.identity())
        }

    }

    private data class PendingChoices(val playerUuid: UUID, val pos: Vec3d, val poolChoices: List<Identifier>, val key: IdKey?, val onSuccess: BiConsumer<ServerPlayerEntity, Vec3d>, val onAbort: BiConsumer<ServerPlayerEntity?, Vec3d>) {
        fun succeed(playerEntity: ServerPlayerEntity) {
            onSuccess.accept(playerEntity, pos)
        }

        fun abort(playerEntity: ServerPlayerEntity?) {
            onAbort.accept(playerEntity, pos)
        }
    }
}