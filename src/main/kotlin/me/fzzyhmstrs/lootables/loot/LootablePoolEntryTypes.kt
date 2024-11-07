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

package me.fzzyhmstrs.lootables.loot

import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.loot.display.CustomLootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.display.ItemLootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.display.PoolLootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.entry.CustomLootablePoolEntry
import me.fzzyhmstrs.lootables.loot.entry.ItemLootablePoolEntry
import me.fzzyhmstrs.lootables.loot.entry.PoolLootablePoolEntry

object LootablePoolEntryTypes {

    val CUSTOM = LootablePoolEntryType.create(Lootables.identity("custom"), CustomLootablePoolEntry.CODEC, CustomLootablePoolEntryDisplay.PACKET_CODEC.cast())
    val ITEM = LootablePoolEntryType.create(Lootables.identity("item"), ItemLootablePoolEntry.CODEC, ItemLootablePoolEntryDisplay.PACKET_CODEC)
    val POOL = LootablePoolEntryType.create(Lootables.identity("pool"), PoolLootablePoolEntry.CODEC, PoolLootablePoolEntryDisplay.PACKET_CODEC)

}