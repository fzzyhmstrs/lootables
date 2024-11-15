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
import me.fzzyhmstrs.fzzy_config.util.FcText
import me.fzzyhmstrs.lootables.Lootables
import me.fzzyhmstrs.lootables.loot.LootablePoolEntry
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryDisplay
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryType
import me.fzzyhmstrs.lootables.loot.LootablePoolEntryTypes
import me.fzzyhmstrs.lootables.loot.display.RandomLootablePoolEntryDisplay
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d

class RandomLootablePoolEntry(private val children: List<LootablePoolEntry>): LootablePoolEntry {

    override fun type(): LootablePoolEntryType {
        return LootablePoolEntryTypes.RANDOM
    }

    override fun apply(player: PlayerEntity, origin: Vec3d) {
        val index = Lootables.random().nextInt(children.size)
        children[index].apply(player, origin)
    }

    override fun defaultDescription(playerEntity: ServerPlayerEntity): Text {
        val text = FcText.translatable("lootables.entry.random")
        text.append(FcText.literal("\n"))
        for ((i, child) in children.withIndex()) {
            text.append(child.defaultDescription(playerEntity))
            if (i != children.lastIndex) {
                text.append(FcText.literal("\n"))
            }
        }
        return text
    }

    override fun createDisplay(playerEntity: ServerPlayerEntity): LootablePoolEntryDisplay {
        return RandomLootablePoolEntryDisplay(children.map { it.createDisplay(playerEntity) })
    }

    companion object {

        val CODEC: MapCodec<RandomLootablePoolEntry> = RecordCodecBuilder.mapCodec { instance: RecordCodecBuilder.Instance<RandomLootablePoolEntry> ->
            instance.group(
                LootablePoolEntry.CODEC.listOf().fieldOf("children").forGetter(RandomLootablePoolEntry::children)
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