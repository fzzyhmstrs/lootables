package me.fzzyhmstrs.lootables

import me.fzzyhmstrs.fzzy_config.nullCast
import me.fzzyhmstrs.lootables.command.LootablesCommands
import net.minecraft.server.network.ServerPlayerEntity
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.OnDatapackSyncEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent


@Mod(Lootables.ID)
class ISNeoforge(bus: IEventBus) {
    init {
        Lootables.init()
        LootablesCommands.init(bus)
        NeoForge.EVENT_BUS.addListener(::onStart)
        NeoForge.EVENT_BUS.addListener(::onLoad)
        NeoForge.EVENT_BUS.addListener(::onLogout)
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

    private fun onLogout(event: PlayerLoggedOutEvent) {
        event.entity.nullCast<ServerPlayerEntity>()?.let {
            Lootables.runPlayerDisconnect(it)
        }
    }

}

@Mod(Lootables.ID, dist = [Dist.CLIENT])
class ISNeoForgeClient(@Suppress("UNUSED_PARAMETER") bus: IEventBus) {

    init {
        LootablesClient.init()
    }


}