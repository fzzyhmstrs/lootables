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

package me.fzzyhmstrs.lootables.loot.entry

import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.RandomLootablePoolEntryDisplay
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

/**
 * Randomly picks one entry from its children and applies that; rerolled on every roll. This is an "OR" entry
 * @param children List&lt;[LootablePoolEntry]&gt; children entries to randomly pick from
 * @author fzzyhmstrs
 * @since 0.1.0
 */
class RandomLootablePoolEntry(private val children: List<LootablePoolEntry>): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.RANDOM
    }

    override fun apply(player: ServerPlayerEntity, origin: Vec3d) {
        val index = Lootables.random().nextInt(children.size)
        children[index].apply(player, origin)
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return RandomLootablePoolEntryDisplay(children.map { LootablePoolEntryDisplay.DisplayWithDesc(it.createDisplay(playerEntity), it.serverDescription(playerEntity)) })
    }

    override fun needsInvalidation(type: LootablePoolEntry.InvalidationType): Boolean {
        return children.any { it.needsInvalidation(type) }
    }

    internal companion object {

        private val NON_EMPTY_LIST_CODEC = LootablePoolEntry.CODEC.listOf().flatXmap(
            { l -> if (l.isEmpty()) DataResult.error { "Empty list not allowed" } else DataResult.success(l) },
            { l -> if (l.isEmpty()) DataResult.error { "Empty list not allowed" } else DataResult.success(l) }
        )

        val CODEC: MapCodec<RandomLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<RandomLootablePoolEntry> ->
            instance.group(
                NON_EMPTY_LIST_CODEC.fieldOf("children").forGetter(RandomLootablePoolEntry::children)
            ).apply(instance, ::RandomLootablePoolEntry)
        }

        fun createRandomInstance(playerEntity: ServerPlayerEntity): LootablePoolEntry {
            val list: MutableList<LootablePoolEntry> = mutableListOf()
            for (i in 0..Lootables.random().nextInt(5)) {
                LootablePoolEntryType.random(playerEntity)?.let { list.add(it) }
            }
            return RandomLootablePoolEntry(list)
        }
    }

}