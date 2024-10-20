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

package me.fzzyhmstrs.lootables.client.screen

import me.fzzyhmstrs.fzzy_config.util.FcText
import me.fzzyhmstrs.lootables.client.LootablesClientData
import me.fzzyhmstrs.lootables.network.ChoicesS2CCustomPayload
import net.minecraft.client.gui.screen.Screen

class ChoicesScreen(private val choiceData: ChoicesS2CCustomPayload, private var choicesLeft: Int, private val oldScreen: Screen?): Screen(FcText.empty()) {

    override fun close() {
        this.client?.setScreen(oldScreen)
    }

    override fun init() {
        super.init()
        val data = LootablesClientData.getData(choiceData.table, choiceData.choices)
    }

}