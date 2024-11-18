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

package me.fzzyhmstrs.lootables.loot.custom

import me.fzzyhmstrs.lootables.client.screen.TileIcon
import net.minecraft.text.Text

@FunctionalInterface
@JvmDefaultWithCompatibility
interface CustomLootableEntryDisplay {
    fun provideIcons(): List<TileIcon>
    fun clientDescription(): Text? {
        return null
    }
}