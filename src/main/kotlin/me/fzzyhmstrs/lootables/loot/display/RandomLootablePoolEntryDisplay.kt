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

import me.fzzyhmstrs.lootables.client.screen.TileIcon
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import net.minecraft.network.codec.PacketCodecs

class RandomLootablePoolEntryDisplay(private val children: List<LootablePoolEntryDisplay>): LootablePoolEntryDisplay {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.RANDOM
    }

    private val iconChoices by lazy {
        children.map { it.provideIcons() }
    }

    override fun provideIcons(): List<TileIcon> {
        val time = System.currentTimeMillis() / 1000L
        val index = time % iconChoices.size
        return iconChoices[index.toInt()]
    }

    companion object {
        val PACKET_CODEC = LootablePoolEntryDisplay.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(
            ::RandomLootablePoolEntryDisplay,
            RandomLootablePoolEntryDisplay::children
        )
    }
}