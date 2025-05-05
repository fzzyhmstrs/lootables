package me.fzzyhmstrs.lootables

import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import me.fzzyhmstrs.lootables.config.LootablesConfig
import me.fzzyhmstrs.lootables.data.LootablesData
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.network.LootablesNetworking
import me.fzzyhmstrs.lootables.registry.ComponentRegistry
import me.fzzyhmstrs.lootables.registry.LootFunctionRegistry
import net.minecraft.resource.ResourceManager
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import net.minecraft.util.math.random.Random
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


object Lootables {
    const val ID = "lootables"
    val LOGGER: Logger = LoggerFactory.getLogger(ID)
    @Suppress("unused")
    val DEVLOG: Logger = ConfigApi.platform().devLogger(ID)
    @Suppress("DEPRECATION")
    private val random = Random.createThreadSafe()
    val DECIMAL_FORMAT: DecimalFormat by lazy {
        Util.make(DecimalFormat("#.##")) { format: DecimalFormat ->
            format.decimalFormatSymbols = DecimalFormatSymbols.getInstance(
                Locale.ROOT
            )
        }
    }

    fun identity(path: String): Identifier {
        return Identifier.of(ID, path)
    }

    fun random(): Random {
        return random
    }

    fun init() {
        LootablesConfig.init()
        LootablePoolEntryTypes.init()
        LootablesData.init()
        LootablesNetworking.init()

        ComponentRegistry.init()
        LootFunctionRegistry.init()

    }

    fun runServerStarting(server: MinecraftServer) {
        LootablesData.runServerStart(server)
    }

    fun runEndReload(server: MinecraftServer, resourceManager: ResourceManager) {
        LootablesData.runEndReload(server, resourceManager)
    }

    fun runPlayerJoin(player: ServerPlayerEntity, server: MinecraftServer) {
        LootablesData.runPlayerJoin(player, server)
    }

    fun runPlayerDisconnect(player: ServerPlayerEntity) {
        LootablesData.runPlayerDisconnect(player)
    }
}

object LootablesClient {


    fun init() {}
}