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
import me.fzzyhmstrs.lootables.loot.display.*
import me.fzzyhmstrs.lootables.loot.entry.*

object LootablePoolEntryTypes {

    val CUSTOM = LootablePoolEntryType.create(Lootables.identity("custom"), CustomLootablePoolEntry.CODEC, CustomLootablePoolEntryDisplay.PACKET_CODEC.cast())
    val ITEM = LootablePoolEntryType.create(Lootables.identity("item"), ItemLootablePoolEntry.CODEC, ItemLootablePoolEntryDisplay.PACKET_CODEC, ItemLootablePoolEntry.Companion::createRandomInstance)
    val POOL = LootablePoolEntryType.create(Lootables.identity("pool"), PoolLootablePoolEntry.CODEC, PoolLootablePoolEntryDisplay.PACKET_CODEC)
    val TABLE = LootablePoolEntryType.create(Lootables.identity("table"), TableLootablePoolEntry.CODEC, TableLootablePoolEntryDisplay.PACKET_CODEC)
    val XP = LootablePoolEntryType.create(Lootables.identity("xp"), ExperienceLootablePoolEntry.CODEC, ExperienceLootablePoolEntryDisplay.PACKET_CODEC, ExperienceLootablePoolEntry.Companion::createRandomInstance)
    val HEAL = LootablePoolEntryType.create(Lootables.identity("heal"), HealLootablePoolEntry.CODEC, HealLootablePoolEntryDisplay.PACKET_CODEC, HealLootablePoolEntry.Companion::createRandomInstance)
    val STATUS = LootablePoolEntryType.create(Lootables.identity("status"), StatusEffectLootablePoolEntry.CODEC, StatusEffectLootablePoolEntryDisplay.PACKET_CODEC)
    val ATTRIBUTE = LootablePoolEntryType.create(Lootables.identity("attribute"), AttributeLootablePoolEntry.CODEC, AttributeLootablePoolEntryDisplay.PACKET_CODEC, AttributeLootablePoolEntry.Companion::createRandomInstance)
    val MULTI = LootablePoolEntryType.create(Lootables.identity("multi"), MultiLootablePoolEntry.CODEC, MultiLootablePoolEntryDisplay.PACKET_CODEC, MultiLootablePoolEntry.Companion::createRandomInstance)
    val RANDOM = LootablePoolEntryType.create(Lootables.identity("random"), RandomLootablePoolEntry.CODEC, RandomLootablePoolEntryDisplay.PACKET_CODEC, RandomLootablePoolEntry.Companion::createRandomInstance)
    val FUNCTION = LootablePoolEntryType.create(Lootables.identity("function"), LootFunctionLootablePoolEntry.CODEC, LootFunctionLootablePoolEntryDisplay.PACKET_CODEC)
    val ADVANCEMENT = LootablePoolEntryType.create(Lootables.identity("advancement"), AdvancementLootablePoolEntry.CODEC, AdvancementLootablePoolEntryDisplay.PACKET_CODEC, AdvancementLootablePoolEntry.Companion::createRandomInstance)
    val PLAY_SOUND = LootablePoolEntryType.create(Lootables.identity("play_sound"), PlaySoundLootablePoolEntry.CODEC, PlaySoundLootablePoolEntryDisplay.PACKET_CODEC)
    val PARTICLE = LootablePoolEntryType.create(Lootables.identity("particle"), SpawnParticlesLootablePoolEntry.CODEC, SpawnParticlesLootablePoolEntryDisplay.PACKET_CODEC)


    fun init(){}

}