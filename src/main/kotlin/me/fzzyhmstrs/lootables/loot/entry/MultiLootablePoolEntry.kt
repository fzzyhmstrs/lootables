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

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.MultiLootablePoolEntryDisplay
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

/**
 * Applies multiple child entries together. This is an "AND" entry.
 * @param children List&lt;[LootablePoolEntry]&gt; list of children to apply when this parent entry is applied.
 * @author fzzyhmstrs
 * @since 0.1.0
 */
class MultiLootablePoolEntry(private val children: List<LootablePoolEntry>): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.MULTI
    }

    override fun apply(player: ServerPlayerEntity, origin: Vec3d) {
        for (child in children) {
            child.apply(player, origin)
        }
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return MultiLootablePoolEntryDisplay(children.map { LootablePoolEntryDisplay.DisplayWithDesc(it.createDisplay(playerEntity), it.serverDescription(playerEntity)) })
    }

    override fun needsInvalidation(type: LootablePoolEntry.InvalidationType): Boolean {
        return children.any { it.needsInvalidation(type) }
    }

    internal companion object {

        val CODEC: MapCodec<MultiLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<MultiLootablePoolEntry> ->
            instance.group(
                LootablePoolEntry.CODEC.listOf().fieldOf("children").forGetter(MultiLootablePoolEntry::children)
            ).apply(instance, ::MultiLootablePoolEntry)
        }

        fun createRandomInstance(playerEntity: ServerPlayerEntity): LootablePoolEntry {
            val list: MutableList<LootablePoolEntry> = mutableListOf()
            for (i in 0..Lootables.random().nextInt(5)) {
                LootablePoolEntryType.random(playerEntity)?.let { list.add(it) }
            }
            return MultiLootablePoolEntry(list)
        }
    }

}