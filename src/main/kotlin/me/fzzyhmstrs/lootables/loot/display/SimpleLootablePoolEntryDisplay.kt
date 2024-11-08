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

package me.fzzyhmstrs.lootables.loot.display

import me.fzzyhmstrs.fzzy_config.util.RenderUtil.drawTex
import me.fzzyhmstrs.lootables.client.screen.TileIcon
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import net.minecraft.util.Identifier

abstract class SimpleLootablePoolEntryDisplay(private val tex: Identifier): LootablePoolEntryDisplay {

    override fun provideIcons(): List<TileIcon> {
        return listOf(TileIcon { context, x, y ->
            context.drawTex(tex, x, y, 18, 18)
        })
    }
}