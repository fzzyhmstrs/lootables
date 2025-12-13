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

package me.fzzyhmstrs.lootables_test

import com.google.gson.GsonBuilder
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.serialization.JsonOps
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.api.LootablesApi
import me.fzzyhmstrs.lootables.loot.LootableTable
import me.fzzyhmstrs.lootables_test.entry.TestCustomEntry
import me.fzzyhmstrs.lootables_test.entry.TestCustomEntryDisplay
import me.fzzyhmstrs.lootables_test.screen.TestScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.command.CommandManager
import java.io.File
import java.util.*

object LootablesTest: ModInitializer {

    override fun onInitialize() {

        LootablesApi.registerCustomEntry(Lootables.identity("test_custom_entry"), TestCustomEntry, TestCustomEntryDisplay)

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                CommandManager.literal("create_random")
                    .then(CommandManager.argument("count", IntegerArgumentType.integer(1))
                        .executes { context ->
                            try {
                                val count = IntegerArgumentType.getInteger(context, "count")
                                val ops = context.source.world.registryManager.getOps(JsonOps.INSTANCE)
                                val gson = GsonBuilder().setPrettyPrinting().setLenient().create()
                                for (i in 1..count) {
                                    val table = LootableTable.random(context.source.playerOrThrow, 25)
                                    val json = LootableTable.CODEC.encodeStart(ops, table)
                                    json.ifSuccess { jsonEl ->
                                        val name = UUID.randomUUID().toString().lowercase()
                                        val configDir = FabricLoader.getInstance().configDir.toFile()
                                        val configSubDir = File(configDir, "random")
                                        if (!configSubDir.exists()) {
                                            configSubDir.mkdirs()
                                        }
                                        val configFile = File(configSubDir, "$name.json")
                                        if (!(!configFile.exists() && !configFile.createNewFile())) {
                                            val str = gson.toJson(jsonEl)
                                            configFile.writeText(str)
                                        }
                                    }.ifError { error ->
                                        Lootables.LOGGER.error(error.messageSupplier.get())
                                    }
                                }
                            } catch (e: Throwable) {
                                Lootables.LOGGER.error("Eror happen :<", e)
                            }
                            1
                        }
                    )
            )
        }
    }
}

object LootablesTestClient: ClientModInitializer {

    private var openDamnScreen = 0

    override fun onInitializeClient() {

        ClientCommandRegistrationCallback.EVENT.register{ dispatcher, _ ->
            registerClientCommands(dispatcher)
        }
        ClientTickEvents.START_CLIENT_TICK.register{ client ->
            if (openDamnScreen != 0) {
                client.setScreen(TestScreen(openDamnScreen))
                openDamnScreen = 0
            }
        }
    }

    private fun registerClientCommands(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        dispatcher.register(
            ClientCommandManager.literal("lootables_screen")
                .then(ClientCommandManager.argument("choices", IntegerArgumentType.integer(1))
                    .executes { context ->
                        openDamnScreen = IntegerArgumentType.getInteger(context, "choices")
                        1
                    }
                )
        )
    }
}