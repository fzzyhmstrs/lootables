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
import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.fzzyhmstrs.lootables.Lootables
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import java.io.File

import java.util.UUID

object LootablesData: SimpleSynchronousResourceReloadListener {

    private val usesMap: MutableMap<Identifier, MutableMap<UUID, Int>> by lazy {
        loadUsesMap()
    }

    private var lootableTables: Map<Identifier, LootableTable> = mapOf()

    private val gson = GsonBuilder().setPrettyPrinting().create()

    private fun loadUsesMap(): MutableMap<Identifier, MutableMap<UUID, Int>> {
        try {
            val dir = ConfigApi.platform().gameDir()
            val f = File(dir, "lootables_uses.json")
            if (!f.exists()) return mutableMapOf()
            val str = f.readLines().joinToString("\n")
            if (str.isEmpty()) {
                return mutableMapOf()
            }
            val json = JsonParser.parseString(str)
            if (!json.isJsonObject) {
                Lootables.LOGGER.error("Lootables usage map file was malformed. Using empty map.")
            }
            val map: MutableMap<Identifier, MutableMap<UUID, Int>> = mutableMapOf()
            for ((idStr, element) in json.asJsonObject.entrySet()) {
                if (!element.isJsonObject) {
                    Lootables.LOGGER.error("Lootables usage file encountered read error for element $idStr. Not an object")
                    continue
                }
                val id = Identifier.tryParse(idStr)
                if (id == null) {
                    Lootables.LOGGER.error("Lootables usage file encountered read error for element $idStr. Not an object")
                    continue
                }
                val elementMap: MutableMap<UUID, Int> = mutableMapOf()
                for ((uuidStr, elementElement) in element.asJsonObject.entrySet()) {
                    val uuid = UUID.fromString(uuidStr)
                    val usages = elementElement.asInt
                    elementMap[uuid] = usages
                }
                map[id] = elementMap
            }
            return map
        } catch (e: Throwable) {
            Lootables.LOGGER.error("Critical exception encountered while reading lootables usage map. Using empty map.")
            e.printStackTrace()
            return mutableMapOf()
        }
    }



    private fun saveUsesMap() {
        val dir = ConfigApi.platform().gameDir()
        val f = File(dir, "lootables_uses.json")
        val json = JsonObject()
        for ((id, map) in usesMap) {
            val mapJson = JsonObject()
            for ((uuid, uses) in map) {
                mapJson.add(uuid.toString(), JsonPrimitive(uses))
            }
            json.add(id.toString(), mapJson)
        }
        if (f.exists()) {
            f.writeText(gson.toJson(json))
        } else if (f.createNewFile()) {
            f.writeText(gson.toJson(json))
        } else {
            Lootables.LOGGER.error("Couldn't create new lootables_uses json; data wasn't saved.")
        }
    }

    fun getUses(id: Identifier, uuid: UUID): Int {
        return usesMap.get(id)?.get(uuid) ?: 0
    }

    fun use(id: Identifier, uuid: UUID) {
        val map = usesMap.computeIfAbsent(id) { _ -> mutableMapOf() }
        val uses = map[uuid] ?: 0
        map[uuid] = uses + 1
    }

    override fun reload(manager: ResourceManager) {
        val map: MutableMap<Identifier, LootableTable> = mutableMapOf()
        manager.findResources("lootable_tables") { path -> path.path.endsWith(".json") }
            .forEach { (id, resource) ->

            }
        lootableTables = map
    }

    override fun getFabricId(): Identifier {
        return Lootables.identity("data_reloader")
    }

    fun init() {
        ServerLifecycleEvents.BEFORE_SAVE.register { _, _, _ ->
            saveUsesMap()
        }
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(this)
    }


}