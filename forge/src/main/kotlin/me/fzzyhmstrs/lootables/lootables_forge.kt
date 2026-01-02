package me.fzzyhmstrs.lootables

import me.fzzyhmstrs.fzzy_config.nullCast
import me.fzzyhmstrs.lootables.command.LootablesCommands
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.OnDatapackSyncEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.server.ServerStartingEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.loading.FMLLoader


@Mod(Lootables.ID)
class ISNeoforge(bus: IEventBus) {
    init {
        Lootables.init()
        LootablesCommands.init(bus)
        MinecraftForge.EVENT_BUS.addListener(::onStart)
        MinecraftForge.EVENT_BUS.addListener(::onLoad)
        MinecraftForge.EVENT_BUS.addListener(::onLogout)

        if (FMLLoader.getDist().isClient) {
            LootablesClient.init()
        }
    }

    private fun onStart(event: ServerStartingEvent) {
        Lootables.runServerStarting(event.server)
    }

    //handles player join and reloads
    private fun onLoad(event: OnDatapackSyncEvent) {
        val player = event.player
        if (player == null) {
            Lootables.runEndReload(event.playerList.server, event.playerList.server.resourceManager)
        } else {
            Lootables.runPlayerJoin(player, player.server)
        }
    }

    private fun onLogout(event: PlayerEvent.PlayerLoggedOutEvent) {
        event.entity.nullCast<ServerPlayerEntity>()?.let {
            Lootables.runPlayerDisconnect(it)
        }
    }

}