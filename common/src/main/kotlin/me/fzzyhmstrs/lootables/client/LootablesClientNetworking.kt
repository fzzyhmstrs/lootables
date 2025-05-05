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

package me.fzzyhmstrs.lootables.client

import me.fzzyhmstrs.fzzy_config.networking.api.ClientPlayNetworkContext
import me.fzzyhmstrs.lootables.client.screen.ChoicesScreen
import me.fzzyhmstrs.lootables.network.ChoicesS2CCustomPayload
import me.fzzyhmstrs.lootables.network.DataSyncS2CCustomPayload
import net.minecraft.client.MinecraftClient

object LootablesClientNetworking {

    internal fun handleChoicesS2C(payload: ChoicesS2CCustomPayload, @Suppress("UNUSED_PARAMETER") context: ClientPlayNetworkContext) {
        val choicesLeft = payload.choicesToMake
        //don't stack choice screens on unchecked user spam, use the previous choice screen iterations screen
        val previousScreen = MinecraftClient.getInstance().currentScreen
        val oldScreen = if (previousScreen is ChoicesScreen) {
            previousScreen.oldScreen
        } else {
            previousScreen
        }
        val screen = ChoicesScreen(payload, choicesLeft, oldScreen)
        MinecraftClient.getInstance().setScreen(screen)
    }

    internal fun handleDataSyncS2C(payload: DataSyncS2CCustomPayload, @Suppress("UNUSED_PARAMETER") context: ClientPlayNetworkContext) {
        LootablesClientData.receiveSync(payload.tables)
    }

}