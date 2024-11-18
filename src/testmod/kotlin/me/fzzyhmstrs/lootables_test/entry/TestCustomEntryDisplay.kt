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

package me.fzzyhmstrs.lootables_test.entry

import me.fzzyhmstrs.fzzy_config.util.FcText.translate
import me.fzzyhmstrs.lootables.client.screen.TileIcon
import me.fzzyhmstrs.lootables.loot.custom.CustomLootableEntryDisplay
import me.fzzyhmstrs.lootables.loot.display.StatusEffectLootablePoolEntryDisplay
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.text.Text

object TestCustomEntryDisplay: CustomLootableEntryDisplay {

    private val iconDisplay = StatusEffectLootablePoolEntryDisplay(StatusEffects.SATURATION, 0, 1)

    override fun provideIcons(): List<TileIcon> {
        return iconDisplay.provideIcons()
    }

    override fun clientDescription(): Text {
        return "lootables_test.custom.desc".translate()
    }
}