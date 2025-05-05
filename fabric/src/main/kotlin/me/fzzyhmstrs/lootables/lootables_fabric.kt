package me.fzzyhmstrs.lootables

import me.fzzyhmstrs.lootables.command.LootablesCommands
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents


object ISFabric: ModInitializer {

    override fun onInitialize() {
        Lootables.init()
        LootablesCommands.init()

        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            Lootables.runServerStarting(server)
        }

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { server, resourceManager, _ ->
            Lootables.runEndReload(server, resourceManager)
        }

        ServerPlayConnectionEvents.JOIN.register { handler, _, server ->
            Lootables.runPlayerJoin(handler.getPlayer(), server)
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            Lootables.runPlayerDisconnect(handler.getPlayer())
        }
    }
}

object ISFabricClient: ClientModInitializer {

    override fun onInitializeClient() {
        LootablesClient.init()
    }
}